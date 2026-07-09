#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
INSTALL_DIR="/opt/burnmetrix-dashboard"
SERVICE_USER="${SUDO_USER:-pi}"

if [[ "$(id -u)" -ne 0 ]]; then
  echo "Re-running installer with sudo..."
  exec sudo "$0" "$@"
fi

echo "Installing BurnMetrix Dashboard for user ${SERVICE_USER}"

apt-get update
apt-get install -y ca-certificates curl gnupg

if ! command -v node >/dev/null 2>&1 || ! node --version | grep -Eq '^v(20|22|24)\.'; then
  echo "Installing Node.js 20 from NodeSource..."
  curl -fsSL https://deb.nodesource.com/setup_20.x | bash -
fi

if apt-cache show chromium-browser >/dev/null 2>&1; then
  CHROMIUM_PACKAGE="chromium-browser"
  CHROMIUM_COMMAND="/usr/bin/chromium-browser"
else
  CHROMIUM_PACKAGE="chromium"
  CHROMIUM_COMMAND="/usr/bin/chromium"
fi

apt-get install -y \
  ca-certificates \
  "${CHROMIUM_PACKAGE}" \
  curl \
  git \
  maven \
  nginx \
  nodejs \
  openjdk-17-jdk \
  rsync \
  x11-xserver-utils

install -d -o "${SERVICE_USER}" -g "${SERVICE_USER}" "${INSTALL_DIR}"
install -d -o "${SERVICE_USER}" -g "${SERVICE_USER}" "${INSTALL_DIR}/backend"
install -d -o "${SERVICE_USER}" -g "${SERVICE_USER}" "${INSTALL_DIR}/frontend"
install -d -o "${SERVICE_USER}" -g "${SERVICE_USER}" "${INSTALL_DIR}/database/data"
install -d /etc/burnmetrix-dashboard
if [[ ! -f /etc/burnmetrix-dashboard/backend.env ]]; then
  cat >/etc/burnmetrix-dashboard/backend.env <<ENV
# Optional Strava integration for the Calories page.
# STRAVA_CLIENT_ID=
# STRAVA_CLIENT_SECRET=
STRAVA_REDIRECT_URI=http://localhost:8080/api/metabolic/auth/callback
ENV
fi

echo "Building backend..."
sudo -u "${SERVICE_USER}" mvn -f "${PROJECT_ROOT}/backend/pom.xml" clean package
cp "${PROJECT_ROOT}"/backend/target/*.jar "${INSTALL_DIR}/backend/burnmetrix-backend.jar"

echo "Building frontend..."
rm -rf "${PROJECT_ROOT}/frontend/node_modules"
rm -f "${PROJECT_ROOT}/frontend/package-lock.json"
sudo -u "${SERVICE_USER}" npm --prefix "${PROJECT_ROOT}/frontend" install --include=optional
sudo -u "${SERVICE_USER}" npm --prefix "${PROJECT_ROOT}/frontend" run build
rsync -a --delete "${PROJECT_ROOT}/frontend/dist/" "${INSTALL_DIR}/frontend/"

cat >/etc/nginx/sites-available/burnmetrix-dashboard <<NGINX
server {
    listen 80 default_server;
    server_name _;
    root ${INSTALL_DIR}/frontend;
    index index.html;

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
    }

    location /actuator/ {
        proxy_pass http://127.0.0.1:8080/actuator/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
    }

    location / {
        try_files \$uri \$uri/ /index.html;
    }
}
NGINX

ln -sf /etc/nginx/sites-available/burnmetrix-dashboard /etc/nginx/sites-enabled/burnmetrix-dashboard
rm -f /etc/nginx/sites-enabled/default

sed "s/User=pi/User=${SERVICE_USER}/g" "${PROJECT_ROOT}/scripts/burnmetrix-backend.service" >/etc/systemd/system/burnmetrix-backend.service
sed "s/User=pi/User=${SERVICE_USER}/g; s#/home/pi#/home/${SERVICE_USER}#g; s#/usr/bin/chromium-browser#${CHROMIUM_COMMAND}#g" "${PROJECT_ROOT}/scripts/burnmetrix-kiosk.service" >/etc/systemd/system/burnmetrix-kiosk.service
install -m 755 "${PROJECT_ROOT}/scripts/start-dashboard.sh" /usr/local/bin/burnmetrix-start
install -m 755 "${PROJECT_ROOT}/scripts/stop-dashboard.sh" /usr/local/bin/burnmetrix-stop

systemctl daemon-reload
systemctl enable nginx
systemctl disable burnmetrix-backend burnmetrix-kiosk || true
systemctl restart nginx

echo "BurnMetrix Dashboard installed."
echo "Start it manually with: burnmetrix-start"
echo "Stop it manually with: burnmetrix-stop"

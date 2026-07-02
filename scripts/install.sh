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
apt-get install -y \
  ca-certificates \
  chromium-browser \
  curl \
  git \
  maven \
  nginx \
  nodejs \
  npm \
  openjdk-21-jdk \
  rsync \
  x11-xserver-utils

install -d -o "${SERVICE_USER}" -g "${SERVICE_USER}" "${INSTALL_DIR}"
install -d -o "${SERVICE_USER}" -g "${SERVICE_USER}" "${INSTALL_DIR}/backend"
install -d -o "${SERVICE_USER}" -g "${SERVICE_USER}" "${INSTALL_DIR}/frontend"
install -d -o "${SERVICE_USER}" -g "${SERVICE_USER}" "${INSTALL_DIR}/database/data"

echo "Building backend..."
sudo -u "${SERVICE_USER}" mvn -f "${PROJECT_ROOT}/backend/pom.xml" clean package
cp "${PROJECT_ROOT}"/backend/target/*.jar "${INSTALL_DIR}/backend/burnmetrix-backend.jar"

echo "Building frontend..."
sudo -u "${SERVICE_USER}" npm --prefix "${PROJECT_ROOT}/frontend" install
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
sed "s/User=pi/User=${SERVICE_USER}/g; s#/home/pi#/home/${SERVICE_USER}#g" "${PROJECT_ROOT}/scripts/burnmetrix-kiosk.service" >/etc/systemd/system/burnmetrix-kiosk.service

systemctl daemon-reload
systemctl enable nginx burnmetrix-backend burnmetrix-kiosk
systemctl restart nginx
systemctl restart burnmetrix-backend
systemctl restart burnmetrix-kiosk || true

echo "BurnMetrix Dashboard installed. Chromium will open http://localhost in kiosk mode on boot."

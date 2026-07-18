#!/usr/bin/env bash
set -euo pipefail

SERVICE_USER="${SUDO_USER:-${USER}}"
USER_HOME="$(getent passwd "${SERVICE_USER}" | cut -d: -f6)"

if [[ "$(id -u)" -ne 0 ]]; then
  exec sudo "$0" "$@"
fi

systemctl stop burnmetrix-kiosk || true
systemctl stop burnmetrix-backend || true

rm -rf "${USER_HOME}/.cache/chromium"
rm -rf "${USER_HOME}/.cache/chromium-browser"
rm -rf "${USER_HOME}/.config/chromium/Default/Cache"
rm -rf "${USER_HOME}/.config/chromium/Default/Code Cache"
rm -rf "${USER_HOME}/.config/chromium/Default/GPUCache"
rm -rf "${USER_HOME}/.config/chromium/Default/Service Worker"

systemctl start nginx
systemctl start burnmetrix-backend
systemctl start burnmetrix-kiosk

echo "BurnMetrix Chromium cache cleared and dashboard restarted."

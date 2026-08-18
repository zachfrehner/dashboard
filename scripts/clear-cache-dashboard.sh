#!/usr/bin/env bash
set -euo pipefail

SERVICE_USER="${SUDO_USER:-${USER}}"
USER_HOME="$(getent passwd "${SERVICE_USER}" | cut -d: -f6)"

if [[ "$(id -u)" -ne 0 ]]; then
  exec sudo "$0" "$@"
fi

systemctl stop burnmetrix-kiosk || true
systemctl stop burnmetrix-backend || true

pkill -f "chromium.*localhost" || true
pkill -f "chromium-browser.*localhost" || true

rm -rf "${USER_HOME}/.config/burnmetrix-kiosk"
rm -rf "${USER_HOME}/.cache/burnmetrix-kiosk"
rm -rf "${USER_HOME}/.cache/chromium"
rm -rf "${USER_HOME}/.cache/chromium-browser"
rm -rf "${USER_HOME}/.config/chromium/Default/Cache"
rm -rf "${USER_HOME}/.config/chromium/Default/Code Cache"
rm -rf "${USER_HOME}/.config/chromium/Default/GPUCache"
rm -rf "${USER_HOME}/.config/chromium/Default/IndexedDB"
rm -rf "${USER_HOME}/.config/chromium/Default/Local Storage"
rm -rf "${USER_HOME}/.config/chromium/Default/Service Worker"
rm -rf "${USER_HOME}/.config/chromium/Default/Storage"

systemctl restart nginx
systemctl start burnmetrix-backend
systemctl start burnmetrix-kiosk

echo "BurnMetrix Chromium cache cleared and dashboard restarted."

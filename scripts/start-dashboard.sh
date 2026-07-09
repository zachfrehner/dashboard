#!/usr/bin/env bash
set -euo pipefail

if [[ "$(id -u)" -ne 0 ]]; then
  exec sudo "$0" "$@"
fi

systemctl start nginx
systemctl start burnmetrix-backend
systemctl start burnmetrix-kiosk

echo "BurnMetrix Dashboard started."


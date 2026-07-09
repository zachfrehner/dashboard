#!/usr/bin/env bash
set -euo pipefail

if [[ "$(id -u)" -ne 0 ]]; then
  exec sudo "$0" "$@"
fi

systemctl stop burnmetrix-kiosk || true
systemctl stop burnmetrix-backend || true

echo "BurnMetrix Dashboard stopped."


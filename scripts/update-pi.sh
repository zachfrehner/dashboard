#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "${PROJECT_ROOT}"

if [[ -n "$(git status --short -- frontend/package-lock.json)" ]]; then
  echo "Restoring frontend/package-lock.json so git can pull cleanly..."
  git restore frontend/package-lock.json
fi

echo "Pulling latest BurnMetrix Dashboard code..."
git pull --ff-only

echo "Installing rebuilt backend/frontend and service files..."
"${PROJECT_ROOT}/scripts/install.sh"

echo "Clearing kiosk cache and restarting dashboard..."
burnmetrix-clear-cache

echo "BurnMetrix Dashboard is updated and running."

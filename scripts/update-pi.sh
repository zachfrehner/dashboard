#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [[ -n "${BURNMETRIX_PROJECT_ROOT:-}" ]]; then
  PROJECT_ROOT="${BURNMETRIX_PROJECT_ROOT}"
elif [[ -f "${SCRIPT_DIR}/../scripts/install.sh" ]]; then
  PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
elif [[ -f "${PWD}/scripts/install.sh" ]]; then
  PROJECT_ROOT="${PWD}"
else
  RUN_USER="${SUDO_USER:-${USER}}"
  USER_HOME="$(getent passwd "${RUN_USER}" | cut -d: -f6)"
  PROJECT_ROOT="${USER_HOME}/burnmetrix-dashboard"
fi

cd "${PROJECT_ROOT}"

REPO_USER="$(stat -c '%U' "${PROJECT_ROOT}")"

run_as_repo_user() {
  if [[ "$(id -u)" -eq 0 && "${REPO_USER}" != "root" ]]; then
    sudo -u "${REPO_USER}" "$@"
  else
    "$@"
  fi
}

if [[ -n "$(run_as_repo_user git status --short -- frontend/package-lock.json)" ]]; then
  echo "Restoring frontend/package-lock.json so git can pull cleanly..."
  run_as_repo_user git restore frontend/package-lock.json
fi

echo "Pulling latest BurnMetrix Dashboard code..."
run_as_repo_user git pull --ff-only

echo "Installing rebuilt backend/frontend and service files..."
"${PROJECT_ROOT}/scripts/install.sh"

echo "Clearing kiosk cache and restarting dashboard..."
burnmetrix-clear-cache

echo "BurnMetrix Dashboard is updated and running."

#!/usr/bin/env sh
set -eu
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
DIST_URL='https://services.gradle.org/distributions/gradle-8.13-bin.zip'
GRADLE_VERSION='8.13'
GRADLE_USER_HOME=${GRADLE_USER_HOME:-"$HOME/.gradle"}
if command -v gradle >/dev/null 2>&1; then
  exec gradle -p "$APP_HOME" "$@"
fi
for candidate in "$GRADLE_USER_HOME/wrapper/dists/gradle-$GRADLE_VERSION-bin"/*/gradle-$GRADLE_VERSION/bin/gradle "$GRADLE_USER_HOME/dispensa-bootstrap/gradle-$GRADLE_VERSION/bin/gradle"; do
  if [ -x "$candidate" ]; then exec "$candidate" -p "$APP_HOME" "$@"; fi
done
BOOT="$GRADLE_USER_HOME/dispensa-bootstrap"
ZIP="$BOOT/gradle-$GRADLE_VERSION-bin.zip"
mkdir -p "$BOOT"
if [ ! -x "$BOOT/gradle-$GRADLE_VERSION/bin/gradle" ]; then
  echo "Gradle $GRADLE_VERSION non presente: download della distribuzione ufficiale..." >&2
  if command -v curl >/dev/null 2>&1; then curl -fL --retry 2 "$DIST_URL" -o "$ZIP"
  elif command -v wget >/dev/null 2>&1; then wget -O "$ZIP" "$DIST_URL"
  else echo 'Installare curl o wget per scaricare Gradle.' >&2; exit 2; fi
  unzip -q -o "$ZIP" -d "$BOOT"
  rm -f "$ZIP"
fi
exec "$BOOT/gradle-$GRADLE_VERSION/bin/gradle" -p "$APP_HOME" "$@"

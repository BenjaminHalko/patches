export GRADLE_OPTS := "-Xmx1g"
export RESEAM_BIN := justfile_directory() / "build/engine/reseam"
export RESEAM_BUNDLE_KEY := justfile_directory() / "build/bundle-signing.key"

gradle := "./gradlew --no-daemon -Dorg.gradle.jvmargs=-Xmx1536m"
engine := `sed -n 's/.*id("app.reseam.workspace") version "\(.*\)".*/\1/p' settings.gradle.kts`
# The macOS builds are only published to the fork's GitHub releases.
cli_url := if os() == "macos" {
    "https://github.com/BenjaminHalko/reseam/releases/download/v" + engine + "/reseam-macos-" + if arch() == "aarch64" { "arm64" } else { "x64" }
} else {
    "https://cdn.reseam.app/engine/v" + engine + "/reseam-linux-x64"
}

cli:
    test -x "$RESEAM_BIN" || curl -fsSL --create-dirs -o "$RESEAM_BIN" {{ cli_url }}
    chmod +x "$RESEAM_BIN"

check: cli
    test -f "$RESEAM_BUNDLE_KEY" || "$RESEAM_BIN" bundle keygen --out "$RESEAM_BUNDLE_KEY"
    {{ gradle }} bundle

release tag: cli
    echo "$BUNDLE_SIGNING_KEY_B64" | base64 -d > "$RESEAM_BUNDLE_KEY"
    {{ gradle }} bundle -PreleaseTag={{ tag }}
    "$RESEAM_BIN" release patches build/reseam/reseam-patches.reseam --tag {{ tag }} --to s3://reseam-releases/patches \
        --public-url https://api.reseam.app --homepage https://reseam.app --forge-release

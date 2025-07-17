#!/usr/bin/env bash
set -euo pipefail

show_help() {
  cat <<EOF
Usage: $0 [--linux | --windows] [--help]

Options:
  --linux      Compile for Linux (default)
  --windows    Cross‑compile for Windows (x86_64)
  --help       Show this help message and exit
EOF
}

# Domyślny target
TARGET="linux"

# Parsowanie argumentów
while [[ $# -gt 0 ]]; do
  case "$1" in
    --linux)
      TARGET="linux"; shift ;;
    --windows)
      TARGET="windows"; shift ;;
    --help)
      show_help; exit 0 ;;
    *)
      echo "Unknown option: $1" >&2
      show_help; exit 1 ;;
  esac
done

# Wybór kompilatora i rozszerzenia
case "$TARGET" in
  linux)
    CC="gcc"
    OUT="out/alg"
    ;;
  windows)
    CC="x86_64-w64-mingw32-gcc"
    OUT="out/alg.exe"
    ;;
esac

SRC="src/main.c"
FLAGS="-O3 -Wextra -Wconversion -Wshadow"
TEST="out/test.toml"

# 1) Kompilacja
rm -fr out
mkdir -p out
echo "Compiling for $TARGET..."
"$CC" "$SRC" $FLAGS -o "$OUT"
echo "  → built: $OUT"

# 2) Generacja pliku TOML
cat > "$TEST" <<EOF
[node]
id = 123
x = 30
y = 30
connections = [122]
[node]
id = 122
x = 30
y = 30
connections = [302]
[node]
id = 302
x = 234
y = 234
connections = [123, 122]

EOF
echo "  → generated test file: $TEST"

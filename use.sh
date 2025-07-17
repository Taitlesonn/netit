#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'
trap 'echo "❌ Wystąpił błąd w linii $LINENO. Skrypt przerwany." >&2' ERR


#-------------------------------------------------------------------------------------------------------------------
# Wywołanie: use.sh [ --clean ] [ --no-run ] [ --set-version <version>] [ --rpm ] [ --wininstaler ] [ --help ]
#   --clean : czyści katalog wyjściowy
#   --no-run : kompiluje projekt bez uruchamiania
#   --set-version <version> : ustawia określoną wersje (dowolny string)
#   --rpm : buduje cały projekt i tworzy repozytorium rpm
#   --wininstaler : tworzy projekt na windows
#   --help : wyświetlenie tego co teraz czytasz
# 
# Zmiennymi środowiskowymi można kontrolować:
#   JAVA_FX_SDK : ścieżka do katalogu lib JavaFX
#   MAVEN_ARGS dodatkowe flagi do mvn
#   JAVA_ARGS dodatkowe falgi do javy
#---------------------------------------------------------------------------------------------------------------------

# Wymagane oprogramowanie

required_programs=("mvn" "git" "java" "tar" "rpm" "rpmbuild" "gcc")
missing=false

for prog in "${required_programs[@]}"; do
    if ! command -v "$prog" >/dev/null 2>&1; then
        echo "Brakuje: $prog"
        missing=true
    fi
done

if [ "$missing" = true ]; then
    echo "Zainstaluj brakujące programy i spróbuj ponownie."
    exit 1
fi

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"   # ścierzka wykonywanego skryptu
DEFAULT_VERSION="$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)"
VERSION="$DEFAULT_VERSION"


# Domyślna ścieżka do JavaFX
JAVA_FX_SDK="${JAVA_FX_SDK:-lib/linux/javafx-sdk-21.0.7/lib}"
MAVEN_ARGS="${MAVEN_ARGS:-}"
JAVA_ARGS="${JAVA_ARGS:-}"

OUT_DIR="$PROJECT_DIR/out"
TARGET_JAR="$PROJECT_DIR/target/original-netit-${VERSION}.jar"
OUTPUT_JAR="$OUT_DIR/netit.jar"

log()  { echo -e "\e[1;34m[INFO]\e[0m $*"; }
warn() { echo -e "\e[1;33m[WARN]\e[0m $*" >&2; }
error(){ echo -e "\e[1;31m[ERROR]\e[0m $*" >&2; exit 1; }

# Funkcja dla Help
usage(){
cat <<EOF
-------------------------------------------------------------------------------------------------------------------
Wywołanie: use.sh [ --clean ] [ --no-run ] [ --set-version <version>] [ --rpm ] [ --wininstaler ] [ --help ]
   --clean : czyści katalog wyjściowy
   --no-run : kompiluje projekt bez uruchamiania
   --set-version <version> : ustawia określoną wersje (dowolny string)
   --rpm : buduje cały projekt i tworzy repozytorium rpm
   --wininstaler : tworzy projekt na windows (domyślnie tworzy na linux)
   --help : wyświetlenie tego co teraz czytasz
 
Zmiennymi środowiskowymi można kontrolować:
   JAVA_FX_SDK : ścieżka do katalogu lib JavaFX
   MAVEN_ARGS dodatkowe flagi do mvn
   JAVA_ARGS dodatkowe falgi do javy
---------------------------------------------------------------------------------------------------------------------
EOF
}

CLEAN=false
NO_RUN=false
RPM=false
LINUX=true


# Parsowanie argumentów
while [[ $# -gt 0 ]]; do
    case $1 in
        --clean) CLEAN=true; shift;;
        --no-run) NO_RUN=true; shift;;
        --set-version)
            VERSION="$2"
            shift 2;;
        --rpm) RPM=true; shift;;
        --wininstaler) LINUX=false; shift;;
        --help) usage; exit 0;;
        *) error "Nieznana opcja: $1";;
    esac
done

# Wesja
if [[ "$VERSION" != "$DEFAULT_VERSION" ]]; then
    log "Ustawiam wersję na $VERSION w pom.xml..."
    mvn versions:set -DnewVersion="$VERSION" -DgenerateBackupPoms=false
    TARGET_JAR="$PROJECT_DIR/target/original-netit-${VERSION}.jar" 
else
    log "Używam wersji z pom.xml: $VERSION"
fi

# Czyszczenie
if $CLEAN; then
    log "Czyszczenie katalogów: out i target"
    rm -fr "$PROJECT_DIR/target" "$OUT_DIR"
    exit 0;
fi

# Budowanie Maven
log "Buduję projekt"
mvn clean package > /dev/null || {
    log "❌ Błąd podczas budowania projektu:"
    mvn clean package
    exit 1
}
# Budowa algorytmu w C
if $LINUX; then
    CC="gcc"
    OUT_C="$OUT_DIR/alg.out"
    log "Compiling for Linux"
else
    CC="x86_64-w64-mingw32-gcc"
    OUT_C="$OUT_DIR/alg.exe"
    log "Compiling for Windows"
fi
SRC_C="alg/src/main.c"
FLAGS_C=(-O3 -Wextra -Wconversion -Wshadow)
mkdir -p "$OUT_DIR"
"$CC" "$SRC_C" "${FLAGS_C[@]}" -o "$OUT_C"


# Przygotowywanie katalogu out
log "Przygotowuję katalog wyjściowy: $OUT_DIR"
mkdir -p "$OUT_DIR/files/"
mkdir -p "$OUT_DIR/files/ruter"
mkdir -p "$OUT_DIR/files/switch"
mkdir -p "$OUT_DIR/files/windows"
mkdir -p "$OUT_DIR/files/linux"
mkdir -p "$OUT_DIR/files/windows_s"
mkdir -p "$OUT_DIR/files/linux_s"
mkdir -p "$OUT_DIR/files/src"

cat > "$OUT_DIR/files/src/style-blue.css" << EOF
@keyframes deepShiftBlue {
  0%   { background-position: 0% 0%; }
  100% { background-position: 100% 100%; }
}

.body-std {
  /* tło: płynny, ciemny gradient błękitów */
  background: linear-gradient(135deg, #0a1b3d, #112a54, #0a1b3d);
  background-size: 400% 400%;
  animation: deepShiftBlue 30s ease infinite;

  color: #c8e1ff;
  font-family: 'Georgia', 'Times New Roman', serif;
  margin: 60px;
  line-height: 1.8;
  position: relative;
  overflow-x: hidden;
}

#particles-canvas {
  display: none;
}

.body-std .links-std {
  color: #9ecbff;
  text-decoration: underline dotted;
}

.body-std .h1 {
  font-family: 'Playfair Display', serif;
  font-size: 3em;
  color: #e0f0ff;
  margin-bottom: 0.5em;
  border-bottom: 2px solid #6fa8dc;
  padding-bottom: 0.2em;
}

.body-std .h2 {
  font-family: 'Playfair Display', serif;
  font-size: 2em;
  color: #b0d4ff;
  margin-top: 1.5em;
  margin-bottom: 0.3em;
}

.body-std .text {
  color: #d0e4ff;
  max-width: 700px;
  margin: 20px auto;
  font-size: 18px;
  text-align: justify;
  padding: 20px;
  background: rgba(16, 35, 56, 0.85); /* głęboki granat */
  box-shadow: inset 0 0 8px rgba(0,0,0,0.3);
}

.body-std .article {
  max-width: 800px;
  margin: auto;
  padding: 30px;
  background: rgba(10, 20, 35, 0.9); /* bardzo ciemny granat */
  border: 1px solid #3a558f; /* kontrastowy niebieski */
  border-radius: 6px;
  box-shadow: 0 6px 15px rgba(0,0,0,0.4);
}

.body-std .hr {
  border: none;
  border-top: 1px solid #3a558f;
  margin: 30px 0;
}

.reveal {
  opacity: 0;
  transform: translateY(15px);
  transition: opacity 0.6s ease-out, transform 0.6s ease-out;
}

.reveal.visible {
  opacity: 1;
  transform: translateY(0);
}
.responsive-img {
  display: block;      /* usuwa ewentualne białe przestrzenie obok */
  max-width: 100%;     /* obraz nigdy nie wyjdzie poza szerokość kontenera */
  height: auto;        /* zachowuje proporcje oryginału */
  margin: 0 auto;      /* wycentrowanie wewnątrz rodzica (opcjonalnie) */
}
EOF
cat > "$OUT_DIR/files/src/style-gray.css" << EOF
@keyframes gradientShift {
    0% { background-position: 0% 50%; }
    50% { background-position: 100% 50%; }
    100% { background-position: 0% 50%; }
}

.body-std {
    background: linear-gradient(-45deg, #1b2921, #304232, #3e4d3e, #1b2921);
    background-size: 400% 400%;
    animation: gradientShift 20s ease infinite;
    color: darkseagreen;
    font-family: Arial;
    margin: 60px;
    line-height: 1.6;
    overflow-x: hidden;
    position: relative;
}

#particles-canvas {
    position: fixed;
    top: 0;
    left: 0;
    z-index: -1;
    width: 100vw;
    height: 100vh;
    pointer-events: none;
}



/* osobna reguła dla linków */
.body-std .links-std {
    color: yellowgreen;
    text-decoration: none;
}

.body-std .h1{
    color: #a8ff60;
}
.body-std .h2 {
    color: #1e3f02;
}

.body-std .text {
    color: #9db18b;
    width: 600px;
    margin: 20px auto;
    font-family: Arial;
    font-size: 18px;
    line-height: 1.6;
    text-align: justify;
    padding: 20px;
}


.body-std .article {
    max-width: 900px;
    margin: auto;
    padding: 20px;
    background-color: #3e4d3e;
    border-radius: 10px;
    box-shadow: 0 0 20px #00000033;
}

.body-std .hr {
    border: 0;
    border-top: 1px solid #99cc99;
    margin: 20px 0;
}


.reveal {
    opacity: 0;
    transform: translateY(20px);
    transition: opacity 0.6s ease-out, transform 0.6s ease-out;
}

.reveal.visible {
    opacity: 1;
    transform: translateY(0);
}
.responsive-img {
  display: block;      /* usuwa ewentualne białe przestrzenie obok */
  max-width: 100%;     /* obraz nigdy nie wyjdzie poza szerokość kontenera */
  height: auto;        /* zachowuje proporcje oryginału */
  margin: 0 auto;      /* wycentrowanie wewnątrz rodzica (opcjonalnie) */
}
EOF
cat > "$OUT_DIR/files/src/style-mono.css" << EOF
@keyframes subtleShift {
  0%   { background-position: 0% 0%; }
  100% { background-position: 100% 100%; }
}

.body-std {
  /* lekko postarzana tekstura papieru w sepii */
  background-size: 200px 200px;
  color: #2e2e2e;
  font-family: 'Georgia', 'Times New Roman', serif;
  margin: 60px;
  line-height: 1.8;
  position: relative;
  overflow-x: hidden;
  filter: sepia(0.3) contrast(1.1) brightness(0.95);
}

#particles-canvas {
  display: none; /* wyłączamy efekt cząsteczek */
}

.body-std .links-std {
  color: #2e2e2e;
  text-decoration: underline dotted;
}

.body-std .h1 {
  font-family: 'Playfair Display', serif;
  font-size: 3em;
  color: #1a1a1a;
  margin-bottom: 0.5em;
  border-bottom: 2px solid #999;
  padding-bottom: 0.2em;
}

.body-std .h2 {
  font-family: 'Playfair Display', serif;
  font-size: 2em;
  color: #333;
  margin-top: 1.5em;
  margin-bottom: 0.3em;
}

.body-std .text {
  color: #2e2e2e;
  max-width: 700px;
  margin: 20px auto;
  font-size: 18px;
  text-align: justify;
  padding: 20px;
  background: rgba(255,255,255,0.8);
  box-shadow: inset 0 0 5px rgba(0,0,0,0.1);
}

.body-std .article {
  max-width: 800px;
  margin: auto;
  padding: 30px;
  background: rgba(255,255,255,0.9);
  border: 1px solid #ccc;
  border-radius: 4px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.2);
}

.body-std .hr {
  border: none;
  border-top: 1px solid #bbb;
  margin: 30px 0;
}

.reveal {
  opacity: 0;
  transform: translateY(15px);
  transition: opacity 0.6s ease-out, transform 0.6s ease-out;
}

.reveal.visible {
  opacity: 1;
  transform: translateY(0);
}

.responsive-img {
  display: block;      /* usuwa ewentualne białe przestrzenie obok */
  max-width: 100%;     /* obraz nigdy nie wyjdzie poza szerokość kontenera */
  height: auto;        /* zachowuje proporcje oryginału */
  margin: 0 auto;      /* wycentrowanie wewnątrz rodzica (opcjonalnie) */
}

EOF

cat > "$OUT_DIR/files/src/index.js" << EOF
EOF

SHADDED_JAR=$(ls "$PROJECT_DIR"/target/*-shaded.jar 2>/dev/null || true)
if [[ -z "$SHADDED_JAR" ]]; then
    SHADDED_JAR=$(ls "$PROJECT_DIR"/target/netit-*.jar | grep -v original || true)
fi


[[ -f "$SHADDED_JAR" ]] || error "Nie odnaleziono shaded JAR-a w target/"

log "Kopiuję uber-JAR do $OUT_DIR jako $OUTPUT_JAR"
cp "$SHADDED_JAR" "$OUTPUT_JAR"

# dalej bez zmian:
if $NO_RUN && $LINUX ; then
    if $RPM; then
        echo "[INFO] nie uruchamiam"
    else
        log "Uruchamiam aplikację JavaFX…"
        java \
            --module-path "$JAVA_FX_SDK" \
            --add-modules javafx.controls,javafx.fxml,javafx.web \
            $JAVA_ARGS \
            -jar "$OUTPUT_JAR"
    fi
else
   log "Pomijam uruchamianie aplikacji (--no-run)"
fi


SOURCE_DIR="$HOME/rpmbuild/SOURCES"
RPM_DIR="$HOME/rpmbuild/RPMS/x86_64"
OUTPUT_DIR="$PROJECT_DIR/rpm"
SPEC_FILE="$HOME/rpmbuild/SPECS/netit.spec"
TARBALL="$SOURCE_DIR/netit-$VERSION.tar.gz"
BUILD_DIR="$SOURCE_DIR/netit-$VERSION"
CHANGELOG_DATE=$(date +"%a %b %d %Y")
CHANGELOG_AUTHOR="Michal <michal.skoczylas.adam@gmail.com>"
INSTALLER_EXE="$PROJECT_DIR/wininstall/bin/netit-instaler.exe"
INSTALLER_F="$PROJECT_DIR/wininstall/bin/netit-instaler.exe.config"
JAVAFX_SDK_DIR_WINDOWS="$PROJECT_DIR/lib/windows/javafx-sdk-21.0.7"
JAVAFX_SDK_DIR_LINUX="$PROJECT_DIR/lib/linux/javafx-sdk-21.0.7"
OUT_JAR="$PROJECT_DIR/out/netit.jar"
TARGET_ROOT="$PROJECT_DIR/win-install"
TARGET_NETIT="$TARGET_ROOT/netit"


windows() {
  for f in "$INSTALLER_EXE" "$OUT_JAR" "$INSTALLER_F"; do
    [[ -f "$f" ]] || { echo "❌ Nie znaleziono pliku: $f"; exit 1; }
  done
  [[ -d "$JAVAFX_SDK_DIR_WINDOWS/lib" ]] || { echo "❌ Niepoprawna ścieżka do JavaFX: $JAVAFX_SDK_DIR_WINDOWS"; exit 1; }
  rm -fr "$TARGET_ROOT"
  mkdir -p "$TARGET_NETIT"
  cp -a "$JAVAFX_SDK_DIR_WINDOWS" "$TARGET_NETIT/javafx"
  cp -a "$INSTALLER_EXE" "$TARGET_ROOT"
  cp -a "$INSTALLER_F" "$TARGET_ROOT"
  cp -a "$OUT_JAR" "$TARGET_NETIT/"
  echo "[OK] Windows packaging done."
  ZIP_NAME="netit.zip"
  if ! command -v zip >/dev/null 2>&1; then
    echo "❌ Program 'zip' nie jest zainstalowany. Zainstaluj go i spróbuj ponownie."
    exit 1
  fi
    
  cd "$TARGET_ROOT"
  # Tworzenie archiwum (rekurencyjnie, z zachowaniem ścieżek)
  zip -rq "$PROJECT_DIR/$ZIP_NAME" . || error "Nie udało się utworzyć ZIP-a"
  cd "$PROJECT_DIR"

  echo "✅ Repozytorium zip utworzone: $ZIP_NAME"
  rm -fr "$TARGET_ROOT"
}

clean_old_builds() {
  echo "🔄 Czyszczenie starych plików..."
  rm -rf "$SOURCE_DIR"/* "$RPM_DIR"/* "$OUTPUT_DIR"/*
}

prepare_source_tarball() {
  echo "📁 Przygotowanie katalogu źródłowego i kopiowanie plików..."
  mkdir -p "$BUILD_DIR"
  cp -r "$JAVAFX_SDK_DIR_LINUX" "$BUILD_DIR/javafx-sdk" || { echo "❌ Brak Linux JavaFX SDK"; exit 1; }
  [[ -f out/netit.jar ]] || { echo "❌ Brak out/netit.jar"; exit 1; }
  cp -r out "$BUILD_DIR/"
  echo "📦 Tworzenie tarballa: $TARBALL"
  if ! tar -czf "$TARBALL" -C "$SOURCE_DIR" "netit-$VERSION"; then
    echo "❌ Nie udało się utworzyć archiwum TAR: $TARBALL"
    exit 1
  fi

} 

generate_spec_and_build_rpm() {
  echo "🛠 Generowanie SPEC i budowanie RPM..."
  cat > "$SPEC_FILE" <<EOF
Name:           netit
Version:        $VERSION
Release:        1%{?dist}
Summary:        NETIT JavaFX Application

License:        ISC
Source0:        netit-$VERSION.tar.gz

%global debug_package %{nil}
BuildArch:      x86_64
Requires:       java-21-openjdk

%description
NETIT JavaFX-based application.

%prep
%setup -q

%install
rm -rf %{buildroot}
mkdir -p %{buildroot}/opt/netit
cp -a * %{buildroot}/opt/netit/
mkdir -p %{buildroot}/usr/bin
cat > %{buildroot}/usr/bin/netit << 'EOL'
#!/bin/bash
cd /opt/netit
export LIBGL_ALWAYS_SOFTWARE=1
exec java -Dprism.order=sw \
  --module-path /opt/netit/javafx-sdk/lib \
  --add-modules javafx.controls,javafx.fxml,javafx.web \
  -jar /opt/netit/out/netit.jar
EOL
chmod +x %{buildroot}/usr/bin/netit

%files
/opt/netit
/usr/bin/netit

%changelog
* $CHANGELOG_DATE $CHANGELOG_AUTHOR - $VERSION-1
- Initial RPM packaging
EOF
  if ! rpmbuild -ba "$SPEC_FILE" &> /dev/null; then
    echo "❌ Błąd podczas budowania RPM"
    exit 1
  fi
  RPM_FILE=$(ls "$RPM_DIR"/*.rpm 2>/dev/null || true)
  [[ -n "$RPM_FILE" ]] || { echo "❌ Brak RPM do podpisania"; exit 1; }
  mkdir -p "$OUTPUT_DIR"
  mv "$RPM_FILE" "$OUTPUT_DIR/netit-$VERSION-x86_64.rpm"
  echo "✅ RPM gotowe: $OUTPUT_DIR/netit-$VERSION-x86_64.rpm"
}

if $RPM; then
    clean_old_builds
    prepare_source_tarball
    generate_spec_and_build_rpm
elif ! $LINUX; then
    windows
fi

echo "🎉 Wszystkie kroki wykonane pomyślnie."


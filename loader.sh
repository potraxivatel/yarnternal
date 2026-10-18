#!/usr/bin/env bash
# ============================================================
#   YARNNTERNAL · loader
#
#   Dev-запуск клиента: ./gradlew runClient
#
#   Использование:
#       ./loader                        интерактивное меню
#       ./loader --run                  запустить сразу (настройки из run/loader.conf)
#       ./loader -u Foks -r 6G -l en_us [--run]
#       ./loader --prism                собрать jar, поставить в Prism и запустить
#       ./loader --dry                  проверить настройки, ничего не запуская
#       ./loader --help
#
#   Настройки хранятся в run/loader.conf (папка run/ в .gitignore).
# ============================================================
set -euo pipefail
cd "$(dirname "$0")"

# ── пути ────────────────────────────────────────────────────────────────────
CONFIG_FILE="run/loader.conf"
GRADLE_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
ASSETS_DIR="$GRADLE_HOME/caches/fabric-loom/assets"

PRISM_INSTANCE="1.21.11"
PRISM_DIR="$HOME/.local/share/PrismLauncher/instances/$PRISM_INSTANCE"
PRISM_APP="$HOME/Загрузки/PrismLauncher-Linux-x86_64.AppImage"

# ── настройки (переопределяются из run/loader.conf и флагами) ────────────────
USERNAME="Foks"
RAM="4G"
LANG_CODE="ru_ru"

# ── цвета ───────────────────────────────────────────────────────────────────
c() { printf '\e[%sm' "$1"; }
RESET="$(c 0)"; BOLD="$(c 1)"; DIM="$(c 2)"
MAG="$(c '38;5;213')"; CYAN="$(c '38;5;51')"; GRAY="$(c '38;5;243')"
GREEN="$(c '38;5;84')"; YELLOW="$(c '38;5;228')"; RED="$(c '38;5;203')"

# ── ASCII-арт ───────────────────────────────────────────────────────────────
banner() {
    printf '%s' "$MAG"
    cat <<'ART'
██╗   ██╗  █████╗  ██████╗  ███╗   ██╗ ████████╗ ███████╗ ██████╗  ███╗   ██╗  █████╗  ██╗
╚██╗ ██╔╝ ██╔══██╗ ██╔══██╗ ████╗  ██║ ╚══██╔══╝ ██╔════╝ ██╔══██╗ ████╗  ██║ ██╔══██╗ ██║
 ╚████╔╝  ███████║ ██████╔╝ ██╔██╗ ██║    ██║    █████╗   ██████╔╝ ██╔██╗ ██║ ███████║ ██║
  ╚██╔╝   ██╔══██║ ██╔══██╗ ██║╚██╗██║    ██║    ██╔══╝   ██╔══██╗ ██║╚██╗██║ ██╔══██║ ██║
   ██║    ██║  ██║ ██║  ██║ ██║ ╚████║    ██║    ███████╗ ██║  ██║ ██║ ╚████║ ██║  ██║ ███████╗
   ╚═╝    ╚═╝  ╚═╝ ╚═╝  ╚═╝ ╚═╝  ╚═══╝    ╚═╝    ╚══════╝ ╚═╝  ╚═╝ ╚═╝  ╚═══╝ ╚═╝  ╚═╝ ╚══════╝
ART
    printf '%s' "$RESET"
}

# ── вывод ───────────────────────────────────────────────────────────────────
# padding не через %-6s: printf считает ширину в байтах, а «ник» на не-UTF-8
# локали весит 6 байт — колонки разъезжаются. Поэтому отступы заданы в метке.
kv()   { printf '   %s%s%s %s%s%s\n' "$GRAY" "$1" "$RESET" "$BOLD" "$2" "$RESET"; }
info() { printf '   %s%s%s\n' "$GRAY" "$1" "$RESET"; }
ok()   { printf '   %s✔%s %s\n' "$GREEN" "$RESET" "$1"; }
warn() { printf '   %s!%s %s\n' "$YELLOW" "$RESET" "$1"; }
err()  { printf '   %s✘%s %s\n' "$RED" "$RESET" "$1"; }
ask()  { printf '   %s?%s %s' "$CYAN" "$RESET" "$1"; }

# ── валидация ───────────────────────────────────────────────────────────────
valid_username() { [[ "$1" =~ ^[A-Za-z0-9_]{3,16}$ ]]; }
valid_ram()      { [[ "$1" =~ ^[0-9]+[GgMm]$ ]]; }
valid_lang()     { [[ "$1" =~ ^[a-z]{2}_[a-z]{2}$ ]]; }

# ── конфиг ──────────────────────────────────────────────────────────────────
load_config() {
    [ -f "$CONFIG_FILE" ] || return 0
    while IFS='=' read -r key value; do
        case "$key" in
            username) USERNAME="$value" ;;
            ram)      RAM="$value" ;;
            lang)     LANG_CODE="$value" ;;
        esac
    done < "$CONFIG_FILE"
}

save_config() {
    mkdir -p "$(dirname "$CONFIG_FILE")"
    {
        printf 'username=%s\n' "$USERNAME"
        printf 'ram=%s\n'      "$RAM"
        printf 'lang=%s\n'     "$LANG_CODE"
    } > "$CONFIG_FILE"
}

validate_all() {
    local bad=0
    valid_username "$USERNAME" || { err "ник «$USERNAME»: 3-16 символов, A-Z a-z 0-9 _"; bad=1; }
    valid_ram      "$RAM"      || { err "озу «$RAM»: формат 4G или 2048M"; bad=1; }
    valid_lang     "$LANG_CODE" || { err "язык «$LANG_CODE»: формат xx_yy (ru_ru)"; bad=1; }
    [ "$bad" = "0" ] || exit 1
}

# ── язык игры ───────────────────────────────────────────────────────────────
# У Minecraft нет аргумента командной строки для языка — он лежит в options.txt.
# Поэтому аккуратно правим одну строку: убираем старую lang: и дописываем новую.
patch_lang() {
    local file="$1"
    mkdir -p "$(dirname "$file")"
    if [ -f "$file" ]; then
        grep -v '^lang:' "$file" > "$file.tmp" || true
        mv "$file.tmp" "$file"
    fi
    printf 'lang:%s\n' "$LANG_CODE" >> "$file"
}

# ── java ────────────────────────────────────────────────────────────────────
find_java() {
    local -a candidates=()
    local d major
    for d in "$HOME"/jdk/jdk-*; do
        [ -x "$d/bin/java" ] && candidates+=("$d")
    done
    if command -v java >/dev/null 2>&1; then
        candidates+=("$(dirname "$(dirname "$(readlink -f "$(command -v java)")")")")
    fi
    [ "${#candidates[@]}" -gt 0 ] || return 1
    for d in "${candidates[@]}"; do
        major="$("$d/bin/java" -version 2>&1 | sed -nE '1s/.*version "([0-9]+).*/\1/p')"
        if [ -n "$major" ] && [ "$major" -ge 21 ]; then
            printf '%s' "$d"; return 0
        fi
    done
    return 1
}

setup_java() {
    local jdk
    if ! jdk="$(find_java)"; then
        err "не нашёл Java 21+ — поставь JDK и повтори"
        exit 1
    fi
    export JAVA_HOME="$jdk"
    info "java: $("$jdk/bin/java" -version 2>&1 | head -n1)"
}

# ── запуск: dev через gradlew runClient ─────────────────────────────────────
run_runclient() {
    setup_java
    patch_lang "run/options.txt"
    info "язык игры: $LANG_CODE"
    echo
    info "./gradlew runClient -PmcUsername=$USERNAME -PmcRam=$RAM"
    if [ ! -d "$ASSETS_DIR" ]; then
        warn "первый запуск: Gradle скачает ассеты игры (~500 МБ), это надолго"
    fi
    echo
    printf '   %s▸ ЗАПУСКАЮ YARNNTERNAL...%s\n\n' "$GREEN$BOLD" "$RESET"
    ./gradlew runClient -PmcUsername="$USERNAME" -PmcRam="$RAM" --console=plain
    echo
    ok "игра закрыта"
}

# ── запуск: сборка jar + PrismLauncher (реальный аккаунт, можно на сервера) ──
run_prism() {
    setup_java
    printf '   %s▸ собираю мод...%s\n' "$GREEN" "$RESET"
    ./gradlew build --console=plain -q

    local jar
    jar="$(ls build/libs/yarnternal-*.jar 2>/dev/null | grep -vE 'sources|dev' | head -n1 || true)"
    if [ -z "$jar" ]; then
        err "jar не найден в build/libs"
        return 1
    fi

    mkdir -p "$PRISM_DIR/minecraft/mods"
    rm -f "$PRISM_DIR/minecraft/mods"/yarnternal-*.jar
    cp "$jar" "$PRISM_DIR/minecraft/mods/"
    ok "мод установлен: $jar"

    if [ -d "$PRISM_DIR/minecraft" ]; then
        patch_lang "$PRISM_DIR/minecraft/options.txt"
    fi

    local prism="$PRISM_APP"
    if [ ! -x "$prism" ]; then
        prism="$(command -v prismlauncher || true)"
    fi
    if [ -z "$prism" ] || [ ! -x "$prism" ]; then
        err "PrismLauncher не найден — мод уже установлен, запусти игру сам"
        return 1
    fi

    printf '   %s▸ запускаю Prism (инстанс %s)...%s\n' "$GREEN" "$PRISM_INSTANCE" "$RESET"
    nohup "$prism" --launch "$PRISM_INSTANCE" >/dev/null 2>&1 &
}

# ── меню ────────────────────────────────────────────────────────────────────
usage() {
    cat <<'EOF'
   Использование:
     ./loader                        интерактивное меню
     ./loader --run                  сразу запустить (настройки из run/loader.conf)
     ./loader -u Ник -r 6G -l en_us [--run]
     ./loader --prism                собрать jar и запустить через Prism
     ./loader --dry                  проверить настройки, ничего не запуская
     ./loader --help

   Флаги:
     -u, --username <ник>   игровой ник (3-16 символов: A-Z a-z 0-9 _)
     -r, --ram <4G|2048M>   сколько ОЗУ дать игре
     -l, --lang <ru_ru>     язык игры
         --run              без меню, сразу запуск
         --prism            через PrismLauncher вместо gradlew runClient
EOF
}

ask_username() {
    local input
    ask "новый ник [$USERNAME]: "
    read -r input || return 0
    [ -z "$input" ] && return 0
    if valid_username "$input"; then
        USERNAME="$input"; ok "ник: $USERNAME"
    else
        err "ник: 3-16 символов, только A-Z a-z 0-9 _"; sleep 2
    fi
}

ask_ram() {
    local input
    info "2G 1 · 3G 2 · 4G 3 · 6G 4 · 8G 5 · 12G 6 · свой 0"
    ask "выбор: "
    read -r input || return 0
    case "$input" in
        1) RAM="2G" ;;  2) RAM="3G" ;;  3) RAM="4G" ;;
        4) RAM="6G" ;;  5) RAM="8G" ;;  6) RAM="12G" ;;
        0) ask "введи (например 5G или 2048M): "
           read -r input || return 0
           if valid_ram "$input"; then RAM="${input^^}"; else err "формат: 4G / 2048M"; sleep 2; return 0; fi ;;
        *) return 0 ;;
    esac
    ok "озу: $RAM"
}

ask_language() {
    local input
    info "ru_ru 1 · en_us 2 · uk_ua 3 · de_de 4 · fr_fr 5 · es_es 6 · свой 0"
    ask "выбор: "
    read -r input || return 0
    case "$input" in
        1) LANG_CODE="ru_ru" ;; 2) LANG_CODE="en_us" ;; 3) LANG_CODE="uk_ua" ;;
        4) LANG_CODE="de_de" ;; 5) LANG_CODE="fr_fr" ;; 6) LANG_CODE="es_es" ;;
        0) ask "код языка (например pl_pl): "
           read -r input || return 0
           if valid_lang "$input"; then LANG_CODE="$input"; else err "формат: xx_yy"; sleep 2; return 0; fi ;;
        *) return 0 ;;
    esac
    ok "язык: $LANG_CODE"
}

pause() { ask "Enter — назад в меню..."; read -r _ || true; }

menu_loop() {
    while true; do
        clear 2>/dev/null || true
        banner
        echo
        kv "ник " "$USERNAME"
        kv "озу " "$RAM"
        kv "язык" "$LANG_CODE"
        echo
        printf '   %s[1]%s ник     %s[2]%s озу     %s[3]%s язык\n' \
               "$CYAN" "$RESET" "$CYAN" "$RESET" "$CYAN" "$RESET"
        echo
        printf '   %s[4]%s %s▸ ЗАПУСТИТЬ%s   %sgradlew runClient%s\n' \
               "$CYAN" "$RESET" "$GREEN$BOLD" "$RESET" "$DIM" "$RESET"
        printf '   %s[5]%s %s▸ Prism + запустить%s   %sсобрать jar и в инстанс%s\n' \
               "$CYAN" "$RESET" "$CYAN" "$RESET" "$DIM" "$RESET"
        printf '   %s[0]%s выход\n' "$CYAN" "$RESET"
        echo
        ask "выбор: "
        local choice=""
        read -r choice || exit 0
        case "$choice" in
            1) ask_username; save_config ;;
            2) ask_ram; save_config ;;
            3) ask_language; save_config ;;
            4) save_config; run_runclient; pause ;;
            5) save_config; run_prism; pause ;;
            0|q|Q|exit) exit 0 ;;
            *) ;;
        esac
    done
}

# ── main ────────────────────────────────────────────────────────────────────
load_config

MODE="menu"
while [ $# -gt 0 ]; do
    case "$1" in
        -u|--username) USERNAME="${2:-}"; shift 2 ;;
        -r|--ram)      RAM="${2:-}"; shift 2 ;;
        -l|--lang)     LANG_CODE="${2:-}"; shift 2 ;;
        --run)         MODE="run"; shift ;;
        --prism)       MODE="prism"; shift ;;
        --dry|--check) MODE="dry"; shift ;;
        -h|--help)     MODE="help"; shift ;;
        *) printf 'неизвестный флаг: %s\n\n' "$1"; usage; exit 1 ;;
    esac
done

case "$MODE" in
    help)  usage; exit 0 ;;
    run)   validate_all; run_runclient; exit 0 ;;
    prism) validate_all; run_prism; exit 0 ;;
    dry)   validate_all
           printf '   %s▸ проверка без запуска%s\n\n' "$CYAN" "$RESET"
           kv "ник " "$USERNAME"
           kv "озу " "$RAM"
           kv "язык" "$LANG_CODE"
           patch_lang "run/options.txt"
           echo
           info "options.txt: run/options.txt -> lang:$LANG_CODE"
           info "команда: ./gradlew runClient -PmcUsername=$USERNAME -PmcRam=$RAM"
           echo
           ok "всё ок, запуск не выполнялся"
           exit 0 ;;
esac

menu_loop

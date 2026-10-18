package main

import (
	"fmt"
	"os"
	"strings"

	"github.com/charmbracelet/lipgloss"
	"golang.org/x/term"
)

// ── логотип ─────────────────────────────────────────────────────────────────
// Лого берётся из logo.txt в корне проекта — туда можно вставить любой свой
// ASCII (отступы строк сохраняются как есть). Если файла нет — встроенный
// вариант: широкий (ansi_regular), на узком терминале маленький.
var logoWide = []string{
	`██    ██  █████  ██████  ███    ██ ████████ ███████ ██████  ███    ██  █████  ██`,
	` ██  ██  ██   ██ ██   ██ ████   ██    ██    ██      ██   ██ ████   ██ ██   ██ ██`,
	`  ████   ███████ ██████  ██ ██  ██    ██    █████   ██████  ██ ██  ██ ███████ ██`,
	`   ██    ██   ██ ██   ██ ██  ██ ██    ██    ██      ██   ██ ██  ██ ██ ██   ██ ██`,
	`   ██    ██   ██ ██   ██ ██   ████    ██    ███████ ██   ██ ██   ████ ██   ██ ███████`,
}

var logoSmall = []string{
	"__   ___   ___ _  _ _____ ___ ___ _  _   _   _",
	"\\ \\ / /_\\ | _ \\ \\| |_   _| __| _ \\ \\| | /_\\ | |",
	" \\ V / _ \\|   / .` | | | | _||   / .` |/ _ \\| |__",
	"  |_/_/ \\_\\_|_\\_|\\_| |_| |___|_|_\\_|\\_/_/ \\_\\____|",
}

func loadLogo(root string, termWidth int) []string {
	if data, err := os.ReadFile(logoFile(root)); err == nil {
		var lines []string
		for _, l := range strings.Split(strings.ReplaceAll(string(data), "\r\n", "\n"), "\n") {
			lines = append(lines, strings.TrimRight(l, " \t"))
		}
		for len(lines) > 0 && lines[0] == "" {
			lines = lines[1:]
		}
		for len(lines) > 0 && lines[len(lines)-1] == "" {
			lines = lines[:len(lines)-1]
		}
		if len(lines) > 0 {
			return lines
		}
	}
	if termWidth >= 96 {
		return logoWide
	}
	return logoSmall
}

func maxWidth(lines []string) int {
	w := 0
	for _, l := range lines {
		if n := lipgloss.Width(l); n > w {
			w = n
		}
	}
	return w
}

func padRight(s string, width int) string {
	if n := width - lipgloss.Width(s); n > 0 {
		return s + strings.Repeat(" ", n)
	}
	return s
}

func centerText(s string, width int) string {
	n := width - lipgloss.Width(s)
	if n <= 0 {
		return s
	}
	left := n / 2
	return strings.Repeat(" ", left) + s + strings.Repeat(" ", n-left)
}

// renderPanelFromLogo — лого вертикальным градиентом в рамке + подпись.
// static = true: без анимации (для --help / --dry), иначе «водопад» по flow.
func renderPanelFromLogo(logo []string, static bool, flow float64) string {
	width := maxWidth(logo)
	last := float64(len(logo) - 1)
	if last < 1 {
		last = 1
	}

	var b strings.Builder
	for i, line := range logo {
		if i > 0 {
			b.WriteString("\n")
		}
		pos := float64(i) / last
		col := gradAt(pos)
		if !static {
			col = waterfall(pos, flow)
		}
		b.WriteString(lipgloss.NewStyle().Foreground(col).Render(padRight(line, width)))
	}
	sub := fmt.Sprintf("v%s — dev loader", version)
	b.WriteString("\n" + styleDim.Render(centerText(sub, width)))

	return styleBorder.Render(b.String())
}

func terminalWidth() int {
	if w, _, err := term.GetSize(int(os.Stdout.Fd())); err == nil && w > 0 {
		return w
	}
	if cols := os.Getenv("COLUMNS"); cols != "" {
		var w int
		if _, err := fmt.Sscanf(cols, "%d", &w); err == nil && w > 0 {
			return w
		}
	}
	return 100
}

func clearScreen() {
	if term.IsTerminal(int(os.Stdout.Fd())) {
		fmt.Print("\033[2J\033[H")
	}
}

// printCenteredBlock — печатает многострочный блок по центру терминала.
func printCenteredBlock(block string) {
	w := terminalWidth()
	for _, line := range strings.Split(block, "\n") {
		fmt.Println(centerText(line, w))
	}
}

// settingsLine — строка как «Memory: … | Provider: …» из примера: текущие настройки.
func settingsLine(s settings) string {
	sep := styleDim.Render("   |   ")
	label := func(t string) string { return stylePurple.Render(t + ": ") }
	value := func(t string) string { return styleBold.Render(t) }
	return label("Память") + value(s.Ram) + sep +
		label("Ник") + value(s.Username) + sep +
		label("Язык") + value(s.Lang)
}

// ── справка ─────────────────────────────────────────────────────────────────
func usage(root string) {
	clearScreen()
	printCenteredBlock(renderPanelFromLogo(loadLogo(root, terminalWidth()), true, 0))
	fmt.Println()

	type row struct{ cmd, desc string }

	fmt.Println(styleBold.Render("   Использование:"))
	commands := []row{
		{"loader-go", "интерактивное меню"},
		{"loader-go --run", "сразу запустить (настройки из run/loader.conf)"},
		{"loader-go -u Ник -r 6G -l en_us [--run]", ""},
		{"loader-go --prism", "собрать jar и запустить через Prism"},
		{"loader-go --dry", "проверить настройки, ничего не запуская"},
		{"loader-go --help", ""},
	}
	for _, r := range commands {
		line := "     " + styleCyan.Render(r.cmd)
		if r.desc != "" {
			line += styleDim.Render("  " + r.desc)
		}
		fmt.Println(line)
	}

	fmt.Println()
	fmt.Println(styleBold.Render("   Флаги:"))
	flags := []row{
		{"-u, --username <ник>", "игровой ник (3-16 символов: A-Z a-z 0-9 _)"},
		{"-r, --ram <4G|2048M>", "сколько ОЗУ дать игре"},
		{"-l, --lang <ru_ru>", "язык игры"},
		{"--run", "без меню, сразу запуск"},
		{"--prism", "через PrismLauncher вместо gradlew runClient"},
	}
	for _, f := range flags {
		fmt.Println("     " + styleCyan.Render(f.cmd) + styleDim.Render("  "+f.desc))
	}
}

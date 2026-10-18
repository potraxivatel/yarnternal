package main

import (
	"fmt"
	"math"
	"strconv"
	"strings"

	"github.com/charmbracelet/lipgloss"
)

// Палитра — та же, что у питоновского лоадера: два цвета градиента
// («заменяют друг друга» бегущей полосой) плюс служебные оттенки.
const (
	gradTop     = "#C74BFF"
	gradBot     = "#4C63FF"
	colorBorder = "#8A46E8"
	colorGreen  = "#7CE38B"
	colorPurple = "#B98CFF"
	colorGray   = "#8A8A9E"
	colorDim    = "#5F5F73"
	colorYellow = "#E8D96A"
	colorRed    = "#F26D6D"
	colorCyan   = "#66D9FF"
	colorWhite  = "#FFFFFF"
	colorInk    = "#0E0E14"
)

var (
	styleDim    = lipgloss.NewStyle().Foreground(lipgloss.Color(colorDim))
	styleGray   = lipgloss.NewStyle().Foreground(lipgloss.Color(colorGray))
	styleBold   = lipgloss.NewStyle().Bold(true).Foreground(lipgloss.Color(colorWhite))
	styleGreen  = lipgloss.NewStyle().Bold(true).Foreground(lipgloss.Color(colorGreen))
	styleYellow = lipgloss.NewStyle().Foreground(lipgloss.Color(colorYellow))
	styleRed    = lipgloss.NewStyle().Foreground(lipgloss.Color(colorRed))
	styleCyan   = lipgloss.NewStyle().Bold(true).Foreground(lipgloss.Color(colorCyan))
	stylePurple = lipgloss.NewStyle().Foreground(lipgloss.Color(colorPurple))
	styleBorder = lipgloss.NewStyle().
			Border(lipgloss.RoundedBorder()).
			BorderForeground(lipgloss.Color(colorBorder)).
			Padding(1, 3)
)

func parseHex(c string) (int, int, int) {
	c = strings.TrimPrefix(c, "#")
	r, _ := strconv.ParseInt(c[0:2], 16, 64)
	g, _ := strconv.ParseInt(c[2:4], 16, 64)
	b, _ := strconv.ParseInt(c[4:6], 16, 64)
	return int(r), int(g), int(b)
}

// lerpHex — плавный переход между двумя hex-цветами, t = 0..1.
func lerpHex(a, b string, t float64) lipgloss.Color {
	t = math.Max(0, math.Min(1, t))
	ar, ag, ab := parseHex(a)
	br, bg, bb := parseHex(b)
	return lipgloss.Color(fmt.Sprintf("#%02X%02X%02X",
		int(float64(ar)+float64(br-ar)*t+0.5),
		int(float64(ag)+float64(bg-ag)*t+0.5),
		int(float64(ab)+float64(bb-ab)*t+0.5)))
}

// gradAt — статичный вертикальный градиент (для --help / --dry).
func gradAt(t float64) lipgloss.Color { return lerpHex(gradTop, gradBot, t) }

// gradWave — цвет «бегущей полосы»: две краски гоняются друг за другом
// синусоидой по фазе (для пульсации ключей и акцентов).
func gradWave(pos, phase float64) lipgloss.Color {
	t := 0.5 + 0.5*math.Sin(2*math.Pi*(pos-phase))
	return lerpHex(gradTop, gradBot, t)
}

// waterfall — «водопад» для лого: сверху вниз плывёт длинная полоса
// градиента A→B→A (период — две высоты лого). Строки при этом показывают
// ровный градиент, а не мигание; на стыке сходятся одинаковые цвета —
// шва не видно. pos — позиция строки по высоте (0..1), phase — время.
func waterfall(pos, phase float64) lipgloss.Color {
	return gradWave(pos/2, phase)
}

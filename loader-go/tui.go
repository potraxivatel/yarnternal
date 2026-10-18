package main

import (
	"strings"
	"time"

	tea "github.com/charmbracelet/bubbletea"
	"github.com/charmbracelet/lipgloss"
)

type itemKind int

const (
	itemRun itemKind = iota
	itemNick
	itemRam
	itemLang
	itemPrism
)

type menuItem struct {
	key   string
	label string
	hint  string
	kind  itemKind
}

var menuItems = []menuItem{
	{key: "1", label: "Запустить игру", hint: "gradlew runClient · дев-режим, фейк-аккаунт", kind: itemRun},
	{key: "2", label: "Ник", kind: itemNick},
	{key: "3", label: "ОЗУ", kind: itemRam},
	{key: "4", label: "Язык", kind: itemLang},
	{key: "5", label: "Prism + запустить", hint: "реальный аккаунт · для игры на серверах", kind: itemPrism},
}

type action int

const (
	actionNone action = iota
	actionRun
	actionPrism
)

type inputState struct {
	kind    itemKind
	title   string
	hint    string
	buffer  string
	invalid string
}

type tickMsg struct{}

type model struct {
	root     string
	settings settings
	logo     []string

	width, height int

	phase  float64 // пульсация (ключи/акценты)
	flow   float64 // «водопад» лого
	cursor int
	input  *inputState

	status      string
	statusErr   bool
	statusTicks int

	action action
}

func newModel(root string) model {
	return model{
		root:     root,
		settings: loadSettings(root),
		logo:     loadLogo(root, terminalWidth()),
	}
}

const (
	keyPhaseStep = 0.018 // пульсация ключей и акцентов (кадр ~100 мс)
	logoFlowStep = 0.025 // «водопад» лого: волна света проходит сверху вниз ≈ 2 сек
)

func (m model) Init() tea.Cmd { return tickCmd() }

func tickCmd() tea.Cmd {
	return tea.Tick(100*time.Millisecond, func(time.Time) tea.Msg { return tickMsg{} })
}

func (m model) Update(msg tea.Msg) (tea.Model, tea.Cmd) {
	switch msg := msg.(type) {
	case tea.WindowSizeMsg:
		m.width, m.height = msg.Width, msg.Height
		m.logo = loadLogo(m.root, msg.Width)
		return m, nil
	case tickMsg:
		m.phase += keyPhaseStep
		if m.phase > 1 {
			m.phase--
		}
		m.flow += logoFlowStep
		if m.flow > 1 {
			m.flow--
		}
		if m.statusTicks > 0 {
			m.statusTicks--
			if m.statusTicks == 0 {
				m.status = ""
			}
		}
		return m, tickCmd()
	case tea.KeyMsg:
		if m.input != nil {
			return m.updateInput(msg)
		}
		return m.updateMenu(msg)
	}
	return m, nil
}

func (m model) updateMenu(msg tea.KeyMsg) (tea.Model, tea.Cmd) {
	switch msg.String() {
	case "up":
		m.cursor = (m.cursor - 1 + len(menuItems)) % len(menuItems)
	case "down":
		m.cursor = (m.cursor + 1) % len(menuItems)
	case "q", "ctrl+c":
		m.action = actionNone
		return m, tea.Quit
	case "enter":
		return m.activate(m.cursor)
	}
	return m, nil
}

func (m model) activate(i int) (tea.Model, tea.Cmd) {
	switch menuItems[i].kind {
	case itemRun:
		m.action = actionRun
		return m, tea.Quit
	case itemPrism:
		m.action = actionPrism
		return m, tea.Quit
	case itemNick:
		m.input = &inputState{kind: itemNick, title: "новый ник", hint: "3-16 символов: A-Z a-z 0-9 _", buffer: m.settings.Username}
	case itemRam:
		m.input = &inputState{kind: itemRam, title: "озу", hint: "например 4G или 2048M", buffer: m.settings.Ram}
	case itemLang:
		m.input = &inputState{kind: itemLang, title: "язык", hint: "формат xx_yy (ru_ru)", buffer: m.settings.Lang}
	}
	return m, nil
}

func (m model) updateInput(msg tea.KeyMsg) (tea.Model, tea.Cmd) {
	switch msg.String() {
	case "ctrl+c":
		m.action = actionNone
		return m, tea.Quit
	case "esc":
		m.input = nil
	case "enter":
		value := strings.TrimSpace(m.input.buffer)
		if value == "" {
			m.input = nil
			return m, nil
		}
		switch m.input.kind {
		case itemNick:
			if !usernameRe.MatchString(value) {
				m.input.invalid = "ник: 3-16 символов, только A-Z a-z 0-9 _"
				return m, nil
			}
			m.settings.Username = value
		case itemRam:
			value = strings.ToUpper(value)
			if !ramRe.MatchString(value) {
				m.input.invalid = "формат: 4G / 2048M"
				return m, nil
			}
			m.settings.Ram = value
		case itemLang:
			if !langRe.MatchString(value) {
				m.input.invalid = "формат: xx_yy (ru_ru)"
				return m, nil
			}
			m.settings.Lang = value
		}
		if err := m.settings.save(m.root); err != nil {
			m.flash("не смог сохранить run/loader.conf: "+err.Error(), true)
		} else {
			m.flash("сохранено в run/loader.conf", false)
		}
		m.input = nil
	case "backspace":
		r := []rune(m.input.buffer)
		if len(r) > 0 {
			m.input.buffer = string(r[:len(r)-1])
		}
		m.input.invalid = ""
	default:
		text := ""
		if msg.Type == tea.KeyRunes {
			text = string(msg.Runes)
		} else if msg.Type == tea.KeySpace || msg.String() == " " {
			text = " "
		}
		if text != "" {
			if len([]rune(m.input.buffer))+len([]rune(text)) <= 24 {
				m.input.buffer += text
			}
			m.input.invalid = ""
		}
	}
	return m, nil
}

func (m *model) flash(text string, isErr bool) {
	m.status = text
	m.statusErr = isErr
	m.statusTicks = 20
}

func (m model) View() string {
	w, h := m.width, m.height
	if w <= 0 {
		w = 80
	}
	if h <= 0 {
		h = 24
	}

	panel := renderPanelFromLogo(m.logo, false, m.flow)
	stats := settingsLine(m.settings)

	footer1 := ""
	if m.status != "" {
		mark := styleGreen.Render("✔")
		if m.statusErr {
			mark = styleRed.Render("✘")
		}
		footer1 = "   " + mark + " " + m.status
	}

	content := strings.Join([]string{
		panel,
		"",
		stats,
		"",
		m.renderMenu(),
		"",
		footer1,
		m.renderFooter(),
	}, "\n")

	return lipgloss.Place(w, h, lipgloss.Center, lipgloss.Center, content)
}

func (m model) renderMenu() string {
	labelW := 0
	for _, it := range menuItems {
		if lw := lipgloss.Width(it.label); lw > labelW {
			labelW = lw
		}
	}

	var rows []string
	for i, it := range menuItems {
		selected := i == m.cursor
		keyColor := gradWave(float64(i)*0.12, m.phase)

		pointer := "  "
		if selected {
			pointer = lipgloss.NewStyle().Bold(true).Foreground(keyColor).Render("❯ ")
		}

		key := "[" + it.key + "]"
		var keyRendered string
		if selected {
			keyRendered = lipgloss.NewStyle().Bold(true).
				Background(keyColor).Foreground(lipgloss.Color(colorInk)).Render(key)
		} else {
			keyRendered = lipgloss.NewStyle().Foreground(keyColor).Render(key)
		}

		label := padRight(it.label, labelW)
		var labelRendered string
		if selected {
			labelRendered = styleBold.Render(label)
		} else {
			labelRendered = styleGray.Render(label)
		}

		row := pointer + keyRendered + " " + labelRendered
		if it.hint != "" {
			row += "   " + styleDim.Render(it.hint)
		}
		rows = append(rows, row)
	}

	// lipgloss.Place центрирует каждую строку отдельно — доводим строки меню
	// до общей ширины, чтобы ключи [1]…[0] стояли в одной колонке.
	blockW := 0
	for _, r := range rows {
		if w := lipgloss.Width(r); w > blockW {
			blockW = w
		}
	}
	for i, r := range rows {
		rows[i] = padRight(r, blockW)
	}
	return strings.Join(rows, "\n")
}

func (m model) renderFooter() string {
	if m.input != nil {
		line := "   " + styleCyan.Render("?") + " " + styleBold.Render(m.input.title)
		line += " " + styleDim.Render("["+m.input.hint+"]") + " "
		line += styleBold.Render(m.input.buffer)
		line += lipgloss.NewStyle().Foreground(gradWave(0.5, m.phase)).Render("▌")
		if m.input.invalid != "" {
			line += "   " + styleRed.Render("✘ "+m.input.invalid)
		} else {
			line += styleDim.Render("   Enter сохранить · esc отмена")
		}
		return line
	}

	accent := func(t string) string {
		return lipgloss.NewStyle().Foreground(gradWave(0.3, m.phase)).Render(t)
	}
	return "   " + accent("↑↓") + styleDim.Render(" выбор") +
		styleDim.Render("   ") + accent("Enter") + styleDim.Render(" подтвердить") +
		styleDim.Render("   ") + accent("q") + styleDim.Render(" выход")
}

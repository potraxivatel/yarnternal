package main

import (
	"bufio"
	"fmt"
	"os"

	tea "github.com/charmbracelet/bubbletea"
)

func main() {
	os.Exit(run(os.Args[1:]))
}

func run(argv []string) int {
	var (
		username, ram, lang      string
		hasUser, hasRam, hasLang bool
		mode                     = "menu"
	)

	root, _ := findRoot() // нужен для usage, даже если корня нет

	i := 0
	for i < len(argv) {
		arg := argv[i]
		switch arg {
		case "-u", "--username":
			if i+1 < len(argv) {
				username = argv[i+1]
			}
			hasUser = true
			i += 2
		case "-r", "--ram":
			if i+1 < len(argv) {
				ram = argv[i+1]
			}
			hasRam = true
			i += 2
		case "-l", "--lang":
			if i+1 < len(argv) {
				lang = argv[i+1]
			}
			hasLang = true
			i += 2
		case "--run":
			mode = "run"
			i++
		case "--prism":
			mode = "prism"
			i++
		case "--dry", "--check":
			mode = "dry"
			i++
		case "-h", "--help":
			mode = "help"
			i++
		default:
			errMsg("неизвестный флаг: " + arg)
			fmt.Println()
			usage(root)
			return 1
		}
	}

	if mode == "help" {
		usage(root)
		return 0
	}

	if root == "" {
		errMsg("не нашёл корень проекта — запусти из папки yarnternal (рядом с gradlew)")
		return 1
	}

	s := loadSettings(root)
	if hasUser {
		s.Username = username
	}
	if hasRam {
		s.Ram = ram
	}
	if hasLang {
		s.Lang = lang
	}

	switch mode {
	case "dry":
		return runDry(root, s)
	case "run":
		if !validateOrPrint(s) {
			return 1
		}
		runRunClient(root, s)
		return 0
	case "prism":
		if !validateOrPrint(s) {
			return 1
		}
		runPrism(root, s)
		return 0
	}
	return menuLoop(root)
}

func validateOrPrint(s settings) bool {
	problems := s.validate()
	if len(problems) == 0 {
		return true
	}
	for _, p := range problems {
		errMsg(p)
	}
	return false
}

func runDry(root string, s settings) int {
	if !validateOrPrint(s) {
		return 1
	}
	clearScreen()
	printCenteredBlock(renderPanelFromLogo(loadLogo(root, terminalWidth()), true, 0))
	fmt.Println()
	fmt.Println("   " + styleCyan.Render("▸ проверка без запуска"))
	fmt.Println()
	fmt.Println("   " + settingsLine(s))
	fmt.Println()
	_ = patchLang(optionsFile(root), s.Lang)
	infoMsg("options.txt: run/options.txt -> lang:" + s.Lang)
	infoMsg(fmt.Sprintf("команда: ./gradlew runClient -PmcUsername=%s -PmcRam=%s", s.Username, s.Ram))
	fmt.Println()
	okMsg("всё ок, запуск не выполнялся")
	return 0
}

func menuLoop(root string) int {
	for {
		p := tea.NewProgram(newModel(root), tea.WithAltScreen())
		fin, err := p.Run()
		if err != nil {
			errMsg("TUI не смог запуститься: " + err.Error())
			return 1
		}
		fm, ok := fin.(model)
		if !ok {
			return 0
		}
		switch fm.action {
		case actionRun:
			_ = fm.settings.save(root)
			runRunClient(root, fm.settings)
			pause()
		case actionPrism:
			_ = fm.settings.save(root)
			runPrism(root, fm.settings)
			pause()
		default:
			return 0
		}
	}
}

func pause() {
	fmt.Println()
	fmt.Println(styleDim.Render("   Enter — назад в меню..."))
	_, _ = bufio.NewReader(os.Stdin).ReadString('\n')
}

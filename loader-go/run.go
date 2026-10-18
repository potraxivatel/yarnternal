package main

import (
	"fmt"
	"io"
	"os"
	"os/exec"
	"path/filepath"
	"sort"
	"strings"
	"syscall"
)

// ── вывод (вне TUI) ─────────────────────────────────────────────────────────
func infoMsg(text string) { fmt.Println(styleGray.Render("   " + text)) }
func okMsg(text string)   { fmt.Println("   " + styleGreen.Render("✔") + " " + text) }
func warnMsg(text string) { fmt.Println("   " + styleYellow.Render("!") + " " + text) }
func errMsg(text string)  { fmt.Println("   " + styleRed.Render("✘") + " " + text) }

// ── запуск: dev через gradlew runClient ─────────────────────────────────────
func runRunClient(root string, s settings) {
	if !setupJava() {
		return
	}
	_ = patchLang(optionsFile(root), s.Lang)
	infoMsg("язык игры: " + s.Lang)
	infoMsg(fmt.Sprintf("./gradlew runClient -PmcUsername=%s -PmcRam=%s", s.Username, s.Ram))
	if !dirExists(assetsDir()) {
		warnMsg("первый запуск: Gradle скачает ассеты игры (~500 МБ), это надолго")
	}
	fmt.Println()
	fmt.Println("   " + styleGreen.Render("▸ ЗАПУСКАЮ YARNTERNAL..."))
	fmt.Println()

	cmd := exec.Command("./gradlew", "runClient",
		"-PmcUsername="+s.Username, "-PmcRam="+s.Ram, "--console=plain")
	cmd.Dir = root
	cmd.Stdin, cmd.Stdout, cmd.Stderr = os.Stdin, os.Stdout, os.Stderr
	err := cmd.Run()
	fmt.Println()
	if ee, ok := err.(*exec.ExitError); ok && ee.ExitCode() < 0 {
		warnMsg("прервано")
		return
	}
	if err != nil {
		errMsg("не смог запустить gradlew: " + err.Error())
		return
	}
	okMsg("игра закрыта")
}

// ── запуск: сборка jar + PrismLauncher (реальный аккаунт) ───────────────────
func runPrism(root string, s settings) {
	if !setupJava() {
		return
	}

	fmt.Println("   " + styleGreen.Render("▸ собираю мод..."))
	build := exec.Command("./gradlew", "build", "--console=plain", "-q")
	build.Dir = root
	build.Stdin, build.Stdout, build.Stderr = os.Stdin, os.Stdout, os.Stderr
	if err := build.Run(); err != nil {
		errMsg("сборка упала — смотри вывод gradle выше")
		return
	}

	matches, _ := filepath.Glob(filepath.Join(root, "build", "libs", "yarnternal-*.jar"))
	sort.Strings(matches)
	jar := ""
	for _, m := range matches {
		name := filepath.Base(m)
		if strings.Contains(name, "sources") || strings.Contains(name, "dev") {
			continue
		}
		jar = m
		break
	}
	if jar == "" {
		errMsg("jar не найден в build/libs")
		return
	}

	modsDir := filepath.Join(prismDir(), "minecraft", "mods")
	if err := os.MkdirAll(modsDir, 0o755); err != nil {
		errMsg("не смог создать " + modsDir + ": " + err.Error())
		return
	}
	if old, err := filepath.Glob(filepath.Join(modsDir, "yarnternal-*.jar")); err == nil {
		for _, o := range old {
			_ = os.Remove(o)
		}
	}
	if err := copyFile(jar, filepath.Join(modsDir, filepath.Base(jar))); err != nil {
		errMsg("не смог скопировать мод: " + err.Error())
		return
	}
	if rel, err := filepath.Rel(root, jar); err == nil {
		okMsg("мод установлен: " + rel)
	} else {
		okMsg("мод установлен: " + jar)
	}

	if dirExists(filepath.Join(prismDir(), "minecraft")) {
		_ = patchLang(filepath.Join(prismDir(), "minecraft", "options.txt"), s.Lang)
	}

	prism := ""
	if st, err := os.Stat(prismApp()); err == nil && !st.IsDir() && st.Mode()&0o111 != 0 {
		prism = prismApp()
	} else if p, err := exec.LookPath("prismlauncher"); err == nil {
		prism = p
	}
	if prism == "" {
		errMsg("PrismLauncher не найден — мод уже установлен, запусти игру сам")
		return
	}

	fmt.Println("   " + styleGreen.Render(fmt.Sprintf("▸ запускаю Prism (инстанс %s)...", prismInstance)))
	cmd := exec.Command(prism, "--launch", prismInstance)
	if devNull, err := os.OpenFile(os.DevNull, os.O_WRONLY, 0); err == nil {
		defer devNull.Close()
		cmd.Stdout, cmd.Stderr = devNull, devNull
	}
	cmd.SysProcAttr = &syscall.SysProcAttr{Setsid: true}
	if err := cmd.Start(); err != nil {
		errMsg("не смог запустить Prism: " + err.Error())
		return
	}
	go func() { _ = cmd.Wait() }()
}

func copyFile(src, dst string) error {
	in, err := os.Open(src)
	if err != nil {
		return err
	}
	defer in.Close()
	out, err := os.Create(dst)
	if err != nil {
		return err
	}
	if _, err := io.Copy(out, in); err != nil {
		out.Close()
		return err
	}
	return out.Close()
}

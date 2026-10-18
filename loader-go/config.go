package main

import (
	"bytes"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"regexp"
	"sort"
	"strconv"
	"strings"
)

const (
	version       = "0.1.0"
	prismInstance = "1.21.11"
)

var (
	usernameRe    = regexp.MustCompile(`^[A-Za-z0-9_]{3,16}$`)
	ramRe         = regexp.MustCompile(`^[0-9]+[GgMm]$`)
	langRe        = regexp.MustCompile(`^[a-z]{2}_[a-z]{2}$`)
	javaVersionRe = regexp.MustCompile(`version "(\d+)(?:\.(\d+))?`)
)

type settings struct {
	Username string
	Ram      string
	Lang     string
}

func defaultSettings() settings {
	return settings{Username: "Foks", Ram: "4G", Lang: "ru_ru"}
}

// ── пути ────────────────────────────────────────────────────────────────────

// findRoot — корень проекта: вверх по дереву от cwd (и от самого бинарника)
// до папки, где лежат gradlew и build.gradle.
func findRoot() (string, bool) {
	if cwd, err := os.Getwd(); err == nil {
		if root, ok := walkUpForGradle(cwd); ok {
			return root, true
		}
	}
	if exe, err := os.Executable(); err == nil {
		if root, ok := walkUpForGradle(filepath.Dir(exe)); ok {
			return root, true
		}
	}
	return "", false
}

func walkUpForGradle(dir string) (string, bool) {
	for {
		if fileExists(filepath.Join(dir, "gradlew")) && fileExists(filepath.Join(dir, "build.gradle")) {
			return dir, true
		}
		parent := filepath.Dir(dir)
		if parent == dir {
			return "", false
		}
		dir = parent
	}
}

func fileExists(path string) bool {
	st, err := os.Stat(path)
	return err == nil && !st.IsDir()
}

func dirExists(path string) bool {
	st, err := os.Stat(path)
	return err == nil && st.IsDir()
}

func configFile(root string) string  { return filepath.Join(root, "run", "loader.conf") }
func optionsFile(root string) string { return filepath.Join(root, "run", "options.txt") }
func logoFile(root string) string    { return filepath.Join(root, "logo.txt") }

func assetsDir() string {
	gradleHome := os.Getenv("GRADLE_USER_HOME")
	if gradleHome == "" {
		home, _ := os.UserHomeDir()
		gradleHome = filepath.Join(home, ".gradle")
	}
	return filepath.Join(gradleHome, "caches", "fabric-loom", "assets")
}

func prismDir() string {
	home, _ := os.UserHomeDir()
	return filepath.Join(home, ".local", "share", "PrismLauncher", "instances", prismInstance)
}

func prismApp() string {
	home, _ := os.UserHomeDir()
	return filepath.Join(home, "Загрузки", "PrismLauncher-Linux-x86_64.AppImage")
}

// ── настройки ───────────────────────────────────────────────────────────────
// Формат run/loader.conf — тот же, что у питоновского лоадера:
//   username=Foks
//   ram=4G
//   lang=ru_ru

func loadSettings(root string) settings {
	s := defaultSettings()
	data, err := os.ReadFile(configFile(root))
	if err != nil {
		return s
	}
	for _, line := range strings.Split(strings.ReplaceAll(string(data), "\r\n", "\n"), "\n") {
		key, value, _ := strings.Cut(line, "=")
		switch key {
		case "username":
			s.Username = value
		case "ram":
			s.Ram = value
		case "lang":
			s.Lang = value
		}
	}
	return s
}

func (s settings) save(root string) error {
	if err := os.MkdirAll(filepath.Dir(configFile(root)), 0o755); err != nil {
		return err
	}
	conf := fmt.Sprintf("username=%s\nram=%s\nlang=%s\n", s.Username, s.Ram, s.Lang)
	return os.WriteFile(configFile(root), []byte(conf), 0o644)
}

// validate — список проблем; пусто = всё ок.
func (s settings) validate() []string {
	var problems []string
	if !usernameRe.MatchString(s.Username) {
		problems = append(problems, fmt.Sprintf("ник «%s»: 3-16 символов, A-Z a-z 0-9 _", s.Username))
	}
	if !ramRe.MatchString(s.Ram) {
		problems = append(problems, fmt.Sprintf("озу «%s»: формат 4G или 2048M", s.Ram))
	}
	if !langRe.MatchString(s.Lang) {
		problems = append(problems, fmt.Sprintf("язык «%s»: формат xx_yy (ru_ru)", s.Lang))
	}
	return problems
}

// ── язык игры ───────────────────────────────────────────────────────────────
// У Minecraft нет аргумента командной строки для языка — он лежит в options.txt.
// Аккуратно правим одну строку: убираем старую lang: и дописываем новую.
func patchLang(path, lang string) error {
	if err := os.MkdirAll(filepath.Dir(path), 0o755); err != nil {
		return err
	}
	var kept []string
	if data, err := os.ReadFile(path); err == nil {
		lines := strings.Split(strings.ReplaceAll(string(data), "\r\n", "\n"), "\n")
		if len(lines) > 0 && lines[len(lines)-1] == "" {
			lines = lines[:len(lines)-1]
		}
		for _, line := range lines {
			if !strings.HasPrefix(line, "lang:") {
				kept = append(kept, line)
			}
		}
	}
	kept = append(kept, "lang:"+lang)
	return os.WriteFile(path, []byte(strings.Join(kept, "\n")+"\n"), 0o644)
}

// ── java ────────────────────────────────────────────────────────────────────

// javaProbe — (мажорная версия, первая строка java -version) или ok=false.
func javaProbe(java string) (major int, firstLine string, ok bool) {
	cmd := exec.Command(java, "-version")
	var buf bytes.Buffer
	cmd.Stdout = &buf
	cmd.Stderr = &buf
	if err := cmd.Run(); err != nil && buf.Len() == 0 {
		return 0, "", false
	}
	out := strings.TrimRight(buf.String(), "\r\n")
	if out == "" {
		return 0, "", false
	}
	firstLine = strings.Split(out, "\n")[0]
	m := javaVersionRe.FindStringSubmatch(firstLine)
	if m == nil {
		return 0, "", false
	}
	major, _ = strconv.Atoi(m[1])
	if major == 1 && m[2] != "" {
		major, _ = strconv.Atoi(m[2])
	}
	return major, firstLine, true
}

// findJava — сначала ~/jdk/jdk-*, потом системная java; нужна 21+.
func findJava() (dir, firstLine string, ok bool) {
	home, _ := os.UserHomeDir()
	var candidates []string
	if matches, err := filepath.Glob(filepath.Join(home, "jdk", "jdk-*")); err == nil {
		sort.Strings(matches)
		for _, d := range matches {
			if fileExists(filepath.Join(d, "bin", "java")) {
				candidates = append(candidates, d)
			}
		}
	}
	if p, err := exec.LookPath("java"); err == nil {
		if resolved, err := filepath.EvalSymlinks(p); err == nil {
			p = resolved
		}
		candidates = append(candidates, filepath.Dir(filepath.Dir(p)))
	}
	for _, d := range candidates {
		if major, line, ok := javaProbe(filepath.Join(d, "bin", "java")); ok && major >= 21 {
			return d, line, true
		}
	}
	return "", "", false
}

func setupJava() bool {
	dir, line, ok := findJava()
	if !ok {
		errMsg("не нашёл Java 21+ — поставь JDK и повтори")
		return false
	}
	os.Setenv("JAVA_HOME", dir)
	infoMsg("java: " + line)
	return true
}

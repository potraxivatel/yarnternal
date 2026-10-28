package yarnternal.lol.api.utility.sound;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Утилита воспроизведения звуков через нативный Java Sound API.
 * Работает асинхронно в виртуальных потоках, кэширует байты аудио
 * и не вызывает задержек и лагов в игровом цикле.
 */
public final class SoundUtil {

    private static final ExecutorService SOUND_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
    private static final Map<String, byte[]> AUDIO_CACHE = new ConcurrentHashMap<>();

    /** Номер текущего пресета переключения (1 .. 7). */
    public static int currentPreset = 1;

    /** Громкость по умолчанию (0.0 .. 1.0). */
    public static float defaultVolume = 0.8f;

    /** Включены ли звуки клиента в целом. */
    public static boolean enabled = true;

    private SoundUtil() {
    }

    /**
     * Воспроизводит звук переключения модуля (включение / выключение).
     *
     * @param state {@code true} — включение, {@code false} — выключение
     */
    public static void playToggle(boolean state) {
        if (!enabled) return;

        int preset = Math.clamp(currentPreset, 1, 7);
        String name = state
                ? "module_enable_" + preset + ".wav"
                : "module_disable_" + preset + ".wav";

        playSound("/assets/yarnternal/sounds/" + name, defaultVolume);
    }

    /**
     * Воспроизводит звук из ресурсов по указанному пути.
     *
     * @param resourcePath путь к .wav файлу в ресурсах (например, "/assets/yarnternal/sounds/module_enable_1.wav")
     * @param volume       громкость от 0.0f до 1.0f
     */
    public static void playSound(String resourcePath, float volume) {
        if (!enabled || volume <= 0.001f) return;

        SOUND_EXECUTOR.submit(() -> {
            try {
                byte[] audioData = AUDIO_CACHE.computeIfAbsent(resourcePath, path -> {
                    try (InputStream is = SoundUtil.class.getResourceAsStream(path)) {
                        if (is == null) {
                            System.err.println("[SoundUtil] Звук не найден в ресурсах: " + path);
                            return null;
                        }
                        return is.readAllBytes();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                });

                if (audioData == null || audioData.length == 0) {
                    return;
                }

                try (AudioInputStream ais = AudioSystem.getAudioInputStream(new ByteArrayInputStream(audioData))) {
                    Clip clip = AudioSystem.getClip();
                    clip.open(ais);

                    // Регулировка громкости через Master Gain (dB)
                    if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                        float dB = volume <= 0.0001f
                                ? -80.0f
                                : (float) (20.0 * Math.log10(volume));
                        dB = Math.clamp(dB, gain.getMinimum(), gain.getMaximum());
                        gain.setValue(dB);
                    }

                    // Закрываем клип и освобождаем аудиолинию после завершения проигрывания
                    clip.addLineListener(event -> {
                        if (event.getType() == LineEvent.Type.STOP) {
                            clip.close();
                        }
                    });

                    clip.start();
                }
            } catch (Throwable t) {
                // Защита от падений, если занято аудиоустройство или проблемы с драйвером
                System.err.println("[SoundUtil] Ошибка воспроизведения звука: " + t.getMessage());
            }
        });
    }
}

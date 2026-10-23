package yarnternal.lol.core.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Профиль пользователя из {@code data.json}.
 *
 * <p>Файл и аватар лежат в этом же пакете и попадают в jar как ресурсы.
 * Если JSON нет или он битый — используются безопасные значения по умолчанию.
 */
public final class ProfileData {

    private static final String ROOT = "/yarnternal/lol/core/data/";
    private static final String DATA_FILE = ROOT + "data.json";

    private static ProfileData cached;

    private final String nickname;
    private final String uid;
    private final String avatar;

    private ProfileData(String nickname, String uid, String avatar) {
        this.nickname = nickname;
        this.uid = uid;
        this.avatar = avatar;
    }

    public static ProfileData get() {
        ProfileData data = cached;
        if (data == null) {
            synchronized (ProfileData.class) {
                data = cached;
                if (data == null) {
                    data = load();
                    cached = data;
                }
            }
        }
        return data;
    }

    public static void reload() {
        synchronized (ProfileData.class) {
            cached = null;
        }
    }

    public String nickname() {
        return nickname;
    }

    public String uid() {
        return uid;
    }

    public String avatar() {
        return avatar;
    }

    /** Поток с аватаркой или {@code null}, если файла нет. */
    public InputStream openAvatar() {
        if (avatar == null || avatar.isBlank()) {
            return null;
        }

        String file = avatar.trim().replace('\\', '/');
        while (file.startsWith("/")) {
            file = file.substring(1);
        }
        if (file.contains("..")) {
            return null;
        }
        return ProfileData.class.getResourceAsStream(ROOT + file);
    }

    private static ProfileData load() {
        try (InputStream stream = ProfileData.class.getResourceAsStream(DATA_FILE)) {
            if (stream == null) {
                return fallback();
            }

            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            String nickname = string(root, "nickname", "Yarnternal");
            String uid = string(root, "uid", "LOCAL");
            String avatar = string(root, "avatar", "avatar.jpg");
            return new ProfileData(nickname, uid, avatar);
        } catch (Exception e) {
            System.err.println("[yarnternal] profile data: " + e.getMessage());
            return fallback();
        }
    }

    private static String string(JsonObject root, String key, String fallback) {
        if (root == null || !root.has(key) || root.get(key).isJsonNull()) {
            return fallback;
        }
        String value = root.get(key).getAsString();
        return value == null || value.isBlank() ? fallback : value;
    }

    private static ProfileData fallback() {
        return new ProfileData("Yarnternal", "LOCAL", "avatar.jpg");
    }
}

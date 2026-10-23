package yarnternal.lol.common.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import yarnternal.lol.api.utility.render.color.ColorUtil;
import yarnternal.lol.render.utils.color.Color;

/**
 * Настройка цвета: ARGB/RGBA цвет, поддержка прозрачности и режима радуги.
 */
public final class ColorSetting extends Setting<Integer> {

    private boolean rainbow;
    private float rainbowSpeed = 1.0f;
    private final boolean allowAlpha;

    public ColorSetting(String name, int defaultColor) {
        this(name, defaultColor, true);
    }

    public ColorSetting(String name, int defaultColor, boolean allowAlpha) {
        super(name, defaultColor);
        this.allowAlpha = allowAlpha;
    }

    public int getRed() {
        return ColorUtil.getRed(get());
    }

    public int getGreen() {
        return ColorUtil.getGreen(get());
    }

    public int getBlue() {
        return ColorUtil.getBlue(get());
    }

    public int getAlpha() {
        return allowAlpha ? ColorUtil.getAlpha(get()) : 255;
    }

    /** Текущий цвет (учитывает режим радуги, если включен). */
    public int get() {
        if (rainbow) {
            float time = (System.currentTimeMillis() % 10_000L) / 10_000.0f * rainbowSpeed;
            int rainbowColor = ColorUtil.rainbow(time, 0.8f, 1.0f);
            return allowAlpha ? ColorUtil.withAlpha(rainbowColor, ColorUtil.getAlpha(value)) : rainbowColor;
        }
        return value;
    }

    /** Шейдерный цвет Color для Renderer2D. */
    public Color getRenderColor() {
        int c = get();
        return new Color(ColorUtil.getRed(c), ColorUtil.getGreen(c), ColorUtil.getBlue(c), ColorUtil.getAlpha(c));
    }

    public boolean isRainbow() {
        return rainbow;
    }

    public void setRainbow(boolean rainbow) {
        this.rainbow = rainbow;
    }

    public float getRainbowSpeed() {
        return rainbowSpeed;
    }

    public void setRainbowSpeed(float rainbowSpeed) {
        this.rainbowSpeed = rainbowSpeed;
    }

    public boolean isAllowAlpha() {
        return allowAlpha;
    }

    public String toHex() {
        return ColorUtil.toHex(get());
    }

    @Override
    public String displayValue() {
        return rainbow ? "Rainbow" : toHex();
    }

    @Override
    public JsonElement toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("color", value);
        obj.addProperty("hex", ColorUtil.toHex(value));
        obj.addProperty("rainbow", rainbow);
        obj.addProperty("rainbowSpeed", rainbowSpeed);
        return obj;
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json == null) {
            return;
        }
        if (json.isJsonPrimitive()) {
            try {
                if (json.getAsJsonPrimitive().isNumber()) {
                    setValue(json.getAsInt());
                } else {
                    setValue(ColorUtil.fromHex(json.getAsString()));
                }
            } catch (RuntimeException ignored) {
            }
            return;
        }

        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            if (obj.has("color")) {
                setValue(obj.get("color").getAsInt());
            } else if (obj.has("hex")) {
                setValue(ColorUtil.fromHex(obj.get("hex").getAsString()));
            }
            if (obj.has("rainbow")) {
                this.rainbow = obj.get("rainbow").getAsBoolean();
            }
            if (obj.has("rainbowSpeed")) {
                this.rainbowSpeed = obj.get("rainbowSpeed").getAsFloat();
            }
        }
    }
}

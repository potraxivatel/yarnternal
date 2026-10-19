package yarnternal.lol.render.renderer;

import yarnternal.lol.render.RenderLog;
import yarnternal.lol.render.renderer.text.CustomTextRenderer;
import yarnternal.lol.render.renderer.text.FontFace;
import yarnternal.lol.render.renderer.text.FontFamily;
import yarnternal.lol.render.renderer.text.FontInfo;
import yarnternal.lol.render.utils.render.FontUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class Fonts {

    private static final String[] BUILTIN_FONTS = {
            "Comfortaa"
    };

    private static final List<FontFamily> FONT_FAMILIES = new ArrayList<>();

    public static CustomTextRenderer RENDERER;
    public static FontFace FONT;

    private Fonts() {
    }

    public static void init() {
        FONT_FAMILIES.clear();

        for (String name : BUILTIN_FONTS) {
            FontUtils.loadBuiltin(FONT_FAMILIES, name);
        }

        FONT_FAMILIES.sort(Comparator.comparing(FontFamily::getName));

        if (FONT_FAMILIES.isEmpty()) {
            throw new IllegalStateException("No font families loaded.");
        }

        FontFamily family = FONT_FAMILIES.get(0);
        FONT = family.get(FontInfo.Type.Regular);

        if (FONT == null) {
            throw new IllegalStateException(
                    "Regular font not found in family: " + family.getName()
            );
        }

        RENDERER = new CustomTextRenderer(FONT);

        RenderLog.LOG.info(
                "Loaded font: {} ({})",
                family.getName(),
                FontInfo.Type.Regular
        );
    }

    public static CustomTextRenderer get() {
        return RENDERER;
    }

    public static FontFace getFont() {
        return FONT;
    }

    public static FontFamily getFamily(String name) {
        for (FontFamily family : FONT_FAMILIES) {
            if (family.getName().equalsIgnoreCase(name)) {
                return family;
            }
        }
        return null;
    }
}

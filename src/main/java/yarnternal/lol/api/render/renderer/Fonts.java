package yarnternal.lol.render.renderer;

import yarnternal.lol.render.RenderLog;
import yarnternal.lol.render.renderer.text.BuiltinFontFace;
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

    /** Montserrat для GUI: от лёгкого до жирного. */
    public static FontFace GUI_LIGHT;
    public static FontFace GUI_REGULAR;
    public static FontFace GUI_MEDIUM;
    public static FontFace GUI_SEMIBOLD;
    public static FontFace GUI_BOLD;

    /** Sora — заголовки категорий. */
    public static FontFace GUI_CATEGORY;

    /** Gilroy — логотип и профиль. */
    public static FontFace GUI_BRAND;
    public static FontFace GUI_BRAND_BOLD;

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

        GUI_LIGHT = loadGuiFace("Montserrat-Light");
        GUI_REGULAR = loadGuiFace("Montserrat-Regular");
        GUI_MEDIUM = loadGuiFace("Montserrat-Medium");
        GUI_SEMIBOLD = loadGuiFace("Montserrat-SemiBold");
        GUI_BOLD = loadGuiFace("Montserrat-Bold");

        GUI_CATEGORY = loadGuiFace("Sora");
        GUI_BRAND = loadGuiFace("Gilroy");
        GUI_BRAND_BOLD = loadGuiFace("Gilroy-Bold");

        RenderLog.LOG.info(
                "Loaded font: {} ({})",
                family.getName(),
                FontInfo.Type.Regular
        );
        RenderLog.LOG.info(
                "GUI fonts: regular={} | semibold={} | category={} | brand={}",
                GUI_REGULAR, GUI_SEMIBOLD, GUI_CATEGORY, GUI_BRAND_BOLD
        );
    }

    private static FontFace loadGuiFace(String name) {
        FontInfo info = FontUtils.getBuiltinFontInfo(name);
        if (info == null) {
            RenderLog.LOG.warn("Failed to load GUI font {}, falling back to {}", name, FONT);
            return FONT;
        }
        return new BuiltinFontFace(info, name);
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

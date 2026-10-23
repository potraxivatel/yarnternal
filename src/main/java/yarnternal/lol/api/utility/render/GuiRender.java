package yarnternal.lol.api.utility.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import yarnternal.lol.render.renderer.Fonts;
import yarnternal.lol.render.renderer.Renderer2D;
import yarnternal.lol.render.renderer.text.FontFace;
import yarnternal.lol.render.utils.HudRenderer;
import yarnternal.lol.render.utils.Utils;
import yarnternal.lol.render.utils.color.Color;

/**
 * Рисование GUI поверх кастомного рендер-стека.
 *
 * <p>Все координаты и размеры — в обычных GUI-пикселях (как у
 * {@link DrawContext}), а внутрь рендера они переводятся в пиксели
 * фреймбуфера умножением на GUI scale. Геометрия идёт через SDF-шейдер
 * (настоящие скругления и градиенты), текст — через шрифт клиента.
 *
 * <p>Использование:
 * <pre>{@code
 * GuiRender.begin(context);
 * GuiRender.rect(10, 10, 100, 20, 0xFF112233, 4);
 * GuiRender.text("hi", 14, 15, 0xFFFFFFFF);
 * GuiRender.end();
 * }</pre>
 */
public final class GuiRender {

    /** Размер шрифта по умолчанию: 9 GUI-пикселей — как у ванильного. */
    public static final double FONT_SIZE = 0.5;
    public static final int FONT_H = 9;

    /** Насыщенность шрифта GUI. */
    public enum Weight {
        LIGHT, REGULAR, MEDIUM, SEMIBOLD, BOLD,
        /** Sora — заголовки категорий. */
        CATEGORY,
        /** Gilroy — логотип и профиль. */
        BRAND, BRAND_BOLD
    }

    private static DrawContext context;
    private static double scale = 1;
    private static float alpha = 1f;

    private GuiRender() {}

    /** Размер шрифта из высоты в GUI-пикселях. */
    public static double size(double pixels) {
        return pixels / 18.0;
    }

    /** Глобальная прозрачность кадра: 0 — невидимо, 1 — как обычно. */
    public static void setAlpha(float value) {
        alpha = Math.max(0f, Math.min(1f, value));
    }

    public static void begin(DrawContext drawContext) {
        context = drawContext;
        alpha = 1f;
        scale = MinecraftClient.getInstance().getWindow().getScaleFactor();

        HudRenderer.INSTANCE.resetScissor();
        drawContext.createNewRootLayer();

        // Свой кадр рисуем с чистой model-view матрицей: экран не должен
        // зависеть от того, что оставил предыдущий проход рендера.
        RenderSystem.getModelViewStack().pushMatrix().identity();

        Utils.guiProjection();
        HudRenderer.INSTANCE.begin(drawContext);
    }

    public static void end() {
        HudRenderer.INSTANCE.end();
        HudRenderer.INSTANCE.resetScissor();
        RenderSystem.getModelViewStack().popMatrix();
        Utils.endGuiProjection();
        context = null;
    }

    public static DrawContext context() {
        return context;
    }

    public static double scale() {
        return scale;
    }

    /** Сбрасывает накопленный батч (нужно перед сменой ножниц). */
    public static void flush() {
        HudRenderer.INSTANCE.flush();
    }

    // ── Ножницы ──────────────────────────────────────────────────────────────

    /** Ограничивает отрисовку прямоугольником в GUI-координатах. */
    public static void scissor(double x, double y, double width, double height) {
        flush();

        int x1 = (int) Math.floor(x * scale);
        int y1 = (int) Math.floor(y * scale);
        int x2 = (int) Math.ceil((x + width) * scale);
        int y2 = (int) Math.ceil((y + height) * scale);

        HudRenderer.INSTANCE.scissor(x1, y1, x2 - x1, y2 - y1);
    }

    public static void resetScissor() {
        flush();
        HudRenderer.INSTANCE.resetScissor();
    }

    // ── Геометрия ────────────────────────────────────────────────────────────

    /** Текстура в GUI-координатах (аватарки и другие готовые изображения). */
    public static void texture(Identifier id, double x, double y,
                               double width, double height, int color) {
        // Текстура рисуется отдельным проходом, поэтому сначала отдаём
        // накопленный батч, иначе она окажется под уже нарисованным GUI.
        flush();
        HudRenderer.INSTANCE.texture(id, x * scale, y * scale,
                width * scale, height * scale, color(color));
    }

    public static void rect(double x, double y, double width, double height, int color) {
        rect(x, y, width, height, color, 0);
    }

    public static void rect(double x, double y, double width, double height, int color, double radius) {
        Renderer2D.COLOR.roundedQuad(
                x * scale, y * scale, width * scale, height * scale,
                color(color), radius * scale, 1.5
        );
    }

    /** Прямоугольник с разными радиусами углов: tl, tr, br, bl. */
    public static void rectCorners(double x, double y, double width, double height, int color,
                                   double tl, double tr, double br, double bl) {
        Color c = color(color);
        Renderer2D.COLOR.roundedQuad(
                x * scale, y * scale, width * scale, height * scale,
                c, c, c, c,
                tl * scale, tr * scale, br * scale, bl * scale,
                1.5
        );
    }

    /** Вертикальный градиент. */
    public static void rectV(double x, double y, double width, double height,
                             int top, int bottom, double radius) {
        Renderer2D.COLOR.roundedQuad(
                x * scale, y * scale, width * scale, height * scale,
                color(top), color(top), color(bottom), color(bottom),
                radius * scale, radius * scale, radius * scale, radius * scale, 1.5
        );
    }

    /** Вертикальный градиент с разными радиусами углов: tl, tr, br, bl. */
    public static void rectVCorners(double x, double y, double width, double height,
                                    int top, int bottom,
                                    double tl, double tr, double br, double bl) {
        Renderer2D.COLOR.roundedQuad(
                x * scale, y * scale, width * scale, height * scale,
                color(top), color(top), color(bottom), color(bottom),
                tl * scale, tr * scale, br * scale, bl * scale, 1.5
        );
    }

    /** Четыре цвета по углам и разные радиусы: tl, tr, br, bl. */
    public static void rect4Corners(double x, double y, double width, double height,
                                    int tlColor, int trColor, int brColor, int blColor,
                                    double tl, double tr, double br, double bl) {
        Renderer2D.COLOR.roundedQuad(
                x * scale, y * scale, width * scale, height * scale,
                color(tlColor), color(trColor), color(brColor), color(blColor),
                tl * scale, tr * scale, br * scale, bl * scale, 1.5
        );
    }

    /** Четыре цвета по углам: tl, tr, br, bl. */
    public static void rect4(double x, double y, double width, double height,
                             int tl, int tr, int br, int bl, double radius) {
        Renderer2D.COLOR.roundedQuad(
                x * scale, y * scale, width * scale, height * scale,
                color(tl), color(tr), color(br), color(bl),
                radius * scale, radius * scale, radius * scale, radius * scale, 1.5
        );
    }

    /** Заливка со скруглением и рамкой заданной толщины (внутри). */
    public static void panel(double x, double y, double width, double height,
                             double radius, int fill, int border, double borderWidth) {
        if (borderWidth > 0 && (border >>> 24) != 0) {
            rect(x, y, width, height, border, radius);
            rect(x + borderWidth, y + borderWidth,
                    width - borderWidth * 2, height - borderWidth * 2,
                    fill, Math.max(0, radius - borderWidth));
        } else {
            rect(x, y, width, height, fill, radius);
        }
    }

    /** Вертикальный градиент с рамкой. */
    public static void panelV(double x, double y, double width, double height,
                              double radius, int top, int bottom, int border, double borderWidth) {
        if (borderWidth > 0 && (border >>> 24) != 0) {
            rect(x, y, width, height, border, radius);
            rectV(x + borderWidth, y + borderWidth,
                    width - borderWidth * 2, height - borderWidth * 2,
                    top, bottom, Math.max(0, radius - borderWidth));
        } else {
            rectV(x, y, width, height, top, bottom, radius);
        }
    }

    public static void line(double x1, double y1, double x2, double y2, int color) {
        Renderer2D.COLOR.line(x1 * scale, y1 * scale, x2 * scale, y2 * scale, color(color));
    }

    /** Заливной треугольник — для маленьких указателей и галочек. */
    public static void triangle(double x1, double y1, double x2, double y2,
                                double x3, double y3, int color) {
        Renderer2D.COLOR.triangle(x1 * scale, y1 * scale,
                x2 * scale, y2 * scale, x3 * scale, y3 * scale, color(color));
    }

    /** Горизонтальная линия толщиной {@code thickness} GUI-пикселей. */
    public static void hline(double x, double y, double width, double thickness, int color) {
        rect(x, y, width, thickness, color);
    }

    // ── Текст ────────────────────────────────────────────────────────────────

    public static void text(String text, double x, double y, int color) {
        text(text, x, y, color, FONT_SIZE, false, Weight.REGULAR);
    }

    public static void text(String text, double x, double y, int color, double size) {
        text(text, x, y, color, size, false, Weight.REGULAR);
    }

    public static void text(String text, double x, double y, int color, double size, Weight weight) {
        text(text, x, y, color, size, false, weight);
    }

    public static void text(String text, double x, double y, int color, Weight weight) {
        text(text, x, y, color, FONT_SIZE, false, weight);
    }

    public static void textShadow(String text, double x, double y, int color, double size) {
        text(text, x, y, color, size, true, Weight.REGULAR);
    }

    public static void textShadow(String text, double x, double y, int color, double size, Weight weight) {
        text(text, x, y, color, size, true, weight);
    }

    public static void text(String text, double x, double y, int color, double size, boolean shadow) {
        text(text, x, y, color, size, shadow, Weight.REGULAR);
    }

    public static void text(String text, double x, double y, int color, double size, boolean shadow, Weight weight) {
        HudRenderer.INSTANCE.text(text, x * scale, y * scale, color(color), shadow, size * scale, face(weight));
    }

    public static double textWidth(String text) {
        return textWidth(text, FONT_SIZE, Weight.REGULAR);
    }

    public static double textWidth(String text, double size) {
        return textWidth(text, size, Weight.REGULAR);
    }

    public static double textWidth(String text, double size, Weight weight) {
        return HudRenderer.INSTANCE.textWidth(text, false, size * scale, face(weight)) / scale;
    }

    public static double textHeight() {
        return textHeight(FONT_SIZE, Weight.REGULAR);
    }

    public static double textHeight(double size) {
        return textHeight(size, Weight.REGULAR);
    }

    public static double textHeight(double size, Weight weight) {
        return HudRenderer.INSTANCE.textHeight(false, size * scale, face(weight)) / scale;
    }

    private static FontFace face(Weight weight) {
        FontFace face = switch (weight) {
            case LIGHT -> Fonts.GUI_LIGHT;
            case MEDIUM -> Fonts.GUI_MEDIUM;
            case SEMIBOLD -> Fonts.GUI_SEMIBOLD;
            case BOLD -> Fonts.GUI_BOLD;
            case CATEGORY -> Fonts.GUI_CATEGORY;
            case BRAND -> Fonts.GUI_BRAND;
            case BRAND_BOLD -> Fonts.GUI_BRAND_BOLD;
            case REGULAR -> Fonts.GUI_REGULAR;
        };
        return face != null ? face : Fonts.FONT;
    }

    // ── Блюр ─────────────────────────────────────────────────────────────────

    /**
     * Размывает то, что уже нарисовано в кадре, и рисует результат
     * скруглённым прямоугольником. Вызывать до отрисовки содержимого.
     */
    public static void blur(double x, double y, double width, double height, double radius) {
        blur(x, y, width, height, radius, 2);
    }

    public static void blur(double x, double y, double width, double height,
                            double radius, int strength) {
        Renderer2D.BLUR.update(strength);

        var window = MinecraftClient.getInstance().getWindow();
        double fbW = window.getFramebufferWidth();
        double fbH = window.getFramebufferHeight();

        double px = x * scale;
        double py = y * scale;
        double pw = width * scale;
        double ph = height * scale;

        float u1 = (float) (px / fbW);
        float v1 = (float) (1.0 - py / fbH);
        float u2 = (float) ((px + pw) / fbW);
        float v2 = (float) (1.0 - (py + ph) / fbH);

        Renderer2D.TEXTURE.begin();
        Renderer2D.TEXTURE.roundedTexQuad(
                px, py, pw, ph,
                0, u1, v1, u2, v2,
                Color.WHITE,
                radius * scale, radius * scale, radius * scale, radius * scale,
                1.5
        );
        Renderer2D.TEXTURE.render(
                Renderer2D.BLUR.getBlurredTexture(),
                RenderSystem.getSamplerCache().get(FilterMode.LINEAR)
        );
    }

    // ── Цвет ─────────────────────────────────────────────────────────────────

    public static Color color(int argb) {
        if (alpha >= 1f) {
            return new Color(argb);
        }
        int a = (int) (((argb >>> 24) & 0xFF) * alpha + 0.5f);
        return new Color((a << 24) | (argb & 0x00FFFFFF));
    }
}

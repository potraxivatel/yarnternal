package yarnternal.lol.common.ui.hud;

import net.minecraft.client.MinecraftClient;
import yarnternal.lol.api.event.EventTarget;
import yarnternal.lol.api.event.events.Render2DEvent;
import yarnternal.lol.api.utility.render.color.ColorUtil;
import yarnternal.lol.core.Yarnternal;
import yarnternal.lol.render.renderer.Renderer2D;
import yarnternal.lol.render.utils.HudRenderer;
import yarnternal.lol.render.utils.color.Color;

/**
 * Витрина рендера: панель, градиенты, палитра, прозрачность и скругления —
 * всё на шейдерном рендере (SDF, без «пиксельных» краёв) и своём шрифте.
 *
 * <p>Рисуется подпиской на {@link Render2DEvent}. Координаты рендера —
 * пиксели фреймбуфера, поэтому всю геометрию домножаем на GUI scale:
 * физический размер панели остаётся таким же, как у старой версии на DrawContext.
 */
public final class ColorTestHud {

    // ── палитра клиента ─────────────────────────────────────────────────────
    private static final int THEME_ONE = ColorUtil.rgb(122, 92, 255);   // #7A5CFF
    private static final int THEME_TWO = ColorUtil.rgb(255, 79, 216);   // #FF4FD8

    private static final Color TEXT = new Color(200, 200, 200, 255);
    private static final Color TEXT_DIM = new Color(150, 145, 165, 255);
    private static final Color PANEL_TOP = new Color(20, 18, 28, 205);
    private static final Color PANEL_BOTTOM = new Color(11, 10, 16, 220);
    private static final Color PANEL_SOLID = new Color(16, 15, 22, 235);
    private static final Color BORDER_WHITE = new Color(255, 255, 255, 110);
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    @EventTarget
    private void onRender2D(Render2DEvent event) {
        var window = MinecraftClient.getInstance().getWindow();
        double s = window.getScaleFactor();

        double x = 8 * s;
        double y = 8 * s;
        double w = 186 * s;
        double h = 142 * s;
        double phase = (System.currentTimeMillis() % 3000L) / 3000.0;

        // ── панель: рамка бегущей волной + заливка с градиентом ─────────────
        Renderer2D.COLOR.roundedQuad(x, y, w, h,
                wave(phase), wave(phase + 0.33), wave(phase + 0.66), wave(phase + 1.0),
                8 * s, 8 * s, 8 * s, 8 * s, 1.5);
        Renderer2D.COLOR.roundedQuad(x + 1.5 * s, y + 1.5 * s, w - 3 * s, h - 3 * s,
                PANEL_TOP, PANEL_TOP, PANEL_BOTTOM, PANEL_BOTTOM,
                6.5 * s, 6.5 * s, 6.5 * s, 6.5 * s, 1.5);

        double ix = x + 6 * s;
        double iy = y + 5 * s;
        double textScale = 0.5 * s;

        // ── заголовок: каждая буква окрашена своей фазой волны ──────────────
        String title = "Yarnternal — стиль клиента";
        double tx = ix;
        for (int i = 0; i < title.length(); i++) {
            String ch = String.valueOf(title.charAt(i));
            HudRenderer.INSTANCE.text(ch, tx, iy, wave(phase + i * 0.035), false, textScale);
            tx += HudRenderer.INSTANCE.textWidth(ch, false, textScale);
        }

        // ── полоса темы ────────────────────────────────────────────────────
        waveBar(ix, y + 18 * s, 174 * s, 9 * s, phase);

        // ── градиенты: вертикальный и горизонтальный ───────────────────────
        Renderer2D.COLOR.quad(ix, y + 32 * s, 84 * s, 10 * s,
                argb(ColorUtil.rgb(255, 85, 85)), argb(ColorUtil.rgb(255, 85, 85)),
                argb(ColorUtil.rgb(85, 85, 255)), argb(ColorUtil.rgb(85, 85, 255)));
        Renderer2D.COLOR.quad(x + 96 * s, y + 32 * s, 84 * s, 10 * s,
                argb(ColorUtil.rgb(85, 255, 85)), argb(ColorUtil.rgb(255, 85, 255)),
                argb(ColorUtil.rgb(255, 85, 255)), argb(ColorUtil.rgb(85, 255, 85)));

        // ── анимированная радуга ───────────────────────────────────────────
        rainbowBar(ix, y + 46 * s, 174 * s, 8 * s, phase);

        // ── палитра базовых цветов ─────────────────────────────────────────
        int[] palette = {
                ColorUtil.rgb(255, 85, 85),   // красный
                ColorUtil.rgb(255, 170, 0),   // оранжевый
                ColorUtil.rgb(255, 255, 85),  // жёлтый
                ColorUtil.rgb(85, 255, 85),   // зелёный
                ColorUtil.rgb(85, 255, 255),  // голубой
                ColorUtil.rgb(85, 85, 255),   // синий
                ColorUtil.rgb(255, 85, 255),  // фиолетовый
                ColorUtil.rgb(255, 255, 255), // белый
                ColorUtil.rgb(0, 0, 0),       // чёрный
        };
        for (int i = 0; i < palette.length; i++) {
            outlined(ix + i * 19 * s, y + 58 * s, 16 * s, 12 * s, 1.5 * s,
                    BORDER_WHITE, argb(palette[i]), s);
        }

        // ── шкала прозрачности (один цвет, разная alpha) ───────────────────
        for (int i = 0; i < 10; i++) {
            int alpha = Math.round(255.0f * (i + 1) / 10.0f);
            Renderer2D.COLOR.quad(ix + i * 17.4 * s, y + 74 * s, 15 * s, 10 * s,
                    argb(ColorUtil.rgba(85, 170, 255, alpha)));
        }

        // ── HEX и компоненты из ColorUtil ──────────────────────────────────
        int sample = ColorUtil.fromHex("#8034C6FF");
        HudRenderer.INSTANCE.text("HEX " + ColorUtil.toHex(sample)
                        + "  A" + ColorUtil.getAlpha(sample)
                        + " R" + ColorUtil.getRed(sample)
                        + " G" + ColorUtil.getGreen(sample)
                        + " B" + ColorUtil.getBlue(sample),
                ix, y + 88 * s, TEXT, false, 0.45 * s);

        // ── скруглённые прямоугольники ─────────────────────────────────────
        double cy = y + 102 * s;
        double ch = 12 * s;

        // заливка
        Renderer2D.COLOR.roundedQuad(ix, cy, 40 * s, ch,
                new Color(255, 85, 85, 255), 2 * s, 1.5);
        // волна
        Renderer2D.COLOR.roundedQuad(ix + 44 * s, cy, 40 * s, ch,
                wave(phase), wave(phase + 0.25), wave(phase + 0.5), wave(phase + 0.75),
                6 * s, 6 * s, 6 * s, 6 * s, 1.5);
        // обводка (внутри прозрачно — видно панель)
        outlined(ix + 88 * s, cy, 40 * s, ch, 6 * s, BORDER_WHITE, TRANSPARENT, s);
        // панелька с рамкой
        outlined(ix + 132 * s, cy, 42 * s, ch, 6 * s, TEXT, PANEL_SOLID, s);

        // ── формы: круг, пилюля и малый радиус ─────────────────────────────
        double sy = y + 120 * s;
        double sh = 14 * s;

        Renderer2D.COLOR.roundedQuad(ix, sy, sh, sh, argb(themeOne()), 7 * s, 1.5);
        Renderer2D.COLOR.roundedQuad(ix + 22 * s, sy, 40 * s, sh,
                argb(themeOne()), argb(themeTwo()), argb(themeTwo()), argb(themeOne()),
                7 * s, 7 * s, 7 * s, 7 * s, 1.5);
        Renderer2D.COLOR.roundedQuad(ix + 66 * s, sy, 24 * s, sh,
                argb(ColorUtil.rgb(85, 255, 85)), 2 * s, 1.5);
        HudRenderer.INSTANCE.text("SDF: smooth edges", ix + 96 * s, y + 121 * s, TEXT_DIM, false, 0.45 * s);
    }

    /** Рамка + заливка: внешний скруглённый квад и внутренний с отступом. */
    private static void outlined(double x, double y, double w, double h, double radius,
                                 Color border, Color fill, double s) {
        Renderer2D.COLOR.roundedQuad(x, y, w, h, border, radius, 1.5);
        Renderer2D.COLOR.roundedQuad(x + s, y + s, w - 2 * s, h - 2 * s, fill,
                Math.max(0, radius - s), 1.5);
    }

    /** Бегущая волна из плотных квадов — без «ступенек» на стыках. */
    private static void waveBar(double x, double y, double w, double h, double phase) {
        int segments = 64;
        for (int i = 0; i < segments; i++) {
            double sx = x + w * i / segments;
            double sw = w / segments + 1.0;
            Renderer2D.COLOR.quad(sx, y, sw, h, wave(phase + i / (double) segments));
        }
    }

    private static void rainbowBar(double x, double y, double w, double h, double phase) {
        int segments = 64;
        for (int i = 0; i < segments; i++) {
            double sx = x + w * i / segments;
            double sw = w / segments + 1.0;
            Renderer2D.COLOR.quad(sx, y, sw, h,
                    argb(ColorUtil.rainbow((float) (phase + i / (double) segments), 0.75f, 1.0f)));
        }
    }

    private static int themeOne() {
        Yarnternal y = Yarnternal.getInstance();
        return y != null && y.getColorOne() != 0 ? y.getColorOne() : THEME_ONE;
    }

    private static int themeTwo() {
        Yarnternal y = Yarnternal.getInstance();
        return y != null && y.getColorTwo() != 0 ? y.getColorTwo() : THEME_TWO;
    }

    private static Color wave(double phase) {
        return argb(ColorUtil.wave(themeOne(), themeTwo(), (float) phase));
    }

    private static Color argb(int color) {
        return new Color(
                ColorUtil.getRed(color),
                ColorUtil.getGreen(color),
                ColorUtil.getBlue(color),
                ColorUtil.getAlpha(color)
        );
    }
}

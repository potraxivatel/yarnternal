package yarnternal.lol.render;

import yarnternal.lol.render.renderer.Fonts;
import yarnternal.lol.render.renderer.FullScreenRenderer;
import yarnternal.lol.render.renderer.Renderer2D;
import yarnternal.lol.render.utils.render.RenderUtils;

/**
 * Инициализация рендер-стека: буферы фуллскрина, шрифты, утилиты рендера
 * и 2D-рисовальщики. Зовётся один раз из {@code ClientLifecycleEvents.CLIENT_STARTED},
 * когда GPU уже готов.
 */
public final class RenderBootstrap {

    private static boolean initialized;

    private RenderBootstrap() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        FullScreenRenderer.init();
        Fonts.init();
        RenderUtils.init();
        Renderer2D.init();

        RenderLog.LOG.info("Рендер-стек инициализирован");
    }
}

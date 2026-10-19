package yarnternal.lol.api.event.events;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import yarnternal.lol.api.event.Event;

/**
 * Кадр HUD: приходит из хука в конце InGameHud.render, когда открыт кадр
 * HudRenderer (шейдерный рендер) и включены unscaled-координаты.
 * Подписчики рисуют через {@code event.getContext()} и HudRenderer/Renderer2D.
 */
@Getter
public class Render2DEvent implements Event {

    private final DrawContext context;

    /** Ширина окна в GUI-координатах. */
    private final int screenWidth;

    /** Высота окна в GUI-координатах. */
    private final int screenHeight;

    /** Время кадра в секундах (сколько прошло с прошлого кадра). */
    private final double frameTime;

    /** Прогресс текущего тика (0..1) — для плавной интерполяции. */
    private final float tickDelta;

    public Render2DEvent(DrawContext context, int screenWidth, int screenHeight, double frameTime, float tickDelta) {
        this.context = context;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.frameTime = frameTime;
        this.tickDelta = tickDelta;
    }
}

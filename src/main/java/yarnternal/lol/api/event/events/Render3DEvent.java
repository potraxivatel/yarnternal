package yarnternal.lol.api.event.events;

import lombok.Getter;
import net.minecraft.client.util.math.MatrixStack;
import yarnternal.lol.api.event.Event;
import yarnternal.lol.render.renderer.Renderer3D;

/**
 * Кадр мира: приходит из хука GameRenderer (после отрисовки мира).
 * Внутри — готовые 3D-рисовальщики (обычный и с учётом глубины) и матрицы.
 * Подписчики рисуют через {@code event.getRenderer().box(...)}.
 */
@Getter
public class Render3DEvent implements Event {

    /** Матрицы рендера мира (для передачи в Renderer3D.render). */
    private final MatrixStack matrices;

    /** 3D-рисовальщик поверх всего (без глубины). */
    private final Renderer3D renderer;

    /** 3D-рисовальщик с проверкой глубины — линии обрезаются об стены. */
    private final Renderer3D depthRenderer;

    /** Время кадра в секундах. */
    private final double frameTime;

    /** Прогресс текущего тика (0..1) — для плавной интерполяции. */
    private final float tickDelta;

    /** Позиция камеры — смещение центра мира от начала координат. */
    private final double offsetX;

    private final double offsetY;

    private final double offsetZ;

    public Render3DEvent(MatrixStack matrices, Renderer3D renderer, Renderer3D depthRenderer,
                         double frameTime, float tickDelta,
                         double offsetX, double offsetY, double offsetZ) {
        this.matrices = matrices;
        this.renderer = renderer;
        this.depthRenderer = depthRenderer;
        this.frameTime = frameTime;
        this.tickDelta = tickDelta;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
    }
}

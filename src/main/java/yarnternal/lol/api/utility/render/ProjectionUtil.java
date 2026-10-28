package yarnternal.lol.api.utility.render;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2f;
import yarnternal.lol.api.utility.client.IMinecraft;

/**
 * Утилита проекции 3D координат мира в 2D координаты экрана.
 * Использует нативный проектор GameRenderer из Minecraft 1.21.
 */
public final class ProjectionUtil implements IMinecraft {

    private ProjectionUtil() {
    }

    /**
     * Проецирует 3D позицию мира в координаты GUI экрана.
     *
     * @param x мировая координата X
     * @param y мировая координата Y
     * @param z мировая координата Z
     * @return 2D координаты на экране в пикселях GUI, либо {@code null}, если точка за спиной.
     */
    public static Vector2f project(double x, double y, double z) {
        if (mc.gameRenderer == null || mc.getWindow() == null || mc.player == null) {
            return null;
        }

        Vec3d targetPos = new Vec3d(x, y, z);
        Vec3d cameraPos = mc.gameRenderer.getCamera().getCameraPos();
        Vec3d toTarget = targetPos.subtract(cameraPos);

        // Проверяем вектор взгляда игрока: цель должна быть перед глазами
        float tickDelta = mc.getRenderTickCounter().getTickProgress(false);
        Vec3d lookVec = mc.player.getRotationVec(tickDelta);
        if (toTarget.dotProduct(lookVec) <= 0.0) {
            return null; // цель за спиной
        }

        // Встроенный проектор Майнкрафта переводит координаты в NDC [-1.0 .. 1.0]
        Vec3d ndc = mc.gameRenderer.project(targetPos);
        if (ndc == null) {
            return null;
        }

        // Переводим NDC в координаты экрана GUI
        float screenX = (float) ((ndc.x + 1.0) * 0.5 * mc.getWindow().getScaledWidth());
        float screenY = (float) ((1.0 - ndc.y) * 0.5 * mc.getWindow().getScaledHeight());

        return new Vector2f(screenX, screenY);
    }

    /**
     * Проецирует вектор {@link Vec3d} в координаты экрана.
     */
    public static Vector2f project(Vec3d pos) {
        if (pos == null) return null;
        return project(pos.x, pos.y, pos.z);
    }

    /**
     * Проецирует центр сущности (с интерполяцией между тиками через tickDelta).
     *
     * @param entity    сущность
     * @param tickDelta дельта рендера текущего кадра
     * @return 2D координаты центра модельки на экране
     */
    public static Vector2f projectEntityCenter(Entity entity, float tickDelta) {
        if (entity == null) return null;

        Vec3d pos = entity.getLerpedPos(tickDelta).add(0.0, entity.getHeight() / 2.0, 0.0);
        return project(pos);
    }

    /**
     * Проверяет, попадает ли точка в границы видимого экрана.
     */
    public static boolean isInView(Vector2f screenPos) {
        if (screenPos == null || mc.getWindow() == null) return false;
        return screenPos.x >= 0 && screenPos.x <= mc.getWindow().getScaledWidth()
                && screenPos.y >= 0 && screenPos.y <= mc.getWindow().getScaledHeight();
    }
}

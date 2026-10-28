package yarnternal.lol.api.utility.player;

import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;
import yarnternal.lol.api.utility.client.IMinecraft;

/**
 * Утилиты для работы с перемещением игрока (движение, скорость, углы).
 */
public final class MoveUtil implements IMinecraft {

    private MoveUtil() {
    }

    /**
     * Базовая скорость перемещения игрока (с учетом эффекта Скорости и Замедления).
     */
    public static double getBaseMoveSpeed() {
        double base = 0.285;
        if (mc.player == null) return base;

        if (mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            var effect = mc.player.getStatusEffect(StatusEffects.SPEED);
            if (effect != null) {
                base *= 1.0 + 0.2 * (effect.getAmplifier() + 1);
            }
        }
        if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
            var effect = mc.player.getStatusEffect(StatusEffects.SLOWNESS);
            if (effect != null) {
                base *= 1.0 - 0.15 * (effect.getAmplifier() + 1);
            }
        }
        return base;
    }

    /**
     * Проверяет, зажата ли хотя бы одна клавиша перемещения (WASD).
     */
    public static boolean isMoving() {
        if (mc.player == null) return false;
        return mc.options.forwardKey.isPressed()
                || mc.options.backKey.isPressed()
                || mc.options.leftKey.isPressed()
                || mc.options.rightKey.isPressed();
    }

    /**
     * Возвращает текущую горизонтальную скорость игрока (hypot X и Z).
     */
    public static double getSpeed() {
        if (mc.player == null) return 0.0;
        Vec3d vel = mc.player.getVelocity();
        return Math.hypot(vel.x, vel.z);
    }

    /**
     * Вычисляет точный угол направления движения в радианах с учетом зажатых клавиш WASD.
     */
    public static double getDirection() {
        if (mc.player == null) return 0.0;
        return getDirection(mc.player.getYaw());
    }

    /**
     * Вычисляет точный угол направления движения в радианах от переданного yaw.
     */
    public static double getDirection(float yaw) {
        float forward = 0;
        float strafe = 0;

        if (mc.options.forwardKey.isPressed()) forward += 1;
        if (mc.options.backKey.isPressed()) forward -= 1;
        if (mc.options.leftKey.isPressed()) strafe += 1;
        if (mc.options.rightKey.isPressed()) strafe -= 1;

        if (forward == 0 && strafe == 0) {
            return Math.toRadians(yaw);
        }

        if (forward > 0) {
            if (strafe > 0) {
                yaw -= 45;
            } else if (strafe < 0) {
                yaw += 45;
            }
        } else if (forward < 0) {
            if (strafe > 0) {
                yaw -= 135;
            } else if (strafe < 0) {
                yaw += 135;
            } else {
                yaw += 180;
            }
        } else {
            if (strafe > 0) {
                yaw -= 90;
            } else if (strafe < 0) {
                yaw += 90;
            }
        }

        return Math.toRadians(yaw);
    }

    /**
     * Направляет горизонтальную скорость игрока в сторону движения с заданной величиной скорости.
     */
    public static void setSpeed(double speed) {
        if (mc.player == null) return;
        if (!isMoving()) {
            mc.player.setVelocity(0.0, mc.player.getVelocity().y, 0.0);
            return;
        }

        double dir = getDirection();
        mc.player.setVelocity(-Math.sin(dir) * speed, mc.player.getVelocity().y, Math.cos(dir) * speed);
    }

    /**
     * Направляет горизонтальную скорость игрока по переданному углу (в градусах).
     */
    public static void setSpeed(double speed, float yawDegrees) {
        if (mc.player == null) return;
        double rad = Math.toRadians(yawDegrees);
        mc.player.setVelocity(-Math.sin(rad) * speed, mc.player.getVelocity().y, Math.cos(rad) * speed);
    }

    /**
     * Стрейфит, сохраняя текущую величину горизонтальной скорости игрока.
     */
    public static void strafe() {
        setSpeed(getSpeed());
    }
}

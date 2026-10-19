package yarnternal.lol.render.utils;

import net.minecraft.client.MinecraftClient;

import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import yarnternal.lol.render.RenderLog;
import yarnternal.lol.render.mixin.ProjectionMatrix2Accessor;
import yarnternal.lol.render.utils.render.RenderUtils;
import net.minecraft.client.render.ProjectionMatrix2;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;


public class Utils {
    public static boolean rendering3D = true;
    public static double frameTime;

    private static final ProjectionMatrix2 matrix = new ProjectionMatrix2("yarnternal-projection-matrix", -10, 100, true);

    private static long lastFrameTime;

    public static boolean canUpdate() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.world != null && client.player != null;
    }

    /** Считает время кадра — зовётся из хука HUD перед постом Render2DEvent. */
    public static void updateFrameTime() {
        long now = System.currentTimeMillis();
        frameTime = lastFrameTime == 0 ? 0.0 : (now - lastFrameTime) / 1000.0;
        lastFrameTime = now;
    }

    public static void unscaledProjection() {
        float width = MinecraftClient.getInstance().getWindow().getFramebufferWidth();
        float height = MinecraftClient.getInstance().getWindow().getFramebufferHeight();

        RenderSystem.setProjectionMatrix(matrix.set(width, height), ProjectionType.ORTHOGRAPHIC);
        RenderUtils.projection.set(((ProjectionMatrix2Accessor) matrix).yarn$callGetMatrix(width, height));

        rendering3D = false;
    }

    public static void scaledProjection() {
        float width = (float) (MinecraftClient.getInstance().getWindow().getFramebufferWidth() / MinecraftClient.getInstance().getWindow().getScaleFactor());
        float height = (float) (MinecraftClient.getInstance().getWindow().getFramebufferHeight() / MinecraftClient.getInstance().getWindow().getScaleFactor());

        RenderSystem.setProjectionMatrix(matrix.set(width, height), ProjectionType.PERSPECTIVE);
        RenderUtils.projection.set(((ProjectionMatrix2Accessor) matrix).yarn$callGetMatrix(width, height));

        rendering3D = true;
    }

    public static byte[] readBytes(InputStream in) {
        try {
            return in.readAllBytes();
        } catch (IOException e) {
            RenderLog.LOG.error("Error reading from stream.", e);
            return new byte[0];
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}

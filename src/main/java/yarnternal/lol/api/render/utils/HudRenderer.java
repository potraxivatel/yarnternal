package yarnternal.lol.render.utils;

import net.minecraft.client.MinecraftClient;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import yarnternal.lol.render.renderer.*;
import yarnternal.lol.render.renderer.text.CustomTextRenderer;
import yarnternal.lol.render.renderer.text.Font;
import yarnternal.lol.render.renderer.text.FontFace;
import yarnternal.lol.render.utils.color.Color;
import yarnternal.lol.render.utils.render.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class HudRenderer {
    public static final HudRenderer INSTANCE = new HudRenderer();

    private static final double SCALE_TO_HEIGHT = 1.0 / 18.0;

    private final List<Runnable> postTasks = new ArrayList<>();

    /** Ключ кэша: шрифт + высота растеризации. */
    private record FontKey(FontFace face, int height) {}

    private final Map<FontKey, FontHolder> fontsInUse = new HashMap<>();
    private final LoadingCache<FontKey, FontHolder> fontCache = CacheBuilder.newBuilder()
            .maximumSize(24)
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener(notification -> {
                if (notification.wasEvicted())
                    ((FontHolder) notification.getValue()).destroy();
            })
            .build(CacheLoader.from(key -> loadFont(key.face(), key.height())));

    public DrawContext drawContext;
    public double delta;

    private boolean scissorEnabled;
    private int scissorX, scissorY, scissorWidth, scissorHeight;

    private HudRenderer() {
    }

    public void begin(DrawContext drawContext) {
        Renderer2D.COLOR.begin();

        this.drawContext = drawContext;
        this.delta = Utils.frameTime;

        drawContext.createNewRootLayer();
    }

    /**
     * Сбрасывает накопленную геометрию и текст на экран, не закрывая кадр.
     * Нужно перед сменой ножниц — батч рисуется одним проходом.
     */
    public void flush() {
        Renderer2D.COLOR.render();
        renderFonts();
        Renderer2D.COLOR.begin();
    }

    public void end() {
        Renderer2D.COLOR.render();
        renderFonts();

        for (Runnable task : postTasks) task.run();
        postTasks.clear();

        drawContext.createNewRootLayer();

        drawContext = null;
    }

    /** Ограничивает отрисовку прямоугольником (пиксели фреймбуфера, левый верхний угол). */
    public void scissor(int x, int y, int width, int height) {
        scissorEnabled = true;
        scissorX = x;
        scissorY = y;
        scissorWidth = width;
        scissorHeight = height;
        Renderer2D.COLOR.setScissor(x, y, width, height);
    }

    public void resetScissor() {
        scissorEnabled = false;
        Renderer2D.COLOR.clearScissor();
    }

    private void renderFonts() {
        // Render fonts that were visited this frame and move to cache which weren't visited
        for (Iterator<FontHolder> it = fontsInUse.values().iterator(); it.hasNext(); ) {
            FontHolder fontHolder = it.next();

            if (fontHolder.visited) {
                MeshRenderer renderer = MeshRenderer.begin()
                        .attachments(MinecraftClient.getInstance().getFramebuffer())
                        .pipeline(YarnRenderPipelines.UI_TEXT)
                        .mesh(fontHolder.getMesh())
                        .sampler("u_Texture", fontHolder.font.texture.getGlTextureView(), fontHolder.font.texture.getSampler());

                if (scissorEnabled) {
                    renderer.scissor(scissorX, scissorY, scissorWidth, scissorHeight);
                }

                renderer.end();
            }
            else {
                it.remove();
                fontCache.put(new FontKey(fontHolder.face, fontHolder.font.getHeight()), fontHolder);
            }

            fontHolder.visited = false;
        }
    }

    public void line(double x1, double y1, double x2, double y2, Color color) {
        Renderer2D.COLOR.line(x1, y1, x2, y2, color);
    }

    public void quad(double x, double y, double width, double height, Color color) {
        Renderer2D.COLOR.quad(x, y, width, height, color);
    }

    public void quad(double x, double y, double width, double height, Color cTopLeft, Color cTopRight, Color cBottomRight, Color cBottomLeft) {
        Renderer2D.COLOR.quad(x, y, width, height, cTopLeft, cTopRight, cBottomRight, cBottomLeft);
    }

    public void triangle(double x1, double y1, double x2, double y2, double x3, double y3, Color color) {
        Renderer2D.COLOR.triangle(x1, y1, x2, y2, x3, y3, color);
    }

    public void texture(Identifier id, double x, double y, double width, double height, Color color) {
        texture(id, x, y, width, height, 0.0, color);
    }

    public void texture(Identifier id, double x, double y, double width, double height, double rotation, Color color) {
        var tm = MinecraftClient.getInstance().getTextureManager();
        var tex = tm.getTexture(id);
        if (tex == null) {
            tm.registerTexture(id);
            tex = tm.getTexture(id);
        }
        if (tex == null) {
            return;
        }

        Renderer2D.TEXTURE.begin();
        Renderer2D.TEXTURE.texQuad(x, y, width, height, rotation, color);
        Renderer2D.TEXTURE.render(tex.getGlTextureView(), tex.getSampler());
    }

    public double text(String text, double x, double y, Color color, boolean shadow, double scale) {
        return text(text, x, y, color, shadow, scale, Fonts.FONT);
    }

    public double text(String text, double x, double y, Color color, boolean shadow, double scale, FontFace face) {
        FontHolder fontHolder = getFontHolder(face, scale, true);

        Font font = fontHolder.font;
        MeshBuilder mesh = fontHolder.getMesh();

        double width;

        if (shadow) {
            int preShadowA = CustomTextRenderer.SHADOW_COLOR.a;
            CustomTextRenderer.SHADOW_COLOR.a = (int) (color.a / 255.0 * preShadowA);

            width = font.render(mesh, text, x + 1, y + 1, CustomTextRenderer.SHADOW_COLOR, 1.0);
            font.render(mesh, text, x, y, color, 1.0);

            CustomTextRenderer.SHADOW_COLOR.a = preShadowA;
        }
        else {
            width = font.render(mesh, text, x, y, color, 1.0);
        }

        return width;
    }
    public double text(String text, double x, double y, Color color, boolean shadow) {
        return text(text, x, y, color, shadow, 1.0);
    }

    public double textWidth(String text, boolean shadow, double scale) {
        return textWidth(text, shadow, scale, Fonts.FONT);
    }

    public double textWidth(String text, boolean shadow, double scale, FontFace face) {
        if (text.isEmpty()) return 0;

        double width = getFont(face, scale).getWidth(text, text.length());
        return width + (shadow ? 1 : 0);
    }

    public double textWidth(String text, boolean shadow) {
        return textWidth(text, shadow, 1.0);
    }
    public double textWidth(String text, double scale) {
        return textWidth(text, false, scale);
    }
    public double textWidth(String text) {
        return textWidth(text, false, 1.0);
    }

    public double textHeight(boolean shadow, double scale) {
        return textHeight(shadow, scale, Fonts.FONT);
    }

    public double textHeight(boolean shadow, double scale, FontFace face) {
        double height = getFont(face, scale).getHeight() + 1;
        return height + (shadow ? 1 : 0);
    }

    public double textHeight(boolean shadow) {
        return textHeight(shadow, 1.0);
    }
    public double textHeight() {
        return textHeight(false, 1.0);
    }

    public void post(Runnable task) {
        postTasks.add(task);
    }

    public void item(ItemStack itemStack, int x, int y, float scale, boolean overlay, String countOverlay) {
        RenderUtils.drawItem(drawContext, itemStack, x, y, scale, overlay, countOverlay, true);
    }

    public void item(ItemStack itemStack, int x, int y, float scale, boolean overlay) {
        RenderUtils.drawItem(drawContext, itemStack, x, y, scale, overlay);
    }

    public void entity(LivingEntity entity,  int x, int y, int width, int height, float yaw, float pitch) {
        float previousBodyYaw = entity.bodyYaw;
        float previousYaw = entity.getYaw();
        float previousPitch = entity.getPitch();
        float lastLastHeadYaw = entity.lastHeadYaw;
        float lastHeadYaw = entity.headYaw;

        float tanYaw = (float) Math.atan((yaw) / 40.0f);
        float tanPitch = (float) Math.atan((pitch) / 40.0f);
        entity.bodyYaw = 180.0f + tanYaw * 20.0f;
        entity.setYaw(180.0f + tanYaw * 40.0f);
        entity.setPitch(-tanPitch * 20.0f);
        entity.headYaw = entity.getYaw();
        entity.lastHeadYaw = entity.getYaw();

        var state = (LivingEntityRenderState) MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(entity).getAndUpdateRenderState(entity, 1);

        entity.bodyYaw = previousBodyYaw;
        entity.setYaw(previousYaw);
        entity.setPitch(previousPitch);
        entity.lastHeadYaw = lastLastHeadYaw;
        entity.headYaw = lastHeadYaw;

        float s = 1.0f / MinecraftClient.getInstance().getWindow().getScaleFactor();
        int x1 = (int) (x * s);
        int y1 = (int) (y * s);
        int x2 = (int) ((x + width) * s);
        int y2 = (int) ((y + height) * s);

        float scale = Math.max(width, height) * s / 2f;
        Vector3f translation = new Vector3f(0, 1f, 0);
        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);

        drawContext.addEntity(state, scale, translation, rotation, null, x1, y1, x2, y2);
    }

    private FontHolder getFontHolder(FontFace face, double scale, boolean render) {
        int height = (int) Math.round(scale / SCALE_TO_HEIGHT);
        FontKey key = new FontKey(face, height);

        // Check fonts in use
        FontHolder fontHolder = fontsInUse.get(key);
        if (fontHolder != null) {
            if (render) fontHolder.visited = true;
            return fontHolder;
        }

        // Create font if not in cache otherwise remove from cache and add to fonts in use
        if (render) {
            fontHolder = fontCache.getIfPresent(key);
            if (fontHolder == null) fontHolder = loadFont(face, height);
            else fontCache.invalidate(key);

            fontsInUse.put(key, fontHolder);
            fontHolder.visited = true;

            return fontHolder;
        }

        // Otherwise get from cache
        return fontCache.getUnchecked(key);
    }

    private Font getFont(FontFace face, double scale) {
        return getFontHolder(face, scale, false).font;
    }

    private static FontHolder loadFont(FontFace face, int height) {
        byte[] data = Utils.readBytes(face.toStream());
        ByteBuffer buffer = BufferUtils.createByteBuffer(data.length).put(data).flip();

        return new FontHolder(face, new Font(buffer, height));
    }

    private static class FontHolder {
        public final FontFace face;
        public final Font font;
        public boolean visited;

        private MeshBuilder mesh;

        public FontHolder(FontFace face, Font font) {
            this.face = face;
            this.font = font;
        }

        public MeshBuilder getMesh() {
            if (mesh == null) mesh = new MeshBuilder(YarnRenderPipelines.UI_TEXT);
            if (!mesh.isBuilding()) mesh.begin();
            return mesh;
        }

        public void destroy() {
            font.texture.close();
        }
    }
}
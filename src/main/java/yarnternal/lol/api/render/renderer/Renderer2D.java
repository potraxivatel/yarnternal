package yarnternal.lol.render.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import yarnternal.lol.render.utils.color.Color;
import yarnternal.lol.render.utils.render.TextureRegion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import static yarnternal.lol.render.renderer.Blurrer.createLinearSampler;

public class Renderer2D {
    public static Renderer2D COLOR;
    public static Renderer2D TEXTURE;
    public static Blurrer BLUR;

    private final boolean textured;

    public final MeshBuilder triangles;
    public final MeshBuilder lines;

    private boolean scissorEnabled;
    private int scissorX, scissorY, scissorWidth, scissorHeight;

    public Renderer2D(boolean textured) {
        this.textured = textured;

        triangles = new MeshBuilder(textured ? YarnRenderPipelines.UI_TEXTURED : YarnRenderPipelines.UI_COLORED);
        lines = new MeshBuilder(YarnRenderPipelines.UI_COLORED_LINES);
    }

    public static void init() {
        COLOR = new Renderer2D(false);
        TEXTURE = new Renderer2D(true);

        BLUR = new Blurrer();
    }

    public void setAlpha(double alpha) {
        triangles.alpha = alpha;
    }

    /** Ограничивает всё, что рисуется дальше, прямоугольником (пиксели фреймбуфера). */
    public void setScissor(int x, int y, int width, int height) {
        scissorEnabled = true;
        scissorX = x;
        scissorY = y;
        scissorWidth = width;
        scissorHeight = height;
    }

    public void clearScissor() {
        scissorEnabled = false;
    }

    private MeshRenderer applyScissor(MeshRenderer renderer) {
        if (scissorEnabled) {
            renderer.scissor(scissorX, scissorY, scissorWidth, scissorHeight);
        }
        return renderer;
    }

    public void begin() {
        triangles.begin();
        lines.begin();
    }

    public void end() {
        triangles.end();
        lines.end();
    }

    public void render() {
        render(null, null, null);
    }

    public void render(GpuTextureView textureView, GpuSampler sampler) {
        if (!textured)
            throw new IllegalStateException("Tried to render with a texture with a non-textured Renderer2D");

        render("u_Texture", textureView, sampler);
    }

    public void render(String samplerName, GpuTextureView samplerView, GpuSampler sampler) {
        if (lines.isBuilding()) lines.end();
        if (triangles.isBuilding()) triangles.end();

        MeshRenderer lineRenderer = MeshRenderer.begin()
            .attachments(MinecraftClient.getInstance().getFramebuffer())
            .pipeline(YarnRenderPipelines.UI_COLORED_LINES)
            .mesh(lines);
        applyScissor(lineRenderer).end();

        MeshRenderer triangleRenderer = MeshRenderer.begin()
            .attachments(MinecraftClient.getInstance().getFramebuffer())
            .pipeline(textured ? YarnRenderPipelines.UI_TEXTURED : YarnRenderPipelines.UI_COLORED)
            .mesh(triangles)
            .sampler(samplerName, samplerView, sampler);
        applyScissor(triangleRenderer).end();
    }

    // Tris
    public void triangle(double x1, double y1, double x2, double y2, double x3, double y3, Color color) {
        triangles.ensureTriCapacity();

        // size(0,0) — форма не SDF, шейдер рисует сплошной цвет без скруглений
        triangles.triangle(
            triangles.vec2(x1, y1).color(color).uv(0, 0).size(0, 0).radius(0, 0, 0, 0).smoothness(1.5).next(),
            triangles.vec2(x2, y2).color(color).uv(0, 0).size(0, 0).radius(0, 0, 0, 0).smoothness(1.5).next(),
            triangles.vec2(x3, y3).color(color).uv(0, 0).size(0, 0).radius(0, 0, 0, 0).smoothness(1.5).next()
        );
    }

    // Lines

    public void line(double x1, double y1, double x2, double y2, Color color) {
        lines.ensureLineCapacity();

        lines.line(
            lines.vec2(x1, y1).color(color).next(),
            lines.vec2(x2, y2).color(color).next()
        );
    }

    public void boxLines(double x, double y, double width, double height, Color color) {
        lines.ensureCapacity(4, 8);

        int i1 = lines.vec2(x, y).color(color).next();
        int i2 = lines.vec2(x, y + height).color(color).next();
        int i3 = lines.vec2(x + width, y + height).color(color).next();
        int i4 = lines.vec2(x + width, y).color(color).next();

        lines.line(i1, i2);
        lines.line(i2, i3);
        lines.line(i3, i4);
        lines.line(i4, i1);
    }

    // Quads

    public void quad(double x, double y, double width, double height, Color cTopLeft, Color cTopRight, Color cBottomRight, Color cBottomLeft) {
        triangles.ensureQuadCapacity();

        triangles.quad(
            triangles.vec2(x, y).color(cTopLeft).uv(0, 0).size(width, height).radius(0, 0, 0, 0).smoothness(1.5).next(),
            triangles.vec2(x, y + height).color(cBottomLeft).uv(0, 1).size(width, height).radius(0, 0, 0, 0).smoothness(1.5).next(),
            triangles.vec2(x + width, y + height).color(cBottomRight).uv(1, 1).size(width, height).radius(0, 0, 0, 0).smoothness(1.5).next(),
            triangles.vec2(x + width, y).color(cTopRight).uv(1, 0).size(width, height).radius(0, 0, 0, 0).smoothness(1.5).next()
        );
    }

    public void roundedQuad(double x, double y, double width, double height, Color c, double radius, double smoothness){
        roundedQuad(x, y, width, height, c, c, c, c, radius, radius, radius, radius, smoothness);
    }

    public void roundedQuad(double x, double y, double width, double height, Color cTopLeft, Color cTopRight, Color cBottomRight, Color cBottomLeft, double r1, double r2, double r3, double r4, double smoothness){
        triangles.ensureQuadCapacity();

        triangles.quad(
                triangles.vec2(x, y)
                        .color(cTopLeft)
                        .uv(0, 0)
                        .size(width, height)
                        .radius(r1, r2, r3, r4)
                        .smoothness(smoothness)
                        .next(),

                triangles.vec2(x, y + height)
                        .color(cBottomLeft)
                        .uv(0, 1)
                        .size(width, height)
                        .radius(r1, r2, r3, r4)
                        .smoothness(smoothness)
                        .next(),

                triangles.vec2(x + width, y + height)
                        .color(cBottomRight)
                        .uv(1, 1)
                        .size(width, height)
                        .radius(r1, r2, r3, r4)
                        .smoothness(smoothness)
                        .next(),

                triangles.vec2(x + width, y)
                        .color(cTopRight)
                        .uv(1, 0)
                        .size(width, height)
                        .radius(r1, r2, r3, r4)
                        .smoothness(smoothness)
                        .next()
        );
    }

    public void quad(double x, double y, double width, double height, Color color) {
        quad(x, y, width, height, color, color, color, color);
    }

    // Textured quads

    public void texQuad(double x, double y, double width, double height, Color color) {
        triangles.ensureQuadCapacity();

        // порядок атрибутов = порядок в YarnVertexFormats: pos, color, uv, geoUv, size, radius, smoothness
        triangles.quad(
            triangles.vec2(x, y).color(color).uv(0, 0).geoUv(0, 0).size(width, height).radius(0, 0, 0, 0).smoothness(1.5).next(),
            triangles.vec2(x, y + height).color(color).uv(0, 1).geoUv(0, 1).size(width, height).radius(0, 0, 0, 0).smoothness(1.5).next(),
            triangles.vec2(x + width, y + height).color(color).uv(1, 1).geoUv(1, 1).size(width, height).radius(0, 0, 0, 0).smoothness(1.5).next(),
            triangles.vec2(x + width, y).color(color).uv(1, 0).geoUv(1, 0).size(width, height).radius(0, 0, 0, 0).smoothness(1.5).next()
        );
    }

    public void texQuad(double x, double y, double width, double height, TextureRegion texture, Color color) {
        triangles.ensureQuadCapacity();

        triangles.quad(
            triangles.vec2(x, y).color(color).uv(texture.x1, texture.y1).geoUv(0, 0).size(width, height).radius(0, 0, 0, 0).smoothness(1.5).next(),
            triangles.vec2(x, y + height).color(color).uv(texture.x1, texture.y2).geoUv(0, 1).size(width, height).radius(0, 0, 0, 0).smoothness(1.5).next(),
            triangles.vec2(x + width, y + height).color(color).uv(texture.x2, texture.y2).geoUv(1, 1).size(width, height).radius(0, 0, 0, 0).smoothness(1.5).next(),
            triangles.vec2(x + width, y).color(color).uv(texture.x2, texture.y1).geoUv(1, 0).size(width, height).radius(0, 0, 0, 0).smoothness(1.5).next()
        );
    }

    public void texQuad(double x, double y, double width, double height, double rotation, Color color) {
        texQuad(x, y, width, height, rotation, 0, 0, 1, 1, color);
    }

    public void texQuad(double x, double y, double width, double height, double rotation, double texX1, double texY1, double texX2, double texY2, Color color) {
        triangles.ensureQuadCapacity();

        double rad = Math.toRadians(rotation);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        double oX = x + width / 2;
        double oY = y + height / 2;

        double _x1 = ((x - oX) * cos) - ((y - oY) * sin) + oX;
        double _y1 = ((y - oY) * cos) + ((x - oX) * sin) + oY;
        int i1 = triangles.vec2(_x1, _y1).color(color).uv(texX1, texY1).geoUv(0, 0)
                .size(width, height).radius(0, 0, 0, 0).smoothness(1.5).next();

        double _x2 = ((x - oX) * cos) - ((y + height - oY) * sin) + oX;
        double _y2 = ((y + height - oY) * cos) + ((x - oX) * sin) + oY;
        int i2 = triangles.vec2(_x2, _y2).color(color).uv(texX1, texY2).geoUv(0, 1)
                .size(width, height).radius(0, 0, 0, 0).smoothness(1.5).next();

        double _x3 = ((x + width - oX) * cos) - ((y + height - oY) * sin) + oX;
        double _y3 = ((y + height - oY) * cos) + ((x + width - oX) * sin) + oY;
        int i3 = triangles.vec2(_x3, _y3).color(color).uv(texX2, texY2).geoUv(1, 1)
                .size(width, height).radius(0, 0, 0, 0).smoothness(1.5).next();

        double _x4 = ((x + width - oX) * cos) - ((y - oY) * sin) + oX;
        double _y4 = ((y - oY) * cos) + ((x + width - oX) * sin) + oY;
        int i4 = triangles.vec2(_x4, _y4).color(color).uv(texX2, texY1).geoUv(1, 0)
                .size(width, height).radius(0, 0, 0, 0).smoothness(1.5).next();

        triangles.quad(i1, i2, i3, i4);
    }

    public void texQuad(double x, double y, double width, double height, double rotation, TextureRegion region, Color color) {
        texQuad(x, y, width, height, rotation, region.x1, region.y1, region.x2, region.y2, color);
    }

    // Основной метод с полным контролем
    public void roundedTexQuad(double x, double y, double width, double height, double rotation, double texX1, double texY1, double texX2, double texY2, Color c, double r1, double r2, double r3, double r4, double smoothness) {
        triangles.ensureQuadCapacity();

        double rad = Math.toRadians(rotation);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        double oX = x + width / 2;
        double oY = y + height / 2;

        // Считаем 4 угла с учетом вращения
        double x1 = ((x - oX) * cos) - ((y - oY) * sin) + oX;
        double y1 = ((y - oY) * cos) + ((x - oX) * sin) + oY;

        double x2 = ((x - oX) * cos) - ((y + height - oY) * sin) + oX;
        double y2 = ((y + height - oY) * cos) + ((x - oX) * sin) + oY;

        double x3 = ((x + width - oX) * cos) - ((y + height - oY) * sin) + oX;
        double y3 = ((y + height - oY) * cos) + ((x + width - oX) * sin) + oY;

        double x4 = ((x + width - oX) * cos) - ((y - oY) * sin) + oX;
        double y4 = ((y - oY) * cos) + ((x + width - oX) * sin) + oY;

        triangles.quad(
                triangles.vec2(x1, y1).color(c).uv(texX1, texY1).geoUv(0, 0)
                        .size(width, height).radius(r1, r2, r3, r4).smoothness(smoothness).next(),

                triangles.vec2(x2, y2).color(c).uv(texX1, texY2).geoUv(0, 1)
                        .size(width, height).radius(r1, r2, r3, r4).smoothness(smoothness).next(),

                triangles.vec2(x3, y3).color(c).uv(texX2, texY2).geoUv(1, 1)
                        .size(width, height).radius(r1, r2, r3, r4).smoothness(smoothness).next(),

                triangles.vec2(x4, y4).color(c).uv(texX2, texY1).geoUv(1, 0)
                        .size(width, height).radius(r1, r2, r3, r4).smoothness(smoothness).next()
        );
    }

    public void roundedTexQuad(double x, double y, double width, double height, double rotation, TextureRegion region, Color c, double radius, double smoothness) {
        roundedTexQuad(
                x, y, width, height, rotation,
                region.x1, region.y1, region.x2, region.y2,
                c,
                radius, radius, radius, radius,
                smoothness
        );
    }

    public void roundedTexQuad(double x, double y, double width, double height, TextureRegion region, Color c, double radius) {
        roundedTexQuad(x, y, width, height, 0, region, c, radius, 1.5);
    }

    public void blurQuad(double x, double y, double width, double height, double radius, double smoothness) {
        GpuTextureView blurTexture = BLUR.getBlurredTexture();

        double windowW = MinecraftClient.getInstance().getWindow().getScaledWidth();
        double windowH = MinecraftClient.getInstance().getWindow().getScaledHeight();

        float u1 = (float) (x / windowW);
        float v1 = 1.0f - (float) (y / windowH);

        float u2 = (float) ((x + width) / windowW);
        float v2 = 1.0f - (float) ((y + height) / windowH);

        boolean wasBuilding = triangles.isBuilding();

        if (wasBuilding) {
            triangles.end();
        }

        triangles.begin();

        roundedTexQuad(
                x, y, width, height,
                0,
                u1, v1, u2, v2,
                Color.WHITE,
                radius, radius, radius, radius,
                smoothness
        );
        triangles.end();

        MeshRenderer.begin()
                .attachments(MinecraftClient.getInstance().getFramebuffer())
                .pipeline(YarnRenderPipelines.UI_TEXTURED)
                .mesh(triangles)
                .sampler("u_Texture", blurTexture, createLinearSampler())
                .end();
                
        if (wasBuilding) {
            triangles.begin();
        }
    }
}

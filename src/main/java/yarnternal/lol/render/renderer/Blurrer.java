package yarnternal.lol.render.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import it.unimi.dsi.fastutil.ints.IntFloatImmutablePair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GpuSampler;

import java.util.OptionalDouble;


public class Blurrer {
    private static final IntFloatImmutablePair[] STRENGTHS = new IntFloatImmutablePair[]{
            IntFloatImmutablePair.of(1, 1.25f),
            IntFloatImmutablePair.of(1, 2.25f),
            IntFloatImmutablePair.of(2, 2.0f),
            IntFloatImmutablePair.of(2, 3.0f),
            IntFloatImmutablePair.of(2, 4.25f),
            IntFloatImmutablePair.of(3, 2.5f),
            IntFloatImmutablePair.of(3, 3.25f),
            IntFloatImmutablePair.of(3, 4.25f),
            IntFloatImmutablePair.of(3, 5.5f),
            IntFloatImmutablePair.of(4, 3.25f),
    };

    static GpuSampler createLinearSampler() {
        return RenderSystem.getDevice().createSampler(
                AddressMode.CLAMP_TO_EDGE,
                AddressMode.CLAMP_TO_EDGE,
                FilterMode.LINEAR,
                FilterMode.LINEAR,
                1,
                OptionalDouble.empty()
        );
    }

    private final GpuTextureView[] fbos = new GpuTextureView[6];
    private int lastWidth, lastHeight;

    public Blurrer() {
        resizeIfNeeded();
    }

    public void update(int strengthLevel) {
        resizeIfNeeded();

        if (strengthLevel < 1) strengthLevel = 1;
        if (strengthLevel > STRENGTHS.length) strengthLevel = STRENGTHS.length;

        IntFloatImmutablePair pair = STRENGTHS[strengthLevel - 1];
        int iterations = pair.leftInt();
        float offset = pair.rightFloat();

        renderPass(fbos[0], MinecraftClient.getInstance().getFramebuffer().getColorAttachmentView(), YarnRenderPipelines.BLUR_DOWN, offset);

        for (int i = 0; i < iterations; i++) {
            renderPass(fbos[i + 1], fbos[i], YarnRenderPipelines.BLUR_DOWN, offset);
        }

        for (int i = iterations; i >= 1; i--) {
            renderPass(fbos[i - 1], fbos[i], YarnRenderPipelines.BLUR_UP, offset);
        }

    }

    public GpuTextureView getBlurredTexture() {
        return fbos[0];
    }

    private void renderPass(GpuTextureView target, GpuTextureView source, RenderPipeline pipeline, float offset) {
        float w = source.getWidth(0);
        float h = source.getHeight(0);

        MeshRenderer.begin()
                .attachments(target, null)
                .pipeline(pipeline)
                .fullscreen()
                .uniform("BlurData", BlurUniforms.write(w, h, offset))
                .sampler("u_Texture", source, RenderSystem.getSamplerCache().get(FilterMode.LINEAR))
                .end();
    }

    public void resizeIfNeeded() {
        int w = MinecraftClient.getInstance().getWindow().getFramebufferWidth();
        int h = MinecraftClient.getInstance().getWindow().getFramebufferHeight();

        if (w == lastWidth && h == lastHeight && fbos[0] != null) return;

        lastWidth = w;
        lastHeight = h;
        
        for (int i = 0; i < fbos.length; i++) {
            if (fbos[i] != null) fbos[i].close();

            double scale = 1 / Math.pow(2, i);
            int fw = (int) (w * scale);
            int fh = (int) (h * scale);
            if (fw < 1) fw = 1;
            if (fh < 1) fh = 1;

            fbos[i] = RenderSystem.getDevice().createTextureView(
                    RenderSystem.getDevice().createTexture(
                            "Blur Level " + i,
                            15,
                            TextureFormat.RGBA8,
                            fw, fh,
                            1,
                            1
                    )
            );
        }
    }
}
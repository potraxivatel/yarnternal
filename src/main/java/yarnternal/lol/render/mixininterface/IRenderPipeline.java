package yarnternal.lol.render.mixininterface;

/**
 * Добавка к ванильному {@code RenderPipeline}: флаг сглаживания линий.
 * Реализуется в {@code RenderPipelineMixin}, читается в GlCommandEncoderMixin.
 */
public interface IRenderPipeline {

    void yarn$setLineSmooth(boolean lineSmooth);

    boolean yarn$getLineSmooth();
}

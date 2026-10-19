package yarnternal.lol.render.renderer;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public abstract class YarnVertexFormats {
    public static final VertexFormat POS2 = VertexFormat.builder()
        .add("Position", YarnVertexFormatElements.POS2)
        .build();

    public static final VertexFormat POS2_COLOR = VertexFormat.builder()
        .add("Position", YarnVertexFormatElements.POS2)
        .add("Color", VertexFormatElement.COLOR)
        .build();

    public static final VertexFormat POS2_TEXTURE_COLOR = VertexFormat.builder()
        .add("Position", YarnVertexFormatElements.POS2)
        .add("Texture", VertexFormatElement.UV)
        .add("Color", VertexFormatElement.COLOR)
        .build();

    public static final VertexFormat POS2_COLOR_UV0_SIZE_RADIUS_SMOOTHNESS = VertexFormat.builder()
            .add("Position", YarnVertexFormatElements.POS2)
            .add("Color", VertexFormatElement.COLOR)
            .add("UV0", VertexFormatElement.UV0)
            .add("Size", YarnVertexFormatElements.SIZE)
            .add("Radius", YarnVertexFormatElements.RADIUS)
            .add("Smoothness", YarnVertexFormatElements.SMOOTHNESS)
            .build();

    public static final VertexFormat POS2_TEXTURE_COLOR_UV0_SIZE_RADIUS_SMOOTHNESS = VertexFormat.builder()
            .add("Position", YarnVertexFormatElements.POS2)
            .add("Color", VertexFormatElement.COLOR)
            .add("TexCoord", VertexFormatElement.UV)
            .add("GeoUV", YarnVertexFormatElements.GEO_UV)
            .add("Size", YarnVertexFormatElements.SIZE)
            .add("Radius", YarnVertexFormatElements.RADIUS)
            .add("Smoothness", YarnVertexFormatElements.SMOOTHNESS)
            .build();

    private YarnVertexFormats() {}
}

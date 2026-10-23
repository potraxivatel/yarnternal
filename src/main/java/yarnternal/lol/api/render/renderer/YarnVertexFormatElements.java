package yarnternal.lol.render.renderer;

import com.mojang.blaze3d.vertex.VertexFormatElement;

public abstract class YarnVertexFormatElements {
    public static final VertexFormatElement POS2 = VertexFormatElement.register(getNextVertexFormatElementId(), 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 2);

    public static final VertexFormatElement RADIUS = VertexFormatElement.register(
            getNextVertexFormatElementId(),
            0,
            VertexFormatElement.Type.FLOAT,
            VertexFormatElement.Usage.GENERIC,
            4
    );
    public static final VertexFormatElement SIZE = VertexFormatElement.register(
            getNextVertexFormatElementId(),
            0,
            VertexFormatElement.Type.FLOAT,
            VertexFormatElement.Usage.GENERIC,
            2
    );
    public static final VertexFormatElement SMOOTHNESS = VertexFormatElement.register(
            getNextVertexFormatElementId(),
            0,
            VertexFormatElement.Type.FLOAT,
            VertexFormatElement.Usage.GENERIC,
            1
    );

    public static final VertexFormatElement GEO_UV = VertexFormatElement.register(
            getNextVertexFormatElementId(),
            0,
            VertexFormatElement.Type.FLOAT,
            VertexFormatElement.Usage.GENERIC,
            2 // vec2
    );

    private YarnVertexFormatElements() {}

    private static int getNextVertexFormatElementId() {
        int id = 0;

        while (VertexFormatElement.byId(id) != null) {
            id++;

            if (id >= 32) {
                throw new RuntimeException("Too many mods registering VertexFormatElements");
            }
        }

        return id;
    }
}

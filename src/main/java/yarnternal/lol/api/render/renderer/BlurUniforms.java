package yarnternal.lol.render.renderer;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.gl.DynamicUniformStorage;

import java.nio.ByteBuffer;

public class BlurUniforms {
    private static final int SIZE = new Std140SizeCalculator()
            .putVec2()
            .putFloat()
            .get();

    private static final Data DATA = new Data();
    private static final DynamicUniformStorage<Data> STORAGE = new DynamicUniformStorage<>("Blur UBO", SIZE, 6);

    public static GpuBufferSlice write(float width, float height, float offset) {
        DATA.halfW = 0.5f / width;
        DATA.halfH = 0.5f / height;
        DATA.offset = offset;

        return STORAGE.write(DATA);
    }

    private static final class Data implements DynamicUniformStorage.Uploadable {
        float halfW, halfH, offset;

        @Override
        public void write(ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer)
                    .putVec2(halfW, halfH)
                    .putFloat(offset);
        }

        @Override
        public boolean equals(Object o) { return false; }
    }
}
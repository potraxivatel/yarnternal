package yarnternal.lol.common.ui.gui;

import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import yarnternal.lol.core.data.ProfileData;
import yarnternal.lol.render.renderer.Texture;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Ленивая загрузка аватарки из профиля в текстуру Minecraft.
 *
 * <p>Картинка кадрируется в квадрат, уменьшается до 128×128 и получает
 * скруглённые углы через альфа-маску, чтобы не выбиваться из стиля GUI.
 */
final class ProfileAvatar {

    private static final int SIZE = 128;
    private static final Identifier ID = Identifier.of("yarnternal", "profile/avatar");

    private static boolean attempted;
    private static Identifier textureId;

    private ProfileAvatar() {}

    /** Идентификатор готовой текстуры или {@code null}, если аватарку загрузить не удалось. */
    static Identifier texture() {
        if (!attempted) {
            attempted = true;
            textureId = load();
        }
        return textureId;
    }

    private static Identifier load() {
        try (InputStream stream = ProfileData.get().openAvatar()) {
            if (stream == null) {
                return null;
            }

            BufferedImage source = ImageIO.read(stream);
            if (source == null || source.getWidth() <= 0 || source.getHeight() <= 0) {
                return null;
            }

            int side = Math.min(source.getWidth(), source.getHeight());
            int sx = (source.getWidth() - side) / 2;
            int sy = (source.getHeight() - side) / 2;

            BufferedImage avatar = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = avatar.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(source, 0, 0, SIZE, SIZE,
                    sx, sy, sx + side, sy + side, null);

            // Скругляем углы: оставляем картинку только внутри маски.
            graphics.setComposite(AlphaComposite.DstIn);
            graphics.setColor(java.awt.Color.WHITE);
            int arc = Math.round(SIZE * 0.4f);
            graphics.fillRoundRect(0, 0, SIZE, SIZE, arc, arc);
            graphics.dispose();

            ByteBuffer pixels = ByteBuffer.allocateDirect(SIZE * SIZE * 4);
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    int argb = avatar.getRGB(x, y);
                    pixels.put((byte) (argb >> 16));
                    pixels.put((byte) (argb >> 8));
                    pixels.put((byte) argb);
                    pixels.put((byte) (argb >>> 24));
                }
            }
            pixels.flip();

            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null) {
                return null;
            }

            Texture texture = new Texture(SIZE, SIZE, TextureFormat.RGBA8,
                    FilterMode.LINEAR, FilterMode.LINEAR);
            texture.upload(pixels);
            client.getTextureManager().registerTexture(ID, texture);
            return ID;
        } catch (Exception e) {
            System.err.println("[yarnternal] avatar: " + e.getMessage());
            return null;
        }
    }
}

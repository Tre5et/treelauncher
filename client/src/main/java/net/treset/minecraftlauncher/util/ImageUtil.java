package net.treset.minecraftlauncher.util;

import javafx.scene.image.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.IntBuffer;

public class ImageUtil {
    public static Image getImage(BufferedImage img){
        //converting to a good type, read about types here: https://openjfx.io/javadoc/13/javafx.graphics/javafx/scene/image/PixelBuffer.html
        BufferedImage newImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
        newImg.createGraphics().drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);

        //converting the BufferedImage to an IntBuffer
        int[] type_int_agrb = ((DataBufferInt) newImg.getRaster().getDataBuffer()).getData();
        IntBuffer buffer = IntBuffer.wrap(type_int_agrb);

        //converting the IntBuffer to an Image, read more about it here: https://openjfx.io/javadoc/13/javafx.graphics/javafx/scene/image/PixelBuffer.html
        PixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbPreInstance();
        PixelBuffer<IntBuffer> pixelBuffer = new PixelBuffer<>(newImg.getWidth(), newImg.getHeight(), buffer, pixelFormat);
        return new WritableImage(pixelBuffer);
    }

    public static Image combine(Image base, Image top) {
        WritableImage combinedImage = new WritableImage(
                (int) base.getWidth(),
                (int) base.getHeight()
        );
        
        // Get the pixel readers for both images
        PixelReader baseReader = base.getPixelReader();
        PixelReader topReader = top.getPixelReader();

        // Get the pixel writer for the combined image
        PixelWriter writer = combinedImage.getPixelWriter();

        // Iterate over each pixel and overlay top on base
        for (int x = 0; x < top.getWidth(); x++) {
            for (int y = 0; y < top.getHeight(); y++) {
                // Get the color of the corresponding pixel in base
                int baseArgb = baseReader.getArgb(x, y);

                // Get the color of the corresponding pixel in top
                int topArgb = topReader.getArgb(x, y);

                // Extract the alpha component from top
                int topAlpha = (topArgb >> 24) & 0xFF;

                if (topAlpha != 0) {
                    // Extract the color components from base
                    int baseRed = (baseArgb >> 16) & 0xFF;
                    int baseGreen = (baseArgb >> 8) & 0xFF;
                    int baseBlue = baseArgb & 0xFF;
                    double baseAlpha = ((baseArgb >> 24) & 0xFF) / 255.0;

                    // Extract the color components from top
                    int topRed = (topArgb >> 16) & 0xFF;
                    int topGreen = (topArgb >> 8) & 0xFF;
                    int topBlue = topArgb & 0xFF;
                    double topAlphaNormalized = topAlpha / 255.0;

                    // Calculate the premultiplied color components
                    int premultipliedRed = (int) ((topRed * topAlphaNormalized) + (baseRed * (1 - topAlphaNormalized)) + 0.5);
                    int premultipliedGreen = (int) ((topGreen * topAlphaNormalized) + (baseGreen * (1 - topAlphaNormalized)) + 0.5);
                    int premultipliedBlue = (int) ((topBlue * topAlphaNormalized) + (baseBlue * (1 - topAlphaNormalized)) + 0.5);
                    double combinedAlpha = 1 - (1 - topAlphaNormalized) * (1 - baseAlpha);

                    // Calculate the combined ARGB
                    int combinedArgb = ((int) (combinedAlpha * 255) << 24) |
                            (premultipliedRed << 16) |
                            (premultipliedGreen << 8) |
                            premultipliedBlue;

                    // Write the combined pixel to the combined image
                    writer.setArgb(x, y, combinedArgb);
                } else {
                    // Write the pixel from base to the combined image
                    writer.setArgb(x, y, baseArgb);
                }
            }
        }
        return combinedImage;
    }

    public static Image rescale(Image input, int scaleFactor) {
        final int W = (int) input.getWidth();
        final int H = (int) input.getHeight();

        WritableImage output = new WritableImage(
                W * scaleFactor,
                H * scaleFactor
        );

        PixelReader reader = input.getPixelReader();
        PixelWriter writer = output.getPixelWriter();

        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                final int argb = reader.getArgb(x, y);
                for (int dy = 0; dy < scaleFactor; dy++) {
                    for (int dx = 0; dx < scaleFactor; dx++) {
                        writer.setArgb(x * scaleFactor + dx, y * scaleFactor + dy, argb);
                    }
                }
            }
        }

        return output;
    }
}

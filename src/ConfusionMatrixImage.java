import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConfusionMatrixImage {

    private BufferedImage image;
    private int pixelSize;

    /**
     *
     * @param confusionMatrix - numbers are expected to be in range [-1, 1]
     * @param pixelSize - square size in the resulting image dedicated to each element in confusion matrix
     */
    public ConfusionMatrixImage(List<List<Double>> confusionMatrix, int pixelSize) {
        this.pixelSize = pixelSize;
        int size = confusionMatrix.size();

        image = new BufferedImage(pixelSize*size, pixelSize*size, BufferedImage.TYPE_3BYTE_BGR);
        for (int row = 0; row < size; ++row) {
            for (int col = 0; col < size; ++col) {
                double value = confusionMatrix.get(row).get(col);
                int red =  255 - (int) (-255 * Math.min(0, value));
                int blue = 255 - (int) ( 255 * Math.max(0, value));
                int green = Math.min(red, blue);
                Color color = new Color(red, green, blue);
                for (int k = 0; k < pixelSize; ++k) {
                    for (int l = 0; l < pixelSize; ++l) {
                        image.setRGB(col*pixelSize+k, row*pixelSize+l, color.getRGB());
                    }
                }
            }
        }
    }

    public void saveImage(String filename) throws IOException {
        File outputFile = new File(filename);
        ImageIO.write(image, "jpg", outputFile);
    }

}

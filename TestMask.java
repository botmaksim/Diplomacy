import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestMask {
    public static void main(String[] args) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get("core/src/main/resources/data/map_data_europe_1900.json")));
        Set<String> jsonColors = new HashSet<>();
        Pattern p = Pattern.compile("\"hex_ID\"\\s*:\\s*\"([0-9a-fA-F]{6})\"");
        Matcher m = p.matcher(json);
        while (m.find()) {
            jsonColors.add(m.group(1).toUpperCase());
        }
        System.out.println("Colors in JSON: " + jsonColors.size());

        BufferedImage img = ImageIO.read(new File("desktop/src/main/resources/maps/map_mask_europe_1900.png"));
        Set<String> imgColors = new HashSet<>();
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int argb = img.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                if (a > 0) {
                    int r = (argb >> 16) & 0xFF;
                    int g = (argb >> 8) & 0xFF;
                    int b = argb & 0xFF;
                    imgColors.add(String.format("%02X%02X%02X", r, g, b));
                }
            }
        }
        System.out.println("Colors in Image: " + imgColors.size());

        Set<String> missing = new HashSet<>(jsonColors);
        missing.removeAll(imgColors);
        if (!missing.isEmpty()) {
            System.out.println("Colors in JSON but NOT in image: " + missing);
        } else {
            System.out.println("All JSON colors are present in the image!");
        }
    }
}

package projektzespolowy.utils;
import java.util.Random;

public class ColorGenerator {

    // Random object to be used for generating random numbers
    private static final Random random = new Random();

    public static String getRandomLightColor() {
        // Generate random numbers for RGB, each between 128 and 255
        int r = random.nextInt(128) + 128; // Red
        int g = random.nextInt(128) + 128; // Green
        int b = random.nextInt(128) + 128; // Blue

        // Format as a hexadecimal color string
        return String.format("#%02x%02x%02x", r, g, b);
    }


}
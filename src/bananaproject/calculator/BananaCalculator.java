package bananaproject.calculator;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Made by Alexander Söderberg, for "The Banana Lab"
 */
public class BananaCalculator {

    private static final char DEGREE_CHAR  = '\u00b0';

    private static Collection<String> getLines(String filePath) throws Exception {
        File file = new File("./", filePath);
        if (!file.exists() || !file.canRead()) {
            if (!file.createNewFile()) {
                throw new Exception("Invalid file");
            }
        }
        Collection<String> lines = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    public static void main(final String[] args) throws Exception {
        System.out.println("Thanks for using the BananaCalculator :D" +
                "\nMade by Alexander Söderberg\n");

        // Using atomic integers only to allow executing in
        // async streams
        final AtomicInteger globalRed = new AtomicInteger(0);
        final AtomicInteger globalGreen = new AtomicInteger(0);
        final AtomicInteger globalBlue = new AtomicInteger(0);
        final AtomicInteger globalHue = new AtomicInteger(0);
        final AtomicInteger globalSaturation = new AtomicInteger(0);
        final AtomicInteger globalValue = new AtomicInteger(0);

        // Load in the colours from text files
        final Collection<String> hexColours = getLines("rgb.txt");
        final Collection<String> hslColours = getLines("hsl.txt");
        final Collection<String> executionInfo = getLines("info.txt");

        // This will process the hexadecimal colours
        final Consumer<String> hexColourProcessor = colour -> {
            char[] characters = colour.toCharArray();
            int partialRed = Integer.valueOf(characters[1] + "" + characters[2], 16);
            int partialGreen = Integer.valueOf(characters[3]+ "" + characters[4], 16);
            int partialBlue = Integer.valueOf(characters[5] + "" + characters[6], 16);
            globalRed.addAndGet(partialRed);
            globalGreen.addAndGet(partialGreen);
            globalBlue.addAndGet(partialBlue);
        };

        // This will process the hsl colours
        final Consumer<String> hslColourProcessor = colour -> {
            String[] parts = colour.split(",");
            globalHue.addAndGet(Integer.parseInt(parts[0]));
            globalSaturation.addAndGet(Integer.parseInt(parts[1]));
            globalValue.addAndGet(Integer.parseInt(parts[2]));
        };

        // Loops (I love lambdas)
        hexColours.forEach(hexColourProcessor);
        hslColours.forEach(hslColourProcessor);

        // Calculate the averages
        int     aR = average(globalRed, hexColours.size()),
                aG = average(globalGreen, hexColours.size()),
                aB = average(globalBlue, hexColours.size()),
                aH = average(globalHue, hslColours.size()),
                aS = average(globalSaturation, hslColours.size()),
                aV = average(globalValue, hslColours.size());

        // Format the hex colour
        String averageColour = ("#" + Integer.toHexString(aR) + Integer.toHexString(aG) + Integer.toHexString(aB))
                .toUpperCase();

        System.out.printf("HTML:\n\tAverages: hex colour: %s, red: %d, green: %d, blue: %d\n",
                averageColour, aR, aG, aB);
        System.out.print("HSL:\n\tAverages: hue: " + aH + "" + DEGREE_CHAR + ", saturation: " + aS + "%, " +
                "value: " + aV + "%\n");

        File file = new File("output.txt");
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new RuntimeException("Couldn't create output file");
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            executionInfo.forEach(str -> {
                try {
                    writer.write(str + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            writer.write(String.format(
                    "HTML:\n\tAverages: hex colour: %s, red: %d, green: %d, blue: %d\n",
                            averageColour, aR, aG, aB
            ));
            writer.write(
                    "HSL:\n\tAverages: hue: " + aH + "" + DEGREE_CHAR + ", saturation: " + aS + "%, " +
                            "value: " + aV + "%\n"
            );
        }

        System.out.println("\nAll info has been written to output.txt");
    }

    private static int average(AtomicInteger sum, int n) {
        if (n == 0) {
            return 0;
        }
        return sum.get() / n;
    }
}

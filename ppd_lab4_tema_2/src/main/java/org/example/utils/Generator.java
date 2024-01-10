package org.example.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class Generator {
    public static void generateData() {
        for (int c = 1; c <= 5; c++) {
            int participantsSize = 80 + new Random().nextInt(20);
            for (int pr = 1; pr <= 10; pr++) {
                String fileName = String.format("ppd_lab5_tema/src/main/resources/data/C%d_P%d.txt", c, pr);
                Path filePath = Paths.get(fileName);

                try (BufferedWriter bufferedWriter = Files.newBufferedWriter(filePath)) {
                    for (int k = 1; k <= participantsSize; k++) {
                        int score = new Random().nextInt(50) == 4 ? -1 : new Random().nextInt(11);
                        bufferedWriter.write(String.format("%d %d\n", 100 * c + k, score));
                    }
                } catch (IOException e) {
                    System.out.println("Error generating: " + e.getMessage());
                }
            }
        }
    }
}

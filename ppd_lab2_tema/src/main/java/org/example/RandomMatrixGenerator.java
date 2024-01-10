package org.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class RandomMatrixGenerator {
    public static void main(String[] args) throws IOException {
        int n = 10000; // Number of rows
        int m = 10000; // Number of columns

        int[][] matrix = generateRandomMatrix(n, m);

        write("C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\ppd_lab2_tema\\src\\main\\resources\\marimi.txt", matrix);
    }

    public static int[][] generateRandomMatrix(int n, int m) {
        int[][] matrix = new int[n][m];
        Random random = new Random();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                // Generate a random integer between 0 and 100 (adjust as needed)
                matrix[i][j] = random.nextInt(101);
            }
        }
        return matrix;
    }

    public static void write(String output, int[][] matrix) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
            int rows = 10000;
            int cols = 10000;

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    writer.write(matrix[i][j] + " ");
                }
                writer.newLine(); // Move to the next line
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


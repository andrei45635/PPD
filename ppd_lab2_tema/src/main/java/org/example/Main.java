package org.example;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Main {
    private static int[][] matrix;
    private static int[][] kernel;
    private static int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private static int m = 0;
    private static int n = 0;
    private static int N = 0;
    private static int M = 0;
    private static CyclicBarrier cyclicBarrier;

    public static void main(String[] args) throws IOException, InterruptedException {
        String filename_large = "C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\ppd_lab2_tema\\src\\main\\resources\\marimi.txt";
        String filename = "C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\ppd_lab2_tema\\src\\main\\resources\\date.txt";
        String output = "C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\ppd_lab2_tema\\src\\main\\resources\\output_large.txt";
        String synchronized_output = "C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\ppd_lab2_tema\\src\\main\\resources\\synchronized_output.txt";
        String test_output = "C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\ppd_lab2_tema\\src\\main\\resources\\test_output.txt";

        read(filename);

        THREAD_COUNT = Integer.parseInt(args[0]);

        cyclicBarrier = new CyclicBarrier(THREAD_COUNT);

        parallel();

        if(THREAD_COUNT == 1){
            write(synchronized_output);
        } else {
            write(synchronized_output);
        }

        System.out.println("Sync and Async outputs are the same: " + sameContent(test_output, synchronized_output));
    }

    private static boolean sameContent(String file1, String file2) throws IOException {
        return Files.mismatch(Path.of(file1), Path.of(file2)) == -1;
    }

    public static void read(String filename) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String[] dimensions = br.readLine().split(" ");
            N = Integer.parseInt(dimensions[0]);
            M = Integer.parseInt(dimensions[1]);

            matrix = new int[N][M];

            for (int i = 0; i < N; i++) {
                String[] row = br.readLine().split(" ");
                for (int j = 0; j < M; j++) {
                    matrix[i][j] = Integer.parseInt(row[j]);
                }
            }

            dimensions = br.readLine().split(" ");
            n = Integer.parseInt(dimensions[0]);
            m = Integer.parseInt(dimensions[1]);

            kernel = new int[n][m];

            for (int i = 0; i < n; i++) {
                String[] row = br.readLine().split(" ");
                for (int j = 0; j < m; j++) {
                    kernel[i][j] = Integer.parseInt(row[j]);
                }
            }
            br.close();
        } catch (FileNotFoundException fe) {
            System.out.println("Error: " + fe.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    public static void write(String output) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(output));
            for (int[] elem : matrix) {
                for (int i : elem) {
                    bw.write(i + " ");
                }
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void parallel() throws InterruptedException {
        int start = 1, end;
        int cat = N / THREAD_COUNT;
        int rest = N % THREAD_COUNT;

        MyThread[] threads = new MyThread[THREAD_COUNT];

        long startTime = System.nanoTime();

        for (int i = 0; i < THREAD_COUNT; i++) {
            end = start + cat;
            if (rest > 0) {
                end++;
                rest--;
            }
            threads[i] = new MyThread(start, end);
            threads[i].start();
            start = end;
        }

        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i].join();
        }

        long endTime = System.nanoTime();
        System.out.println((double) (endTime - startTime) / 1E6);
    }

    public static int convolution(int[] frontier, int matrixCol, int kernelRow){
        int leftValue = frontier[Math.max(matrixCol - 1, 0)];
        int centerValue = frontier[matrixCol];
        int rightValue = frontier[Math.min(M - 1, matrixCol + 1)];

        // Get the corresponding kernel coefficients
        int kernelLeft = kernel[kernelRow][0];
        int kernelCenter = kernel[kernelRow][1];
        int kernelRight = kernel[kernelRow][2];

        // Perform the convolution and return the result
        return leftValue * kernelLeft + centerValue * kernelCenter + rightValue * kernelRight;
    }

    public static class MyThread extends Thread {
        private final int start;
        private final int end;

        public MyThread(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            int[] prev = new int[M];
            int[] curr = new int[M];
            int[] frontierNorth = new int[M];
            int[] frontierSouth = new int[M];

            System.arraycopy(matrix[Math.max(start - 1, 0)], 0, prev, 0, M);
            System.arraycopy(matrix[start], 0, curr, 0, M);

            for (int i = start; i < end; i++) {
                for (int j = 0; j < M; j++) {
                    int res = convolution(prev, j, 0) + convolution(curr, j, 1) + convolution(matrix[Math.min(N - i, i + 1)], j, 2);

                    if (i == end - 1) {
                        frontierNorth[j] = res;
                    } else if (i == start) {
                        frontierSouth[j] = res;
                    } else {
                        matrix[i][j] = res;
                    }
                }
                System.arraycopy(curr, 0, prev, 0, curr.length);
                System.arraycopy(matrix[Math.min(N - 1, i + 1)], 0, curr, 0, curr.length);
            }

            try {
                cyclicBarrier.await();
                System.arraycopy(frontierNorth, 0, matrix[start], 0, M);
                System.arraycopy(frontierSouth, 0, matrix[end - 1], 0, M);
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
        }
    }
}



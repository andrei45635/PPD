package org.example;

import java.io.*;

public class Main {
    private static int[][] matrix;
    private static int[][] kernel;
    private static int[][] result;
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private static int m = 0, n = 0, N = 0, M = 0, lineOffset = 0, columnOffset;

    public static void main(String[] args) throws IOException, InterruptedException {
        //String filename = "ppd_lab1_tema/src/main/resources/date.txt";
        String filename = "C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\ppd_lab1_tema\\src\\main\\resources\\date.txt";
        //String output = "ppd_lab1_tema/src/main/resources/output.txt";
        String output = "C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\ppd_lab1_tema\\src\\main\\resources\\output.txt";

        read(filename);

        lineOffset = (n - 1) / 2;
        columnOffset = (m - 1) / 2;

        int p = Integer.parseInt(args[0]);
        //THREAD_COUNT = p;

        if(p == 1){
            sequential();
        } else {
            parallel();
        }

        write(output);
    }

    public static void read(String filename) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String[] dimensions = br.readLine().split(" ");
            N = Integer.parseInt(dimensions[0]);
            M = Integer.parseInt(dimensions[1]);

            matrix = new int[N][M];
            result = new int[N][M];

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

    public static void write(String output) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                stringBuilder.append(result[i][j]);
                if (j < M - 1) {
                    stringBuilder.append(" ");
                }
            }
            stringBuilder.append("\n");
        }
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(output));
        bufferedWriter.write(stringBuilder.toString());
        bufferedWriter.close();
    }

    private static void sequential() {
        long startTime = System.nanoTime();

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                result[i][j] = convolution(i, j);
            }
        }

        long endTime = System.nanoTime();
        System.out.println("Sequential time: " + (double)(endTime - startTime) / 1E6);
    }

    private static void parallel() throws InterruptedException {
        int start = 0, end;
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
            threads[i] = new MyThread(start, end, result);
            threads[i].start();
            start = end;
        }

        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i].join();
        }

        long endTime = System.nanoTime();
        System.out.println((double)(endTime - startTime) / 1E6);
    }

    private static int convolution(int x, int y) {
        int output = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                //calculating neighbors
                int ii = x - lineOffset + i;
                int jj = y - columnOffset + j;

                //out of bounds - line
                if (ii < 0) ii = 0;
                else if (ii >= N) ii = N - 1;

                //out of bounds - column
                if (jj < 0) jj = 0;
                else if (jj >= M) jj = M - 1;

                output += matrix[ii][jj] * kernel[i][j];
            }
        }
        return output;
    }

    public static class MyThread extends Thread {
        private final int start;
        private final int end;
        private final int[][] result;

        public MyThread(int start, int end, int[][] result) {
            this.start = start;
            this.end = end;
            this.result = result;
        }

        @Override
        public void run() {
            for (int i = start; i < end; i++) {
                for (int j = 0; j < M; j++) {
                    this.result[i][j] = convolution(i, j);
                }
            }
//            for (int i = 0; i < N; i++) {
//                for (int j = start; j < end; j++) {
//                    this.result[i][j] = convolution(i, j);
//                }
//            }
//            if (N > M) {
//                for (int i = start; i < end; i++) {
//                    for (int j = 0; j < M; j++) {
//                        this.result[i][j] = convolution(i, j);
//                    }
//                }
//            } else {
//                for (int i = 0; i < N; i++) {
//                    for (int j = start; j < end; j++) {
//                        this.result[i][j] = convolution(i, j);
//                    }
//                }
//            }
        }
    }
}



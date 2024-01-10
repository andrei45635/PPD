package org.example;

import org.example.parallel.Parallel;
import org.example.sequential.Sequential;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    private static boolean sameContent(String file1, String file2) throws IOException {
        return Files.mismatch(Path.of(file1), Path.of(file2)) == -1;
    }

    public static void main(String[] args) throws IOException {
        //Generator.generateData();
        String dataDir = "C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\ppd_lab4_tema_2\\src\\main\\resources\\data";
        String seqFile = "C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\ppd_lab4_tema_2\\src\\main\\resources\\results.txt";
        String parallelFile = "C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\ppd_lab4_tema_2\\src\\main\\resources\\results.txt";
        String parallelWritingFile = "C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\ppd_lab4_tema_2\\src\\main\\resources\\parallel_results.txt";
        if (args[0].equals("seq")) {
            long startTime = System.nanoTime();
            Sequential sequential = new Sequential();
            sequential.sequential(dataDir);
            sequential.write(seqFile);
            long endTime = System.nanoTime();
            System.out.println((double) (endTime - startTime) / 1E6);
        } else {
            long startTime = System.nanoTime();
            int pr = Integer.parseInt(args[0]);
            int pw = Integer.parseInt(args[1]);
            Parallel parallel = new Parallel(pw, pr);
            try {
                parallel.parallel();
            } catch (RuntimeException re) {
                System.out.println(re.getMessage());
            }
            parallel.write(parallelWritingFile);
            long endTime = System.nanoTime();
            System.out.println((double) (endTime - startTime) / 1E6);
        }
        System.out.println("Files are the same: " + sameContent(seqFile, parallelFile));
    }
}
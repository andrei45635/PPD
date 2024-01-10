package org.example.sequential;

import org.example.list.MyLinkedList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;

public class Sequential {
    private final MyLinkedList participantsList;

    public Sequential() {
        this.participantsList = new MyLinkedList(false);
    }

    public void sequential(String directoryPath) {
        File dir = new File(directoryPath);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                try (Stream<String> lines = Files.lines(file.toPath())) {
                    List<String[]> ls = lines.map(line -> line.split(" ")).toList();
                    for(String[] l: ls){
                        int participantID = Integer.parseInt(l[0]);
                        int score = Integer.parseInt(l[1]);
                        participantsList.addScore(participantID, score);
                    }
                } catch (IOException e) {
                    System.out.println("Error in sequential run: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void write(String resultPath){
        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(resultPath, true))){
            bufferedWriter.write(participantsList.toString());
        } catch (IOException e) {
            System.out.println("Error when writing: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}

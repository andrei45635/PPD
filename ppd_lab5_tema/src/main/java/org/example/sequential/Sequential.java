package org.example.sequential;

import org.example.list.MyLinkedList;
import org.example.list.Node;

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
                        String country = file.toPath().toString().substring(73,75);
                        System.out.println(participantID + " " + score + " " + country);
                        Node poppedNode = new Node(participantID, score, country);
                        if (poppedNode.getScore() == -1) {
                            participantsList.deleteNode(poppedNode.getParticipantID());
                        } else {
                            Node target = participantsList.updateNode(poppedNode.getParticipantID(), poppedNode.getScore(), poppedNode.getCountry());
                            if(target == null){
                                participantsList.addNode(poppedNode.getParticipantID(), poppedNode.getScore(), poppedNode.getCountry());
                            }
                        }
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

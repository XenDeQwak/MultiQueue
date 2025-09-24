package com.xen.multiqueue;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class MQController {
    @FXML private TableView<Process> processTable;
    @FXML private TableColumn<Process, String> pidCol;
    @FXML private TableColumn<Process, Number> prioCol;
    @FXML private TableColumn<Process, Number> burstCol;
    @FXML private TableColumn<Process, Number> remainCol;
    @FXML private TextArea logArea;

    private ObservableList<Process> processes;


    @FXML
    public void initialize() {
        processes = FXCollections.observableArrayList();
        processTable.setItems(processes);

        pidCol.setCellValueFactory(data -> data.getValue().pidProperty());
        prioCol.setCellValueFactory(data -> data.getValue().priorityProperty());
        burstCol.setCellValueFactory(data -> data.getValue().burstTimeProperty());
        remainCol.setCellValueFactory(data -> data.getValue().remainingTimeProperty());
    }

    @FXML
    private void onAddProcess() {
        Process p = new Process("P" + (processes.size() + 1), 10, processes.size() % 3);
        processes.add(p);
    }

    @FXML
    private void onStart() {
        MultiQueueScheduler scheduler;
        int[] quantums = {2, 4, 6};
        scheduler = new MultiQueueScheduler(2, quantums, logArea);

        for (Process p : processes) {
            scheduler.addProcess(new Process(p.getPid(), p.getBurstTime(), p.getPriority()));
        }

        new Thread(scheduler::schedule).start();
    }
}

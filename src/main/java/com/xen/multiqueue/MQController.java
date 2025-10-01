package com.xen.multiqueue;

import com.xen.multiqueue.models.Process;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.util.converter.IntegerStringConverter;

public class MQController {

    @FXML private TableView<Process> processTable;
    @FXML private TableColumn<Process, String> pidCol;
    @FXML private TableColumn<Process, Integer> prioCol;
    @FXML private TableColumn<Process, Integer> burstCol;
    @FXML private TableColumn<Process, Integer> remainCol;
    @FXML private TableColumn<Process, Integer> arrivalCol;
    @FXML private TextField agingField;
    @FXML private TextField deagingField;
    @FXML private TextField q1Field;
    @FXML private TextField q2Field;
    @FXML private TextField q3Field;

    @FXML private TextArea logArea;
    @FXML private HBox queue1Box;
    @FXML private HBox queue2Box;
    @FXML private HBox queue3Box;
    @FXML private HBox cpuBox;

    private ObservableList<Process> processes;
    private RoundRobinScheduler scheduler;

    @FXML
    public void initialize() {
        pidCol.setCellValueFactory(data -> data.getValue().pidProperty());
        prioCol.setCellValueFactory(data -> data.getValue().priorityProperty().asObject());
        burstCol.setCellValueFactory(data -> data.getValue().burstTimeProperty().asObject());
        remainCol.setCellValueFactory(data -> data.getValue().remainingTimeProperty().asObject());
        arrivalCol.setCellValueFactory(data -> data.getValue().arrivalTimeProperty().asObject());

        processTable.setEditable(true);

        prioCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        burstCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        arrivalCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        prioCol.setOnEditCommit(e -> {
            e.getRowValue().setPriority(e.getNewValue());
            updateVisualizer();
        });
        burstCol.setOnEditCommit(e -> {
            Process p = e.getRowValue();
            p.setRemainingTime(e.getNewValue());
            p.burstTimeProperty().set(e.getNewValue());
            updateVisualizer();
        });
        arrivalCol.setOnEditCommit(e -> {
            e.getRowValue().setArrivalTime(e.getNewValue());
            updateVisualizer();
        });
    }

    @FXML
    private void onAddProcess() {
        if (processes == null) {
            processes = FXCollections.observableArrayList();
            processTable.setItems(processes);
        }

        Process p = new Process("P" + (processes.size() + 1), 0, 1);
        p.setArrivalTime(0);

        processes.add(p);
        updateVisualizer();
    }


    @FXML
    private void onStart() {
        try {
            int aging = Integer.parseInt(agingField.getText());
            scheduler = getRoundRobinScheduler(aging);

            for (Process p : processes) {
                Process clone = new Process(p.getPid(), p.getBurstTime(), p.getPriority());
                clone.setArrivalTime(p.getArrivalTime());
                scheduler.addProcess(clone);
            }

            new Thread(scheduler::schedule).start();

        } catch (NumberFormatException e) {
            showAlert("Enter valid integers for aging, de-aging, and quantums.");
        }
    }

    private RoundRobinScheduler getRoundRobinScheduler(int aging) {
        int deaging = Integer.parseInt(deagingField.getText());
        int q1 = Integer.parseInt(q1Field.getText());
        int q2 = Integer.parseInt(q2Field.getText());
        int q3 = Integer.parseInt(q3Field.getText());

        int[] quantums = {q1, q2, q3};
        RoundRobinScheduler getScheduler = new RoundRobinScheduler(quantums, logArea);
        getScheduler.setAgingTime(aging);
        getScheduler.setDeAgingTime(deaging);

        getScheduler.setVisualizerUpdate(() -> Platform.runLater(this::updateVisualizer));
        getScheduler.setCpuUpdate(pid -> Platform.runLater(() -> showInCpu(pid)));
        return getScheduler;
    }

    private void updateVisualizer() {
        queue1Box.getChildren().clear();
        queue2Box.getChildren().clear();
        queue3Box.getChildren().clear();

        if (scheduler == null) return;

        for (int i = 0; i < 3; i++) {
            ObservableList<Label> labels = FXCollections.observableArrayList();
            for (Process p : scheduler.getAllProcessesInQueue(i)) {
                if (p.getCurrentQueueIndex() == -1) continue;

                Label procBox = new Label(p.getPid());
                procBox.setStyle("-fx-border-color:black; -fx-padding:5; -fx-background-color:lightblue;");
                labels.add(procBox);
            }
            switch (i) {
                case 0 -> queue1Box.getChildren().addAll(labels);
                case 1 -> queue2Box.getChildren().addAll(labels);
                case 2 -> queue3Box.getChildren().addAll(labels);
            }
        }
    }


    private void showInCpu(String pid) {
        cpuBox.getChildren().clear();
        if (pid != null) {
            Label procBox = new Label(pid);
            procBox.setStyle("-fx-border-color:orange; -fx-padding:5; -fx-background-color:lightyellow;");
            cpuBox.getChildren().add(procBox);
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}

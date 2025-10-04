package com.xen.multiqueue;

import com.xen.multiqueue.models.Process;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.util.converter.IntegerStringConverter;

public class MQController {

    @FXML private TableView<Process> processTable;
    @FXML private TableColumn<Process, String> pidCol;
    @FXML private TableColumn<Process, Integer> prioCol;
    @FXML private TableColumn<Process, Integer> burstCol;
    @FXML private TableColumn<Process, Integer> arrivalCol;
    @FXML private TextField agingField;
    @FXML private TextField deagingField;
    @FXML private TextField q1Field;
    @FXML private TextField q2Field;
    @FXML private TextField q3Field;
    @FXML private TextField q4Field;

    @FXML private TextArea logArea;
    @FXML private HBox queue1Box;
    @FXML private HBox queue2Box;
    @FXML private HBox queue3Box;
    @FXML private HBox queue4Box;
    @FXML private HBox cpuBox;

    private ObservableList<Process> processes;
    private RoundRobinScheduler scheduler;

    @FXML
    public void initialize() {
        pidCol.setCellValueFactory(data -> data.getValue().pidProperty());
        prioCol.setCellValueFactory(data -> data.getValue().priorityProperty().asObject());
        burstCol.setCellValueFactory(data -> data.getValue().burstTimeProperty().asObject());
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

        //deafult values
        processes = FXCollections.observableArrayList(
            new Process("P1",20,4),
            new Process("P2",10,2),
            new Process("P3",2,1),
            new Process("P4",7,2),
            new Process("P5",15,3),
            new Process("P6",8,2),
            new Process("P7",4,1)
        );

        processes.get(0).setArrivalTime(1);
        processes.get(1).setArrivalTime(3);
        processes.get(2).setArrivalTime(5);
        processes.get(3).setArrivalTime(8);
        processes.get(4).setArrivalTime(11);
        processes.get(5).setArrivalTime(15);
        processes.get(6).setArrivalTime(20);

        for (Process p : processes){
            p.setRemainingTime(p.getBurstTime());
        }

        processTable.setItems(processes);
        updateVisualizer();
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
            showAlert("Enter valid integers for aging, de-aging, and quantumTime.");
        }
    }

    private RoundRobinScheduler getRoundRobinScheduler(int aging) {
        int deaging = Integer.parseInt(deagingField.getText());
        int q1 = Integer.parseInt(q1Field.getText());
        int q2 = Integer.parseInt(q2Field.getText());
        int q3 = Integer.parseInt(q3Field.getText());
        int q4 = Integer.parseInt(q4Field.getText());

        int[] quantumTime = {q1, q2, q3, q4};
        RoundRobinScheduler getScheduler = new RoundRobinScheduler(quantumTime, logArea);
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
        queue4Box.getChildren().clear();

        if (scheduler == null) return;

        for (int i = 0; i < 4; i++) {
            ObservableList<Label> labels = FXCollections.observableArrayList();
            for (Process p : scheduler.getAllProcessesInQueue(i)) {
                if (p.getCurrentQueueIndex() == -1) continue;

                String color;

                switch(p.getPriority()) {
                    case 1 -> color = "lightgreen";
                    case 2 -> color = "lightyellow";
                    case 3 -> color = "tomato";
                    case 4 -> color = "red";
                    default -> color = "lightblue";
                }

                Label procBox = new Label(p.getPid());
                procBox.setStyle("-fx-border-color: black; " +
                        "-fx-padding: 5; " +
                        "-fx-background-color: " +
                        color + "; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5;");
                labels.add(procBox);
            }
            switch (i) {
                case 0 -> queue1Box.getChildren().addAll(labels);
                case 1 -> queue2Box.getChildren().addAll(labels);
                case 2 -> queue3Box.getChildren().addAll(labels);
                case 3 -> queue4Box.getChildren().addAll(labels);
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

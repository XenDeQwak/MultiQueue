package com.xen.multiqueue;

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
    @FXML private HBox queue1Box, queue2Box, queue3Box, cpuBox;

    private ObservableList<Process> processes;

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

        prioCol.setOnEditCommit(e -> e.getRowValue().setPriority(e.getNewValue()));
        burstCol.setOnEditCommit(e -> {
            Process p = e.getRowValue();
            p.setRemainingTime(e.getNewValue());
            p.burstTimeProperty().set(e.getNewValue());
        });
        arrivalCol.setOnEditCommit(e -> e.getRowValue().setArrivalTime(e.getNewValue()));
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
            int deaging = Integer.parseInt(deagingField.getText());
            int q1 = Integer.parseInt(q1Field.getText());
            int q2 = Integer.parseInt(q2Field.getText());
            int q3 = Integer.parseInt(q3Field.getText());

            int[] quantums = {q1, q2, q3};
            RoundRobinScheduler scheduler = new RoundRobinScheduler(2, quantums, logArea);
            scheduler.setAgingTime(aging);
            scheduler.setDeAgingTime(deaging);

            scheduler.setVisualizerUpdate(() -> Platform.runLater(this::updateVisualizer));
            scheduler.setCpuUpdate(pid -> Platform.runLater(() -> showInCpu(pid)));

            for (Process p : processes) {
                scheduler.addProcess(new Process(p.getPid(), p.getBurstTime(), p.getPriority()));
            }

            new Thread(scheduler::schedule).start();

        } catch (NumberFormatException e) {
            showAlert("Enter valid integers for aging, de-aging, and quantums.");
        }
    }

    private void updateVisualizer() {
        queue1Box.getChildren().clear();
        queue2Box.getChildren().clear();
        queue3Box.getChildren().clear();

        for (Process p : processes) {
            Label procBox = new Label(p.getPid());
            procBox.setStyle("-fx-border-color:black; -fx-padding:5; -fx-background-color:lightblue;");
            switch (p.getPriority()) {
                case 1 -> queue1Box.getChildren().add(procBox);
                case 2 -> queue2Box.getChildren().add(procBox);
                case 3 -> queue3Box.getChildren().add(procBox);
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

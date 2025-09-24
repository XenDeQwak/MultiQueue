package com.xen.multiqueue;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class MultiQueueScheduler {
    private RRLogic[] queues;
    private int cpuQuantum;
    private int currentQueueIndex = 0;
    private TextArea logArea;

    public MultiQueueScheduler(int cpuQuantum, int[] quantums, TextArea logArea) {
        this.cpuQuantum = cpuQuantum;
        this.logArea = logArea;
        this.queues = new RRLogic[quantums.length];
        for (int i = 0; i < quantums.length; i++) {
            queues[i] = new RRLogic(quantums[i]);
        }
    }

    public void addProcess(Process p) {
        queues[p.getPriority()].addProcess(p);
    }

    public void schedule() {
        while (true) {
            boolean allEmpty = true;
            for (RRLogic q : queues) {
                if (!q.isEmpty()) {
                    allEmpty = false;
                    break;
                }
            }
            if (allEmpty) break;

            RRLogic q = queues[currentQueueIndex];
            if (!q.isEmpty()) {
                Process p = q.getNextProcess();
                int slice = Math.min(p.getRemainingTime(), cpuQuantum);
                p.setRemainingTime(p.getRemainingTime() - slice);

                Platform.runLater(() -> logArea.appendText("Running " + p.getPid() + " from Q" + currentQueueIndex + " for " + slice + " units\n"));

                if (p.getRemainingTime() > 0) {
                    q.requeueProcess(p);
                } else {
                    Platform.runLater(() -> logArea.appendText(p.getPid() + " finished\n"));
                }
            }
            currentQueueIndex = (currentQueueIndex + 1) % queues.length;
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
    }
}



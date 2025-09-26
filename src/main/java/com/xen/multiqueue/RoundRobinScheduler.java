package com.xen.multiqueue;

import javafx.scene.control.TextArea;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RoundRobinScheduler {
    private RoundRobin[] queues;
    private int cpuQuantum;
    private int currentQueueIndex = 0;
    private TextArea logArea;
    private Runnable visualizerUpdate;
    private Consumer<String> cpuUpdate;
    private int agingTimeUnit;
    private int deAgingTimeUnit;

    public RoundRobinScheduler(int cpuQuantum, int[] quantums, TextArea logArea) {
        this.cpuQuantum = cpuQuantum;
        this.logArea = logArea;
        this.queues = new RoundRobin[quantums.length];
        for (int i = 0; i < quantums.length; i++) {
            queues[i] = new RoundRobin(quantums[i]);
        }
    }

    public void addProcess(Process p) {
        queues[p.getPriority() - 1].addProcess(p);
    }

    public void schedule() {
        int time = 0;

        while (true) {
            boolean allEmpty = true;
            for (RoundRobin q : queues) {
                if (!q.isEmpty()) {
                    allEmpty = false;
                    break;
                }
            }
            if (allEmpty) break;

            applyAging();

            RoundRobin q = queues[currentQueueIndex];
            if (!q.isEmpty()) {
                Process p = q.getNextProcess();
                notifyCpu(p.getPid());
                notifyVisualizer();

                int slice = Math.min(p.getRemainingTime(), cpuQuantum);
                p.setRemainingTime(p.getRemainingTime() - slice);

                try { Thread.sleep(slice * 500L); } catch (InterruptedException ignored) {}
                time += slice;

                if (p.getRemainingTime() > 0) {
                    q.requeueProcess(p);
                }
                notifyVisualizer();
            }

            currentQueueIndex = (currentQueueIndex + 1) % queues.length;
            try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        }

        notifyCpu(null);
    }

    private void applyAging() {
        for (int i = queues.length - 1; i > 0; i--) {
            List<Process> toPromote = new ArrayList<>();
            for (Process p : queues[i].getAllProcesses()) {
                int newWait = p.getArrivalTime() + 1;
                p.setArrivalTime(newWait);
                if (newWait >= agingTimeUnit) {
                    p.setArrivalTime(0);
                    p.setPriority(p.getPriority() - 1);
                    toPromote.add(p);
                }
            }
            for (Process p : toPromote) {
                queues[i].removeProcess(p);
                queues[p.getPriority() - 1].addProcess(p);
                logArea.appendText(p.getPid() + " aged up to queue " + p.getPriority() + "\n");
            }
        }
    }

    public void setAgingTime(int t) { this.agingTimeUnit = t; }
    public void setDeAgingTime(int t) { this.deAgingTimeUnit = t; }

    public void setVisualizerUpdate(Runnable update) {
        this.visualizerUpdate = update;
    }

    public void setCpuUpdate(Consumer<String> update) {
        this.cpuUpdate = update;
    }

    private void notifyVisualizer() {
        if (visualizerUpdate != null) visualizerUpdate.run();
    }

    private void notifyCpu(String pid) {
        if (cpuUpdate != null) cpuUpdate.accept(pid);
    }
}

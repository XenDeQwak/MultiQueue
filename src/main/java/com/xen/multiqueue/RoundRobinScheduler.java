package com.xen.multiqueue;

import com.xen.multiqueue.models.Process;
import com.xen.multiqueue.models.RoundRobin;
import javafx.scene.control.TextArea;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RoundRobinScheduler {
    private final RoundRobin[] queues;
    private final List<Process> pending = new ArrayList<>();
    private final TextArea logArea;
    private Runnable visualizerUpdate;
    private Consumer<String> cpuUpdate;
    private int agingTimeUnit;
    private int deAgingTimeUnit;
    private int globalTime = 0;

    public RoundRobinScheduler(int[] quantums, TextArea logArea) {
        this.logArea = logArea;
        this.queues = new RoundRobin[quantums.length];
        for (int i = 0; i < quantums.length; i++) queues[i] = new RoundRobin(quantums[i]);
    }

    public void addProcess(Process p) {
        pending.add(p);
    }

    public void schedule() {
        globalTime = 0;

        while (!allQueuesEmpty() || !pending.isEmpty()) {
            globalTime++;

            releaseArrivedProcesses();
            applyAgingForAllWaiting();

            RoundRobin queueToRun = getHighestPriorityNonEmptyQueue();
            if (queueToRun == null) {
                logCPUidle();
                continue;
            }

            Process p = queueToRun.getNextProcess();
            p.setRunning(true);
            notifyCpu(p.getPid());

            p.setRemainingTime(p.getRemainingTime() - 1);
            p.incrementCpuTime(1);

            if (logArea != null) {
                logArea.appendText("t=" + globalTime + ": " + p.getPid() + " running (1 TU)\n");
                logStatus(p);
            }

            incrementWaitingTimesForAllWaitingExcept(p);

            if (deAgingTimeUnit > 0 && p.getCpuTimeUsed() >= deAgingTimeUnit && p.getPriority() < queues.length) {
                queueToRun.removeProcess(p);
                p.setPriority(p.getPriority() + 1);
                queues[p.getPriority() - 1].addProcess(p);
                p.setCurrentQueueIndex(p.getPriority() - 1);
                p.resetCpuTime();
                if (logArea != null)
                    logArea.appendText("t=" + globalTime + ": " + p.getPid() + " de-aged to queue " + p.getPriority() + "\n");
                notifyVisualizer();
                p.setRunning(false);
                continue;
            }

            if (p.getRemainingTime() <= 0) {
                queueToRun.removeProcess(p);
                p.setCurrentQueueIndex(-1);
                if (logArea != null)
                    logArea.appendText("t=" + globalTime + ": " + p.getPid() + " finished\n");
            } else {
                queueToRun.requeueProcess(p);
            }

            p.setRunning(false);
            notifyVisualizer();
            sleep(500);
        }

        notifyCpu(null);
    }

    private RoundRobin getHighestPriorityNonEmptyQueue() {
        for (RoundRobin q : queues) {
            if (!q.isEmpty()) return q;
        }
        return null;
    }

    private void releaseArrivedProcesses() {
        List<Process> toRelease = new ArrayList<>();
        for (Process p : new ArrayList<>(pending)) {
            if (p.getArrivalTime() <= globalTime) {
                p.resetWaitingTime();
                p.resetCpuTime();
                queues[p.getPriority() - 1].addProcess(p);
                p.setCurrentQueueIndex(p.getPriority() - 1);
                if (logArea != null) {
                    logArea.appendText("t=" + globalTime + ": " + p.getPid() + " arrived in queue " + p.getPriority() + "\n");
                    logStatus(p);
                }
                toRelease.add(p);
            }
        }
        pending.removeAll(toRelease);
        if (!toRelease.isEmpty()) notifyVisualizer();
    }

    private void applyAgingForAllWaiting() {
        for (int i = queues.length - 1; i > 0; i--) {
            List<Process> toPromote = new ArrayList<>();
            for (Process p : new ArrayList<>(queues[i].getAllProcesses())) {
                if (!p.isRunning() && p.getWaitingTime() >= agingTimeUnit) {
                    queues[i].removeProcess(p);
                    p.setPriority(p.getPriority() - 1);
                    queues[p.getPriority() - 1].addProcess(p);
                    p.setCurrentQueueIndex(p.getPriority() - 1);
                    p.resetWaitingTime();
                    toPromote.add(p);
                }
            }
            for (Process p : toPromote) {
                if (logArea != null)
                    logArea.appendText("t=" + globalTime + ": " + p.getPid() + " aged up to queue " + p.getPriority() + "\n");
            }
        }
    }

    private void incrementWaitingTimesForAllWaitingExcept(Process running) {
        for (RoundRobin q : queues) {
            for (Process p : new ArrayList<>(q.getAllProcesses())) {
                if (!p.isRunning() && p != running) p.incrementWaitingTime(1);
            }
        }
    }

    private void logCPUidle() {
        if (logArea != null) logArea.appendText("t=" + globalTime + ": CPU idle\n");
        notifyCpu(null);
        notifyVisualizer();
        sleep(500);
    }

    private void logStatus(Process p) {
        int timeToAge = agingTimeUnit > 0 ? Math.max(0, agingTimeUnit - p.getWaitingTime()) : -1;
        int timeToDeAge = deAgingTimeUnit > 0 ? Math.max(0, deAgingTimeUnit - p.getCpuTimeUsed()) : -1;
        if (logArea != null) logArea.appendText("    Status of " + p.getPid()
                + " | Arrival=" + p.getArrivalTime()
                + " | Remaining=" + p.getRemainingTime()
                + " | TimeToAge=" + (timeToAge >= 0 ? timeToAge : "N/A")
                + " | TimeToDeAge=" + (timeToDeAge >= 0 ? timeToDeAge : "N/A") + "\n");
    }

    private boolean allQueuesEmpty() {
        for (RoundRobin q : queues) if (!q.isEmpty()) return false;
        return true;
    }

    private void sleep(long millis) {
        try { Thread.sleep(millis); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public void setAgingTime(int t) { this.agingTimeUnit = t; }
    public void setDeAgingTime(int t) { this.deAgingTimeUnit = t; }
    public void setVisualizerUpdate(Runnable update) { this.visualizerUpdate = update; }
    public void setCpuUpdate(Consumer<String> update) { this.cpuUpdate = update; }
    private void notifyVisualizer() { if (visualizerUpdate != null) visualizerUpdate.run(); }
    private void notifyCpu(String pid) { if (cpuUpdate != null) cpuUpdate.accept(pid); }

    public List<Process> getAllProcessesInQueue(int index) {
        if (index < 0 || index >= queues.length) return new ArrayList<>();
        return queues[index].getAllProcesses().stream()
                .filter(p -> p.getCurrentQueueIndex() != -1)
                .toList();
    }
}

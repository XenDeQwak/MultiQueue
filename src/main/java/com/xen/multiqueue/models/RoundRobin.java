package com.xen.multiqueue.models;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class RoundRobin {
    private final Queue<Process> queue = new LinkedList<>();
    private final int quantum;

    public RoundRobin(int quantum) {
        this.quantum = quantum;
    }

    public void addProcess(Process p) { queue.add(p); }
    public Process getNextProcess() { return queue.poll(); }
    public void requeueProcess(Process p) { queue.add(p); }
    public boolean isEmpty() { return queue.isEmpty(); }
    public void removeProcess(Process p) { queue.remove(p); }
    public List<Process> getAllProcesses() { return new ArrayList<>(queue); }
    public int getQuantum() { return quantum; }
}

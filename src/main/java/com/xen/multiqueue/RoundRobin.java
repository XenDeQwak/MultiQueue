package com.xen.multiqueue;

import java.util.LinkedList;
import java.util.Queue;

public class RoundRobin {
    private Queue<Process> queue;
    private int timeQuantum;

    public RoundRobin(int timeQuantum) {
        this.queue = new LinkedList<>();
        this.timeQuantum = timeQuantum;
    }

    public void addProcess(Process p) {
        queue.add(p);
    }

    public Process getNextProcess() {
        return queue.poll();
    }

    public void requeueProcess(Process p) {
        queue.add(p);
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public void removeProcess(Process p) {
        queue.remove(p);
    }

    public Queue<Process> getAllProcesses() {
        return queue;
    }
}

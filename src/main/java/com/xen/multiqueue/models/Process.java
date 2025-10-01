package com.xen.multiqueue.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Process {
    private final StringProperty pid;
    private final IntegerProperty burstTime;
    private final IntegerProperty remainingTime;
    private final IntegerProperty priority;
    private final IntegerProperty arrivalTime = new SimpleIntegerProperty(0);
    private final IntegerProperty waitingTime = new SimpleIntegerProperty(0);
    private final IntegerProperty cpuTimeUsed = new SimpleIntegerProperty(0);
    private int currentQueueIndex = -1;

    public Process(String pid, int burstTime, int priority) {
        this.pid = new SimpleStringProperty(pid);
        this.burstTime = new SimpleIntegerProperty(burstTime);
        this.remainingTime = new SimpleIntegerProperty(burstTime);
        this.priority = new SimpleIntegerProperty(priority);
    }

    public String getPid() { return pid.get(); }
    public int getBurstTime() { return burstTime.get(); }
    public int getRemainingTime() { return remainingTime.get(); }
    public int getPriority() { return priority.get(); }
    public int getArrivalTime() { return arrivalTime.get(); }
    public int getWaitingTime() { return waitingTime.get(); }
    public int getCpuTimeUsed() { return cpuTimeUsed.get(); }
    public void setRemainingTime(int v) { remainingTime.set(v); }
    public void setPriority(int v) { priority.set(v); }
    public void setArrivalTime(int v) { arrivalTime.set(v); }

    public void incrementWaitingTime(int delta) { waitingTime.set(waitingTime.get() + delta); }
    public void resetWaitingTime() { waitingTime.set(0); }
    public void incrementCpuTime(int delta) { cpuTimeUsed.set(cpuTimeUsed.get() + delta); }
    public void resetCpuTime() { cpuTimeUsed.set(0); }

    public StringProperty pidProperty() { return pid; }
    public IntegerProperty burstTimeProperty() { return burstTime; }
    public IntegerProperty remainingTimeProperty() { return remainingTime; }
    public IntegerProperty priorityProperty() { return priority; }
    public IntegerProperty arrivalTimeProperty() { return arrivalTime; }
    public int getCurrentQueueIndex() { return currentQueueIndex; }
    public void setCurrentQueueIndex(int q) { this.currentQueueIndex = q; }

}

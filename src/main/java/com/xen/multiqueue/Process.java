package com.xen.multiqueue;

import javafx.beans.property.*;

public class Process {
    private StringProperty pid;
    private IntegerProperty burstTime;
    private IntegerProperty remainingTime;
    private IntegerProperty priority;

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

    public void setRemainingTime(int val) { remainingTime.set(val); }
    public void setPriority(int val) { priority.set(val); }

    public StringProperty pidProperty() { return pid; }
    public IntegerProperty burstTimeProperty() { return burstTime; }
    public IntegerProperty remainingTimeProperty() { return remainingTime; }
    public IntegerProperty priorityProperty() { return priority; }
}



package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {
    private long timeCounter;
    private long duration;

    public TickBroadcast(long otherTimeCounter, long otherDuration){
        timeCounter = otherTimeCounter;
        duration=otherDuration;
    }
    public long getTimeCounter() {
        return timeCounter;
    }
    public long getDuration() {
        return duration;
    }
}

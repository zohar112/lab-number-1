package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;

import java.awt.*;

public class TrainModelEvent implements Event {
    private Model mySelf;

    public TrainModelEvent(Model otherSelf) {
        mySelf = otherSelf;
    }

    public Model getMySelf() {
        return mySelf;
    }
}

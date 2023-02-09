package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

public class TestModelEvent implements Event {
    public Model myself;

    public TestModelEvent(Model model){
        myself= model;
    }

    public Model getMyself() {
        return myself;
    }
}

package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

/**
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link //PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class StudentService extends MicroService {
    private Student student;
    private Future<Model> currFuture;
    private Model currModel;

    public StudentService(Student otherStudent) {
        super("StudentService");
        student = otherStudent;
        currModel=null;
        currFuture = null;
    }
    @Override
    protected void initialize() {
        Callback<TickBroadcast> eventTime = new Callback<TickBroadcast>() {
            @Override
            public void call(TickBroadcast eve) {
                if(currFuture==null ){
                    if(student.getMyModels().size() > 0) {
                        currModel = student.getMyModels().poll();
                        if(currModel!=null) {
                            currFuture = sendEvent(new TrainModelEvent(currModel));
                        }
                    }
                }
                else{
                    if(currFuture.isDone()) {
                        currModel = currFuture.get();
                        if (currModel.getStatus().equals("Trained")) {
                            currFuture = sendEvent(new TestModelEvent(currModel));
                        
                        }
                        if (currModel.getStatus().equals("Tested")){
                         
                            if(currModel.getResults() == Model.Results.Good) {
                                sendEvent(new PublishResultEvent(currModel));
                            }
                            currFuture=null;
                        }
                    }
                }
            }
        };
        this.subscribeBroadcast(TickBroadcast.class, eventTime);

        Callback<PublishConferenceBroadcast> publish = new Callback<PublishConferenceBroadcast>() {
            @Override
            public void call(PublishConferenceBroadcast eve) {
                student.updateStatistics(eve.getConference().getMyModels());
            }
        };
        this.subscribeBroadcast(PublishConferenceBroadcast.class, publish);

        Callback<TickFinish> finishTime = new Callback<TickFinish>() {
            @Override
            public void call(TickFinish eve) {
                terminate();
            }
        };
        this.subscribeBroadcast(TickFinish.class, finishTime);
    }
}
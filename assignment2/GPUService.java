package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.Event;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TickFinish;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.Cluster;
import bgu.spl.mics.application.objects.DataBatch;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {
    private Cluster cluster;
    private GPU gpu;
    private long currTime;

    public GPUService(String name, GPU gpu) {
        super(name);
        this.gpu = gpu;
        currTime = 1;
        cluster= Cluster.getInstance();
    }
    @Override
    protected void initialize() {
        Callback<TickBroadcast> tickTime = new Callback<TickBroadcast>() {
            @Override
            public void call(TickBroadcast eve) {
                currTime = eve.getTimeCounter();
                Event thisEvent = gpu.getTestAndTrain().peek();
                if (gpu.getAvailabe()) { //if im not working on something
                    if (thisEvent != null) {
                        Model currModel;
                        if (thisEvent instanceof TrainModelEvent) { //new event of train event kind
                            TrainModelEvent trainEvent = (TrainModelEvent) gpu.getTestAndTrain().peek();
                            currModel = trainEvent.getMySelf();
                            gpu.setCurrProcessing(currModel);
                            gpu.startTraining(); //sending batches to cluster
                            currModel.setStatus(); //to be training
                        } else { //test event
                            TestModelEvent testEvent = (TestModelEvent) gpu.getTestAndTrain().poll();
                            currModel = testEvent.getMyself();
                            gpu.test(testEvent);
                            complete(testEvent, currModel);
                        }
                    }
                }
                else {//we are here only if we are working on train model
                    if(!gpu.getProcessedData().isEmpty() && currTime%gpu.getTicksToTrain()==0){
                        gpu.getProcessedData().poll();
                        cluster.setGPUtime(gpu.getTicksToTrain()); //statistics +1 to GPU time used in program
                        gpu.setModelTicksRemane(gpu.getTicksToTrain());
                    }
                    if (gpu.getModelTicksRemane() == 0) {
                        if (thisEvent!= null && thisEvent instanceof TrainModelEvent) {
                            TrainModelEvent event = (TrainModelEvent) gpu.getTestAndTrain().poll();
                            complete(event, event.getMySelf());
                            gpu.setAvailabe(true);
                            event.getMySelf().setStatus(); //to be trained
                        }
                    }
                    if (gpu.getProcessedData().size() < gpu.getCapacity()) { //sending
                        DataBatch db= gpu.getModel().getData().getSingleBatch();
                        if(db!=null)
                            cluster.addUnProcessed(db); //add to cluster data from gpu before cpu process
                        synchronized (cluster.getProcessed()) { //sync the hashmap
                            ConcurrentLinkedQueue<DataBatch> l = cluster.getGPUProcessedData(gpu);
                            if (!l.isEmpty()) {
                                DataBatch databch = l.poll();
                                if (databch != null){
                                    gpu.getProcessedData().add(databch);// add to gpu data from cluster before training
                                }
                        }}
                    }
                }
            }
        };

        this.subscribeBroadcast(TickBroadcast.class, tickTime);

        Callback<TestModelEvent> testModel = new Callback<TestModelEvent>() {
            @Override
            public void call(TestModelEvent eve) {
                gpu.getTestAndTrain().add(eve);
            }
        };
        this.subscribeEvent(TestModelEvent.class, testModel);

        Callback<TrainModelEvent> TrainModel = new Callback<TrainModelEvent>() {
            @Override
            public void call(TrainModelEvent eve) {
                gpu.getTestAndTrain().add(eve);
            }
        };
        this.subscribeEvent(TrainModelEvent.class, TrainModel);

        Callback<TickFinish> finishTime = new Callback<TickFinish>() {
            @Override
            public void call(TickFinish eve) {
                terminate();
            }
        };
        this.subscribeBroadcast(TickFinish.class, finishTime);
    }
}

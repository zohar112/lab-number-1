package bgu.spl.mics.application.objects;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.services.GPUService;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
    /**
     * Enum representing the type of the GPU.
     */
    public enum Type {RTX3090, RTX2080, GTX1080}

    private Type typ;
    private String name;
    private Model currModel; //currently processing
    private Cluster cluster;
    private ConcurrentLinkedQueue<DataBatch> processedData; //from cpu before training
    private int capacity; //of proce.sed data in the same time
    private ConcurrentLinkedQueue<Event<?>> testAndTrain;
    private boolean isAvailabe;
    private int ticksToTrain; //The time it takes to train each batch of data in the mode
    private int modelTicksRemane;

    public int getTicksToTrain() {
        return ticksToTrain;
    }

    public GPU(Type type) {
        cluster= Cluster.getInstance();
        testAndTrain = new ConcurrentLinkedQueue<>();
        isAvailabe = true;
        currModel = null;
        typ = type;
        if (typ == Type.GTX1080){
            capacity = 8;
            name = "GTX1080";
            ticksToTrain= 4;
        }
        else if (typ == Type.RTX2080){
            capacity = 16;
            name = "RTX2080";
            ticksToTrain=2;
        }
        else {
            capacity = 32;
            name = "RTX3090";
            ticksToTrain=1;
        }
        processedData= new ConcurrentLinkedQueue<>();
    }
    public ConcurrentLinkedQueue<DataBatch> getProcessedData() {
        return processedData;
    }
    public int getCapacity() {
        return capacity;
    }
    public ConcurrentLinkedQueue<Event<?>> getTestAndTrain() {
        return testAndTrain;
    }
    public boolean getAvailabe() {
        return isAvailabe;
    }
    public Type getType() {
        return typ;
    }
    public Model getModel() {
        return currModel;
    }

    public int getModelTicksRemane() {
        return modelTicksRemane;
    }
    public void setAvailabe(boolean ans) {
        isAvailabe =ans;
    }
    public void setModelTicksRemane(int tmp) {
        modelTicksRemane-= tmp;
    }
    public void setCurrProcessing(Model newCurrProcessing) {
        currModel = newCurrProcessing;
        modelTicksRemane = ticksToTrain*(currModel.getData().getSize()/1000);
        isAvailabe = false;
    }
    public void startTraining(){
        while(processedData.size()<capacity && !currModel.getData().dataIsFinished){
            DataBatch db = currModel.getData().getSingleBatch();
            if(db!=null){
                cluster.addUnProcessed(db);
                db.setGPU(this);
            }
        }
    }

    public void test(TestModelEvent eve){
        Model m = eve.getMyself();
        if(m.getStudent().getDegree() == "MSc"){
            if(Math.random() <= 0.6) {
                m.setResults("Good");
            }
            else
                m.setResults("Bad");
        }
        if(m.getStudent().getDegree() == "PhD"){
            if(Math.random() <= 0.8) {
                m.setResults("Good");
            }
            else
                m.setResults("Bad");
        }
        m.setStatus();
    }
    public Thread start() {
        GPUService service = new GPUService(name,this);
        Thread t = new Thread(service);
        t.start();
        return t;
    }

}
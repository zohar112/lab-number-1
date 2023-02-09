package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.services.CPUService;
import javafx.util.Pair;

import java.util.LinkedList;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {
    private int core;
    private LinkedList<Pair<DataBatch,Integer>> collection; //not processed
    private Cluster cluster;
    public Integer timeToProcess;
    private boolean isProcessing= false;
    private int timeToProcessCurrData=0;

    public CPU(int Core) {
        core = Core;
        collection = new LinkedList<>();
        cluster= Cluster.getInstance();
        timeToProcess=0;
    }
    public boolean isProcessing() {
        return isProcessing;
    }
    public int getCore() {
        return core;
    }
    public Integer getTimeToProcess() {
        return timeToProcess;
    }
    public Cluster getCluster() {
        return cluster;
    }
    public LinkedList<Pair<DataBatch,Integer>> getCollection() {
        return collection;
    }
    /**
     * @pre: container.size() != 0
     * @post: container.size() == container.size()@pre -1
     */
    public void sendToProcess(DataBatch data, int dataTicks){
        collection.addFirst(new Pair<>(data, dataTicks));
    }
    public void setTimeToProcess() {
        this.timeToProcess --;
    }
    public void updateTimeToProcess(int time) {
        timeToProcess += time;
    }
    public void initializeProcess() {
        timeToProcessCurrData = collection.peekLast().getValue();
        isProcessing = true;
    }
    public void finishProcess() {
        synchronized (collection) {
            isProcessing = false;
            cluster.CPUToClusterToGPU(collection.removeLast().getKey());
        }
    }
    public synchronized void setCurrTimeToProcess() {
        timeToProcessCurrData--;
    }
    public int getTimeToProcessCurrData() {
        return timeToProcessCurrData;
    }
    public Thread start(){
        CPUService service= new CPUService(this);
        Thread t=new Thread(service);
        t.start();
        return t;
    }
}
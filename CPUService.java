package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TickFinish;
import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.Cluster;

/**
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class CPUService extends MicroService {
    private long currTime = 1;
    private CPU cpu;
    private Cluster cluster;

    public CPUService(CPU cpu) {
        super("CPU");
        this.cpu = cpu;
        cluster= Cluster.getInstance();
    }

    @Override
    protected void initialize() {
        Callback<TickBroadcast> tickTime = new Callback<TickBroadcast>() {
            @Override
            public void call(TickBroadcast eve) {
                currTime = eve.getTimeCounter();
                if(!cluster.getUnProcessed().isEmpty())
                    cluster.clusterToCPU();
                if (!cpu.isProcessing()) { //start process new databatch
                    if (!cpu.getCollection().isEmpty())
                        cpu.initializeProcess();
                }
                else {
                    if(cpu.getTimeToProcessCurrData() == 0)
                        cpu.finishProcess();
                    else{
                        cpu.setTimeToProcess();
                        cpu.setCurrTimeToProcess();
                        cpu.getCluster().setCPUtime(); //statistics
                    }
                }
            }
        };
        this.subscribeBroadcast(TickBroadcast.class, tickTime);

        Callback<TickFinish> finishTime = new Callback<TickFinish>() {
            @Override
            public void call(TickFinish eve) {
                terminate();
            }
        };
        this.subscribeBroadcast(TickFinish.class, finishTime);
    }
}

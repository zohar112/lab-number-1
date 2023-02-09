package bgu.spl.mics.application.objects;


import javafx.util.Pair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {
	private PriorityQueue<CPU>[] CPUS = new PriorityQueue[6];
	private LinkedList<GPU> GPUS;
	private ConcurrentLinkedQueue<DataBatch> unProcessed;  //from gpu before cpu process
	private HashMap<GPU,ConcurrentLinkedQueue<DataBatch>> processed; //Data Batch return from CPU to the appropriate GPU
	private LinkedList<String> modelNames;
	private int sumProcessedData=0;
	private int numCPUtime=0;
	private int numGPUtime=0;

	private static class ClusterHolder {
		static final Cluster INSTANCE = new Cluster(); //todo add the fields of constructor
	}
	private Cluster () {
		modelNames=new LinkedList<>();
		for (int i=0; i<CPUS.length; i++) {
			CPUS[i] = new PriorityQueue<>((n1,n2) -> {
			Integer time1 = n1.getTimeToProcess();
			Integer time2 = n2.getTimeToProcess();
			return time1.compareTo(time2);
		});
		}
		GPUS = new LinkedList<>();
		unProcessed = new ConcurrentLinkedQueue<>();
		processed = new HashMap<>();
	}
	/**
     * Retrieves the single instance of this class.
     */
	public static Cluster getInstance() {
		return ClusterHolder.INSTANCE;
	}
	 public void addCPU(CPU cpu) {
		if(cpu.getCore()==1)
			CPUS[0].add(cpu);
		else if (cpu.getCore()==2)
			CPUS[1].add(cpu);
		else if (cpu.getCore()==4)
			 CPUS[2].add(cpu);
		else if (cpu.getCore()==8)
			 CPUS[3].add(cpu);
		else if (cpu.getCore()==16)
			 CPUS[4].add(cpu);
		else
		 	 CPUS[5].add(cpu);
	 }

	public void addGPU(GPU gpu) {
		GPUS.add(gpu);
	}

	public ConcurrentLinkedQueue<DataBatch> getUnProcessed() { //TODO delete sync
		return unProcessed;
	}

	public synchronized void addUnProcessed(DataBatch dataBatch) { //TODO delete sync new
		unProcessed.add(dataBatch);
	}

	public synchronized void setCPUtime(){
		numCPUtime++;
	}

	public synchronized void setGPUtime(int tmp){
		numGPUtime +=tmp;
	}

	public HashMap<GPU, ConcurrentLinkedQueue<DataBatch>> getProcessed() {
		return processed;
	}
	public int getNumCPUtime(){
		return numCPUtime;
	}
	public int getNumGPUtime(){
		return numGPUtime;
	}
	public int getSumProcessedData() {
		return sumProcessedData;
	}

	public synchronized ConcurrentLinkedQueue<DataBatch> getGPUProcessedData(GPU gpu){
		if(processed.get(gpu) ==null)
			processed.put(gpu, new ConcurrentLinkedQueue<DataBatch>());
		return processed.get(gpu);
	}
	public synchronized void CPUToClusterToGPU (DataBatch dataBatch) { //puts databatch in the hashmap
		if(processed.get(dataBatch.getGpu()) == null)
			processed.put(dataBatch.getGpu(), new ConcurrentLinkedQueue<DataBatch>());
		processed.get(dataBatch.getGpu()).add(dataBatch);
		sumProcessedData++;
	}

	public void clusterToCPU () {
		synchronized (unProcessed) {
			if (!unProcessed.isEmpty()) {
				DataBatch databatch = unProcessed.poll();
				PriorityQueue<Pair<CPU, Integer>> pairsQueue = new PriorityQueue<Pair<CPU, Integer>>((o1, o2) -> {
					Integer time1 = o1.getKey().getTimeToProcess() + setTick(databatch, o1.getKey().getCore());
					Integer time2 = o2.getKey().getTimeToProcess() + setTick(databatch, o2.getKey().getCore());
					return time1.compareTo(time2);
				});
				for (int i = 0; i < CPUS.length; i++) {
					if (CPUS[i].peek() != null)
						pairsQueue.add(new Pair<>(CPUS[i].peek(), i));
				}
				CPU minCPU = pairsQueue.peek().getKey();
				Integer cpuNum = pairsQueue.poll().getValue();
				CPUS[cpuNum].poll();
				Integer currDataTicks = setTick(databatch, minCPU.getCore());
				minCPU.sendToProcess(databatch, currDataTicks);
				minCPU.updateTimeToProcess(currDataTicks);
				CPUS[cpuNum].add(minCPU);
			}
		}
	 }

	private Integer setTick (DataBatch data, int core) {
		int timeCount=0;
		Data.Type type = data.getData().getType();
		if(type == Data.Type.Images)
			timeCount += 32/core*4;
		else if (type == Data.Type.Tabular)
			timeCount += 32/core*1;
		else
			timeCount += 32/core*2;
		return timeCount;
	}

}

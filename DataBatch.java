package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class DataBatch {
    private int start_index; //The index of the first sample in the batch
    private Data data; //the Data the batch belongs to
    private GPU gpu=null; //which GPU is dealing with me

    public DataBatch (Data otherData, int index) {
        start_index = index;
        this.data = otherData;
    }
    public Data getData() {
        return data;
    }

    public GPU getGpu() {
        return gpu;
    }

    public void setGPU(GPU gpu) {
        this.gpu = gpu;
    }
}

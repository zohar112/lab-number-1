package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Data {
    /**
     * Enum representing the Data type.
     */
    enum Type {
        Images, Text, Tabular
    }
    public Type type;
    private int processed; //number of samples the GPU processed for training
    private int size; //number of sample
    boolean dataIsFinished= false;

    public Data(Type otherType, int otherSize) {
        type = otherType;
        processed = 0;
        size = otherSize;
    }

    public Type getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public DataBatch getSingleBatch() {
        if (dataIsFinished==false) {
            if (processed >= size/1000) {
                dataIsFinished = true;
            }
            else {
                DataBatch db = new DataBatch(this, processed);
                processed++;
                return db;
            }
        }
        return null;
    }
}

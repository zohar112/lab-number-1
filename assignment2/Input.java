
package bgu.spl.mics.application.objects;

public class Input {
    private Student[] Students;
    private GPU.Type[] GPUS;
    private int[] CPUS;
    private ConfrenceInformation[] Conferences;
    private long TickTime;
    private Long Duration;

    public GPU.Type[] getGPUS() {
        return GPUS;
    }

    public int[] getCPUS() {
        return CPUS;
    }

    public ConfrenceInformation[] getConferences() {
        return Conferences;
    }

    public long getTickTime() {
        return TickTime;
    }

    public Long getDuration() {
        return Duration;
    }

    public Student [] getStudents(){
        return Students;
    }

}
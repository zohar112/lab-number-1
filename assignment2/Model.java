package bgu.spl.mics.application.objects;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model {


    public enum Status {PreTrained, Training, Trained, Tested};
    public enum Results {None, Good, Bad};

    private String name;
    private Data data;
    private Student student;
    private Status status;
    private Results results;

    public Model (String name,Data data, Student student){
        this.name = name;
        this.data = data;
        this.student= student;
        status = Status.PreTrained;
        results = Results.None;
    }

    public void setResults(String st) {
        if(st.equals("None"))
            this.results = Results.None;
        if(st.equals("Good"))
            this.results = Results.Good;
        if(st.equals("Bad"))
            this.results = Results.Bad;
    }

    public void setStatus() { //set to the next status
        if(status == Status.Trained)
            this.status = Status.Tested;
        if(status == Status.Training)
            this.status = Status.Trained;
        if(status == Status.PreTrained)
            this.status = Status.Training;
    }
    public Student getStudent(){
        return student;
    }
    public Data getData() {
        return data;
    }
    public String getName() {
        return name;
    }
    public String getStatus() {
        return status.toString();
    }

    public Results getResults() {
        return results;
    }
    public void setStudent(Student student) {
        this.student = student;
    }

}

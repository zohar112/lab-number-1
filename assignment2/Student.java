package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.services.StudentService;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {

    /**
     * Enum representing the Degree the student is studying for.
     */
    enum Degree {
        MSc, PhD
    }
    private String name;
    private String department;
    private Degree status;
    private int publications;
    private int papersRead;
    private ConcurrentLinkedQueue<Model> myModels;

    public Student(String otherName, String otherDep, Degree otherStatus) {
        name = otherName;
        department = otherDep;
        status = otherStatus;
        publications = 0;
        papersRead = 0;
        myModels = new ConcurrentLinkedQueue<>();
    }

    public String getDegree(){
        if(status == Degree.MSc)
            return "MSc";
        else return "PhD";
    }
    public String getName() {
        return name;
    }

    public int getPublications() {
        return publications;
    }

    public int getPapersRead() {
        return papersRead;
    }

    public ConcurrentLinkedQueue<Model> getMyModels() {
        return myModels;
    }
    public Degree getStatus() {
        return status;
    }
    public String getDepartment() {
        return department;
    }

     public void addModel(Model model) {
        myModels.add(model);
     }

    public void updateStatistics(LinkedList<Model> goodModels){
        for (Model model : goodModels) {
            if (model.getStudent().equals(this))
                publications++;
            else
                papersRead++;
        }
    }

    public Thread start(){
        StudentService student= new StudentService(this);
        Thread t=new Thread(student);
        t.start();
        return t;
    }
}

package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.services.ConferenceService;

import java.util.LinkedList;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConfrenceInformation {

    private String name;
    private int date;
    private LinkedList<Model> myModels; //good published models forOutput

    public ConfrenceInformation(String otherName, int date){
        this.date=date;
        name=otherName;
        myModels = new LinkedList<>();
    }

    public String getName() {
        return name;
    }

    public int getDate() {
        return date;
    }

    public LinkedList<Model> getMyModels() {
        return myModels;
    }
    public Thread start(){
        ConferenceService service= new ConferenceService(name, this);
        Thread t=new Thread(service);
        t.start();
        return t;
    }
}

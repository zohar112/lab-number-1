package bgu.spl.net.api.bidi;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ALLInformation {
    public HashMap<User, ConcurrentLinkedQueue<User>> followers; //for each user the list of his followers
    public HashMap<User,ConcurrentLinkedQueue<User> > following; //for each user the list of his following
    public HashMap<User, ConcurrentLinkedQueue<Pair<Timestamp,PMMessage>>> PMmessages; //sent to me
    public HashMap<User, ConcurrentLinkedQueue<Pair<Timestamp,PostMessage>>> PostMessages; //sent to me
    public HashMap<String,User> registered; //registered users
    public String[] filteredWords = {"trump", "war", "hate"};
    public HashMap<User,Integer> hasBeenLoggedIn;

    private static class informationHolder {
        static final ALLInformation INSTANCE = new ALLInformation();
    }

    public ALLInformation(){
        followers= new HashMap<>();
        following=new HashMap<>();
        PMmessages=new HashMap<>();
        PostMessages= new HashMap<>();
        registered= new HashMap<>();
        hasBeenLoggedIn= new HashMap<>();
    }
    // Retrieves the single instance of this class.
    public static ALLInformation getInstance() {
        return informationHolder.INSTANCE;
    }
    public HashMap<User, ConcurrentLinkedQueue<Pair<Timestamp,PostMessage>>> getPostMessages() {
        return PostMessages;
    }
    public HashMap<User, ConcurrentLinkedQueue<Pair<Timestamp,PMMessage>>> getPMmessages() {
        return PMmessages;
    }
    public HashMap<String,User> getRegistered(){
        return registered;
    }
    public HashMap<User, ConcurrentLinkedQueue<User>> getFollowers() {
        return followers;
    }
    public HashMap<User, ConcurrentLinkedQueue<User>> getFollowing() {
        return following;
    }
    public String[] getFilteredWords() {
        return filteredWords;
    }

    public HashMap<User, Integer> getLoggedIn() {
        return hasBeenLoggedIn;
    }
}

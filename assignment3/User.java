package bgu.spl.net.api.bidi;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class User {
    public String name ;
    public String password;
    public String birthday;
    public boolean Logged =false;
    public int numFollowing=0;
    public int postNum =0;
    public int numFollowers = 0;
    public ConcurrentLinkedQueue<User> blockedUsers; //users i blocked + users blocked me
    public int conId;

    public User(String name, String password, String birthday){
        this.name = name;
        this.password = password;
        this.birthday = birthday;
        blockedUsers = new ConcurrentLinkedQueue<>();
    }


    public void setLog(boolean ans) {
        Logged=ans;
    }
    public String getPassword() {
        return password;
    }

    public boolean isLogged() {
        return Logged;
    }

    public int getAge(){
        int length = birthday.length();
        LocalDate current_date = LocalDate.now();
        int age = current_date.getYear()-Integer.parseInt(birthday.substring(length-4, length));
        return age;
    }

    public int getPostNum() {
        return postNum;
    }

    public int getNumFollowers() {
        return numFollowers;
    }

    public int getConId() {
        return conId;
    }

    public void setConId(int conId) {
        this.conId = conId;
    }

    public void setPostNum() {
        postNum++;
    }

    public int getNumFollowing() {
        return numFollowing;
    }

    public String getName() {
        return name;
    }

    public void setNumFollowers(int num) {
        this.numFollowers += num;
    }

    public void setNumFollowing(int num) {
        this.numFollowing +=num;
    }


    public ConcurrentLinkedQueue<User> getBlockedUsers() {
        return blockedUsers;
    }
}

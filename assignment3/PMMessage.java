package bgu.spl.net.api.bidi;

import java.time.LocalDate;

public class PMMessage {
    public User publishingUser;
    public String content;
    public User destUser;
    public LocalDate date;

    public PMMessage(User publishingUseruser, String content, User destUser, LocalDate date){
        this.content=content;
        this.date=date;
        this.destUser = destUser;
        this.publishingUser = publishingUseruser;
    }
}

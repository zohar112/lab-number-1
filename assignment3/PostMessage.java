package bgu.spl.net.api.bidi;

public class PostMessage {
    public User publishingUser;
    public String content;

    public PostMessage(User user, String content){
        this.publishingUser= user;
        this.content=content;
    }

    public String getContent() {
        return content;
    }
}

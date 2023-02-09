package bgu.spl.net.srv;

import bgu.spl.net.api.bidi.User;

import java.io.Closeable;

public interface ConnectionHandler<T> extends Closeable{

    void send(T msg) ;

    User getThisUser();

    void setThisUser(User user);
}
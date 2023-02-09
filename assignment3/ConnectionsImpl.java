package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.ConnectionHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionsImpl<T> implements Connections<T>{
    public ConcurrentHashMap<Integer, ConnectionHandler> handlers; //only for active clients

    public ConnectionsImpl(){
        handlers = new ConcurrentHashMap<>();
    }
    @Override
    public boolean send(int connectionId, Object msg) {
        ConnectionHandler handler= handlers.get(connectionId);
        if (handler!=null){
            handler.send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void broadcast(Object msg) { }

    @Override
    public void disconnect(int connectionId) {
       try {
           handlers.get(connectionId).close();
       }
       catch (IOException e) {
           e.printStackTrace();
       }
        handlers.remove(connectionId);
    }

    public ConcurrentHashMap<Integer, ConnectionHandler> getHandlers() {
        return handlers;
    }
    public void updateId(int newId, int oldId){
        ConnectionHandler tmp = handlers.get(oldId);
        handlers.remove(oldId);
        handlers.put(newId, tmp);
    }
}

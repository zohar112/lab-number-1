package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncDecImpl;
import bgu.spl.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl.net.api.bidi.ConnectionsImpl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public abstract class BaseServer<T> implements Server<T> {

    private final int port;
    private final Supplier<BidiMessagingProtocolImpl<T>> protocolFactory;
    private final Supplier<MessageEncDecImpl<T>> encdecFactory;
    private ServerSocket sock;
    private ConnectionsImpl<T> connections; //maybe singelton
//    private int conId; //maybe need atomic
    private AtomicInteger conID;

    public BaseServer(
            int port,
            Supplier<BidiMessagingProtocolImpl<T>> protocolFactory,
            Supplier<MessageEncDecImpl<T>> encdecFactory) {
        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
		this.sock = null;
//        conId= 0;
        conID = new AtomicInteger();
        connections= new ConnectionsImpl<>();
    }

    @Override
    public void serve() {
        try (ServerSocket serverSock = new ServerSocket(port)) {
			System.out.println("Server started");

            this.sock = serverSock; //just to be able to close
            while (!Thread.currentThread().isInterrupted()) {

                Socket clientSock = serverSock.accept();
                BlockingConnectionHandler<T> handler = new BlockingConnectionHandler<T>(
                        clientSock,
                        encdecFactory.get(),
                        protocolFactory.get(),
                        connections,
                        conID.getAndIncrement()); //Atomically increments by one the current value. Returns: the previous value
                execute(handler);
            }
        }
        catch (IOException ex) {}
        System.out.println("server closed!!!");
    }

    @Override
    public void close() throws IOException {
		if (sock != null)
			sock.close();
    }

    protected abstract void execute(BlockingConnectionHandler<T>  handler);


    public static <T> BaseServer<T> threadPerClient(
            int port,
            Supplier<BidiMessagingProtocolImpl<T>> protocolFactory,
            Supplier<MessageEncDecImpl<T>>encoderDecoderFactory) {
        return new BaseServer<T>(port, protocolFactory, encoderDecoderFactory) {
            @Override
            protected void execute(BlockingConnectionHandler<T> handler) {
                new Thread(handler).start();
            }
        };
    }

}

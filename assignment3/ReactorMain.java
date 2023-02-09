package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.MessageEncDecImpl;
import bgu.spl.net.api.bidi.ALLInformation;
import bgu.spl.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl.net.srv.Reactor;

public class ReactorMain {

    public static void main(String[] args) {
        ALLInformation information = new ALLInformation();
        int port = Integer.parseInt(args[0]);
        int nthreads = Integer.parseInt(args[1]);
        Reactor<String> reactor = new Reactor<>(nthreads, port, ()->new BidiMessagingProtocolImpl(information),()->new MessageEncDecImpl());
        reactor.serve();
    }
}

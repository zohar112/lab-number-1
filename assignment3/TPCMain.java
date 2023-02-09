package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.MessageEncDecImpl;
import bgu.spl.net.api.bidi.ALLInformation;
import bgu.spl.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl.net.srv.BaseServer;

public class TPCMain {
    public static void main(String[] args) {
        ALLInformation information = new ALLInformation();
        int port = Integer.parseInt(args[0]);
        try (BaseServer<String> threadPerClient = BaseServer.threadPerClient(port, () -> new BidiMessagingProtocolImpl<>(information), MessageEncDecImpl::new);) {
            threadPerClient.serve();
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }
}





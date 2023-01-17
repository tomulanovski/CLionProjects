package bgu.spl.net.impl.stomp;

import bgu.spl.net.impl.echo.EchoProtocol;
import bgu.spl.net.srv.ConnectionsImpl;
import bgu.spl.net.srv.Server;

public class StompServer {

    public static void main(String[] args) {
        if(args[1].equals("tpc")) {
                Server.threadPerClient(
                Integer.parseInt(args[0]), //port
                () -> new StompMessagingProtocolImp(), //protocol factory
                StompMessageEncoderDecoder::new //message encoder decoder factory
        ).serve();
        }
        else if (args[1].equals("reactor")) {

        Server.reactor(
                Runtime.getRuntime().availableProcessors(),
                Integer.parseInt(args[0]), //port
                () -> new StompMessagingProtocolImp(), //protocol factory
                StompMessageEncoderDecoder::new //message encoder decoder factory
        ).serve();
    }
}
    }


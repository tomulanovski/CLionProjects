package bgu.spl.net.impl.stomp;

import bgu.spl.net.impl.echo.EchoProtocol;
import bgu.spl.net.srv.ConnectionsImpl;
import bgu.spl.net.srv.Server;

public class StompServer {

    public static void main(String[] args) {
        // TODO: implement this
                Server.threadPerClient(
                7777, //port
                () -> new StompMessagingProtocolImp(), //protocol factory
                StompMessageEncoderDecoder::new //message encoder decoder factory
        ).serve();

//         Server.reactor(
//                 Runtime.getRuntime().availableProcessors(),
//                 7777, //port
//                 () -> new StompMessagingProtocolImp(), //protocol factory
//                 StompMessageEncoderDecoder::new //message encoder decoder factory
//         ).serve();
    }
    }


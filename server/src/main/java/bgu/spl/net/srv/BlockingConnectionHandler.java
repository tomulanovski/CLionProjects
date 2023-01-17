package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.impl.stomp.StompMessageEncoderDecoder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlockingConnectionHandler implements Runnable, ConnectionHandler<String> {

    private final StompMessagingProtocol<String> protocol;
    private final MessageEncoderDecoder<String> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    private ConnectionsImpl connections;
    private ConcurrentLinkedQueue messagesToSend;

    public BlockingConnectionHandler(Socket sock, StompMessageEncoderDecoder reader, StompMessagingProtocol<String> protocol , ConnectionsImpl connections) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        messagesToSend = new ConcurrentLinkedQueue<>();
        this.connections = connections;
    }

    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;
            connections.addConnection(this); //adding client to connections
            protocol.start(connections.getHandlerId(this),connections);
            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());

            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                String nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null && nextMessage.length()!=0) {
                     protocol.process(nextMessage);
                }
                while (!messagesToSend.isEmpty()) {
                    String message =(String) messagesToSend.poll();
                    out.write(encdec.encode(message));
                    out.flush();
                }
//                connected = false;
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }

    @Override
    public void send(String msg) {
        messagesToSend.add(msg);
    }
}

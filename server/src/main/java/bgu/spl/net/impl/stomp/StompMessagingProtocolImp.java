package bgu.spl.net.impl.stomp;
import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.ConnectionsImpl;
import java.util.HashMap;

public class StompMessagingProtocolImp implements StompMessagingProtocol<String> {
    private boolean shouldterminate = false;
    private int connectionId;
    private ConnectionsImpl connections;
    private volatile boolean connected = false;
    private String userName;


    public void start(int connectionId, Connections<String> connections) {
        this.connectionId = connectionId;
        this.connections = (ConnectionsImpl) connections;
    }

    public void process(String message) {
        int endCommand = message.indexOf('\n');
        int endHeaders = message.indexOf("\n\n");
        String Command = message.substring(0, endCommand);
        String Headers = message.substring(endCommand + 1, endHeaders + 1);
        HashMap<String, String> headersMap = organizeHeaders(Headers); //getting the map of all the headers
        String Body = message.substring(endHeaders + 2);
        Frame output = Frame.ErrorFrame(headersMap.get("receipt:"), "", "", "you have to try to connect first");
        if (!connected) {
            if (Command.equals("CONNECT")) {
                output = ConnectProcess(headersMap);
                connections.send(connectionId, output.frameToString());
            } else {
                connections.send(connectionId, Frame.ErrorFrame(headersMap.get("receipt:"), "", "", "you have to try to connect first").frameToString());
                shouldterminate = true;
            }
        } else { //client is connected
            if (Command.equals("DISCONNECT"))
                output = disconnectProcess(headersMap, Body);
            else if (Command.equals("SUBSCRIBE"))
                output = SubscribeProcess(headersMap, Body);
            else if (Command.equals("UNSUBSCRIBE"))
                output = UnsubscribeProcess(headersMap, Body);
            else if (Command.equals("SEND"))
                output = SendProcess(headersMap, Body);
            if (output.getCommand().equals("RECEIPT"))
                connections.send(connectionId, output.frameToString());
            else if (output.getCommand().equals("ERROR")) {
                connections.send(connectionId, output.frameToString());
                connections.disconnect(connectionId, userName);
                shouldterminate = true;
            }
        }
    }

    private Frame SendProcess(HashMap<String, String> headersMap, String body) {
        Frame outputFrame = new Frame("");
        if (headersMap.containsKey("destination:") && headersMap.size() < 3) { //checking if headersmap <3 because the only other possible header is receipt
            String Topic = headersMap.get("destination:");
            String subId = connections.checkSubscription(connectionId, Topic);
            if (connections.getTopicIdMap().containsKey(Topic) && !subId.equals("")) {
                connections.send(Topic, Frame.MessageFrame(Topic, body).frameToString()); //sending the message to all subscribers just if there is such topic and the client subscribed to it
                if (headersMap.containsKey("receipt:"))
                    outputFrame = Frame.ReceiptFrame(headersMap.get("receipt:"));
            } else
                outputFrame = Frame.ErrorFrame(headersMap.get("receipt:"), "malformed frame received", "", "no such topic");
        } else
            outputFrame = Frame.ErrorFrame(headersMap.get("receipt:"), "malformed frame received", "", "no destination or too many headers");
        return outputFrame;
    }

    private Frame ConnectProcess(HashMap<String, String> headersMap) {
        if (!headersMap.isEmpty()) {
            if (headersMap.containsKey("accept-version:") && headersMap.containsKey("host:") && headersMap.containsKey("login:") && headersMap.containsKey("passcode:")) { // checking if no missing headers
                if (headersMap.get("accept-version:").equals("1.2") && headersMap.get("host:").equals("stomp.cs.bgu.ac.il")) { //checking if headers are correct
                    String username = headersMap.get("login:");
                    String passcode = headersMap.get("passcode:");
                    String loginMessage = connections.CheckLogin(username, passcode, connectionId); //checking if can login
                    if (!loginMessage.equals("connected")) {
                        return Frame.ErrorFrame(headersMap.get("receipt:"), "malformed frame received", "CONNECT", loginMessage);
                    }
                    connected = true;
                    this.userName = username; //updating the username field
                    return Frame.connectedFrame(); //was able to connect
                }
            }
        }
        return Frame.ErrorFrame(headersMap.get("receipt:"), "malformed frame received", "CONNECT", "Problem with headers");
    }

    private Frame disconnectProcess(HashMap<String, String> headersMap, String body) {
        Frame output;
        if (body.isEmpty() && headersMap.size() == 1) {
            if (headersMap.containsKey("receipt:")) {
                output = Frame.ReceiptFrame(headersMap.get("receipt:"));
                connections.send(connectionId, output.frameToString());
                shouldterminate = true;
                connections.disconnect(connectionId, userName);
            } else
                output = Frame.ErrorFrame(null, "no receipt sent", "", "");
        } else
            output = Frame.ErrorFrame(null, "malformed frame", "", "send again");
        return output;
    }

    private Frame SubscribeProcess(HashMap<String, String> headersMap, String body) {
        Frame outputFrame = new Frame("");
        if (!headersMap.isEmpty() && body.isEmpty()) {
            if (headersMap.containsKey("destination:") && headersMap.containsKey("id:")) {
                String Topic = headersMap.get("destination:");
                String subId = headersMap.get("id:");
                connections.addSubscription(subId, connectionId, Topic);
                if (headersMap.containsKey("receipt:"))
                    outputFrame = Frame.ReceiptFrame(headersMap.get("receipt:"));
            } else
                outputFrame = Frame.ErrorFrame(null, "malformed frame received", "", "problem with headers");
        } else
            outputFrame = Frame.ErrorFrame(null, "empty", "", "empty frame/body not empty");
        return outputFrame;
    }

    private Frame UnsubscribeProcess(HashMap<String, String> headersMap, String body) {
        Frame outputFrame = new Frame("");
        if (!headersMap.isEmpty() && body.isEmpty()) {
            if (headersMap.containsKey("id:")) {
                String subId = headersMap.get("id:");
                connections.Unsubscribe(subId, connectionId);
                if (headersMap.containsKey("receipt:"))
                    outputFrame = Frame.ReceiptFrame(headersMap.get("receipt:"));
            } else
                outputFrame = Frame.ErrorFrame(headersMap.get("receipt:"), "malformed frame received", "", "no id header");
        } else
            outputFrame = Frame.ErrorFrame(headersMap.get("receipt:"), "", "", "empty/body not empty");
        return outputFrame;
    }


    /**
     * @return true if the connection should be terminated
     */
    public boolean shouldTerminate() {
        return shouldterminate;
    }

    private HashMap<String, String> organizeHeaders(String Headers) {
        HashMap<String, String> headers = new HashMap<>();
        String[] strings = Headers.split("\n", -1);
        for (int i = 0; i < strings.length - 1; i++) {
            int index = strings[i].indexOf(':');
            headers.put(strings[i].substring(0, index + 1), strings[i].substring(index + 1));
        }
        return headers;
    }
    public boolean isConnected() {
        return connected;
    }
}

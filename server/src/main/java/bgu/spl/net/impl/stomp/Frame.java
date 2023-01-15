package bgu.spl.net.impl.stomp;

import java.util.concurrent.atomic.AtomicInteger;

public class Frame {
    private static AtomicInteger FrameCount = new AtomicInteger(0);
    private int FrameId;
    private String command;
    private String header;
    private String body;

    public Frame(String command) {
        this.command = command;
        header = "";
        body = "";
        FrameCount.compareAndSet(FrameCount.intValue(), FrameCount.intValue() + 1);
        FrameId = FrameCount.intValue();

    }

    public static Frame connectedFrame() {
        String command = "CONNECTED";
        Frame outputFrame = new Frame(command);
        String header = "version :1.2\n\n";
        outputFrame.header = header;
        return outputFrame;
    }

    public static Frame ReceiptFrame(String idValue) {
        String command = "RECEIPT";
        String header = "receipt-id:" + idValue;
        Frame outputFrame = new Frame(command);
        outputFrame.header = header;
        return outputFrame;
    }

    public static Frame MessageFrame(String destination, String body) {
        Frame outputFrame = new Frame("MESSAGE");
        outputFrame.header = "destination:" + destination + "\n" +
                "message-id" + outputFrame.FrameId + "\n\n";
        outputFrame.body = body +'\n';
        return outputFrame;
    }

    public static Frame ErrorFrame(String receiptId,String MessageReason, String MessageBody, String details) {
        Frame outputFrame = new Frame("ERROR");
        outputFrame.header = "message:" + MessageReason + "\n\n";
        outputFrame.body = "The message:" + '\n' + "-----" + '\n' + MessageBody + '\n' + "-----" + '\n' + details +'\n';
        if (receiptId!=null)
            outputFrame.header = "receipt-id: message" + receiptId + '\n' + outputFrame.header;
        return outputFrame;
    }

    public String frameToString() {
        return command + '\n' + header + body + "\u0000";
    }
    public String getCommand(){
        return command;
    }


}




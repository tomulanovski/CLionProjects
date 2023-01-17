package bgu.spl.net.srv;
import bgu.spl.net.impl.stomp.Frame;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionsImpl implements Connections<String> {
    private int connectionsCounter;
    private int IdGenerator;
    private ConcurrentHashMap<Integer, ConnectionHandler<String>> idHandlerMap;
    private ConcurrentHashMap<ConnectionHandler<String>, Integer> HandlerIdMap;
    private ConcurrentHashMap<Integer, ConcurrentHashMap<String,String>> idTopicMap;
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<Integer>> TopicIdMap;
    private ConcurrentHashMap<String, String> UserPassMap;
    private ConcurrentHashMap<String , Integer> UserClientMap;

    public ConnectionsImpl(){
        connectionsCounter = 0;
        IdGenerator = 0;
        idHandlerMap = new ConcurrentHashMap<>();
        HandlerIdMap = new ConcurrentHashMap<>();
        idTopicMap = new ConcurrentHashMap<>();
        TopicIdMap = new ConcurrentHashMap<>();
        UserPassMap = new ConcurrentHashMap<>();
        UserClientMap = new ConcurrentHashMap<>();
    }
   public boolean send(int connectionId, String msg){
       if (idHandlerMap.containsKey(connectionId)) { //checking if this connection id is connected
           idHandlerMap.get(connectionId).send(msg); //getting his connection handler
           return true;
       }
       return false;
   }

    public void send(String channel, String msg) {
        if (TopicIdMap.containsKey(channel)) {
            ConcurrentLinkedQueue<Integer> subscribers = TopicIdMap.get(channel); //all the subscribers
            for (Integer subscriber : subscribers) {
                String subId=idTopicMap.get(subscriber).get(channel);
                send(subscriber , addSubId(msg,subId));
            }
        }
    }
    public String addSubId(String messageFrame , String subId) {
        int index = messageFrame.indexOf('\n');
        messageFrame = messageFrame.substring(0,index+1) + "subscription:" + subId + '\n'+ messageFrame.substring(index+1);
        return messageFrame;
    }

    public void disconnect(int connectionId , String userName){
        if (idHandlerMap.containsKey(connectionId)) { //checking if client is connected to connection handler
            ConnectionHandler<String> handler = idHandlerMap.get(connectionId);
        if (idTopicMap.containsKey(connectionId)) {
            ConcurrentHashMap<String,String> Topics = idTopicMap.get(connectionId); //getting the topics that client subscribed to
            for (Map.Entry<String,String> set :Topics.entrySet()) { //removing the client from all topics
                TopicIdMap.get(set.getKey()).remove(connectionId);
            }
            }
            idTopicMap.remove(connectionId);
            idHandlerMap.remove(connectionId);
            HandlerIdMap.remove(handler);
            UserClientMap.remove(userName);
            connectionsCounter--;
        }
        }
    public synchronized void addConnection(ConnectionHandler<String> handler) {
        connectionsCounter++;
        IdGenerator++;
        idHandlerMap.put(IdGenerator , handler);
        HandlerIdMap.put(handler , IdGenerator);
    }
    public boolean containsHandler(ConnectionHandler<String> handler){
        if (HandlerIdMap.contains(handler)) return true;
        else return false;
    }
    public Integer getHandlerId(ConnectionHandler<String> handler) {
        return HandlerIdMap.get(handler);
    }

    public String CheckLogin (String username , String passcode , int connectionId) { //check if the client can log in to his desired user
        if (UserClientMap.containsKey(username)) // different client connected to this user
            return "There is an active client whos logged in to this user";
        if (UserPassMap.containsKey(username)) { //username exists
            if (UserPassMap.get(username).equals(passcode)) {
                UserClientMap.put(username, connectionId);
                return "connected";
            } else {
                return "client gave wrong passcode"; // client gave wrong passcode for this username
            }
        } else {
            UserPassMap.put(username, passcode); //adding new username to the list
            UserClientMap.put(username, connectionId); // adding the client to this user
            return "connected";
        }
    }
        public void addSubscription(String subId , int connectionId  , String Topic) {
        if (idTopicMap.containsKey(connectionId)) { //checking if client is in the map of subscriptions
                idTopicMap.get(connectionId).put(Topic,subId); //if so , adding the topic to his subscriptions
            }
        else {
                idTopicMap.put(connectionId ,new ConcurrentHashMap<>()); //adding client to subscriptions map
                idTopicMap.get(connectionId).put(Topic,subId); //adding the Topic to the client subscriptions
            }
        if (TopicIdMap.containsKey(Topic)) { //checking if topic exists
            TopicIdMap.get(Topic).add(connectionId);
        }
        else {
            TopicIdMap.put(Topic , new ConcurrentLinkedQueue<>()); //creating new topic and adding to the map
            TopicIdMap.get(Topic).add(connectionId);
        }
        }
        public void Unsubscribe (String subId , int connectionId){
        if (idTopicMap.containsKey(connectionId)) {
            ConcurrentHashMap <String,String> topics = idTopicMap.get(connectionId); //list of client topics
            for (Map.Entry<String,String> set :topics.entrySet()) {
                if(set.getValue().equals(subId)) {
                    idTopicMap.get(connectionId).remove(set); // removing topic from client topic
                    TopicIdMap.get(set.getKey()).remove(connectionId);//removing client from subscribers of this topic
                }
            }
        }
        }
        public ConcurrentHashMap <String, ConcurrentLinkedQueue<Integer>> getTopicIdMap(){return TopicIdMap;}
        public ConcurrentHashMap <Integer, ConcurrentHashMap<String ,String>> getidTopicMap() {return idTopicMap;}
    public String checkSubscription(int connectionId , String Topic){
        if(idTopicMap.containsKey(connectionId)) {
            ConcurrentHashMap<String,String> topics = idTopicMap.get(connectionId); //list of client topics
            for (Map.Entry<String,String> set :topics.entrySet()) {
                if (set.getKey().equals(Topic)) {
                    return set.getValue();
                }
            }
        }
        return "";
    }

    }

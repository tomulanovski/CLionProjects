#include "../include/StompProtocol.h"
StompProtocol::StompProtocol(ConnectionHandler &handler) :username("") ,mhandler(&handler) , receiptIdCount(1) ,
subId(1) ,disconnectReceipt(0), topicSubIdmap() , ReceiptToSubIdMap() , Games_UsersMap() {}
string StompProtocol::keyboardProcess(string message) {
    vector<string> words;
    stringstream stream(message);
    string word;
    string out="write a valid command";
    while (stream >> word) {
        words.push_back(word);
    }
    if (!mhandler->isConnected()) {
        if (words[0] != "login" || words.size()!=4)
            cout << "you need to log in correctly first" << endl;
        else 
            out = Login(words);
    }
    else { //client is connected
       if(mhandler->isConnected() && words[0] == "login" )
          out = "Client is already logged in";
       else if (words[0]=="join" && words.size()==2)
            out = join(words);
       else if (words[0]=="exit" && words.size()==2)
            out = exit(words);
       else if (words[0]=="report" && words.size()==2)
           out = report(words);
       else if (words[0]=="logout" && words.size()==1)
           out = logout(words);
       else if (words[0]=="summary" && words.size()==4) {
            summary(words);
            out="";
       }
    }
    if (out=="write a valid command" || out=="Client is already logged in") {
      cout<<out<<endl;
      out="";
    }
    return out;
}
string StompProtocol::Login(vector<string> input) {
    int index = input[1].find(":");
    string host = input[1].substr(0,index);
    string port = input[1].substr(index+1);
    mhandler->SetHost(host);
    mhandler->SetPort(port);
    if (!mhandler->connect()) {
        return "log in first";
    }
    else {
        username = input[2];
        string out = "CONNECT";
        out = out + "\n" + "accept-version:1.2" + '\n' + "host:stomp.cs.bgu.ac.il" + '\n' + "login:" + username + '\n' + "passcode:" + input[3] + "\n\n";
        return out;
    }
}

string StompProtocol::join(vector<string> input) {
    string out = "SUBSCRIBE";
    out = out + '\n' + "destination:/" + input[1] + '\n' + "id:" + to_string(subId) + '\n' + "receipt:" + to_string(receiptIdCount) + "\n\n";\
    ReceiptToSubIdMap.insert(pair<string,pair<string,int>>(to_string(receiptIdCount),make_pair(input[1],subId)));
    addSubscription(subId , input[1]);
    subId++;
    receiptIdCount++;
    return out;
}
string StompProtocol::exit(vector<string> input) {
string out = "UNSUBSCRIBE";
if (topicSubIdmap.count(input[1])>0) {
out = out + '\n' + "id:" + to_string(getSubId(input[1])) +'\n' +"receipt:" + to_string(receiptIdCount) + "\n\n";
ReceiptToSubIdMap.insert(pair<string,pair<string,int>>(to_string(receiptIdCount),make_pair(input[1],getSubId(input[1]))));
receiptIdCount++;
removeSubscription(input[1]);
return out;
}
else {
cout<<"no such topic subscribed"<<endl;
return "";
}
}
string StompProtocol::report(vector<string> input) {
names_and_events namesAndEvents = parseEventsFile(input[1]);
vector<Event> events = namesAndEvents.events;
string topic = namesAndEvents.team_a_name + "_" + namesAndEvents.team_b_name;
if (topicSubIdmap.count(topic)==0) 
  cout<<"not subscribed to this game"<<endl;
else {
for (int i=0;(unsigned)i<events.size();i++) {
    string out = "SEND";
    string topic = events[i].get_team_a_name() + "_" + events[i].get_team_b_name();
    string eventName = events[i].get_name();
    string time = to_string(events[i].get_time());
    map<string,string> GeneralUpdates = events[i].get_game_updates();
    string GeneralUpdatesStr = "general game updates:\n";
    for (pair<string,string> update: GeneralUpdates) {
        GeneralUpdatesStr = GeneralUpdatesStr + update.first +  ':' +update.second + '\n';
    }
    map<string,string> TeamAUpdates = events[i].get_team_a_updates();
    string TeamAstr = "team a updates:\n";
    for (pair<string,string> update: TeamAUpdates) {
        TeamAstr = TeamAstr + update.first +  ':' +update.second + '\n';
    }
    map<string,string> TeamBUpdates = events[i].get_team_b_updates();
    string TeamBStr = "team b updates:\n";
    for (pair<string,string> update: TeamBUpdates) {
        TeamBStr = TeamBStr + update.first +  ':' +update.second + '\n';
    }
    string description = events[i].get_discription();
    out = out + '\n' + "destination:/" + topic +"\n\n" + "user:" + username + '\n' + "team a:" +
            events[i].get_team_a_name() + '\n' + "team b:" + events[i].get_team_b_name() +'\n' +
            "event name:" + eventName + '\n' + "time:" + time + '\n' + GeneralUpdatesStr + TeamAstr +
            TeamBStr + "description:\n" + description + '\0';
    mhandler->sendFrameAscii(out,'\0');
}
}
 return "";

}
string StompProtocol::logout(vector<string> input) {
string out = "DISCONNECT";
disconnectReceipt = receiptIdCount;
receiptIdCount++;
out = out + '\n' + "receipt:" + to_string(disconnectReceipt) + "\n\n";
return out;
}

void StompProtocol::summary(vector<string> input) {
if (Games_UsersMap.count(input[1])==0)
   cout<<"no such game"<<endl;
else if(Games_UsersMap.at(input[1]).count(input[2])==0)
    cout<<"no reports to this game from this user"<<endl;
else {       
string toPrint = Games_UsersMap.at(input[1]).at(input[2]).PrintSummary();
ofstream stream;
stream.open(input[3] , ios::trunc);
if(stream) {
    stream << toPrint;
    stream.close();
}
else {
    ofstream newFile(input[3]);
    if(newFile) {
        newFile << toPrint;
        newFile.close();
    }
    else
        cout<<"couldn't create the file"<<endl;
}
}
}
void StompProtocol::addSubscription(int subId, string Topic) {
    topicSubIdmap.insert(pair<string,int>(Topic,subId));
}
int StompProtocol::getSubId(string topic) {
     return topicSubIdmap.find(topic)->second; 
}
void StompProtocol::removeSubscription(string topic) {
    if ( topicSubIdmap.count(topic)!=0)
        topicSubIdmap.erase(topic);
    if (Games_UsersMap.count(topic)>0 && Games_UsersMap.at(topic).count(username)>0 )
    Games_UsersMap.at(topic).erase(username);    
}
void StompProtocol::ServerProcess(string msg) {
    if(msg.find('\n')==0) msg = msg.substr(1);
    int endCommand = msg.find('\n');
    int endHeaders = msg.find("\n\n");
    string command = msg.substr(0,endCommand);
    string headers = msg.substr(endCommand +1, endHeaders);
    string body = msg.substr(endHeaders+2);
    if (command=="CONNECTED")
        cout<<"successfully logged in"<<endl;
    else if (command=="RECEIPT")
        receipt(headers);
    else if (command=="ERROR")
        error(body);
    else if (command=="MESSAGE")
        message(headers,body);
}
void StompProtocol::receipt(string headers) {
    string receiptId = headers.substr(headers.find(':') + 1, headers.find("\n\n"));
    if (stoi(receiptId) == disconnectReceipt) { //got the disconnect receipt
        cout << "Disconnecting" << endl;
        mhandler->close();
    } else {
        string topic = "";
        bool join = false;
            pair<string,int> topicPair= ReceiptToSubIdMap.find(receiptId)->second;
            if(topicSubIdmap.count(topicPair.first)>0)
              join=true;
            topic = topicPair.first;
        if (join)
            cout << "joined to channel:" + topic << endl;
        else {
            cout << "Exited from channel:" + topic << endl;
        }

    }
}
    void StompProtocol::error(string body) {
        string out = "ERROR\n";
        cout<<out + body <<endl;
        cout<<"Disconnecting due to an error"<<endl;
        mhandler->close();
    }
    void StompProtocol::message(string headers , string body) {
    headers = headers.substr(0,headers.find("\n\n"));
    unordered_map<string,string> generalStats;
    unordered_map<string,string> teamAStats;
    unordered_map<string,string> teamBStats;
    unordered_map<string,string> organizedBody;
    string toCut = body.substr(body.find("general game updates:") +22 ,body.find("team a updates:"));
    toCut = toCut.substr(0,toCut.find("team a updates:"));
    generalStats = stringToMap(toCut);
    toCut = body.substr(body.find("team a updates:")+16 , body.find("team b updates:"));
    toCut = toCut.substr(0,toCut.find("team b updates:"));
    teamAStats = stringToMap(toCut);
    toCut = body.substr(body.find("team b updates:")+16 , body.find("description:"));
    toCut = toCut.substr(0,toCut.find("description:"));
    teamBStats = stringToMap(toCut);
    organizedBody = stringToMap(body);
    string GameName = organizedBody.at("team a") + "_" + organizedBody.at("team b");
    string userName = organizedBody.at("user");
    string eventName = organizedBody.at("event name");
    string time = organizedBody.at("time");
    string event = time+"-"+eventName+"\n\n"+organizedBody.at("description") +"\n\n";
    if (Games_UsersMap.count(GameName)>0) { //the game is in the map
        if (Games_UsersMap.at(GameName).count(userName)>0) { //the user already reported about this game
            Games_UsersMap.at(GameName).at(userName).UpdateGame(generalStats , teamAStats , teamBStats ,event);
        }
        else { //first report on this game from this user
            Game game = Game(GameName);
            game.UpdateGame(generalStats , teamAStats , teamBStats ,event);
            Games_UsersMap.at(GameName).insert(pair<string,Game>(userName,game));
        }
    }
    else { //game not in the map
        unordered_map<string,Game> toAdd;
        Game game = Game(GameName);
        game.UpdateGame(generalStats , teamAStats , teamBStats ,event);
        toAdd.insert(pair<string,Game>(userName,game));
        Games_UsersMap.insert(pair<string,unordered_map<string,Game>>(GameName,toAdd));
    }

}
unordered_map<string,string> StompProtocol::stringToMap(string toCut) {
   unordered_map<string,string> outputMap;
    int LineIndex = toCut.find('\n');
    while (LineIndex != -1) {
        int cutIndex = toCut.find(':');
        outputMap.insert(pair<string,string>(toCut.substr(0,cutIndex),toCut.substr(cutIndex+1,LineIndex-(cutIndex+1))));
        toCut = toCut.substr(LineIndex+1);
        LineIndex = toCut.find('\n');
    }
    return outputMap;
}
StompProtocol::~StompProtocol() {
    if (mhandler) delete mhandler;
}
StompProtocol::StompProtocol(const StompProtocol &otherProtocol) : username(otherProtocol.username) , mhandler(otherProtocol.mhandler) , receiptIdCount(otherProtocol.receiptIdCount) ,
subId(otherProtocol.subId) ,disconnectReceipt(otherProtocol.disconnectReceipt), topicSubIdmap(otherProtocol.topicSubIdmap)
, ReceiptToSubIdMap(otherProtocol.ReceiptToSubIdMap)
, Games_UsersMap(otherProtocol.Games_UsersMap) {}

StompProtocol &StompProtocol::operator=(const StompProtocol & otherProtocol) {
    username = otherProtocol.username;
    mhandler = otherProtocol.mhandler;
    receiptIdCount=otherProtocol.receiptIdCount;
    subId=otherProtocol.subId;
    disconnectReceipt=otherProtocol.disconnectReceipt;
    topicSubIdmap=otherProtocol.topicSubIdmap ;
    ReceiptToSubIdMap=otherProtocol.ReceiptToSubIdMap;
    Games_UsersMap=otherProtocol.Games_UsersMap;
    return *this;
}





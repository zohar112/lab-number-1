package bgu.spl.net.api.bidi;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;


public class BidiMessagingProtocolImpl<T> implements BidiMessagingProtocol<T> {
    private ConnectionsImpl<T> connectionsClass;
    private final ALLInformation information;
    private int conId;
    boolean isTerminated = false;

    public BidiMessagingProtocolImpl(ALLInformation information){
        this.information = ALLInformation.getInstance();
    }

    @Override
    public void start(int connectionId, Connections connections) {
        conId = connectionId;
        connectionsClass= (ConnectionsImpl<T>) connections;
    }

    @Override
    public void process(Object message) {
        String msg = (String) message;
        if (msg.indexOf("REGISTER") == 0) {
            msg = msg.substring(9);
            connectionsClass.send(conId,registerMgs(msg, (short) 1));
        }
        if (msg.indexOf("LOGIN") == 0) {
            msg = msg.substring(6);
            String logmsg = loginMsg(msg, (short) 2);
            connectionsClass.send(conId,logmsg);
        }
        if (msg.indexOf("LOGOUT") == 0) {
            msg = msg.substring(7);
            boolean ans =connectionsClass.send(conId,logoutMsg((short) 3));
            if(ans) {
                isTerminated = true;
                if(information.getLoggedIn().get(conId) != null)
                    connectionsClass.disconnect(conId);
            }
        }
        if (msg.indexOf("FOLLOW") == 0) {
            msg = msg.substring(7);
            connectionsClass.send(conId,followMsg(msg, (short) 4));
        }
        if (msg.indexOf("POST") == 0) {
            msg = msg.substring(5);
            connectionsClass.send(conId,postMsg(msg, (short) 5));
        }
        if (msg.indexOf("PM") == 0) {
            msg = msg.substring(3);
            connectionsClass.send(conId,privateMsg(msg, (short) 6));
        }
        if (msg.indexOf("LOGSTAT") == 0) {
            msg = msg.substring(7);
            connectionsClass.send(conId,logstatMsg((short) 7));
        }
        if (msg.indexOf("STAT") == 0) {
            msg = msg.substring(5);
            connectionsClass.send(conId,statMsg(msg, (short) 8));
        }
        if (msg.indexOf("BLOCK") == 0) {
            msg = msg.substring(6);
            connectionsClass.send(conId,blockMsg(msg, (short) 12));
        }
    }

    public String registerMgs(String input, Short opcode) {
        String name = input.substring(0, input.indexOf(" "));
        input= input.substring(input.indexOf(" ")+1);
        String password = input.substring(0, input.indexOf(" "));
        String birthday= input.substring(input.indexOf("")+password.length());
        synchronized (information) {
            User user = new User(name, password, birthday);
            if (information.getRegistered().get(name) != null || birthday.length() != 8)
                return "ERROR " + opcode;
            information.getRegistered().put(name, user);
            initiate(user);
        }
        return "ACK "+opcode;
    }

    public synchronized String loginMsg(String input, Short opcode) {
        String username = input.substring(0, input.indexOf(" "));
        input= input.substring(input.indexOf(" ")+1);
        String password = input.substring(0, input.indexOf(" "));
        input= input.substring(input.indexOf(" ")+1);
        int captcha = Integer.parseInt(input);
        synchronized (information) {
            User user = information.getRegistered().get(username);
            if (user == null || !password.equals(user.getPassword()) || user.isLogged() || captcha != 1)
                return "ERROR " + opcode;
            else {
                user.setConId(conId);
                connectionsClass.getHandlers().get(conId).setThisUser(user);
                user.setLog(true);
                if (information.getLoggedIn().get(user) == null)
                    information.getLoggedIn().put(user, conId);
                else {
                    int oldId = information.getLoggedIn().get(user);
                    connectionsClass.updateId(oldId, conId);
                    conId = oldId;
                    user.setConId(conId);
                    connectionsClass.getHandlers().get(conId).setThisUser(user);
                }
                Timestamp now = new Timestamp(System.currentTimeMillis());
                for (Pair p : information.getPostMessages().get(user)) {
                    Timestamp time = (Timestamp) p.getKey();
                    if (time.before(now)) {
                        PostMessage post = (PostMessage) p.getValue();
                        connectionsClass.send(user.getConId(), "NOTIFICATION PUBLIC " + user.getName() + " " + post.content);
                        information.getPostMessages().get(user).remove(p);
                    }
                }
                for (Pair p : information.getPMmessages().get(user)) {
                    Timestamp time = (Timestamp) p.getKey();
                    if (time.before(now)) {
                        PMMessage pm = (PMMessage) p.getValue();
                        connectionsClass.send(user.getConId(), "NOTIFICATION PM " + user.getName() + " " + pm.content);
                        information.getPMmessages().get(user).remove(p);
                    }
                }
                return "ACK " + opcode;
            }
        }
    }
    public String logoutMsg(Short opcode) {
        User user =connectionsClass.getHandlers().get(conId).getThisUser();
        if (user == null || !user.isLogged())
            return "ERROR " +opcode;
        else{
            user.setLog(false);
            return "ACK "+opcode;
        }
    }
    public String followMsg(String input, Short opcode){
        User user = connectionsClass.getHandlers().get(conId).getThisUser();
        int followOrNot = Integer.parseInt(String.valueOf(input.charAt(0)));
        String dstUserName= input.substring(2);
        User dst = information.getRegistered().get(dstUserName);
        if(user != null && user.isLogged() && dst!=null) {
            if (followOrNot==0 && !information.getFollowers().get(dst).contains(user) && !dst.getBlockedUsers().contains(user)) {
                //if user isn't already follow dst and if dst hasn't blocked user:
                information.getFollowing().get(user).add(dst);
                information.getFollowers().get(dst).add(user);
                user.setNumFollowing(1);
                dst.setNumFollowers(1);
                return "ACK "+opcode;
            }
            else if (followOrNot==1 && information.getFollowing().get(user).contains(dst)) {
                information.getFollowing().get(user).remove(dst);
                information.getFollowers().get(dst).remove(user);
                user.setNumFollowing(-1);
                dst.setNumFollowers(-1);
                return "ACK "+opcode;
            }
        }
        return "ERROR "+opcode;
    }

    public String postMsg(String input, Short opcode) {
        User user =connectionsClass.getHandlers().get(conId).getThisUser();
        if (user ==null || !user.isLogged())
            return "ERROR "+opcode;
        else {
            PostMessage post = new PostMessage(user, input);
            Set<User> hash_Set = new HashSet<>();
            hash_Set.addAll(information.getFollowers().get(user));
            String[] hashTags = input.split(" "); //tagged
            for (String word : hashTags) {
                if(word.charAt(0)=='@')
                    hash_Set.add(information.getRegistered().get(word.substring(1)));
            }
            for(User userToNotify : hash_Set) { //send notification
                if(userToNotify.isLogged())
                    connectionsClass.send(userToNotify.getConId(), "NOTIFICATION PUBLIC " + user.getName() + " " + post.content);
                else
                    information.getPostMessages().get(userToNotify).add(new Pair(new Timestamp(System.currentTimeMillis()),post));
            }
            user.setPostNum();
            return "ACK "+opcode;
        }
    }
    public String privateMsg(String input, Short opcode){
        User user =connectionsClass.getHandlers().get(conId).getThisUser();
        String dstUserName = input.substring(0, input.indexOf(" "));
        User dst = information.getRegistered().get(dstUserName);
        if(user != null && user.isLogged() && dst!=null && !dst.getBlockedUsers().contains(user)) {
            String content= input.substring(input.indexOf(" ")+1);
            String[] words = content.split(" ");
            for(String badWord: information.getFilteredWords()){
                for (int i=0; i<words.length; i++) {
                    if(words[i].equals(badWord))
                        words[i] = "<filtered>";
                }
            }
            content = String.join(" ", words);
            if(dst.isLogged())
                connectionsClass.send(dst.getConId(), "NOTIFICATION PM " + user.getName() + " " +content);
            else {
                LocalDate current_date = LocalDate.now();
                PMMessage pm = new PMMessage(user, content, dst, current_date);
                Pair p = new Pair(new Timestamp(System.currentTimeMillis()), pm);
                information.getPMmessages().get(dst).add(p);
            }
            return "ACK "+opcode;
        }
        return "ERROR "+opcode;
    }
    public String logstatMsg(Short opcode) {
        User user =connectionsClass.getHandlers().get(conId).getThisUser();
        if (user == null || !user.isLogged())
            return "ERROR "+opcode;
        else {
            String outputACK = "";
            short ackOpcode = 10;
            for (User statUser : information.getRegistered().values()) {
                if (statUser.isLogged()){
                    if(!statUser.getBlockedUsers().contains(user) && !user.getBlockedUsers().contains(statUser)) {
                        short age = (short) user.getAge();
                        outputACK = outputACK + "ACK " + ackOpcode + " LOGSTAT " + opcode + " " + age + " " + (short) statUser.getPostNum() + " " + (short) statUser.getNumFollowers() + " " + (short) statUser.getNumFollowing() + "\n";
                    }
                }
            }
            return outputACK;
        }
    }
    public String statMsg(String input, Short opcode) {
        User user =connectionsClass.getHandlers().get(conId).getThisUser();
        if (user!= null && user.isLogged()) {
            ConcurrentLinkedQueue<User> usersStat = new ConcurrentLinkedQueue<>();
            while (input.contains("|")) {
                String name = input.substring(0, input.indexOf("|"));
                input = input.substring(input.indexOf("|") + 1);
                User dst = information.getRegistered().get(name);
                if (dst == null || dst.getBlockedUsers().contains(user))
                    return "ERROR "+opcode;
                usersStat.add(dst);
            }
            User soloDst = (information.getRegistered().get(input));
            if (soloDst == null || soloDst.getBlockedUsers().contains(user))
                return "ERROR "+opcode;
            usersStat.add(soloDst);
            String outputACK = "";
            short ackOpcode = 10;
            for (User us : usersStat) {
                outputACK= outputACK+ "ACK "+ ackOpcode+" STAT "+opcode+" "+(short)us.getAge()+" "+(short)us.getPostNum()+" "+(short)us.getNumFollowers()+" "+(short)us.getNumFollowing()+"\n";
            }
            return outputACK;
        }
        else
            return "ERROR "+opcode;
    }
    public String blockMsg(String input, Short opcode){
        User user =connectionsClass.getHandlers().get(conId).getThisUser();
        if(user!= null && user.isLogged()) {
            User dst = (information.getRegistered().get(input));
            if (dst != null) {
                if (information.getFollowing().get(user).contains(dst)) {
                    information.getFollowing().get(user).remove(dst);
                    user.setNumFollowing(-1);
                    information.getFollowers().get(dst).remove(user);
                    dst.setNumFollowers(-1);
                }
                if (information.getFollowing().get(dst).contains(user)) {
                    information.getFollowing().get(dst).remove(user);
                    dst.setNumFollowing(-1);
                    information.getFollowers().get(user).remove(dst);
                    user.setNumFollowers(-1);
                }
                user.getBlockedUsers().add(dst);
                return "ACK "+opcode;
            }
        }
        return "ERROR "+opcode;
    }

    public void initiate(User user) {
        information.getFollowers().put(user,new ConcurrentLinkedQueue<>());
        information.getFollowing().put(user, new ConcurrentLinkedQueue<>());
        information.getPostMessages().put(user, new ConcurrentLinkedQueue<>());
        information.getPMmessages().put(user, new ConcurrentLinkedQueue<>());
    }
        @Override
    public boolean shouldTerminate() {
        return isTerminated;
    }

}

package hk.edu.cuhk.ie.iems5722.a2_1155155009;

import java.util.Objects;

public class Message {

    private long id;
    private long chatroom_id;
    private long user_id;
    private String name;
    private String message;
    private String message_time;

    public Message(long id, long chatroom_id, long user_id, String name, String message, String message_time) {
        this.id = id;
        this.chatroom_id = chatroom_id;
        this.user_id = user_id;
        this.name = name;
        this.message = message;
        this.message_time = message_time;
    }

    public Message(String message, String timestamp, long user_id, String name) {
        this.message = message;
        this.message_time = timestamp;
        this.user_id = user_id;
        this.name = name;
    }

    public String getMessage_time() {
        return message_time;
    }

    public void setMessage_time(String message_time) {
        this.message_time = message_time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getChatroom_id() {
        return chatroom_id;
    }

    public void setChatroom_id(long chatroom_id) {
        this.chatroom_id = chatroom_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return id == message.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

package team.dahamdol.dahamnote_deafperson.chatting;

public class MessageData {
    public String name;
    public String content;

    public MessageData( String name,String content) {
        this.content = content;
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

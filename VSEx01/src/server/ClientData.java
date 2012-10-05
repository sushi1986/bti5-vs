package server;

import java.util.Date;

public class ClientData {
    String clientId;
    long messageId;
    long timeout;
    
    public ClientData(String clientId, long messageId, long timeout) {
        this.clientId = clientId;
        this.messageId = messageId;
        this.timeout = new Date().getTime() + timeout;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = new Date().getTime() + timeout;
    }

    public String getClientId() {
        return clientId;
    }
}

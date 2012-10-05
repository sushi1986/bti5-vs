package server;

import java.sql.Timestamp;
import java.util.Date;

public class Message {
    long messageId;
    String clientId;
    String message;
    Timestamp timestamp;
    
    public Message(long messageId, final String clientId, String message) {
        this.messageId = messageId;
        this.clientId = clientId;
        this.message = message;
        this.timestamp = new Timestamp(new Date().getTime()); 
    }

    public long getMessageId() {
        return messageId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((clientId == null) ? 0 : clientId.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + (int) (messageId ^ (messageId >>> 32));
        result = prime * result
                + ((timestamp == null) ? 0 : timestamp.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Message other = (Message) obj;
        if (clientId == null) {
            if (other.clientId != null)
                return false;
        } else if (!clientId.equals(other.clientId))
            return false;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        if (messageId != other.messageId)
            return false;
        if (timestamp == null) {
            if (other.timestamp != null)
                return false;
        } else if (!timestamp.equals(other.timestamp))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return messageId + " " + clientId + " : " + message + " " + timestamp;
    }
}

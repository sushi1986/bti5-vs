package server;

public class IdGenerator {
    long currentId;
    
    public IdGenerator() {
        currentId = 0;
    }
    
    public long nextId() {
        return ++currentId;
    }
}

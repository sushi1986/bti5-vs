package server;

/**
 * VS Lab 1
 * HAW Hamburg
 * 
 * @author Phillip Gesin, Raphael Hiesgen
 * 
 * Generates ids.
 */

public class IdGenerator {
    long currentId;
    
    public IdGenerator() {
        currentId = 0;
    }
    
    public long nextId() {
        return ++currentId;
    }
}

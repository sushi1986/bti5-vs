package mware_lib;

/**
 * VSP Lab03
 * @date 23.11.2012
 * @author Phillip Gesien, Raphael Hiesgen
 */

public abstract class NameService {
	public abstract void rebind(Object servant, String name);
	public abstract Object resolve(String name);
}

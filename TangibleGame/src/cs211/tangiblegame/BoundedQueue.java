package cs211.tangiblegame;

import java.util.LinkedList;
/**
 * @url http://stackoverflow.com/questions/5498865/size-limited-queue-that-holds-last-n-elements-in-java
 */
@SuppressWarnings("serial")
public class BoundedQueue<E> extends LinkedList<E> {
    private int limit;

    public BoundedQueue(int limit) {
        this.limit = limit;
    }
    
    public void setLimit(int limit) {
    	this.limit = limit;
    	while (size() > limit) {
    		super.remove();
    	}
    }

    @Override
    public boolean add(E o) {
        boolean added = super.add(o);
        while (added && size() > limit) {
        	super.remove();
        }
        return added;
    }
}

package common;

/**
 *
 * @author Dustin Dannenhauer
 * @email  dtd212@lehigh.edu
 * @date   Aug 7, 2013
 */
public class Pair<T1,T2> {

    private T1 first = null;
    private T2 second = null;
    
    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }
    
    public T1 getFirst() {
        return this.first;
    }
    
    public T2 getSecond() {
        return this.second;
    }
    
    public String toString() {
        return "<"+this.first.toString() + ","+this.second.toString()+">";
    }
    
    public boolean equals(Pair p) {
        return this.first.equals(p.getFirst()) && this.second.equals(p.getSecond());
    }
}

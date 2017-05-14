package temple.core;

import java.util.LinkedList;

/**
 * Created by temple on 2014/12/13.
 */
public class Way {
    public LinkedList<Node> way;
    public Way() {
        way = new LinkedList<Node>();
    }
    public void add(Node n){
        way.add(n);
    }
}

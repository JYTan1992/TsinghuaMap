package temple.core;

import java.util.LinkedList;

public class SearchResult{
   public Cost c;
   public  LinkedList<Node> route=new LinkedList<Node>();
    public SearchResult(){c=null;route=null;}
    public void setCost(Cost c){this.c=c;}
    public void setRoute(LinkedList<Node> route){this.route=route;}
}

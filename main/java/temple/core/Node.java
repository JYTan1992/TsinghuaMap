package temple.core;

public class Node {
   public long id;
   public double lat;
   public double lon;
   public String name;
   public Node(long id, double lat, double lon) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        name=null;
    }
    public void setName(String n){name=n;}
}

package temple.core;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OSM {
    private ConcurrentHashMap<Long,Node> nodes;
    private Graph map;
    private LinkedList<Way> busline;

    public OSM(InputStream stream1,InputStream stream2){
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser saxParser = null;
        try {
        	InputSource myxml=new InputSource(stream1);
            // 鍒涘缓涓�釜瑙ｆ瀽鍣⊿AXParser瀵硅薄
            saxParser = spf.newSAXParser();
            // 寰楀埌SAXParser涓皝瑁呯殑SAX XMLReader
            NodeEnter counter=new NodeEnter();
            saxParser.parse(myxml,counter);
            //寰楀埌nodes
            nodes=counter.getNodes();
            //System.out.println(nodes.size());
            //寤虹珛鏈夋晥鑺傜偣鍥�
            GraphBuilder graphBuilder;
            graphBuilder=new GraphBuilder(nodes);
            ///////////////////////////////////////////////////////////////
            myxml=new InputSource(stream2);
            saxParser.parse(myxml,graphBuilder);

            map=graphBuilder.getMap();
            busline = graphBuilder.getBusLine();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public OSMResult FindRoute(String start,String target,int cons,Node nowNode){
        OSMResult osmResult=new OSMResult();
        Node startNode=null;
        Node targetNode=null;
        double r = 0.05;
        //瀵绘壘鍑哄彂鍦板拰鐩殑鍦�
        if(start.equals("我的位置")){
            startNode = nowNode;
            osmResult.Check[0]=true;
            r=0.1;
        }
        Iterator iter = nodes.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Long,Node> entry = (Map.Entry<Long,Node>) iter.next();
            Node val = entry.getValue();
            if(val.name!=null) {
                if (val.name.equals(start)) {
                    startNode=val;
                    osmResult.Check[0]=true;
                    if(targetNode!=null)break;
                }
                if(val.name.equals(target)){
                    targetNode=val;
                    osmResult.Check[1]=true;
                    if(startNode!=null)break;
                }
            }
        }
        if(startNode==null||targetNode==null){
            return osmResult;
        }

        //瀵昏矾
        osmResult.result =map.FindPath(startNode,targetNode,2,cons,new Cost(1000,1000),r);
        return osmResult;
    }

    public LinkedList<Way> getBusLine(){
        return busline;
    }
}



class Calc {
    private static double EARTH_RADIUS = 6378.137;
    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }
    public static double GetDistance(Node n1, Node n2) {
        double radLat1 = rad(n1.lat);
        double radLat2 = rad(n2.lat);
        double a = radLat1 - radLat2;
        double b = rad(n1.lon) - rad(n2.lon);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) + Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
        s = s * EARTH_RADIUS;
        return s;
    }
}



class Edge{
    Double dis;
    Long next;
    double []way;
    Edge(Double dis,double []way){this.dis=dis;this.way=way;next=null;}
    void setNext (Long next){this.next=next;}
}

class GraphNode{
    Node n;
    LinkedList<Edge> edge;
    GraphNode(Node n){this.n=n;edge=new LinkedList<Edge>();}
}

class PathNode{
    GraphNode now;
    Cost c;
    double f;
    PathNode p;
    PathNode(GraphNode now,Cost c,double f,PathNode p){this.now=now;this.c=c;this.f=f;this.p=p;}
}



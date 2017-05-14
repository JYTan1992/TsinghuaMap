package temple.core;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by temple on 2014/6/7.
 */
public class GraphBuilder extends DefaultHandler {
    private ConcurrentHashMap<Long, Node> nodes;
    private Graph map;
    private int way_flag;
    private double[] way;
    private double[] way_reverse;
    private String id;
    private LinkedList<Long> nodelist;
    private LinkedList<Way> busline =new LinkedList<Way>();
    private Way busway;
    private LinkedList<Long> bus= new LinkedList<Long>(){{add((long) 83800922);add((long)33456967);add((long)80319243);add((long)226625049);add((long)83800921);add((long)32954651);add((long)84727247);add((long)83800915);add((long)79696975);add((long)83800918);add((long)84652153);add((long)83689276);add((long)81813449);add((long)32954653);add((long)83689302);add((long)22799992);add((long)83800912);add((long)32954774);add((long)83800913);add((long)83689620);add((long)83800914);add((long)83892648);add((long)80342198);}};
    private boolean inBus = false;
    public GraphBuilder(ConcurrentHashMap<Long, Node> nodes) {
        this.nodes = nodes;
        nodelist = new LinkedList<Long>();
        map = new Graph();
        way_flag = 0;
    }
    //对每一个开始元属进行处理

    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts)
            throws SAXException {
        if (qName.equals("way")) {
            nodelist.clear();
            id = atts.getValue("id");
            way_flag = 1;
            way = new double[]{0, 0, 0};
            way_reverse = new double[]{0, 0, 0};
            if(bus.contains(Long.valueOf(id))){
                inBus = true;
                busway = new Way();
            }
            else {
                inBus = false;
            }
        } else if (way_flag == 1) {
            if (qName.equals("nd")) {
                long nd = Long.valueOf(atts.getValue("ref"));
                nodelist.offerLast(nd);
                if (inBus){
                    busway.add(nodes.get(nd));
                }
            } else if (qName.equals("tag")) {
                String k = atts.getValue("k");
                if (k.equals("highway")) {
                    String highway = atts.getValue("v");
                    if (highway != null) {
                        if (highway.equals("motorway") || highway.equals("motorway_link") || highway.equals("motorway_junction") || highway.equals("trunk") || highway.equals("trunk_link") || highway.equals("primary_link")) {
                            way[0] = 90;
                            way_reverse[0] = 90;
                            way[1] = 15;
                            way_reverse[1] = 15;
                            way[2] = 6;
                        } else if (highway.equals("primary") || highway.equals("service") || highway.equals("secondary") || highway.equals("tertiary") || highway.equals("unclassified") || highway.equals("unsurfaced") || highway.equals("track") || highway.equals("residential") || highway.equals("living_street")) {
                            way[0] = 60;
                            way[1] = 15;
                            way[2] = 5;
                            way_reverse[2] = 5;
                        } else if (highway.equals("bridleway") || highway.equals("footway") || highway.equals("pedestrian") || highway.equals("steps")) {
                            way[2] = 4;
                            way_reverse[2] = 5;
                        }
                        else{
                            way = new double[]{40, 15, 5};
                            way_reverse = new double[]{40, 15, 5};
                        }
                    }
                } else if (k.equals("motorcar")) {
                    String v = atts.getValue("v");
                    if (v.equals("yes")) {
                        way[0] = 60;
                    } else if (v.equals("no")) {
                        way[0] = 0;
                    }
                } else if (k.equals("bicycle")) {
                    String v = atts.getValue("v");
                    if (v.equals("yes")) {
                        way[1] = 15;
                    } else if (v.equals("no")) {
                        way[1] = 0;
                    }
                } else if (k.equals("foot")) {
                    String v = atts.getValue("v");
                    if (v.equals("yes")) {
                        way[2] = 5;
                        way_reverse[2] = 5;
                    } else if (v.equals("no")) {
                        way[2] = 0;
                        way_reverse[2] = 5;
                    }
                } else if (k.equals("oneway") && atts.getValue("v").equals("yes")) {
                    way_reverse[0] = 0;
                    way_reverse[1] = 0;
                }
            }
        }
    }

    public void endElement(String namespaceURI, String localName,
                           String qName)
            throws SAXException {
        if (qName.equals("way")) {
            try {
                if(inBus){
                    busline.add(busway);
                }
                if(way[0]!=0||way[1]!=0||way[2]!=0) {
                    long n1 = nodelist.pollFirst();
                    Node nd1 = nodes.get(n1);
                    /*
                    boolean reverse_dir = false;
                    for (double r : way_reverse) {
                        if (r != 0) {
                            reverse_dir = true;
                            break;
                        }
                    }
                    */
                    for (Long n2 : nodelist) {
                        Node nd2 = nodes.get(n2);
                        if (nd1 != null && nd2 != null) {
                            Double dis = Calc.GetDistance(nd1, nd2);
                            Edge e1 = new Edge(dis, way);
                            map.add(new GraphNode(nd1), new GraphNode(nd2), e1);
                            Edge e2 = new Edge(dis, way);
                            map.add(new GraphNode(nd2), new GraphNode(nd1), e2);
                            /*
                            if (reverse_dir) {
                                e = new Edge(dis, way_reverse);
                                map.add(new GraphNode(nd2), new GraphNode(nd1), e);
                            }
                            */
                        }
                        nd1 = nd2;
                    }
                }
            } catch (Exception ex) {
                System.out.println("错误id：" + id);
                ex.printStackTrace();
                System.exit(1);
            }
            way_flag = 0;
        }
    }

    public Graph getMap() {
        return map;
    }

    public LinkedList<Way> getBusLine(){
        return busline;
    }
}

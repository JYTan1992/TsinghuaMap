package temple.core;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by temple on 2014/6/7.
 */

public class NodeEnter extends DefaultHandler {
    private ConcurrentHashMap<Long, Node> nodes; //杩欎釜HashMap鐢ㄦ潵璁板綍node
    String name;
    Node n;
    long id;
    // 处理文档前的工作
    public void startDocument() throws SAXException {
        nodes = new ConcurrentHashMap<Long, Node>();//鍒濆鍖朒ashMap
    }

    //对每一个开始元属进行处理
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts)
            throws SAXException {
        if (qName.equals("node")) {
            double lat, lon;
            id = Long.valueOf(atts.getValue("id"));
            lat = Double.valueOf(atts.getValue("lat"));
            lon = Double.valueOf(atts.getValue("lon"));
            n= new Node(id, lat, lon);
        }
        else if(qName.equals("tag")&&atts.getValue("k").equals("name")){
            name=atts.getValue("v");
        }
        else if(qName.equals("nd")){
            id = Long.valueOf(atts.getValue("ref"));
        }
    }

    public void endElement(String namespaceURI, String localName,
                           String qName)
            throws SAXException {
        if(qName.equals("node")){
            if(name != null){
                n.setName(name);
                name=null;
            }
            nodes.put(id,n);
        }
        else if(qName.equals("way")){
            if(name != null){
                Node nd = nodes.get(id);
                nd.setName(name);
                name=null;
            }
        }
    }

    public ConcurrentHashMap<Long, Node> getNodes() {
        return nodes;
    }
}


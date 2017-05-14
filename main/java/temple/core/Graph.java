package temple.core;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Created by temple on 2014/6/8.
 */

public class Graph {
    private ConcurrentHashMap<Long,GraphNode> graph;
    public Graph(){
        graph=new ConcurrentHashMap<Long, GraphNode>();
    }
    public ConcurrentHashMap<Long,GraphNode> getGraph(){
        return graph;
    }
    public void add(GraphNode n1,GraphNode n2,Edge e){
        Long id1=n1.n.id;
        Long id2=n2.n.id;
        if(!graph.containsKey(id2)){
            graph.put(id2,n2);
        }
        e.setNext(id2);
        GraphNode gn1=n1;
        if(graph.containsKey(id1)){
            gn1=graph.get(id1);
        }
        gn1.edge.push(e);
        graph.put(id1, gn1);
    }

    public SearchResult FindPath(Node start,Node target,int trans,int cons,Cost limit,double r){
        //记录已经搜索过的点
        ConcurrentHashMap<Long,PathNode> close=new ConcurrentHashMap<Long,PathNode>();
        //优先队列，存储候选点;使用ConcurrentHashMap加快搜索
        ConcurrentHashMap<Long,PathNode> open=new ConcurrentHashMap<Long,PathNode>();
        Comparator<PathNode> cmp;
        cmp = new Comparator<PathNode>() {
            public int compare(PathNode n1, PathNode n2) {
                if (n1.f < n2.f) {
                    return -1;
                }
                else if(n1.f > n2.f) {
                    return 1;
                }
                else {
                    return 0;
                }
            }
        };
        Queue<PathNode> openlist = new PriorityQueue<PathNode>(1024,cmp);
        //路径输出
        SearchResult result =new SearchResult();
        PathNode end=null;
        LinkedList<Node> route=new LinkedList<Node>();
        //寻路初始
        double []speed=new double[]{30,15,5};
        double []punish=new double[]{0.1,0.05,0};
        double []topspeed=new double[]{90,30,6};
        double DIS=Calc.GetDistance(start,target);
        PathNode pstart=new PathNode(new GraphNode(start),new Cost(),new Cost(DIS,DIS/topspeed[trans]).value(cons),null);
        Iterator iter = graph.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Long, GraphNode> entry = (Map.Entry<Long, GraphNode>) iter.next();
            GraphNode val = entry.getValue();
            Long key = entry.getKey();
            double R=Calc.GetDistance(val.n,start);
            if(R<=r){
                Cost c1=new Cost(1.4*R,punish[trans]+1.4*R/speed[trans]);
                double fdis=Calc.GetDistance(val.n,target);
                Cost cf=new Cost(fdis,fdis/topspeed[trans]);
                cf.add(c1);
                PathNode pn1=new PathNode(val,c1,cf.value(cons),pstart);
                openlist.add(pn1);
                open.put(key,pn1);
            }
        }
        GraphNode gtarget = new GraphNode(target);
        PathNode lastnode=pstart;
        double still=limit.dis;

        while(!openlist.isEmpty()) {
            while (!openlist.isEmpty()) {
                PathNode pn = openlist.poll();
                Long pre = pn.now.n.id;
                open.remove(pre);
                close.put(pre, pn);
                if (pre.equals(target.id)) {
                    end = pn;
                    break;
                }
                Cost c1 = pn.c;
                double R = Calc.GetDistance(pn.now.n, target);
                if (R <= r) {
                    Cost ct = new Cost(1.4*R,1.4*R / speed[trans]);
                    ct.add(c1);
                    PathNode fend = new PathNode(gtarget, ct, ct.value(cons), pn);
                    Long endId = gtarget.n.id;
                    if (!open.containsKey(endId)) {
                        openlist.add(fend);
                        open.put(endId, fend);
                    } else {
                        PathNode nn = open.get(endId);
                        if (nn.c.value(cons) > ct.value(cons)) {
                            openlist.remove(nn);
                            open.remove(endId);
                            openlist.add(fend);
                            open.put(endId, fend);
                        }
                    }
                } else {
                    if (R < still) {
                        still = R;
                        lastnode = pn;
                    }
                }
                for (Edge e : pn.now.edge) {
                    if (e.way[trans] != 0) {
                        if (!close.containsKey(e.next)) {
                            Cost cn = new Cost();
                            Cost ce = new Cost(e.dis, e.dis / e.way[trans]);
                            cn.add(c1);
                            cn.add(ce);
                            if (cn.under(limit)) {
                                Long nid = e.next;
                                GraphNode ng = graph.get(nid);
                                double dis = Calc.GetDistance(ng.n, target);
                                Cost cf = new Cost(dis, dis / topspeed[trans]);
                                cf.add(cn);
                                PathNode npn = new PathNode(ng, cn, cf.value(cons), pn);
                                if (!open.containsKey(nid)) {
                                    openlist.add(npn);
                                    open.put(nid, npn);
                                } else {
                                    PathNode nn = open.get(nid);
                                    if (nn.c.value(cons) > cn.value(cons)) {
                                        openlist.remove(nn);
                                        open.remove(nid);
                                        openlist.add(npn);
                                        open.put(nid, npn);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if(end==null){
                iter = graph.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<Long, GraphNode> entry = (Map.Entry<Long, GraphNode>) iter.next();
                    GraphNode val = entry.getValue();
                    Long key = entry.getKey();
                    if(!close.containsKey(key)) {
                        double R = Calc.GetDistance(val.n, lastnode.now.n);
                        if (R <= 2*r) {
                            Cost c1 = new Cost(2 * R,2*R / speed[trans]);
                            c1.add(lastnode.c);
                            double fdis = Calc.GetDistance(val.n, target);
                            Cost cf = new Cost(fdis, fdis / topspeed[trans]);
                            cf.add(c1);
                            PathNode pn1 = new PathNode(val, c1, cf.value(cons), lastnode);
                            openlist.add(pn1);
                            open.put(key, pn1);
                        }
                    }
                }
            }
            else {
                break;
            }
        }
        if(end!=null){
            result.setCost(end.c);
            while(end!=null){
                Node n=end.now.n;
                route.push(n);
                end=end.p;
            }
            result.setRoute(route);
        }

        return result;
    }
}

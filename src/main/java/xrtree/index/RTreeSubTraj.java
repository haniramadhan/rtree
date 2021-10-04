package xrtree.index;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.geometry.Rectangle;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.Serializable;

public class RTreeSubTraj implements Serializable {
    private final int splitNumber;
    private final RTree<Integer, Rectangle> submbrTree;
    private final RTree<Integer, Rectangle> mbrTree;
    private final RTree<Integer, Point> firstPointTree;
    private final RTree<Integer, Point> lastPointTree;
    private final TIntObjectHashMap lengthMap;
    private final SubTrajTable subTrajTable;

    public RTree getSubmbrTree(){
        return submbrTree;
    }
    public RTree getMbrTree(){
        return mbrTree;
    }

    public SubTrajTable getSubTrajTable(){
        return subTrajTable;
    }

    public RTreeSubTraj(int splitNumber, RTree mbrTree, RTree subTree,
                        RTree firstPointTree, RTree lastPointTree,
                        TIntObjectHashMap lengthMap, SubTrajTable subTrajTable){
        this.splitNumber = splitNumber;
        this.mbrTree = mbrTree;
        this.submbrTree = subTree;
        this.firstPointTree = firstPointTree;
        this.lastPointTree = lastPointTree;
        this.lengthMap = lengthMap;
        this.subTrajTable = subTrajTable;
    }

    public RTree<Integer, Point> getFirstPointTree() {
        return firstPointTree;
    }

    public RTree<Integer, Point> getLastPointTree() {
        return lastPointTree;
    }

    public TIntObjectHashMap getLengthMap(){
        return lengthMap;
    }
    public TIntArrayList getTidWithLenBetween(int p, int q){
        TIntArrayList tidList = new TIntArrayList();
        for(int i=p;i<=q;i++){
            TIntArrayList tidWithLen = (TIntArrayList) lengthMap.get(i);
            if(tidWithLen == null)
                continue;
            tidList.addAll(tidWithLen);
        }
        return tidList;
    }
    public int getSplitNumber(){
        return  splitNumber;
    }

}

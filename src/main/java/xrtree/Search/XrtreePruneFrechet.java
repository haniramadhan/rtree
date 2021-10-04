package xrtree.Search;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.geometry.Rectangle;
import gnu.trove.list.array.TIntArrayList;
import xrtree.Utils.Util;
import xrtree.index.RTreeSubTraj;

import java.util.BitSet;
import java.util.List;

import static com.github.davidmoten.rtree.geometry.Geometries.point;
import static com.github.davidmoten.rtree.geometry.Geometries.rectangle;

public class XrtreePruneFrechet extends XrtreePruneUtil {



    public static void execute(RTreeSubTraj index, double tau){
        trajLevelPrune(index, tau);
        finalResult = computeTrajLevelResult();
        BitSet[] matchTable = new BitSet[querySubTrajLengths.length];
        for(int i=SUBSTART;i< queryPointPairs.length;i++){
            XrtreePruneUtil.subtrajLevelPrune(index.getSubmbrTree(),tau,i);
            matchTable[i-2] = XrtreePruneUtil.computeSubtrajResult(i);
        }
        finalResult = FrechetDtwPrune.retrieveTidFromMatchTable(matchTable, index.getSubTrajTable(), finalResult,
                null);
    }

    public static void trajLevelPrune(RTreeSubTraj index,double tau){
        Rectangle qRange = rectangle(queryPointPairs[MBR][MIN][0] - tau,
                queryPointPairs[MBR][MIN][1] - tau,
                queryPointPairs[MBR][MAX][0] + tau,
                queryPointPairs[MBR][MAX][1] + tau);

        RTree<Integer, Rectangle> mbrTree = index.getMbrTree();
        mbrTree.search(qRange).map(Entry::value).subscribe(results[MBR][0]::set);
        RTree<Integer, Point> fpTree = index.getFirstPointTree();
        results[POSITION][FIRST] = searchByQueryPointDistance(queryPointPairs[POSITION][FIRST],
                fpTree,tau);
        RTree<Integer, Point> lpTree = index.getLastPointTree();
        results[POSITION][LAST] = searchByQueryPointDistance(queryPointPairs[POSITION][LAST],
                lpTree,tau);
    }

    private static BitSet searchByQueryPointDistance
            (double[] queryPoint, RTree<Integer, Point> index, double tau) {
        Rectangle qRange = rectangle(queryPoint[0] - tau, queryPoint[1] - tau,
                queryPoint[0] + tau, queryPoint[1] + tau);

        BitSet pointResult = new BitSet();
        index.search(qRange).map(Entry::value).subscribe(pointResult::set);

        return pointResult;
    }

    public static BitSet computeTrajLevelResult(){
        BitSet result = new BitSet();

        result.or(results[MBR][0]);
        result.and(results[POSITION][FIRST]);
        result.and(results[POSITION][LAST]);
        return result;
    }

}

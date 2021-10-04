package xrtree.Search;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;
import gnu.trove.list.array.TIntArrayList;
import xrtree.Utils.Distance;
import xrtree.Utils.MoreTrajUtil;
import xrtree.Utils.TrajUtil;
import xrtree.index.RTreeSubTraj;

import java.util.BitSet;
import java.util.List;

import static com.github.davidmoten.rtree.geometry.Geometries.rectangle;

public class XrtreePruneUtil extends PruneUtil{

    protected static void extractQueryPointPairs(List<double[]> trajQuery, TIntArrayList indices){

        double[][] mbr = TrajUtil.findMBR(trajQuery);

        queryPointPairs = new double[2 + indices.size()][][];
        queryPointPairs[0] = mbr;
        queryPointPairs[1] = new double[2][];//MIN & MAX points
        queryPointPairs[1][0] = trajQuery.get(0);
        queryPointPairs[1][1] = trajQuery.get(trajQuery.size()-1);

        querySubTrajLengths = new int[indices.size()];

        for(int s=0;s<indices.size();s++){
            List<double[]> subTraj;
            if(s==0)
                subTraj = trajQuery.subList( 0,  indices.get(s));
            else
                subTraj = trajQuery.subList( indices.get(s - 1),  indices.get(s));
            querySubTrajLengths[s] = subTraj.size();
            queryPointPairs[2 + s] = TrajUtil.findMBR(subTraj);
        }
    }

    protected static void initResults(){
        PruneUtil.initResults();
        for(int i=SUBSTART;i< queryPointPairs.length;i++){
            results[i] = new BitSet[3]; //for MIN, MAX
            for(int j=0;j< results[i].length;j++)
                results[i][j] = new BitSet();
        }
    }


    public static void prune(RTreeSubTraj index, List<double[]> trajQuery, double tau) {


        switch (Distance.currentDistance){
            case Distance.DTW:
                XrtreePruneDTW.execute(index, tau);
                break;
            case Distance.FRECHET:
                XrtreePruneFrechet.execute(index, tau);
                break;
            case Distance.EDR:
            case Distance.LCSS:
                XrtreePruneEdrLcss.execute(index,trajQuery.size(),Distance.eps,tau);
                break;
            default:
                break;
        }

        extractTidFromFinalResult();
    }

    public static void initEachQueryV1(List<double[]> trajQuery,int split){
        TIntArrayList indices = MoreTrajUtil.heurSplit(trajQuery,split-1);
        extractQueryPointPairs(trajQuery, indices);
        initResults();
    }


    public static void subtrajLevelPrune(RTree<Integer,Rectangle> indexSubTraj,
                                         double tau, int i){

        BitSet res = new BitSet();
        Rectangle qRectangle = rectangle(queryPointPairs[i][MIN][0]-tau,
                queryPointPairs[i][MIN][1]-tau,
                queryPointPairs[i][MAX][0]+tau,
                queryPointPairs[i][MAX][1]+tau);
        indexSubTraj.search(qRectangle).map(a -> a.value())
                .subscribe(a -> res.set(a));

        results[i][0] = res;
    }

    public static BitSet computeSubtrajResult(int iQ){
        return results[iQ][0];
    }
}

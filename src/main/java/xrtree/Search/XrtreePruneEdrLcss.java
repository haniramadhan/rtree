package xrtree.Search;


import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.geometry.Rectangle;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import xrtree.Utils.Distance;
import xrtree.Utils.Util;
import xrtree.index.RTreeSubTraj;
import xrtree.index.SubTrajTable;

import java.util.BitSet;
import java.util.PriorityQueue;

import static com.github.davidmoten.rtree.geometry.Geometries.rectangle;

public class XrtreePruneEdrLcss extends XrtreePruneUtil {
    private static BitSet resultFilterByLength;
    private static PriorityQueue<Util.IntDblPair> dissimilarDist;

    public static void trajLevelPrune(RTreeSubTraj index, int trajLength, double eps, double tau){

        resultFilterByLength = EdrLcssPrune.filterByLength(trajLength,index.getLengthMap(),tau);

        //intersect!
        Rectangle qRange = rectangle(queryPointPairs[MBR][MIN][0] - eps,
                queryPointPairs[MBR][MIN][1] - eps,
                queryPointPairs[MBR][MAX][0] + eps,
                queryPointPairs[MBR][MAX][1] + eps);

        RTree<Integer, Rectangle> mbrTree = index.getMbrTree();
        mbrTree.search(qRange).map(Entry::value).subscribe(results[MBR][0]::set);
    }

    public static void computeDissimilarDist(int qTrajLen, TIntObjectHashMap lengthMap,
                                             double tau, BitSet trajLevelResult) {
        dissimilarDist = new PriorityQueue<>();
        int minLength = qTrajLen - (int) tau;
        if(minLength<0)
            minLength = 0;

        for(int i=minLength;i<=qTrajLen+(int) tau;i++){
            TIntArrayList trajs  = (TIntArrayList) lengthMap.get(i);
            if(trajs == null)
                continue;

            double dist = computeDiss(i,qTrajLen);
            for(int tid:trajs.toArray()) {
                if(trajLevelResult.get(tid))
                    continue;

//                if(!KNN_MODE || !KnnPruneUtil.ifTidComputed(tid))
//                    dissimilarDist.add(new Util.IntDblPair(tid, dist));
            }
        }
    }

    public static double computeDiss(int length1, int length2){
//        if(Distance.currentDistance == Distance.EDR)
//            return Math.max(length1,length2);
//        return length1+length2;
        return 0;
    }

    public static BitSet computeTrajLevelResult(){
        BitSet result = new BitSet();
        result.or(resultFilterByLength);
        result.and(results[MBR][0]);
//        if(KNN_MODE)
//            result.andNot(KnnPruneUtil.getEstimatedTid());
        return  result;
    }



    private static void addDissimilarsToComputed(double tau){
        for(int iDiss = 0; iDiss<dissimilarDist.size();iDiss++){
            Util.IntDblPair dissPair = dissimilarDist.peek();
            int disTid = dissPair.intVal;
            double dissDist = dissPair.dblVal;

            if(dissDist <= tau) {
                finalResult.set(disTid);
                dissimilarDist.poll();
            }
            else
                break;

//            if(KNN_MODE && !KnnPruneUtil.isEstimated(disTid)) {
//                KnnPruneUtil.addEstimatedDistance(disTid,dissDist);
//            }
        }
    }


    public static void execute(RTreeSubTraj index, int trajLength, double eps, double tau){

//        if(!KNN_MODE || !hasRangeQueried()) {
            trajLevelPrune(index, trajLength, eps, tau);
            for (int i = SUBSTART; i < queryPointPairs.length; i++) {
                XrtreePruneUtil.subtrajLevelPrune(index.getSubmbrTree(), eps, i);
            }
//        }
//        else
//            EdrLcssPrune.filterByLength(trajLength,index.getLengthMap(),tau);
//        trajLevelPrune(index,trajLength,eps,tau);

        BitSet result = computeTrajLevelResult();
        SubTrajTable table = index.getSubTrajTable();
        int currentQsubtrajLastIndex = 0;
        BitSet[] matchTable = new BitSet[querySubTrajLengths.length];
        for (int i = SUBSTART; i < queryPointPairs.length; i++) {
            matchTable[i - 2] = XrtreePruneUtil.computeSubtrajResult(i);
            currentQsubtrajLastIndex += querySubTrajLengths[0];
            if (Distance.currentDistance == Distance.LCSS)
                matchTable[i - 2] = EdrLcssPrune.applySpaceConstraint(matchTable[i - 2], table, currentQsubtrajLastIndex,
                        querySubTrajLengths[i - 2]);
        }

        finalResult = EdrLcssPrune.retrieveTidFromMatchTable(matchTable, table, result,
                tau, querySubTrajLengths);
        computeDissimilarDist(trajLength, index.getLengthMap(), tau, result);

        addDissimilarsToComputed(tau);
    }



}

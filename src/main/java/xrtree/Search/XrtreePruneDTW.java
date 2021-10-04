package xrtree.Search;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.geometry.Rectangle;
import rx.Observable;
import xrtree.Utils.Util;
import xrtree.index.RTreeSubTraj;

import java.util.*;

import static com.github.davidmoten.rtree.geometry.Geometries.point;
import static com.github.davidmoten.rtree.geometry.Geometries.rectangle;

public class XrtreePruneDTW extends XrtreePruneUtil {

    public static void trajLevelPrune(RTreeSubTraj index, double tau){

        Rectangle rMbr = rectangle(queryPointPairs[MBR][MIN][0] - tau,
                queryPointPairs[MBR][MIN][1] - tau,
                queryPointPairs[MBR][MAX][0] + tau,
                queryPointPairs[MBR][MAX][1] + tau);


        RTree<Integer, Rectangle> mbrTree = index.getMbrTree();
        Observable<Entry<Integer, Rectangle>> mbrResult =
                mbrTree.search(rMbr);

        List<Integer> rMbrIdList = mbrResult.map(Entry::value).toList().toBlocking().single();
        for (int id : rMbrIdList)
            results[MBR][0].set(id);
    }

    public static BitSet computeTrajLevelResult(){
        return results[MBR][0];
    }

    public static void execute(RTreeSubTraj index, double tau){
//        trajLevelPrune(index,tau);
//        BitSet result = computeTrajLevelResult();
//        if(KNN_MODE)
//            result.andNot(KnnPruneUtil.getComputedTid());



        List<Util.IntDblPair> firstO = searchByFirstLastDtw(queryPointPairs[POSITION][FIRST],index.getFirstPointTree(),tau);
        List<Util.IntDblPair> lastO = searchByFirstLastDtw(queryPointPairs[POSITION][LAST],index.getLastPointTree(),tau);


        finalResult = combineFirstLast(firstO,lastO,tau);
        BitSet[] matchTable = new BitSet[querySubTrajLengths.length];
        for(int i=SUBSTART;i< queryPointPairs.length;i++){
            XrtreePruneUtil.subtrajLevelPrune(index.getSubmbrTree(),tau,i);
            matchTable[i-2] = XrtreePruneUtil.computeSubtrajResult(i);
        }

        finalResult = FrechetDtwPrune.retrieveTidFromMatchTable(matchTable, index.getSubTrajTable(), finalResult,
                null);
    }


    private static List<Util.IntDblPair> searchByFirstLastDtw
            (double[] queryPoint, RTree<Integer, Point> index, double tau) {
        Rectangle qRange = rectangle(queryPoint[0] - tau, queryPoint[1] - tau,
                queryPoint[0] + tau, queryPoint[1] + tau);

        Point qp = point(queryPoint[0], queryPoint[1]);
        List<Util.IntDblPair> result =
                index.search(qRange)
                        .map( a-> new Util.IntDblPair(a.value(), qp.distance(a.geometry())))
                        .toList().toBlocking().single();;

        return result;
    }

    private static BitSet combineFirstLast(List<Util.IntDblPair> firstResult,
                                           List<Util.IntDblPair> lastResult, double tau){
        BitSet result = new BitSet();

        Collections.sort(firstResult, Comparator.comparingInt(o -> o.intVal));
        Collections.sort(lastResult, Comparator.comparingInt(o -> o.intVal));

        int iF = 0, iL = 0;
        while(iF<firstResult.size() && iL < lastResult.size()){
            if(firstResult.get(iF).intVal<lastResult.get(iL).intVal)
                iF++;
            else if(firstResult.get(iF).intVal>lastResult.get(iL).intVal)
                iL++;
            else{
                if(firstResult.get(iF).dblVal+lastResult.get(iL).dblVal <= tau)
                    result.set(firstResult.get(iF).intVal);
                iF++;
                iL++;
            }
        }

        return result;
    }

//TODO: an optimization for DTW pruning should be implemented for sub-traj level
//
//    private static List<Util.IntDblPair> searchByIndexMbrDTW(BitSet prevBitSetResult, double[][] mbr, FloodIndex index, double eps) {
//        List<Util.IntDblPair> currentResultPair = new ArrayList<>();
//        double[][] ranges = XfistPruneCoverUtil.findRangeMbrEps(mbr,index.getSortedDim(),eps);
//        TIntList iBins = XfistPruneCoverUtil.findBinWithinRange(mbr[0].length-1,ranges[INDEX_MIN],ranges[INDEX_MAX],index.getNonSortedDimTails());
//
//
//        for (int i = 0; i < iBins.size(); i++) {
//            List<Util.IntDblPair> currentResultPairBin = new ArrayList<>();
//
//            int ib = iBins.get(i);
//
//            if (index.getLrSetArray()[ib] == null)
//                continue;
//            int lowPos = findByKeyAtPartition(index, ib, mbr[INDEX_MIN], eps, true);
//            int highPos = findByKeyAtPartition(index, ib, mbr[INDEX_MAX], eps, false);
//
//            TIntList resultBin = index.getArrayPtrSubList(lowPos, highPos);
//
//            for (int j = lowPos; j < highPos; j++) {
//                int tid = resultBin.get(j - lowPos);
//                if(!prevBitSetResult.get(tid))
//                    continue;
//                currentResultPairBin.add(new Util.IntDblPair(tid, 0));
//            }
//            currentResultPair = ResultOpUtil.addAllDistSortedUnsorted(currentResultPair,currentResultPairBin);
//        }
//        return currentResultPair;
//    }
//
//
//    private static List<Util.IntDblPair> searchByIndexIntersectDTW(BitSet prevBitSetResult, double[] queryPoint, FloodIndex index, double eps, boolean isIndexMin) {
//        List<Util.IntDblPair> currentResultPair = new ArrayList<>();
//
//
//        double[][] ranges = XfistPruneIntersectUtil.findRangeIntersect(queryPoint,isIndexMin, index.getSortedDim(),eps);
//        TIntList iBins = XfistPruneCoverUtil.findBinWithinRange(queryPoint.length-1,ranges[INDEX_MIN],ranges[INDEX_MAX],index.getNonSortedDimTails());
//        for (int i = 0; i < iBins.size(); i++) {
//            List<Util.IntDblPair> currentResultPairBin = new ArrayList<>();
//
//            int ib = iBins.get(i);
//
//            if (index.getLrSetArray()[ib] == null)
//                continue;
//            int lowPos = index.getFirstPositionAtBin(ib), highPos = index.getLastPositionAtBin(ib)+1;
//            if(isIndexMin)
//                highPos = findByKeyAtPartition(index, ib, queryPoint, eps, false);
//            else
//                lowPos = findByKeyAtPartition(index, ib, queryPoint, eps, true);
//
//            TIntList resultBin = index.getArrayPtrSubList(lowPos, highPos);
//
//            for (int j = lowPos; j < highPos; j++) {
//                int tid = resultBin.get(j - lowPos);
//                if(!prevBitSetResult.get(tid))
//                    continue;
//                int mult = 1;
//                if(isIndexMin) mult = -1;
//                double keyValue =index.getArray().getKeyAt(j);
//                double pseudoDist = (mult) *(queryPoint[index.getSortedDim()] - keyValue);
//                if(pseudoDist < 0)
//                    pseudoDist = 0;
//
//                currentResultPairBin.add(new Util.IntDblPair(tid, pseudoDist));
//            }
//            currentResultPair = ResultOpUtil.addAllDistSortedUnsorted(currentResultPair,currentResultPairBin);
//        }
//
//        return currentResultPair;
//
//    }
//    public static List<Util.IntDblPair> computeResultDtw(List<Util.IntDblPair> resultI, List<Util.IntDblPair> prevResult,
//                                                         SubTrajTable table, double eps, boolean singleFirst, boolean singleLasts){
//        double[] prevDistArray = new double[table.getLength()];
//        Arrays.fill(prevDistArray,INF);
//        for(int i=0;i<prevResult.size();i++)
//            prevDistArray[prevResult.get(i).intVal] = prevResult.get(i).dblVal;
//
//        double[] currentDistArray = new double[table.getLength()];
//        Arrays.fill(currentDistArray,INF);
//        for(int i=0;i<resultI.size();i++) {
//            if(singleFirst && table.isFirstSub(resultI.get(i).intVal))
//                currentDistArray[resultI.get(i).intVal] = 0;
//            else if(singleLasts && table.isLastSub(resultI.get(i).intVal))
//                currentDistArray[resultI.get(i).intVal] = 0;
//            else
//                currentDistArray[resultI.get(i).intVal] = resultI.get(i).dblVal;
//        }
//
//        BitSet firstTids = new BitSet(table.getLength());
//        int cTid = -1;
//        for(int i=0;i<prevResult.size();i++){
//            if( cTid != table.getTidOfPtr(prevResult.get(i).intVal)){
//                firstTids.set(prevResult.get(i).intVal);
//                cTid = table.getTidOfPtr(prevResult.get(i).intVal);
//            }
//        }
//
//        List<Util.IntDblPair> currentDistance = new ArrayList<>();
//
//        int p = firstTids.nextSetBit(0);
//        while(p!=-1){
//            cTid = table.getTidOfPtr(p);
//            for(int i=0;p+i < table.getLength() && table.getTidOfPtr(p+i) == cTid ;i++){
//                double dist = currentDistArray[p+i];
//                double distPrev = prevDistArray[p+i];
//                if(i>0){
//                    distPrev = Math.min(distPrev,prevDistArray[p+i-1]);
//                    distPrev = Math.min(distPrev,currentDistArray[p+i-1]);
//                }
//                currentDistArray[p+i] = dist + distPrev;
//                if(currentDistArray[p+i]<eps)
//                    currentDistance.add(new Util.IntDblPair(p+i,currentDistArray[p+i]));
//            }
//            p = firstTids.nextSetBit(p+1);
//        }
//
//        return currentDistance;
//    }

}

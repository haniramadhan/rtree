package xrtree.Search;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import xrtree.Utils.Distance;
import xrtree.index.SubTrajTable;

import java.util.BitSet;

public class EdrLcssPrune {

    public static BitSet retrieveTidFromMatchTable(BitSet[] matchTable, SubTrajTable table,
                                                   BitSet tidResult, double tau,
                                                   int[] qSubTrajLengths) {

        BitSet refinedTid = new BitSet();
//        tidResult = filterByNoSubMatch(matchTable,tidResult,table,tau);
        int cTid=tidResult.nextSetBit(0);
        for(int i=0;i<table.getLength();i++){
            if(cTid == -1)
                break;
            if(table.getTidOfPtr(i)==cTid){
                double dist = computeDistTable(i,matchTable,table, qSubTrajLengths);
                if(dist <= tau)
                    refinedTid.set(cTid);
                cTid = tidResult.nextSetBit(cTid+1);
            }
            else if(table.getTidOfPtr(i)>cTid) {
                cTid = tidResult.nextSetBit(cTid + 1);
                i--;
            }
        }
        return refinedTid;
    }

    private static double[][] initDistTable(int ptr, SubTrajTable stTable, int tid,
                                            int[] qSubTrajLengths){
        int iSubtrajLength = 0;
        for(int j=ptr;j<stTable.getLength();j++){
            if(stTable.getTidOfPtr(j)!=tid)
                break;
            iSubtrajLength++;
        }

        double[][] estDist = new double[qSubTrajLengths.length+1][];
        for(int i=0;i<qSubTrajLengths.length+1;i++) {
            estDist[i] = new double[iSubtrajLength+1];
            if(i>0)
                estDist[i][0] = estDist[i-1][0] + qSubTrajLengths[i-1];
            else
                estDist[i][0] = 0;
        }
        for(int j=1;j<iSubtrajLength+1;j++){
            estDist[0][j] = estDist[0][j-1] + stTable.getSubLength(ptr+j-1);
        }
        return estDist;
    }

    public static double computeDistTable(int ptr, BitSet[] matchTable,
                                          SubTrajTable stTable, int[] qSubTrajLengths){


        int tid = stTable.getTidOfPtr(ptr);
        double[][] estDist = initDistTable(ptr,stTable,tid, qSubTrajLengths);

        for(int i=1;i<estDist.length;i++){
            for(int j=1;j<estDist[i].length;j++){
                TDoubleArrayList tripletDist = new TDoubleArrayList();
                tripletDist.add(new double[]{estDist[i-1][j-1],estDist[i-1][j],estDist[i][j-1]});
                //0: upperLeftDIst, 1: upperDIst, 2:leftDist;

                if(i>1&&j>1) {
                    if (!matchTable[i - 2].get(ptr + j - 2))//UpperLeft does not match
                        addToTriplet(tripletDist,0,
                                Math.max(stTable.getSubLength(ptr + j - 2), qSubTrajLengths[i - 2]));
                }
                if(i>1) {
                    if (!matchTable[i - 2].get(ptr + j - 1)) //Upper does not match
                        addToTriplet(tripletDist,1,
                                - 1 + qSubTrajLengths[i - 2]);
                }
                if(j>1) {
                    if(!matchTable[i-1].get(ptr+j-2)) //Left does not match
                        addToTriplet(tripletDist,2,
                                - 1 + stTable.getSubLength(ptr+j-2));
                }

                if(!matchTable[i-1].get(ptr+j-1)){
                    if(Distance.currentDistance==Distance.LCSS)
                        tripletDist.set(0,Double.MAX_VALUE);
                    estDist[i][j] = 1 + tripletDist.min();
                    continue;
                }
                if( qSubTrajLengths[i-1] == 1) addOneEdrOrInfLcss(tripletDist,2);
                if( stTable.getSubLength(ptr+j-1) == 1) addOneEdrOrInfLcss(tripletDist,1);
                estDist[i][j] = tripletDist.min();
            }
        }
        return estDist[estDist.length-1][estDist[0].length-1];
    }
    private static void addToTriplet(TDoubleArrayList tripletDist, int index, int addVal){
        tripletDist.set(index,tripletDist.get(index)+addVal);
    }
    private static void addOneEdrOrInfLcss(TDoubleArrayList tripletDist, int index){
        if(Distance.currentDistance==Distance.LCSS) {
            tripletDist.set(index, Double.MAX_VALUE);
        }
        else{
            tripletDist.set(index,tripletDist.get(index)+1);
        }

    }

    public static BitSet applySpaceConstraint(BitSet subResult, SubTrajTable table,
                                              int curQueryLastIndex, int qLength){
        int minAllow = curQueryLastIndex - qLength- Distance.spaceConstraint;

        if(minAllow<0)
            minAllow = 0;
        int maxAllow = curQueryLastIndex + Distance.spaceConstraint;

        int i=subResult.nextSetBit(0);
        while(i!=-1){
            int minSubindex = table.getLastIndex(i) - table.getSubLength(i);
            int maxSubindex = table.getLastIndex(i);
            if(!(minAllow<=minSubindex || maxSubindex<=maxAllow))
                subResult.clear(i);
            i=subResult.nextSetBit(i+1);
        }

        return subResult;
    }

    public static BitSet filterByLength(int qTrajLen, TIntObjectHashMap lengthMap,
                                        double tau) {
        BitSet result = new BitSet();

        int minLength = qTrajLen - (int) tau;
        if(minLength<0)
            minLength = 0;

        for(int i=minLength;i<=qTrajLen+(int) tau;i++){
            TIntArrayList trajs  = (TIntArrayList) lengthMap.get(i);
            if(trajs==null) continue;
            for(int tid:trajs.toArray())
                result.set(tid);
        }
        return result;
    }
}

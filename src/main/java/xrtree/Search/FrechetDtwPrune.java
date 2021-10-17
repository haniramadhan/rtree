package xrtree.Search;

import xrtree.index.SubTrajTable;

import java.util.BitSet;
import java.util.Stack;

public class FrechetDtwPrune {
    public static int dfsDpTable(int i, int j, BitSet[] matchTable, SubTrajTable stTable){
        int iMax = matchTable.length-1;
        int jMax = j;
        for(;jMax<stTable.getLength();jMax++)
            if(stTable.getTidOfPtr(j)!=jMax){
                jMax--;
                break;
            }

        for(int ii=i;ii<=iMax;ii++) {
            for (int jj = j; jj <= jMax; jj++){
                if(ii==i && jj==j)
                    continue;
                if(!matchTable[ii].get(jj))
                    continue;

                boolean value = false;
                if(ii>i)
                    value = value || matchTable[ii-1].get(jj);
                if(jj>j)
                    value = value || matchTable[ii].get(jj-1);
                if(ii>i && jj>j)
                    value = value || matchTable[ii-1].get(jj-1);
                if(!value)
                    matchTable[ii].clear(jj);
            }
        }

        if(matchTable[iMax].get(jMax))
            return jMax;

        return -jMax;
    }
    public static BitSet retrieveTidFromMatchTable(BitSet[] matchTable, SubTrajTable table,
                                                   BitSet tidResult){
        return retrieveTidFromMatchTable(matchTable,table,tidResult,null);
    }

    public static BitSet retrieveTidFromMatchTable(BitSet[] matchTable, SubTrajTable table,
                                                      BitSet tidResult, BitSet ifTidComputedKnn) {
        BitSet refinedTid = new BitSet();


        int i=0;

        i = matchTable[0].nextSetBit(i);
        while(i!=-1){
            if(table.isFirstSub(i)){
                int tid = table.getTidOfPtr(i);
                if(!tidResult.get(tid) || (ifTidComputedKnn !=null
                        && ifTidComputedKnn.get(tid))){
                    i= matchTable[0].nextSetBit(i + 1);
                    continue;
                }
                if(table.isLastSub(i)){
                    refinedTid.set(tid);
                }
                else{
                    i = dfsDpTable(0,i,matchTable,table);
                    if(i>=0)
                        refinedTid.set(tid);
                    else {
                        i = -i;
                    }
                }
            }
            i= matchTable[0].nextSetBit(i + 1);
        }

        return refinedTid;

    }
}

package xrtree.Search;

import xrtree.index.SubTrajTable;

import java.util.BitSet;
import java.util.Stack;

public class FrechetDtwPrune {
    public static int dfsDpTable(int i, int j, BitSet[] matchTable, SubTrajTable stTable){
        Stack<Integer> iStack, jStack;
        iStack = new Stack<>();
        jStack = new Stack<>();
        iStack.add(i);
        jStack.add(j);

        BitSet visited = new BitSet();
        int jMax = j;
        int currentTid = stTable.getTidOfPtr(j);
        while(!iStack.isEmpty()){
            int iCurrent = iStack.pop();
            int jCurrent = jStack.pop();
            if(visited.get(iCurrent * stTable.getLength() + jCurrent))
                continue;
            visited.set(iCurrent * stTable.getLength() + jCurrent);

            if(stTable.isLastSub(jCurrent) && iCurrent == matchTable.length-1)
                return jCurrent;
            if(jMax < jCurrent)
                jMax = jCurrent;
            if(jCurrent<stTable.getLength()-1) {
                if (matchTable[iCurrent].get(jCurrent + 1) && currentTid == stTable.getTidOfPtr(jCurrent + 1)) {
                    iStack.add(iCurrent);
                    jStack.add(jCurrent + 1);
                }
            }
            if(iCurrent<matchTable.length-1){
                if (matchTable[iCurrent+1].get(jCurrent)) {
                    iStack.add(iCurrent + 1);
                    jStack.add(jCurrent);
                }
            }
            if(jCurrent<stTable.getLength()-1 && iCurrent<matchTable.length-1) {
                if (matchTable[iCurrent+1].get(jCurrent + 1) && currentTid == stTable.getTidOfPtr(jCurrent + 1)) {
                    iStack.add(iCurrent + 1);
                    jStack.add(jCurrent + 1);
                }
            }
        }
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

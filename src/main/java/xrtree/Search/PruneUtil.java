package xrtree.Search;

import gnu.trove.list.array.TIntArrayList;
import xrtree.index.SubTrajTable;

import java.util.BitSet;

public class PruneUtil {
    protected static boolean KNN_MODE = false;

    public static void setKnnMode(){
        KNN_MODE = true;
    }

    public static void clearKnnMode(){
        KNN_MODE = false;
    }

    public static int datasetSize;
    public static final int MIN = 0;
    public static final int MAX = 1;

    public static final int FIRST = 0;
    public static final int LAST = 1;

    public static final int INDEX_MIN = 0;
    public static final int INDEX_MAX = 1;

    public static final int INDEX_FIRST = 2;
    public static final int INDEX_LAST = 3;

    public static final int SUBSTART = 2;

    protected static boolean rangeQueried = false;

    protected static BitSet[][] results;

    protected static BitSet finalResult;
    protected static TIntArrayList finalTidResult;


    protected static double[][][] queryPointPairs;
    //structure:
    //0-th pair: MBR points -> MIN (0-0), MAX (0-1)
    //1-st pair: FIRST/LAST points-> FIRST(1-0), LAST (1-1)
    //2-nd until end pairs: SUBTRAJ MBR points-> MIN(x-0), MAX(x-1)

    protected static int[] querySubTrajLengths;


    public static final int MBR = 0;
    public static final int POSITION = 1;


    public static void setDatasetSize(int datasetSize1) {
        datasetSize = datasetSize1;
    }
    protected static BitSet expandTidToPtrSub(BitSet result, SubTrajTable table, boolean firstOnly){
        BitSet subResult = new BitSet(table.getLength());
        int ikey = 0;
        ikey= result.nextSetBit(ikey);
        for(int t = 0; t< table.getLength();t++){
            if(table.getTidOfPtr(t)<ikey)
                continue;
            if(firstOnly) {
                subResult.set(t);
            }
            else {
                while(t < table.getLength() && table.getTidOfPtr(t) == ikey) {
                    subResult.set(t);
                    t++;
                }
                t--;
            }
            ikey = result.nextSetBit(ikey+1);
            if (ikey == -1)
                break;
        }
        return subResult;
    }

    public static void extractTidFromFinalResult(){
        finalTidResult = new TIntArrayList(datasetSize);
        int i=0;

        while(i!=-1){
            int r = finalResult.nextSetBit(i);
            finalTidResult.add(r);
            i = finalResult.nextSetBit(r+1);
        }
    }

    public static BitSet getFinalResult(){
        return finalResult;
    }

    public static TIntArrayList getFinalTidResult(){
        return finalTidResult;
    }


    protected static void initResults(){
        results = new BitSet[queryPointPairs.length][];
        results[MBR] = new BitSet[2];
        results[MBR][MIN] = new BitSet();
        results[MBR][MAX] = new BitSet();
        results[POSITION] = new BitSet[2];
        results[POSITION][FIRST] = new BitSet();
        results[POSITION][LAST] = new BitSet();

    }

    protected static void setRangeQueried(){
        rangeQueried = true;
    }

    protected static boolean hasRangeQueried(){
        return rangeQueried;
    }
}

package xrtree.index;

import gnu.trove.list.array.TIntArrayList;

import java.io.Serializable;

public class SubTrajTable implements Serializable {
    //default serialVersion id
    private static final long serialVersionUID = 1L;

    public final static int TID = 0;
    public final static int LENGTH = 1;

    private final TIntArrayList tid;
    private final TIntArrayList lastIndex;

    public SubTrajTable(){
        super();
        tid = new TIntArrayList();
        lastIndex = new TIntArrayList();

    }

    public void addEntry(int trajTid, int lastIndex){
        tid.add(trajTid);
        this.lastIndex.add(lastIndex);
    }

    public int[] getSubTrajAt(int ptr){
        return new int[]{tid.get(ptr),
                lastIndex.get(ptr)
        };
    }
    public int getTidOfPtr(int ptr){
        if(ptr<0)
            ptr = -ptr;
        return tid.get(ptr);
    }

    public boolean isLastSub(int ptr){
        if(ptr<0)
            ptr = -ptr;
        if (ptr == getLength() - 1)
            return true;
        return tid.get(ptr) != tid.get(ptr+1);
    }

    public boolean isFirstSub(int ptr){
        if(ptr<0)
            ptr = -ptr;

        if(ptr==0)
            return true;
        return tid.get(ptr) != tid.get(ptr-1);
    }

    public int getLastIndex(int ptr){
        return lastIndex.get(ptr);
    }

    public int getSubLength(int ptr){
        if(ptr==0 || tid.get(ptr) != tid.get(ptr-1))
            return lastIndex.get(ptr);
        return lastIndex.get(ptr) - lastIndex.get(ptr-1);
    }

    public int getLength() {
        return tid.size();
    }
}

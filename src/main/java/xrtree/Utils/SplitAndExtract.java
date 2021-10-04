package xrtree.Utils;

import com.github.davidmoten.rtree.geometry.Rectangle;
import gnu.trove.list.array.TIntArrayList;
import static com.github.davidmoten.rtree.geometry.Geometries.rectangle;

import java.util.ArrayList;
import java.util.List;

public class SplitAndExtract {

    private static List<Rectangle> subMbrs;
   private static TIntArrayList ptrSubs;

    public static void initSubmbr(){
        subMbrs = new ArrayList<>();
        ptrSubs = new TIntArrayList();

    }

    public static List<TIntArrayList> splitTrajDbInto(int splitNumber){
        List<TIntArrayList> splitIndices = new ArrayList<>();
        int trSize = TrajUtil.trajectoryDataset.size();
        for(int i=0;i<trSize;i++) {
            List<double[]> traj = TrajUtil.trajectoryDataset.get(i);
            TIntArrayList split = MoreTrajUtil.heurSplit(traj, splitNumber - 1);
            splitIndices.add(split);
        }
        return splitIndices;
    }
    public static List<TIntArrayList> splitTrajDbRange(double[] rangeThreshold){
        List<TIntArrayList> splitIndices = new ArrayList<>();
        int trSize = TrajUtil.trajectoryDataset.size();

        for(int i=0;i<trSize;i++) {
            List<double[]> traj = TrajUtil.trajectoryDataset.get(i);
            TIntArrayList split = MoreTrajUtil.heurSplitWithRange(traj,rangeThreshold);
            splitIndices.add(split);
        }
        return splitIndices;
    }

    public static void extractSubmbrFromDataset(List<TIntArrayList> splitIndices){
        int trSize = TrajUtil.trajectoryDataset.size();
        int iSubs = 0;
        for(int i=0;i<trSize;i++){
            List<double[]> traj = TrajUtil.trajectoryDataset.get(i);

            List<double[]>[] subTrajs = splitTrajFromIndices(traj,splitIndices.get(i));

            for (List<double[]> subTraj : subTrajs) {
                double[][] subMbr = TrajUtil.findMBR(subTraj);
                subMbrs.add(rectangle(subMbr[0][0],subMbr[0][1],subMbr[1][0],subMbr[1][1]));
                ptrSubs.add(iSubs++);
            }
        }
    }


    public static  List<Rectangle> getSubMbrs(){
        return subMbrs;
    }

    public static TIntArrayList getPtrs(){
        return ptrSubs;
    }


    public static List<double[]>[] splitSingleTraj(List<double[]>traj, int split){
        return splitTrajFromIndices(traj, MoreTrajUtil.heurSplit(traj,split-1));
    }

    public static List<double[]>[] splitSingleTrajByRange(List<double[]>traj,double[] range){
        return splitTrajFromIndices(traj, MoreTrajUtil.heurSplitWithRange(traj,range));
    }
    public static List<double[]>[] splitTrajFromIndices(List<double[]> traj, TIntArrayList splitIndices){
        List[] subTrajs = new List[splitIndices.size()];
        for(int j=0;j<splitIndices.size();j++) {
            List<double[]> subTraj;
            if (j == 0)
                subTraj = traj.subList(0, splitIndices.get(j));
            else
                subTraj = traj.subList(splitIndices.get(j-1), splitIndices.get(j));
            subTrajs[j] = subTraj;
        }
        return subTrajs;
    }


}

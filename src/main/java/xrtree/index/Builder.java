package xrtree.index;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.geometry.Rectangle;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import xrtree.Utils.SplitAndExtract;
import xrtree.Utils.TrajUtil;

import java.util.List;

import static com.github.davidmoten.rtree.geometry.Geometries.point;
import static com.github.davidmoten.rtree.geometry.Geometries.rectangle;

public class Builder {
    public static RTreeSubTraj buildIndex(int splitNumber, int minChildren, int maxChildren) {

        TIntObjectHashMap lengthMap = new TIntObjectHashMap();

        SplitAndExtract.initSubmbr();
        List<TIntArrayList> splitIndices = SplitAndExtract.splitTrajDbInto(splitNumber);
        SplitAndExtract.extractSubmbrFromDataset(splitIndices);
        RTree<Integer, Rectangle> mbrTree = RTree.minChildren(minChildren).maxChildren(maxChildren).create();
        for (int i = 0; i < TrajUtil.trajectoryDataset.size(); i++) {
            double[][] mbr = TrajUtil.findMBR(TrajUtil.trajectoryDataset.get(i));
            mbrTree = mbrTree.add(i, rectangle(mbr[0][0], mbr[0][1], mbr[1][0], mbr[1][1]));
        }

        SubTrajTable table = new SubTrajTable();
        List<Rectangle> rectangles = SplitAndExtract.getSubMbrs();

        for (int i = 0; i < splitIndices.size(); i++) {
            for (int j = 0; j < splitIndices.get(i).size(); j++) {
                table.addEntry(i, splitIndices.get(i).get(j));
            }
        }

        RTree<Integer, Rectangle> submbrTree = RTree.minChildren(minChildren).maxChildren(maxChildren).create();
        for (int i = 0; i < rectangles.size(); i++)
            submbrTree = submbrTree.add(i, rectangles.get(i));

        RTree<Integer, Point> firstPointRtree = RTree.minChildren(minChildren).maxChildren(maxChildren).create();
        RTree<Integer, Point> lastPointRtree = RTree.minChildren(minChildren).maxChildren(maxChildren).create();
        for (int i = 0; i < TrajUtil.trajectoryDataset.size(); i++) {
            List<double[]> traj = TrajUtil.trajectoryDataset.get(i);
            double[] firstPoint = traj.get(0);
            double[] lastPoint = traj.get(traj.size()-1);

            firstPointRtree = firstPointRtree.add(i, point(firstPoint[0],firstPoint[1]));
            lastPointRtree = lastPointRtree.add(i, point(lastPoint[0],lastPoint[1]));
            TIntArrayList tidList = new TIntArrayList();

            if(lengthMap.containsKey(traj.size())){
               tidList = (TIntArrayList) lengthMap.get(traj.size());
            }
            tidList.add(i);
            lengthMap.put(traj.size(),tidList);
        }

        return new RTreeSubTraj(splitNumber, mbrTree,submbrTree,firstPointRtree,lastPointRtree, lengthMap, table);
    }
}

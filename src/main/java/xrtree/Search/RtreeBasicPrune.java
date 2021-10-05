package xrtree.Search;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import rx.Observable;
import xrtree.Utils.Distance;
import xrtree.Utils.TrajUtil;
import xrtree.index.RTreeSubTraj;

import java.util.BitSet;
import java.util.List;

import static com.github.davidmoten.rtree.geometry.Geometries.rectangle;

public class RtreeBasicPrune {

    public static TIntList searchSim(RTree<Integer, Rectangle> index,
                                     List<double[]> trajQuery,
                                     double tau) {


        if(Distance.currentDistance == Distance.FRECHET || Distance.currentDistance == Distance.DTW ) {
            Distance.setEps(tau);
        }
        double[][] mbr = TrajUtil.findMBR(trajQuery);
        Rectangle rMbr = rectangle(mbr[0][0] - tau, mbr[0][1] - tau,
                mbr[1][0] + tau, mbr[1][1] + tau);

        Observable<Entry<Integer, Rectangle>> results =
                index.search(rMbr, tau);
        List<Integer> rMbrIdList = results.map(Entry::value).toList().toBlocking().single();
        return filterByDistance(rMbrIdList,trajQuery,tau);
    }

    public static TIntList filterByDistance(List<Integer> searchResult, List<double[]> trajQuery, double tau){
        TIntList ans = new TIntArrayList();
        for (int i = 0; i < searchResult.size(); i++) {
            double distance = Distance.computeDistance(
                    TrajUtil.trajectoryDataset.get(searchResult.get(i)), trajQuery);
            if (distance <= tau)
                ans.add(searchResult.get(i));
        }
        return ans;
    }

}

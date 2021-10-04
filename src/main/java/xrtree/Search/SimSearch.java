package xrtree.Search;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import xrtree.Utils.Distance;
import xrtree.Utils.TrajUtil;
import xrtree.index.RTreeSubTraj;

import java.util.List;

public class SimSearch {

    public static TIntList searchSim(RTreeSubTraj index,
                                     List<double[]> trajQuery,
                                     double tau) {

        XrtreePruneUtil.initEachQueryV1(trajQuery,index.getSplitNumber());
        XrtreePruneUtil.prune(index,trajQuery,tau);
        TIntList searchResult = XrtreePruneUtil.getFinalTidResult();
        return filterByDistance(searchResult,trajQuery,tau);
    }

    public static TIntList filterByDistance(TIntList searchResult, List<double[]> trajQuery, double tau){
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

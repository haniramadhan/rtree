package XfistTest;

import xrtree.index.Builder;
import xrtree.index.RTreeSubTraj;
import org.junit.Test;
import xrtree.Utils.SplitAndExtract;

import static com.github.davidmoten.rtree.geometry.Geometries.rectangle;

public class BuildTest {


    @Test
    public void buildIndex(){
        int splitNumber = 3;

        TestUtil.initDatasetAndTestChengdu();
        SplitAndExtract.initSubmbr();
        long startBuildTime, buildTime;
        startBuildTime = System.currentTimeMillis();
        RTreeSubTraj index = Builder.buildIndex(splitNumber,4,20);
        buildTime = System.currentTimeMillis() - startBuildTime;

        TestUtil.writeToFile(index);
        System.out.print("build time: "+buildTime+" ms,");

    }
}

package XfistTest;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.HasGeometry;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.geometry.internal.PointDouble;
import gnu.trove.list.TIntList;
import org.junit.Assert;
import rx.functions.Func0;
import rx.functions.Func2;
import xrtree.Search.SimSearch;
import xrtree.Utils.Distance;
import xrtree.Utils.Util;
import xrtree.index.Builder;
import xrtree.index.RTreeSubTraj;
import org.junit.Test;
import rx.Observable;
import xrtree.Utils.SplitAndExtract;
import xrtree.Utils.TrajUtil;

import java.util.BitSet;
import java.util.HashSet;
import java.util.List;

import static com.github.davidmoten.rtree.geometry.Geometries.point;
import static com.github.davidmoten.rtree.geometry.Geometries.rectangle;

public class SearchTest {
    double tau;
    RTreeSubTraj index;
    @Test
    public void coverTest(){
        int splitNumber = 3;
        TestUtil.initDatasetAndTestChengdu();
        SplitAndExtract.initSubmbr();
        RTreeSubTraj index = Builder.buildIndex(splitNumber,4,20);

        double tau = 0.005;
        long totalTime = 0;
        for(int j=0;j<10;j++) {

            if (j == 2)
                totalTime = 0;
            long start = System.nanoTime();
            for (int i = 0; i < TestUtil.testSet.size(); i++) {
                List<double[]> trajQuery = TestUtil.testSet.get(i);
                double[][] mbr = TrajUtil.findMBR(trajQuery);
                Rectangle rMbr = rectangle(mbr[0][0] - tau, mbr[0][1] - tau,
                        mbr[1][0] + tau, mbr[1][1] + tau);


                RTree<Integer, Rectangle> mbrTree = index.getMbrTree();
                BitSet bitsetMbr = new BitSet();
                mbrTree.search(rMbr).map(Entry::value).subscribe(bitsetMbr::set);



                RTree<Integer, Point> fpTree = index.getFirstPointTree();
                Rectangle rFirst = rectangle(trajQuery.get(0)[0] - tau, trajQuery.get(0)[1] - tau,
                        trajQuery.get(0)[0] + tau, trajQuery.get(0)[1] + tau);

                BitSet bitsetFp = new BitSet();
                fpTree.search(rFirst).map(Entry::value).subscribe(bitsetFp::set);


                RTree<Integer, Point> lpTree = index.getLastPointTree();
                double[] lastPoint = trajQuery.get(trajQuery.size() - 1);
                Rectangle rLast = rectangle(lastPoint[0] - tau, lastPoint[1] - tau,
                        lastPoint[0] + tau, lastPoint[1] + tau);


                BitSet bitsetLp = new BitSet();
                lpTree.search(rLast).map(Entry::value).subscribe(bitsetLp::set);

                bitsetFp.and(bitsetLp);
                bitsetMbr.and(bitsetFp);

//                System.out.println("SC FL:");
//                for (int r = bitsetFp.nextSetBit(0);
//                     r != -1; r = bitsetFp.nextSetBit(r + 1)) {
//                    System.out.print(r + " ");
//                }
//                System.out.println();
//                bitsetFp.and(bitsetMbr);

//                System.out.println("SC ALL:");
//                for (int r = bitsetFp.nextSetBit(0);
//                     r != -1; r = bitsetFp.nextSetBit(r + 1)) {
//                    System.out.print(r + " ");
//                }
//                System.out.println();
            }
            totalTime += (System.nanoTime() - start);
        }
        System.out.println(totalTime/1000.0 + " ms");

    }

    @Test
    public void intersectTest(){
        int splitNumber = 3;
        TestUtil.initDatasetAndTestChengdu();
        SplitAndExtract.initSubmbr();
        RTreeSubTraj index = Builder.buildIndex(splitNumber,4,20);

        double tau = 0.003;
        long totalTime = 0;
        for(int j=0;j<10;j++) {

            if (j == 2)
                totalTime = 0;
            long start = System.nanoTime();
            for (int i = 0; i < TestUtil.testSet.size(); i++) {
                List<double[]> trajQuery = TestUtil.testSet.get(i);
                double[][] mbr = TrajUtil.findMBR(trajQuery);
                Rectangle rMbr = rectangle(mbr[0][0] - tau, mbr[0][1] - tau,
                        mbr[1][0] + tau, mbr[1][1] + tau);


                RTree<Integer, Rectangle> mbrTree = index.getMbrTree();
                Observable<Entry<Integer, Rectangle>> results =
                        mbrTree.search(rMbr, tau);
                List<Integer> rMbrIdList = results.map(Entry::value).toList().toBlocking().single();
                BitSet bitsetMbr = new BitSet();
                for (int id : rMbrIdList)
                    bitsetMbr.set(id);

            }
            totalTime += (System.nanoTime() - start);
        }
        System.out.println(totalTime / 1000.0 + " ms");
    }

    @Test
    public void testCombineObservables(){

        RTree<Integer, Point> treeF = RTree.minChildren(4).maxChildren(20).create();
        treeF = treeF.add(0, point(1,2))
                .add(1, point(2,4))
                .add(2, point(0,3))
                .add(3, point(5,7))
                .add(4, point(1,3));


        RTree<Integer, Point> treeL = RTree.minChildren(4).maxChildren(20).create();
        treeL = treeL.add(0, point(2,1))
                .add(1, point(3,7))
                .add(4, point(5,5))
                .add(7, point(1,1))
                .add(2, point(3,7));

        Observable<Entry<Integer,Point>> first = treeF.entries();
        Observable<Entry<Integer,Point>> last = treeL.entries();

        first.subscribe(a -> System.out.println(a.value()+", "+a.geometry()));
        System.out.println();
        last.subscribe(b -> System.out.println(b.value()+", "+b.geometry()));

        System.out.println();


        Point p = point(0,0);
        p.distance(point(1,1));

        double[] x =new double[10];
        double[] y =new double[10];
        List<Util.IntDblPair> a = first.mergeWith(last).groupBy(Entry::value)
                .flatMap( group -> group.reduce(new Util.IntDblPair(group.getKey(), 0), (intDblPair, integerPointEntry) -> {
                    intDblPair.dblVal += p.distance(integerPointEntry.geometry());
                    return intDblPair;
                })).toList().toBlocking().single();
        for(int i=0;i<a.size();i++){
            System.out.println(a.get(i).intVal+" "+a.get(i).dblVal);
        }
    }

    @Test
    public void testCombineObservablesIntersection(){

        RTree<Integer, Point> treeF = RTree.minChildren(4).maxChildren(20).create();
        treeF = treeF.add(0, point(1,2))
                .add(1, point(2,4))
                .add(2, point(0,3))
                .add(3, point(5,7))
                .add(4, point(1,3));


        RTree<Integer, Point> treeL = RTree.minChildren(4).maxChildren(20).create();
        treeL = treeL.add(0, point(2,1))
                .add(1, point(3,7))
                .add(4, point(5,5))
                .add(7, point(1,1))
                .add(2, point(3,7));

        Observable<Entry<Integer,Point>> first = treeF.entries();
        Observable<Entry<Integer,Point>> last = treeL.entries();

        first.subscribe(a -> System.out.println(a.value()+", "+a.geometry()));
        System.out.println();
        last.subscribe(b -> System.out.println(b.value()+", "+b.geometry()));

        System.out.println();


        Observable<Integer> idF = first.map(Entry::value);
        Observable<Integer> idL = last.map(Entry::value);
        
        idF.mergeWith(idL)
                .groupBy(a -> a)
                .flatMap(group -> group.reduce(
                        point(group.getKey(), 0), (p, g) -> point(p.x(),p.y()+1)))
                .filter(a -> a.y() == 2)
                .map(a -> (int) a.x())
                .subscribe(System.out::println);

    }


    @Test
    public void testSimFrechet(){
        tau = 0.005;
        Distance.setCurrentDistance(Distance.FRECHET);
        runSingleTestSim();
    }
    @Test
    public void testSimDtw(){
        tau = 0.005;
        Distance.setCurrentDistance(Distance.DTW);
        runSingleTestSim();
    }

    @Test
    public void testSimEdr(){
        tau = 5;
        Distance.setCurrentDistance(Distance.EDR);
        Distance.setParameters(0.003,3);
        runSingleTestSim();
    }


    @Test
    public void testSimLcss(){
        tau = 5;
        Distance.setCurrentDistance(Distance.LCSS);
        Distance.setParameters(0.003,3);
        runSingleTestSim();
    }

    public void runSingleTestSim(){
        long start, time = 0;
        System.out.println("Running " +Distance.getDistanceStr()+ " similarity search query test: "+TestUtil.datasetName+" with tau: "+tau);
        for(int j=0;j<10;j++) {
            if(j==2) {
                time=0;
            }
            for (int i = 0; i < TestUtil.testSet.size(); i++) {

                start = System.nanoTime();
                TIntList result2 = SimSearch.searchSim(index, TestUtil.testSet.get(i), tau);

                time += System.nanoTime() - start;
//                System.out.println(result2);
            }
        }
        System.out.println("RSubTraj " + (time/1000000.0)+" ms");
    }

    @Test
    public void runAllDistanceTestSim(){
        TestUtil.initDatasetAndTestChengdu();
        long startBuildTime, buildTime;
        startBuildTime = System.currentTimeMillis();
        index = Builder.buildIndex(3,4,20);
        buildTime = System.currentTimeMillis() - startBuildTime;

        TestUtil.writeToFile(index);
        System.out.print("build time: "+buildTime+" ms,");
        testSimFrechet();
        testSimDtw();
        testSimEdr();
        testSimLcss();
    }

}

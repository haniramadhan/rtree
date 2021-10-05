package xrtree;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.Serializer;
import com.github.davidmoten.rtree.Serializers;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.geometry.Rectangle;
import gnu.trove.list.TIntList;
import xrtree.Search.RtreeBasicPrune;
import xrtree.Search.SimSearch;
import xrtree.Utils.Distance;
import xrtree.Utils.TrajUtil;
import xrtree.index.Builder;
import xrtree.index.RTreeSubTraj;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.github.davidmoten.rtree.geometry.Geometries.rectangle;

public class Main {

    static String datasetFileChengdu = "../dataset/chengdu.csv";
    static String datasetFilePorto = "../dataset/porto.csv";
    static String testFileChengdu = "test/test_chengdu_1";
    static String testFilePorto = "test/test_porto_1";
    static String datasetFile, testFile;
    static String datasetName;
    static boolean xrTreeMethod = true;

    static double tau;
    static int splitNumber = 6;
    static List<List<double[]>> testSet;
    public static void main(String[] args){

        if(args[0].equals("rtree"))
            xrTreeMethod = false;

        datasetName = args[1];
        if(args[1].equals("chengdu")){
            datasetFile = datasetFileChengdu;
            testFile = testFileChengdu;

        }
        else{
            datasetFile = datasetFilePorto;
            testFile = testFilePorto;
        }

        if(args[2].equals("DF")){
            Distance.setCurrentDistance(Distance.FRECHET);
        }
        else if(args[2].equals("DTW")){
            Distance.setCurrentDistance(Distance.DTW);
        }
        else if(args[2].equals("EDR")){
            Distance.setCurrentDistance(Distance.EDR);
            Distance.setParameters(0.003,3);
        }
        else{
            Distance.setCurrentDistance(Distance.LCSS);
            Distance.setParameters(0.003,3);

        }



        tau = Double.parseDouble(args[3]);

        if(args.length >=5 )
            splitNumber = Integer.parseInt(args[4]);

        testSet = new ArrayList<List<double[]>>();

        System.out.println("Loading dataset");
        TrajUtil.loadTrajDatasetFromFile(testFile);
        for(int i=0;i<TrajUtil.trajectoryDataset.size();i++){
            List<double[]> iTraj = TrajUtil.trajectoryDataset.get(i);
            List<double[]> traj = new ArrayList<>();
            for (double[] iPoint : iTraj) {
                double[] point = new double[iPoint.length];
                System.arraycopy(iPoint, 0, point, 0, iPoint.length);
                traj.add(point);
            }
            testSet.add(traj);
        }
        TrajUtil.loadTrajDatasetFromFile(datasetFile);

        if(xrTreeMethod)
            runXrtree();
        else
            runBasicRtree();

    }

    public static void runBasicRtree(){
        long startBuildTime, buildTime;
        startBuildTime = System.currentTimeMillis();
        RTree<Integer,Rectangle> index = RTree.minChildren(4).maxChildren(50).create();
        for (int i = 0; i < TrajUtil.trajectoryDataset.size(); i++) {
            double[][] mbr = TrajUtil.findMBR(TrajUtil.trajectoryDataset.get(i));
            index = index.add(i, rectangle(mbr[0][0], mbr[0][1], mbr[1][0], mbr[1][1]));
        }
        buildTime = System.currentTimeMillis() - startBuildTime;
        writeToFile(index);
        System.out.print("build time: "+buildTime+" ms,");
        System.out.println(buildTime);

        long start, time = 0;
        int totalFinal = 0;

        for(int i=0;i<testSet.size();i++){

            start = System.nanoTime();
            TIntList result2 = RtreeBasicPrune.searchSim(index, testSet.get(i), tau);
            totalFinal+=result2.size();
            long qTime = System.nanoTime() - start;
            time += qTime;
            System.out.println(qTime/1000.0+","+time/1000.0/(i+1));
        }
        System.out.println(time/1000000.0 +" ms");
        System.out.println(totalFinal);
    }

    public static void runXrtree(){
        long startBuildTime, buildTime;
        startBuildTime = System.currentTimeMillis();
        RTreeSubTraj index = Builder.buildIndex(splitNumber,4,50);
        buildTime = System.currentTimeMillis() - startBuildTime;
        //writeToFile(index);
        System.out.print("build time: "+buildTime+" ms,");
        System.out.println(buildTime);

        long start, time = 0;
        int totalFinal = 0;

        for(int i=0;i<testSet.size();i++){

            start = System.nanoTime();
            TIntList result2 = SimSearch.searchSim(index, testSet.get(i), tau);
            totalFinal+=result2.size();
            long qTime = System.nanoTime() - start;
            time += qTime;
            System.out.println(qTime/1000.0+","+time/1000.0/(i+1));
        }
        System.out.println(time/1000000.0);
        System.out.println(totalFinal);

    }

    public static void writeToFile(RTreeSubTraj index){
        String submbrTreeFilename = "rtreesub-"+datasetName+".iobj";
        String mbrTreeFilename = "rtreetraj-"+datasetName+".iobj";
        String fTreeFilename = "rtreeF-"+datasetName+".iobj";
        String lTreeFilename = "rtreeL-"+datasetName+".iobj";
        String tableFilename = "subtrajtable-"+datasetName+".iobj";
        try {
            FileOutputStream fileOut = new FileOutputStream(tableFilename);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(index.getSubTrajTable());
            objectOut.close();


            Serializer<Integer, Rectangle> serializer = Serializers.flatBuffers().javaIo();
            Serializer<Integer, Point> serializerP = Serializers.flatBuffers().javaIo();

            fileOut = new FileOutputStream(mbrTreeFilename);
            serializer.write(index.getMbrTree(),fileOut);

            fileOut = new FileOutputStream(submbrTreeFilename);
            serializer.write(index.getSubmbrTree(),fileOut);

            fileOut = new FileOutputStream(fTreeFilename);
            serializerP.write(index.getFirstPointTree(),fileOut);

            fileOut = new FileOutputStream(lTreeFilename);
            serializerP.write(index.getLastPointTree(),fileOut);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        File submbrRtreeFile = new File(submbrTreeFilename);
        File mbrRtreeFile = new File(mbrTreeFilename);
        File tableFile = new File(tableFilename);
        File fpointFile = new File(fTreeFilename);
        File lpointFile = new File(lTreeFilename);


        if (submbrRtreeFile.exists() && tableFile.exists() && mbrRtreeFile.exists() &&
                fpointFile.exists() && lpointFile.exists()) {
            long submbrBytes = submbrRtreeFile.length();
            long tablebytes = tableFile.length();
            long mbrbytes = mbrRtreeFile.length();
            long fpointbytes = fpointFile.length();
            long lpointbytes = lpointFile.length();

            long totalKb = (submbrBytes+tablebytes+mbrbytes+fpointbytes+lpointbytes)/1024;


            System.out.print("size: "+String.format("%d", totalKb)+" kb,");
        }
    }


    public static void writeToFile(RTree index){

        String mbrTreeFilename = "rtreetraj-"+datasetName+".iobj";
        try {
            Serializer<Integer, Rectangle> serializer = Serializers.flatBuffers().javaIo();
            FileOutputStream fileOut = new FileOutputStream(mbrTreeFilename);
            serializer.write(index,fileOut);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        File mbrRtreeFile = new File(mbrTreeFilename);


        if ( mbrRtreeFile.exists()) {
            long mbrbytes = mbrRtreeFile.length();

            long totalKb = (mbrbytes)/1024;


            System.out.print("size: "+String.format("%d", totalKb)+" kb,");
        }
    }
}

package XfistTest;

import com.github.davidmoten.rtree.Serializer;
import com.github.davidmoten.rtree.Serializers;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.geometry.Rectangle;
import xrtree.index.RTreeSubTraj;
import xrtree.Utils.TrajUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class TestUtil {
    public static final String demoPath = "sample/";
    public static final String sampleDatasetChengdu = TestUtil.demoPath+ "chengdu-sample.csv";
    public static final String sampleTestChengdu = TestUtil.demoPath+ "chengdu-test.csv";

    public static String datasetName;
    public static List<List<double[]>> testSet;

    public static void initDatasetAndTestChengdu(){
        testSet = new ArrayList<List<double[]>>();
        datasetName = "chengdu";

        System.out.println("Loading dataset");
        TrajUtil.loadTrajDatasetFromFile(sampleTestChengdu);
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
        TrajUtil.loadTrajDatasetFromFile(sampleDatasetChengdu);

    }

    public static void writeToFile(RTreeSubTraj index){
        String submbrTreeFilename = "rtreesub-"+TestUtil.datasetName+".iobj";
        String mbrTreeFilename = "rtreetraj-"+TestUtil.datasetName+".iobj";
        String fTreeFilename = "rtreeF-"+TestUtil.datasetName+".iobj";
        String lTreeFilename = "rtreeL-"+TestUtil.datasetName+".iobj";
        String tableFilename = "subtrajtable-"+TestUtil.datasetName+".iobj";
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
}

package xrtree.Utils;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class TrajUtil {
    public static List<List<double[]>> trajectoryDataset;
    public static List<List<double[]>> samplePointsSet;
    private static int dim;
    private static final int MIN = 0, MAX = 1;

    public static double[] minBoundary, maxBoundary, trajMeanRange, trajStdevRange;

    public static int getDim(){
        return dim;
    }


    public static List<double[]> findPivotPoint(List<double[]> traj, int numPivotPoints){
        //using first/last doubleVal strategy
        List<double[]> pivotPoints = new ArrayList<double[]>();
        if(numPivotPoints == 0)
            return pivotPoints;
        PriorityQueue<Util.PointDistancePair> weights = new PriorityQueue<Util.PointDistancePair>();
        if(traj.size() < 3)
            return pivotPoints;

        double[] first = traj.get(0);
        double[] last = traj.get(traj.size()-1);
        for(int i=1;i<traj.size()-1;i++){
            double distFirst = 0;
            double distLast = 0;
            double[] point = traj.get(i);
            for(int j=0;j<point.length;j++){
                distFirst = distFirst  + Math.pow(point[j]-first[j],2);
                distLast  = distLast  + Math.pow(point[j]-last[j],2);
            }
            distFirst = Math.sqrt(distFirst);
            distLast = Math.sqrt(distLast);

            weights.add(new Util.PointDistancePair(point,-1*(distFirst+distLast)));
        }

        int listSize = numPivotPoints > weights.size() ? weights.size() : numPivotPoints;
        while(listSize>0){
            Util.PointDistancePair pdPair = weights.poll();
            double[] point = new double[pdPair.point.length];
            for(int i=0;i<point.length;i++){
                point[i] = 0 + pdPair.point[i];
            }
            pivotPoints.add(point);
            listSize--;
        }
        return pivotPoints;
    }


    public static void loadTrajDatasetFromFile(String path){
        trajectoryDataset = new ArrayList<List<double[]>>();
        try  {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            while ((line = br.readLine()) != null) {

                String[] values = line.split(";");
                List<double[]> traj = new ArrayList<double[]>();

                for(String s:values){
                    traj.add(Util.parseCommaDelimited(s));
                }
                trajectoryDataset.add(traj);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        analyzeStat();
    }

    public static double[] getMinBoundary(){
        return minBoundary;
    }
    public static double[] getMaxBoundary(){
        return maxBoundary;
    }
    public static double[] getTrajMeanRange(){
        return trajMeanRange;
    }
    public static double[] getTrajStdevRange(){
        return trajStdevRange;
    }

    private static void analyzeStat(){
        dim = trajectoryDataset.get(0).get(0).length;

        minBoundary = new double[dim];
        maxBoundary = new double[dim];
        trajMeanRange = new double[dim];
        trajStdevRange = new double[dim];

        List<double[]> trajRange = new ArrayList<double[]>();

        for(int i=0;i<dim;i++){
            minBoundary[i] = Double.MAX_VALUE;
            maxBoundary[i] = Double.MIN_VALUE;
        }
        for(List<double[]> traj:trajectoryDataset){
            double[][] mbr = findMBR(traj);
            double[] range = new double[dim];
            for(int d=0;d<dim;d++){
                if (minBoundary[d] > mbr[MIN][d])//min
                    minBoundary[d] = mbr[MIN][d];
                if (maxBoundary[d] < mbr[MAX][d])//max
                    maxBoundary[d] = mbr[MAX][d];
                range[d] = mbr[MAX][d] - mbr[MIN][d];
                trajMeanRange[d] += range[d];
            }
            trajRange.add(range);
        }
        for(int d=0;d<dim;d++){
            trajMeanRange[d] = trajMeanRange[d]/ trajectoryDataset.size();
        }

        for(double[] range:trajRange){
            for(int d=0;d<dim;d++){
                double val = (trajMeanRange[d] - range[d]);
                val = val *val;
                trajStdevRange[d] = trajStdevRange[d] + val;
            }
        }
        for(int d=0;d<dim;d++){
            trajStdevRange[d] = trajStdevRange[d] / (trajRange.size()-1);
            trajStdevRange[d] = Math.sqrt(trajStdevRange[d]);
        }
    }

    public static void buildTrajPointsDataset(){

        //transforming the trajectory dataset to sets of points.
        //A set of points contains either first points, last points, or pivot points of the trajectory set
        //When a trajectory only has 1 point, the set of last points will be set as null at that trajectory
        //This case will also apply for the sets of pivot points when length of trajectory - 2 < num of pivot points
        //Illustration
        //trajectory set: {a1,a2,a3,a4}, {b1,b2}, {c1,c2,c3,c4,c5}, {d1}
        //result set:
        // [0] ->{a1,b1,c1,d1}  (first point)
        // [1] -> {a4,b2,c5,null} (last point)
        // [2] -> {a2,null,c3,null} (pivot point)

        int pointLength = trajectoryDataset.get(0).get(0).length;
        List<List<double[]>> trajSampleSet = new ArrayList<List<double[]>>();
        samplePointsSet = new ArrayList<List<double[]>>();

        for(int i=0;i<trajectoryDataset.size();i++) {
            List<double[]> traj = trajectoryDataset.get(i);
            List<double[]> trajSamplePoints = new ArrayList<double[]>();
            double[] first = new double[pointLength];
            for(int j=0;j<traj.get(0).length;j++)
                first[j] = traj.get(0)[j];
            trajSamplePoints.add(first);
            if(traj.size()>1) {
                int iLast = traj.size()-1;
                double[] last = new double[pointLength];
                for (int j = 0; j < traj.get(iLast).length; j++)
                    last[j] = 0 + traj.get(iLast)[j];
                trajSamplePoints.add(last);
            }
            trajSamplePoints.addAll(findPivotPoint(traj, 0));
            trajSampleSet.add(trajSamplePoints);
        }

        for(int i=0;i<2;i++){
            List<double[]> samplePoints = new ArrayList<double[]>();
            for(int j=0;j<trajSampleSet.size();j++){
                if(trajSampleSet.get(j).isEmpty())
                    samplePoints.add(null);
                else{
                    samplePoints.add(trajSampleSet.get(j).get(0));
                    trajSampleSet.get(j).remove(0);
                }
            }
            samplePointsSet.add(samplePoints);
        }
    }

    public static double[][] findMBR(List<double[]> traj){
        int dim = traj.get(0).length;
        double[][] mbr = new double[2][dim];

        for(int i=0;i<dim;i++){
            mbr[MIN][i] = Double.MAX_VALUE;
            mbr[MAX][i] = Double.MIN_VALUE;
        }

        for(int i=0;i<traj.size();i++){
            for(int d=0;d<dim;d++) {
                if (mbr[MIN][d] > traj.get(i)[d])//min
                    mbr[MIN][d] = traj.get(i)[d];
                if (mbr[MAX][d]< traj.get(i)[d])//max
                    mbr[MAX][d] = traj.get(i)[d];
            }
        }
        return mbr;
    }
}

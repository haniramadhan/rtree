package xrtree.Utils;

import gnu.trove.list.array.TIntArrayList;

import java.util.*;

public class MoreTrajUtil {

    public static List<List<double[]>> naiveSplit(List<double[]> traj, int s){
        List<List<double[]>> subTrajs = new ArrayList<>();
        subTrajs.add(traj);
        while(subTrajs.size() < s){
            int i=0;
            double score = Double.MIN_VALUE;
            List<double[]> subTSplit = null;
            for (List<double[]> subT: subTrajs) {
                for(int j=0;j<subT.size()-1;j++){
                    double[][] mbr1points = TrajUtil.findMBR(subT.subList(0,j+1));
                    double[][] mbr2points = TrajUtil.findMBR(subT.subList(j+1,subT.size()));
                    double tscore = findMbrDistance(mbr1points,mbr2points,mbr1points.length/2);
                    if(score<tscore){
                        i = j;
                        score = tscore;
                        subTSplit = subT;
                    }
                }
            }
            List<double[]> subT1 = subTSplit.subList(0,i+1);
            List<double[]> subT2 = subTSplit.subList(i+1,subTSplit.size());
            subTrajs.add(subT1);
            subTrajs.add(subT2);
            subTrajs.remove(subTSplit);
        }

        return subTrajs;
    }

    public static TIntArrayList heurSplitWithRange(List<double[]> traj, int splitNumber){
        TIntArrayList subIndex = new TIntArrayList();
        double[] minPoint = traj.get(0).clone();
        double[] maxPoint = traj.get(0).clone();
        int dim = minPoint.length;
        double[] range = new double[dim];
        double[] rangeThreshold = new double[dim];

        for(int i=1;i<traj.size();i++) {
            double[] point = traj.get(i);
            for(int j=0;j<dim;j++) {
                if(minPoint[j]>point[j])
                    minPoint[j] = point[j];
                if(maxPoint[j]<point[j])
                    maxPoint[j] = point[j];
            }
        }
        for(int j=0;j<dim;j++) {
            rangeThreshold[j] = maxPoint[j] - minPoint[j];
            rangeThreshold[j] = rangeThreshold[j]/splitNumber;
        }
        for(int i=1;i<traj.size();i++){
            double[] point = traj.get(i);

            for(int j=0;j<dim;j++) {
                if(minPoint[j]>point[j])
                    minPoint[j] = point[j];
                if(maxPoint[j]<point[j])
                    maxPoint[j] = point[j];
            }

            for(int j=0;j<dim;j++){
                range[j] = maxPoint[j] - minPoint[j];
                if(range[j] >  rangeThreshold[j]){
                    range = new double[dim];
                    subIndex.add(i);
                    i++;
                    if(i<traj.size()) {
                        point = traj.get(i);
                        minPoint = point.clone();
                        maxPoint = point.clone();
                    }
                    break;
                }
            }
        }
        if(subIndex.size()==0){
            subIndex.add(traj.size());
            return subIndex;
        }
        if(subIndex.get(subIndex.size()-1) != traj.size()-1)
            subIndex.add(traj.size());
        return subIndex;
    }
    public static TIntArrayList heurSplitWithRange(List<double[]> traj, double[] rangeThreshold){
        TIntArrayList subIndex = new TIntArrayList();
        double[] minPoint = traj.get(0).clone();
        double[] maxPoint = traj.get(0).clone();
        int dim = minPoint.length;
        double[] range = new double[dim];
        for(int i=1;i<traj.size();i++){
            double[] point = traj.get(i);

            for(int j=0;j<dim;j++) {
                if(minPoint[j]>point[j])
                    minPoint[j] = point[j];
                if(maxPoint[j]<point[j])
                    maxPoint[j] = point[j];
            }

            for(int j=0;j<dim;j++){
                range[j] = maxPoint[j] - minPoint[j];
                if(range[j] >  rangeThreshold[j]){
                    range = new double[dim];
                    subIndex.add(i);
                    i++;
                    if(i<traj.size()) {
                        point = traj.get(i);
                        minPoint = point.clone();
                        maxPoint = point.clone();
                    }
                    break;
                }
            }
        }
        if(subIndex.size()==0){
            subIndex.add(traj.size());
            return subIndex;
        }
        if(subIndex.get(subIndex.size()-1) != traj.size()-1)
            subIndex.add(traj.size());
        return subIndex;
    }

    public static TIntArrayList heurSplitSeparateFL(List<double[]> traj, int s){
        TIntArrayList subIndex = new TIntArrayList();
        subIndex.add(1);
        if(traj.size()>2){
            List<double[]> trajWithoutFirstLast = traj.subList(1, traj.size() - 1);
            TIntArrayList splitIndex = MoreTrajUtil.heurSplit(trajWithoutFirstLast, s);
            //heurSplit will split in this format
            //... Suppose a traj. without first and last point has length of 10 (orig: 12),
            //... split into three (splitNumber = 2)
            //... result of heurSplit(), ex split at 3rd and 6th xrtree.index-->
            //... [3,6,10]
            for (int j = 0; j < splitIndex.size(); j++) {
                subIndex.add(splitIndex.get(j) + 1);
            }
        }
        subIndex.add(traj.size());
        //thus subIndices will have [1,4,7,11,12] --> 5 subtrajectories
        //(0,0),(1,3),(4,6)(7,10),(11,11)
        return subIndex;
    }

    public static TIntArrayList heurSplit(List<double[]> traj, int s){
        List<Util.IntDblPair> intDoublePairs = new ArrayList<>();


        TIntArrayList subIndices = new TIntArrayList();
        if(s==0) {
            subIndices.add(traj.size());
            return subIndices;
        }
        else if(s + 1 >= traj.size()){
            for(int i=0;i<traj.size();i++){
                subIndices.add(i+1);
            }
            return subIndices;
        }

        for(int i=1;i<traj.size()-1;i++){
            double score = 0;
            int sameDir = 0;
            for(int j=0;j<traj.get(0).length;j++){
                double va = traj.get(i)[j] -traj.get(i-1)[j];
                double vb = traj.get(i+1)[j] -traj.get(i)[j];
                double sign = va*vb;
                double[] compRes = computeVectorMagDir(sign,sameDir,score);
                sameDir = (int)compRes[0];
                score = compRes[1];
            }
            score = Math.sqrt(score);
            if(sameDir == traj.get(0).length)
                score = -score;

            intDoublePairs.add(new Util.IntDblPair(i,score));
            Collections.sort(intDoublePairs);
            if(intDoublePairs.size()>s){
                intDoublePairs.remove(s);
            }
        }

        intDoublePairs.sort(Comparator.comparingInt(Util.IntDblPair::getIntVal));

        for(int i=0; i<s;i++){
            subIndices.add(intDoublePairs.get(i).intVal);
        }
        subIndices.add(traj.size());
        return subIndices;
    }

    public static List<List<double[]>> splitByLength(List<double[]> traj, int subLength, int s){
        List<List<double[]>> subTrajs = new ArrayList<>();
        Random r = new Random();
        int prev = 0;
        while(s>0) {
            if (traj.size() - prev > s * subLength) {
                int rand = r.nextInt(traj.size() - (s * subLength) - prev);
                subTrajs.add(traj.subList(prev + rand, prev + rand + subLength));
                prev = prev + rand + subLength;
            } else {
                subTrajs.add(traj.subList(prev,prev+subLength));
                prev = prev + subLength;
            }
            s--;
        }

        return subTrajs;
    }

    public static double findMbrDistance(double[][] mbr1, double[][] mbr2, int dim){
        int sameDir = 0;
        double dist = 0;
        for(int i=0;i<dim;i++){
            double a1 = mbr1[0][i];
            double a2 = mbr1[1][i];
            double b1 = mbr2[0][i];
            double b2 = mbr2[1][i];
            if(a1>b1) {

                a2 = a1;
                b1 = b2;
            }
            double v = b1 - a2;
            double[] compRes = computeVectorMagDir(v,sameDir,dist);
            sameDir = (int) compRes[0];
            dist = compRes[1];
        }
        return dist;
    }

    private static double[] computeVectorMagDir(double v, int sameDir, double dist){

        if(v>0 && sameDir ==0){
            dist = v;
            sameDir++;
        }
        else if(v>0 && sameDir >0){
            dist +=v;
            sameDir++;
        }
        else if(v<0 && sameDir == 0){
            dist +=v;
        }
        return new double[]{sameDir,dist};
    }
}



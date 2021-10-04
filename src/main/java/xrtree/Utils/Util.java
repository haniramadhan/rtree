package xrtree.Utils;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.List;

public  class Util {

    public static final int CLOSEST = 0;
    public static final int DISTANCE = 1;
    public static List<double[]> dataset;
    public static double computeDistance(double[] p1, double[] p2){
        double distance = 0;
        for(int j=0;j<p1.length;j++){
            distance = distance + Math.pow(p1[j] - p2[j],2);
        }
        distance = Math.sqrt(distance);
        return distance;
    }
    public static double[] findClosestAndDistance(double[] point, double[][] centroids){
        double[] closestAndDistance = new double[2];
        double closestDistance = -1;
        double closest = -1;

        for(int i=0;i<centroids.length; i++){
            double[] centroidPoint = centroids[i];
            double distance = Util.computeDistance(centroidPoint, point);

            if(closestDistance < 0 || closestDistance> distance){
                closestDistance = distance;
                closest = i;
            }
        }

        closestAndDistance[CLOSEST] =  closest;
        closestAndDistance[DISTANCE] = closestDistance;

        return closestAndDistance;
    }

    public static double[] parseCommaDelimited(String s){
        String[] values = s.split(",");
        double[] data = new double[values.length];
        for (int i = 0; i < values.length; i++)
            data[i] = Double.parseDouble(values[i]);
        return data;
    }

    public static double[] parseIdAndDistance(String s){
        String[] values = s.split(",");
        double[] data = new double[values.length];
        data[0] = Double.parseDouble(values[0].substring(1));
        data[1] = Double.parseDouble(values[1].substring(0,values[1].length()-1));
        return data;
    }

    public static int findLessBoundary(TDoubleList arrayList, double val) {
        return findLessBoundary(new TDoubleArrayList(arrayList),val);
    }

    public static int findMoreBoundary(TDoubleList arrayList, double val) {
        return findMoreBoundary(new TDoubleArrayList(arrayList),val);
    }
    public static int findLessBoundary(TDoubleArrayList arrayList, double val){


        if(val<=arrayList.get(0)||arrayList.size()==1)
            return 0;
        int low = 0, high = arrayList.size()-1;
        int index = (low+high)/2;
        while(high-low>1) {
            //System.out.println(val + " ("+ arrayList.get(xrtree.index-1)+ "," + (xrtree.index-1) + ") -  (" + arrayList.get(xrtree.index)+ "," + (xrtree.index) + ")");
            if (arrayList.get(index - 1) < val && arrayList.get(index) >= val) {
                return index;
            }
            else if(val > arrayList.get(index))
                low = index;
            else
                high = index;
            index = (low+high)/2;
        }
        if(val>arrayList.get(low))
            return high;
        return low;

    }

    public static int findMoreBoundary(TDoubleArrayList arrayList, double val){

        if(val>=arrayList.get(arrayList.size()-1)||arrayList.size()==1)
            return arrayList.size()-1;
        int low = 0, high = arrayList.size()-1;
        int index = (low+high)/2;
        while(high-low>1) {
            //System.out.println(val + " ("+ arrayList.get(xrtree.index-1)+ "," + (xrtree.index-1) + ") -  (" + arrayList.get(xrtree.index)+ "," + (xrtree.index) + ")");
            if (arrayList.get(index) <= val && val < arrayList.get(index + 1)) {
                return index;
            }
            else if(val < arrayList.get(index+1))
                high = index;
            else
                low = index;
            index = (low+high)/2;
        }
        if(val<arrayList.get(high))
            return low;
        return high;

    }

    public static class PointDistancePair implements Comparable{
        public double[] point;
        public double distance;

        public PointDistancePair(double[] point, double distance){
            this.point = point;
            this.distance = distance;
        }

        public int compareTo(Object o) {
            Double distance0 = this.distance;
            Double distance1 = ((PointDistancePair) o).distance;

            return distance0.compareTo(distance1);
        }
    }

    public static class IntDblPair implements Comparable{
        public int intVal;
        public double dblVal;

        public IntDblPair(int intVal, double distance){
            this.intVal = intVal;
            this.dblVal = distance;
        }

        public int getIntVal(){return intVal;}

        public int compareTo(Object o) {
            IntDblPair pair = (IntDblPair)o;
            if(Double.compare(this.dblVal,pair.dblVal)==0)
                return Integer.compare(this.intVal,pair.intVal);
            return Double.compare(this.dblVal,pair.dblVal);
        }

        public String toString(){
            String s = "(";
            s = s + intVal + ",\t";
            s = s + dblVal + ")";
            return s;
        }
    }

    public static double sigmoid(double x)
    {
        return 1 / (1 + Math.exp(-x));
    }
}

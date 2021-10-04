package xrtree.Utils;

import java.io.Serializable;
import java.util.List;

public class Trajectory implements Serializable {
    public int id;
    public List<double[]> points;

    public double[] getFirst(){
        return points.get(0);
    }

    public double[] getLast(){
        return points.get(points.size()-1);
    }
    public Trajectory(int id, List<double[]> points){
        this.id = id;
        this.points = points;
    }
    public String toString(){
        String out = "Traj{ID:";
        out = out+id+",{"+points.get(0)[0];
        for(int j=1;j<points.get(0).length;j++)
            out = out + ","+points.get(0)[j];

        for(int i=1;i<points.size();i++){
            out = out + ";"+points.get(i)[0];
            for(int j=1;j<points.get(0).length;j++)
                out = out + ","+points.get(0)[j];
        }
        out = out+"}";

        return out;
    }
}

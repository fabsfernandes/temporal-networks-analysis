package br.ufu.lsi.jam.model;

import org.gephi.graph.api.Edge;

public class TimestampedEdge implements Comparable< TimestampedEdge > {

    private String time;

    private Edge edge;
    

    public String getTime() {
        return time;
    }



    public void setTime( String time ) {
        this.time = time;
    }



    public Edge getEdge() {
        return edge;
    }



    public void setEdge( Edge edge ) {
        this.edge = edge;
    }



    public int compareTo( TimestampedEdge o ) {
        
        return time.compareTo( o.time );
    }

}

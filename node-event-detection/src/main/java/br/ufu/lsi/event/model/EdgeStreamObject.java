
package br.ufu.lsi.event.model;

import org.gephi.graph.api.Edge;

public class EdgeStreamObject implements Comparable< EdgeStreamObject > {

    private long initTimestamp;

    private Edge edge;

    public long getInitTimestamp() {
        return initTimestamp;
    }

    public void setInitTimestamp( long initTimestamp ) {
        this.initTimestamp = initTimestamp;
    }

    public Edge getEdge() {
        return edge;
    }

    public void setEdge( Edge edge ) {
        this.edge = edge;
    }

    public int compareTo( EdgeStreamObject o ) {
        if ( this.initTimestamp > o.initTimestamp )
            return 1;
        if ( this.initTimestamp < o.initTimestamp )
            return -1;
        return 0;
    }

}

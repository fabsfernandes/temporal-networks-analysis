package br.ufu.lsi.event;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.statistics.plugin.GraphDistance;

public class NetworkHandler {
    
    DirectedGraph directedGraph;
    //UndirectedGraph directedGraph;
    
    public NetworkHandler( DirectedGraph directedGraph ) {
        this.directedGraph = directedGraph;   
    }
    
    /*public NetworkHandler( UndirectedGraph undirectedGraph ) {
        this.directedGraph = undirectedGraph;
        
        
    }*/
    
    public void computeDistances() {
        GraphDistance distance = new GraphDistance();
        distance.setNormalized( true );
        distance.execute( this.directedGraph );
    }
    
    public void insertNodes( Map< String, Node > nodes ) {
        
        for( Entry<String,Node> entry : nodes.entrySet() ) {
            directedGraph.addNode( entry.getValue() );
        }
        
    }
    
    public void updateNetwork( Edge incomingEdge ) {
        
        Node source = incomingEdge.getSource();
        Node target = incomingEdge.getTarget();
        
        if( directedGraph.getNode( source.getId() ) == null ) {
            directedGraph.addNode( source );
        }
        if( directedGraph.getNode( target.getId() ) == null ) {
            directedGraph.addNode( target );
        }
        
        directedGraph.addEdge( incomingEdge );
        
    }
    
    
    public void removeOldEdge( Edge edge ) {
        
            Node source = edge.getSource();
            Node target = edge.getTarget();
            
            directedGraph.removeEdge( edge );
            if( getDegree( (String) source.getId() ) == 0 ) {
                directedGraph.removeNode( source );
            }
            if( !source.getId().equals( target.getId() ) ) {
                if( getDegree( (String) target.getId() ) == 0 ) {
                    directedGraph.removeNode( target );
                }
            }
        
        
    }
    
    
    public int removeOldEdges( Long minimumTimestamp ) {
        
        List<Edge> edgesToBeRemoved = new ArrayList<Edge>();
        EdgeIterable iterable = directedGraph.getEdges();
        for( Edge edge : iterable ) {
            long timeInit = (Long) edge.getAttribute( "timeInit" );
            if(  timeInit < minimumTimestamp ) {
                edgesToBeRemoved.add( edge );
            }
        }
        
        for( Edge edge : edgesToBeRemoved ) {
            Node source = edge.getSource();
            Node target = edge.getTarget();
            
            directedGraph.removeEdge( edge );
            if( getDegree( (String) source.getId() ) == 0 ) {
                directedGraph.removeNode( source );
            }
            if( !source.getId().equals( target.getId() ) ) {
                if( getDegree( (String) target.getId() ) == 0 ) {
                    directedGraph.removeNode( target );
                }
            }
        }
        
        return edgesToBeRemoved.size();
    }
    
    public String stats() {
        String stats = "" + directedGraph.getEdgeCount();
        return stats;
    }
    
    public int getDegree( String nodeId ) {
        Node n = directedGraph.getNode( nodeId );
        if( n == null )
            return 0;
        int degree = directedGraph.getDegree( n );
        
        return degree;
    }
    
    public double getWeightedDegree( String nodeId ) {
        Node n = directedGraph.getNode( nodeId );
        int degree = directedGraph.getDegree( n );
        
        double alpha = 0.5;
        double sumWeights = 0.0;
        EdgeIterable iterator = directedGraph.getEdges( n );
        for( Edge e : iterator ) {
            sumWeights += e.getWeight();
        }
        
        double fraction = sumWeights / degree;
        double averageWeight = Math.pow( fraction, alpha );
        
        double degreeWeight = averageWeight / degree;
        
        return degreeWeight;
    }
    
    
    public BigDecimal getCloseness( String nodeId ) {
        
        if( directedGraph.getNode( nodeId) == null )
            return new BigDecimal( 0.0 );
        
        GraphDistance distance = new GraphDistance();
        distance.setNormalized( true );
        distance.execute( this.directedGraph );
        
        Double closeness = (Double) directedGraph.getNode( nodeId).getAttribute( GraphDistance.CLOSENESS );
        //BigDecimal bigDecimalCloseness = BigDecimal.valueOf(closeness).setScale(7, RoundingMode.HALF_UP);
        BigDecimal bigDecimalCloseness = BigDecimal.valueOf(closeness);
        return bigDecimalCloseness;
    }
    
    public BigDecimal getBetweenness( String nodeId ) {
        //GraphDistance distance = new GraphDistance();
        //distance.execute( this.directedGraph );
        
        if( directedGraph.getNode( nodeId) == null )
            return new BigDecimal( 0.0 );
        
        GraphDistance distance = new GraphDistance();
        distance.setNormalized( true );
        distance.execute( this.directedGraph );
        
        Double betweenness = (Double) directedGraph.getNode( nodeId).getAttribute( GraphDistance.BETWEENNESS );
        //BigDecimal bigDecimalBetweenness = BigDecimal.valueOf(betweenness).setScale(7, RoundingMode.HALF_UP);
        BigDecimal bigDecimalBetweenness = BigDecimal.valueOf(betweenness);
        return bigDecimalBetweenness;
    }
    
    
    public BigDecimal getClosenessDirect( String nodeId ) {
        
        if( directedGraph.getNode( nodeId) == null )
            return new BigDecimal( 0.0 );
        
        Double closeness = (Double) directedGraph.getNode( nodeId).getAttribute( GraphDistance.CLOSENESS );
        //BigDecimal bigDecimalCloseness = BigDecimal.valueOf(closeness).setScale(7, RoundingMode.HALF_UP);
        BigDecimal bigDecimalCloseness = BigDecimal.valueOf(closeness);
        return bigDecimalCloseness;
    }
    
    public BigDecimal getBetweennessDirect( String nodeId ) {
        
        if( directedGraph.getNode( nodeId) == null )
            return new BigDecimal( 0.0 );
        
        Double betweenness = (Double) directedGraph.getNode( nodeId).getAttribute( GraphDistance.BETWEENNESS );
        //BigDecimal bigDecimalBetweenness = BigDecimal.valueOf(betweenness).setScale(7, RoundingMode.HALF_UP);
        
        if( betweenness.isNaN() )
            return new BigDecimal( 0.0 );
        
        BigDecimal bigDecimalBetweenness = BigDecimal.valueOf(betweenness);
        return bigDecimalBetweenness;
    }
    
    
    
    public int getNodeCount() {
        return directedGraph.getNodeCount();
    }
    
    public int getEdgeCount() {
        return directedGraph.getEdgeCount();
    }
    
    
    public void clearGraph() {
        directedGraph.clear();
    }
    
    

}

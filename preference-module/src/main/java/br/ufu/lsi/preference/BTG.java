
package br.ufu.lsi.preference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;

import br.ufu.lsi.event.model.EdgeStreamObject;

public class BTG {


    private boolean cancel = false;

    private ProgressTicket progressTicket;

    protected GraphModel gm;

    private List< Node > marked; // marked[v] = has vertex v been marked?

    private Map< Node, Node > edgeTo; // edgeTo[v] = previous vertex on path to v

    private List< Node > onStack; // onStack[v] = is vertex on the stack?

    private List< Stack< Node >> cycle; // directed cycle (or null if no such cycle)

    private List< Node > neigh;
    
    public boolean isEmpty(){
        
        return gm.getDirectedGraph().getEdgeCount() == 0;
    }
    
    
    public void buildBTG( List<EdgeStreamObject> edges ) {
        
        ProjectController pc = Lookup.getDefault().lookup( ProjectController.class );
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();
        gm = Lookup.getDefault().lookup( GraphController.class ).getGraphModel();

        Map<String,Integer> topicNodes = new HashMap<String,Integer>();
        populateTopicNodes( topicNodes );
        
        for( EdgeStreamObject edge : edges ) {
            String topicName = (String) edge.getEdge().getAttribute( "generalTopicName" );
            Integer count = topicNodes.get( topicName );
            topicNodes.put( topicName, ++count );
        }
        
        List<MyTopic> myTopics = new ArrayList<MyTopic>();
        for( Entry<String,Integer> entry : topicNodes.entrySet() ) {
            MyTopic myTopic = new MyTopic( entry.getKey(), entry.getValue() );
            myTopics.add( myTopic );
        }
        Collections.sort( myTopics );
        
        for( int i = 0; i<myTopics.size(); i++ ) {
            MyTopic t1 = myTopics.get(i);
            Node n1 = gm.getDirectedGraph().getNode( t1.topicName );
            for( int j = i+1; j<myTopics.size(); j++ ) {
                MyTopic t2 = myTopics.get( j );
                if( t1.count > t2.count ) {
                    Node n2 = gm.getDirectedGraph().getNode( t2.topicName );
                    Edge edge = gm.factory().newEdge( n1, n2, true );
                    gm.getDirectedGraph().addEdge( edge );
                }
                
            }
        }
        
    }

    public void populateTopicNodes( Map<String,Integer> topicNodes ) {
        
        topicNodes.put( "POLITICS", 0 );
        topicNodes.put( "EDUCATION", 0 );
        topicNodes.put( "ECONOMY", 0 );
        topicNodes.put( "CORRUPTION", 0 );
        topicNodes.put( "OTHERS", 0 );
        topicNodes.put( "INTERNATIONAL", 0 );
        topicNodes.put( "ENTERTAINMENT", 0 );
        topicNodes.put( "SECURITY", 0 );
        topicNodes.put( "RELIGION", 0 );
        topicNodes.put( "SPORTS", 0 );
        
        Node POLITICS = gm.factory().newNode( "POLITICS" );
        gm.getDirectedGraph().addNode( POLITICS );
        
        Node EDUCATION = gm.factory().newNode( "EDUCATION" );
        gm.getDirectedGraph().addNode( EDUCATION );
        
        Node ECONOMY = gm.factory().newNode( "ECONOMY" );
        gm.getDirectedGraph().addNode( ECONOMY );
        
        Node CORRUPTION = gm.factory().newNode( "CORRUPTION" );
        gm.getDirectedGraph().addNode( CORRUPTION );
        
        Node OTHERS = gm.factory().newNode( "OTHERS" );
        gm.getDirectedGraph().addNode( OTHERS );
        
        Node INTERNATIONAL = gm.factory().newNode( "INTERNATIONAL" );
        gm.getDirectedGraph().addNode( INTERNATIONAL );
        
        Node ENTERTAINMENT = gm.factory().newNode( "ENTERTAINMENT" );
        gm.getDirectedGraph().addNode( ENTERTAINMENT );
        
        Node SECURITY = gm.factory().newNode( "SECURITY" );
        gm.getDirectedGraph().addNode( SECURITY );
        
        Node RELIGION = gm.factory().newNode( "RELIGION" );
        gm.getDirectedGraph().addNode( RELIGION );
        
        Node SPORTS = gm.factory().newNode( "SPORTS" );
        gm.getDirectedGraph().addNode( SPORTS );
    }

    public void execute() {


        Graph graph = gm.getDirectedGraph();

        marked = new ArrayList< Node >( graph.getNodeCount() );
        onStack = new ArrayList< Node >( graph.getNodeCount() );
        edgeTo = new HashMap< Node, Node >( graph.getNodeCount() );

        cycle = new ArrayList< Stack< Node >>();

        graph.readLock();

        try {
            // Init the progress tick to the number of nodes to be visited
            Progress.start( progressTicket, graph.getNodeCount() );
            Progress.setDisplayName( progressTicket, "Visiting nodes..." );

            for ( Node v : graph.getNodes() ) {
                if ( ! marked.contains( v ) ) {
                    // Exit loop if cancel is pressed
                    if ( cancel )
                        break;
                    dfs( graph, v );
                }
            }
            graph.readUnlockAll();
        } catch ( Exception e ) {
            e.printStackTrace();
            //Unlock graph
            graph.readUnlockAll();
        }
        Progress.finish( progressTicket );
    }

    private void dfs( Graph graph, Node node ) {

        // A new node has been visited
        Progress.progress( progressTicket );
        marked.add( node );
        onStack.add( node );

        neigh = new ArrayList< Node >();

        // Break the recursion if cancel is pressed
        if ( cancel )
            return;

        // For directed graphs, take only target nodes
        if ( gm.isDirected() ) {
            for ( Edge e : graph.getEdges( node ) ) {
                if ( e.getSource().equals( node ) && ! e.getSource().equals( e.getTarget() ) ) {
                    neigh.add( e.getTarget() );
                }
            }
        }

        for ( Node w : ( gm.isDirected() ? neigh : graph.getNeighbors( node ) ) ) {

            if ( ! marked.contains( w ) ) {
                edgeTo.put( w, node );
                dfs( graph, w );
            } // trace back directed cycle
            else if ( onStack.contains( w ) ) {
                Stack< Node > oneCycle = new Stack< Node >();
                for ( Node x = node; ! x.equals( w ); x = edgeTo.get( x ) ) {
                    oneCycle.push( x );
                }
                oneCycle.push( w );
                oneCycle.push( node );
                // Add to the list of cycles
                cycle.add( oneCycle );
            }
        }
        onStack.remove( node );
    }

    public boolean hasCycle() {
        return cycle != null && cycle.size() > 0;
    }

    /**
     * Return the index<em>th></em> cycle
     * 
     * @param index get the cycle at the given index
     * @return the index-th cycle as an Iterable list of Nodes
     */
    public Iterable< Node > cycle( int index ) {
        if ( index >= cycle.size() || index < 0 )
            throw new IndexOutOfBoundsException();
        return cycle.get( index );
    }
    
    
    public void printGraph() {
        Graph graph = gm.getDirectedGraph();
        for( Edge edge : graph.getEdges() ) {
            System.out.println( edge.getSource().getId() + "->" + edge.getTarget().getId() );
        }
    }
    
    public static BTG aggregateBTG( BTG btg1, BTG btg2 ) {
        
        if( btg1 == null )
            return btg2;
        if( btg2 == null )
            return btg1;
        
        BTG aggregate = new BTG();
        ProjectController pc = Lookup.getDefault().lookup( ProjectController.class );
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();
        aggregate.gm = Lookup.getDefault().lookup( GraphController.class ).getGraphModel();
        
        
        for( Node node : btg1.gm.getDirectedGraph().getNodes() ) {
            Node nodeAgg = aggregate.gm.factory().newNode( node.getId() );
            aggregate.gm.getDirectedGraph().addNode( nodeAgg );
        }
        
        
        for( Edge edge : btg1.gm.getDirectedGraph().getEdges() ) {
            Node source = edge.getSource();
            Node target = edge.getTarget();
            Node sourceAgg = aggregate.gm.getDirectedGraph().getNode( source.getId() );
            Node targetAgg = aggregate.gm.getDirectedGraph().getNode( target.getId() );
            Edge edgeAgg = aggregate.gm.factory().newEdge( sourceAgg, targetAgg, true );
            aggregate.gm.getDirectedGraph().addEdge( edgeAgg );
        }
        
        for( Edge edge : btg2.gm.getDirectedGraph().getEdges() ) {
            Node source = edge.getSource();
            Node target = edge.getTarget();
            Node sourceAgg = aggregate.gm.getDirectedGraph().getNode( source.getId() );
            Node targetAgg = aggregate.gm.getDirectedGraph().getNode( target.getId() );
            Edge edgeAgg = aggregate.gm.factory().newEdge( sourceAgg, targetAgg, true );
            aggregate.gm.getDirectedGraph().addEdge( edgeAgg );
        }
        
        return aggregate;
    }

}

class MyTopic implements Comparable< MyTopic >{
    
    String topicName;
    Integer count;
    
    public MyTopic( String topicName, Integer count ) {
        this.topicName = topicName;
        this.count = count;
    }
    
    public int compareTo( MyTopic o ) {
        if( o.count > this.count )
            return 1;
        if( o.count < this.count )
            return -1;
        return 0;
    }
}
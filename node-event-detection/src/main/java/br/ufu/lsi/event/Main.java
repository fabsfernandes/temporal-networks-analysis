
package br.ufu.lsi.event;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import br.ufu.lsi.event.model.EdgeStreamObject;
import br.ufu.lsi.event.utils.FileUtil;

public class Main {

    private String NODES_FILE = "/Users/fabiola/Desktop/LearnStream/dataset/bipartite/nodes";
    private String EDGES_FILE = "/Users/fabiola/Desktop/LearnStream/dataset/bipartite/edges";

    public static final int BIPARTITE = 0;
    public static final int HOMOGENOUS = 1;

    //private String NODES_FILE = "/Users/fabiola/Desktop/LearnStream/dataset/bipartite/nodes";

    //private String EDGES_FILE = "/Users/fabiola/Desktop/LearnStream/dataset/bipartite/edges";

    public GraphModel graphModel;

    public Map< String, Node > nodes = new HashMap< String, Node >();

    public Map< String, Edge > edges = new HashMap< String, Edge >();

    public List< EdgeStreamObject > edgesList = new ArrayList< EdgeStreamObject >();

    
    public static void repeat( double threshold, double alpha, String nodeId ) throws Exception {
        
        Main main = new Main();

        main.initializeWorkspace( BIPARTITE );
        main.loadStreamToMainMemory( BIPARTITE );
        
      //private static String NODEID = "14594813";
        //private static String NODEID = "334345564";
        //private static String NODEID = "3145222787";
        //private static String NODEID = "343820098";
        //private static String NODEID = "122757872";
        //private static String NODEID = "28958495";
        //private static String NODEID = "260856271";
        //private static String NODEID = "636368737";
        //private static String NODEID = "279635698";
        //private static String NODEID = "58488491";
        
        //NodeEventDetection nodeEventDetection = new NodeEventDetection( main.graphModel.getDirectedGraph(), main.nodes );
        NodeEventDetection nodeEventDetection = new NodeEventDetection( main.graphModel.getUndirectedGraph(), main.nodes );
        nodeEventDetection.setParams( threshold, alpha, nodeId );
        nodeEventDetection.sketchAlgorithm( main.edgesList );
    }
    
    public static void main( String... args ) throws Exception {
        
        //String [] nodeIds = new String[]{"14594813","334345564","3145222787","343820098","122757872","28958495","260856271","636368737","279635698","58488491" };
        //String [] nodeIds = new String[]{"3145222787","343820098","122757872","28958495","260856271","636368737","279635698","58488491" };
        String [] nodeIds = new String[]{"3145222787"};
        
        // closeness, PH, homogenous
        for( String nodeId : nodeIds ) {
            Main.repeat( 0.01, 0.1,  nodeId);
            //Main.repeat( 0.02, 0.1, nodeId );
           // Main.repeat( 0.1, 0.1, nodeId );
           // Main.repeat( 0.1, 0.01, nodeId );
           // Main.repeat( 0.1, 0.05, nodeId );
        }
    }

    public void initializeWorkspace( int type ) throws Exception {

        ProjectController pc = Lookup.getDefault().lookup( ProjectController.class );
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();
        graphModel = Lookup.getDefault().lookup( GraphController.class ).getGraphModel();

        if ( type == BIPARTITE ) {
            graphModel.getNodeTable().addColumn( "mode", String.class );
            graphModel.getNodeTable().addColumn( "topicId", String.class );
            graphModel.getNodeTable().addColumn( "generalTopicId", String.class );
            graphModel.getNodeTable().addColumn( "generalTopicName", String.class );

            graphModel.getEdgeTable().addColumn( "timeInit", Long.class );
            graphModel.getEdgeTable().addColumn( "timeEnd", Long.class );

        } else {
            graphModel.getEdgeTable().addColumn( "timeInit", Long.class );
            graphModel.getEdgeTable().addColumn( "timeEnd", Long.class );
            graphModel.getEdgeTable().addColumn( "topic", String.class );
            graphModel.getEdgeTable().addColumn( "generalTopicId", String.class );
            graphModel.getEdgeTable().addColumn( "generalTopicName", String.class );
        }

    }

    public void loadStreamToMainMemory( String edgesFile, String nodesFile, int type ) throws Exception {
        this.NODES_FILE = nodesFile;
        this.EDGES_FILE = edgesFile;
        this.loadStreamToMainMemory( type );
    }

    public void loadStreamToMainMemory( int type ) throws Exception {

        if ( type == BIPARTITE ) {
            // load bipartite
            loadBipartiteNodes();
            loadBipartiteEdges();
        } else {
            // load homogenous
            loadHomogenousNodes();
            loadHomogenousEdges();
        }

        for ( Entry< String, Edge > entry : edges.entrySet() ) {
            EdgeStreamObject edgeStreamObject = new EdgeStreamObject();
            edgeStreamObject.setEdge( entry.getValue() );
            edgeStreamObject
                    .setInitTimestamp( ( Long ) entry.getValue().getAttribute( "timeInit" ) );
            edgesList.add( edgeStreamObject );
        }
        Collections.sort( edgesList );
    }

    /**
     * @param id
     * @return
     */
    public Node createHomogenousNode( String id ) {
        Node node = graphModel.factory().newNode( id );
        node.setLabel( id );
        return node;
    }

    public Node createBipartiteNode( String id, String mode, String topicId, String generalTopicId,
            String generalTopicName ) {
        Node node = graphModel.factory().newNode( id );
        node.setLabel( id );
        node.setAttribute( "mode", mode );
        node.setAttribute( "topicId", topicId );
        node.setAttribute( "generalTopicId", generalTopicId );
        node.setAttribute( "generalTopicName", generalTopicName );

        return node;
    }

    /**
     * @param source
     * @param target
     * @param timeInit
     * @param timeEnd
     * @param topic
     * @param generalTopicId
     * @param generalTopicName
     * @return
     */
    public Edge createHomogenousEdge( Node source, Node target, long timeInit, long timeEnd,
            String topic, String generalTopicId, String generalTopicName ) {

        Edge edge = graphModel.factory().newEdge( source, target, true );
        edge.setAttribute( "timeInit", timeInit );
        edge.setAttribute( "timeEnd", timeEnd );
        edge.setAttribute( "topic", topic );
        edge.setAttribute( "generalTopicId", generalTopicId );
        edge.setAttribute( "generalTopicName", generalTopicName );
        return edge;
    }

    public Edge createBipartiteEdge( Node source, Node target, long timeInit, long timeEnd,
            double weight ) {

        Edge edge = graphModel.factory().newEdge( source, target, false );
        edge.setAttribute( "timeInit", timeInit );
        edge.setAttribute( "timeEnd", timeEnd );
        edge.setWeight( weight );
        return edge;
    }

    /**
     * @throws Exception
     */
    public void loadHomogenousNodes() throws Exception {

        BufferedReader br = FileUtil.openInputFile( NODES_FILE );
        int count = 0;

        // with header
        String line = br.readLine();

        while ( ( line = br.readLine() ) != null ) {
            String id = line.split( "," )[ 0 ];

            Node node = createHomogenousNode( id );
            nodes.put( id, node );

            count++ ;
            //if ( count % 1000000 == 0 )
                //System.out.println( count );
        }

        br.close();

        System.out.println( "Finish nodes: " + nodes.size() );

    }

    public void loadBipartiteNodes() throws Exception {

        BufferedReader br = FileUtil.openInputFile( NODES_FILE );
        int count = 0;

        // with header
        String line = br.readLine();

        while ( ( line = br.readLine() ) != null ) {
            String id = line.split( "," )[ 0 ];
            String mode = line.split( "," )[ 1 ];

            String topicId = "";
            String generalTopicId = "";
            String generalTopicName = "";
            if ( line.split( "," ).length > 2 ) {
                topicId = line.split( "," )[ 2 ];
                generalTopicId = line.split( "," )[ 3 ];
                generalTopicName = line.split( "," )[ 4 ];
            }

            Node node = createBipartiteNode( id, mode, topicId, generalTopicId, generalTopicName );
            nodes.put( id, node );

            count++ ;
            if ( count % 1000000 == 0 )
                System.out.println( count );
        }

        br.close();

        System.out.println( "Finish nodes: " + nodes.size() );

    }

    /**
     * @throws Exception
     */
    public void loadHomogenousEdges() throws Exception {

        BufferedReader br2 = FileUtil.openInputFile( EDGES_FILE );
        int count = 0;

        // with header
        String line2 = br2.readLine();

        while ( ( line2 = br2.readLine() ) != null ) {
            String source = line2.split( "," )[ 0 ];
            String target = line2.split( "," )[ 1 ];
            String id = line2.split( "," )[ 2 ];
            String tweet = line2.split( "," )[ 3 ];
            String timeInitString = line2.split( "," )[ 4 ];
            String timeEndString = line2.split( "," )[ 5 ];
            String topic = line2.split( "," )[ 6 ];
            String generalTopicId = line2.split( "," )[ 7 ];
            String generalTopicName = line2.split( "," )[ 8 ];

            Node sourceNode = nodes.get( source );
            Node targetNode = nodes.get( target );
            timeInitString = timeInitString.substring( 3, 3 + 13 );
            timeEndString = timeEndString.substring( 0, timeEndString.length() - 3 );
            long timeInit = Long.parseLong( timeInitString );
            long timeEnd = Long.parseLong( timeEndString );

            Edge edge = createHomogenousEdge( sourceNode, targetNode, timeInit, timeEnd, topic, generalTopicId, generalTopicName );
            
            edges.put( source + "-" + target + "-" + id + "-" + timeInit, edge );

            count++ ;
            //if ( count % 100000 == 0 )
              //  System.out.println( count );
        }

        br2.close();

        System.out.println( "Finish edges: " + edges.size() );
        System.out.println( "Finish edges count: " + count );

    }

    public void loadBipartiteEdges() throws Exception {

        BufferedReader br2 = FileUtil.openInputFile( EDGES_FILE );
        int count = 0;

        // with header
        String line2 = br2.readLine();

        while ( ( line2 = br2.readLine() ) != null ) {
            String source = line2.split( "," )[ 0 ];
            String target = line2.split( "," )[ 1 ];
            String timeInitString = line2.split( "," )[ 2 ];
            String timeEndString = line2.split( "," )[ 3 ];
            String weightString = line2.split( "," )[ 4 ];

            Node sourceNode = nodes.get( source );
            Node targetNode = nodes.get( target );
            timeInitString = timeInitString.substring( 3, 3 + 13 );
            timeEndString = timeEndString.substring( 0, timeEndString.length() - 3 );
            long timeInit = Long.parseLong( timeInitString );
            long timeEnd = Long.parseLong( timeEndString );
            double weight = Double.parseDouble( weightString );

            Edge edge = createBipartiteEdge( sourceNode, targetNode, timeInit, timeEnd, weight );
            edges.put( source + "-" + target + "-" + timeInit, edge );

            count++ ;
            if ( count % 100000 == 0 )
                System.out.println( count );
        }

        br2.close();

        System.out.println( "Finish edges: " + edges.size() );

    }

}

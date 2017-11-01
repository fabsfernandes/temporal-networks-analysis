
package br.ufu.lsi.jam.centralities;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;

import br.ufu.lsi.event.NetworkHandler;
import br.ufu.lsi.event.utils.FileUtil;

public class StaticCentralities {

    private static String NODES_FILE = "";

    private static String EDGES_FILE = "";

    public static GraphModel graphModel;

    public static Map< String, Node > nodes = new HashMap< String, Node >();

    public static Map< String, Edge > edges = new HashMap< String, Edge >();

    public static void main( String... args ) throws Exception {
        
        NetworkHandler handler = new NetworkHandler( graphModel.getDirectedGraph() );
        //handler.
    }

    public void initializeWorkspace() throws Exception {

        ProjectController pc = Lookup.getDefault().lookup( ProjectController.class );
        pc.newProject();
        graphModel = Lookup.getDefault().lookup( GraphController.class ).getGraphModel();

        graphModel.getEdgeTable().addColumn( "timeInit", String.class );
        graphModel.getEdgeTable().addColumn( "timeEnd", String.class );
        graphModel.getEdgeTable().addColumn( "genre", String.class );

    }
    
    
    public void loadNetworkToMainMemory(  ) throws Exception {
        
        loadNodes();
        loadEdges();
        
        /*for ( Entry< String, Edge > entry : edges.entrySet() ) {
            EdgeStreamObject edgeStreamObject = new EdgeStreamObject();
            edgeStreamObject.setEdge( entry.getValue() );
            edgeStreamObject
                    .setInitTimestamp( ( Long ) entry.getValue().getAttribute( "timeInit" ) );
            edgesList.add( edgeStreamObject );
        }
        Collections.sort( edgesList );*/
    }
    
    public void loadNodes() throws Exception {

        BufferedReader br = FileUtil.openInputFile( NODES_FILE );

        // with header
        String line = br.readLine();

        while ( ( line = br.readLine() ) != null ) {
            String id = line.split( ";" )[ 0 ];

            Node node = graphModel.factory().newNode( id );
            node.setLabel( id );
            nodes.put( id, node );
        }

        br.close();

        System.out.println( "Finish nodes: " + nodes.size() );

    }

    public void loadEdges() throws Exception {

        BufferedReader br2 = FileUtil.openInputFile( EDGES_FILE );
       
        // with header
        String line2 = br2.readLine();

        while ( ( line2 = br2.readLine() ) != null ) {
            String source = line2.split( ";" )[ 0 ];
            String target = line2.split( ";" )[ 1 ];
            String genre = line2.split( ";" )[ 2 ];
            String timeset = line2.split( ";" )[ 3 ];
            

            Node sourceNode = nodes.get( source );
            Node targetNode = nodes.get( target );
            String timeInit = timeset.substring( 2, 12 );
            
            Edge edge = graphModel.factory().newEdge( sourceNode, targetNode, true );
            edge.setAttribute( "timeInit", timeInit );
            edge.setAttribute( "timeEnd", timeInit );
            edge.setAttribute( "genre", genre );
            
            edges.put( source + "-" + target + "-" + timeInit, edge );

        }

        br2.close();

        System.out.println( "Finish edges: " + edges.size() );

    }


}


package br.ufu.lsi.event.methods;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import br.ufu.lsi.event.utils.FileUtil;

public class PageHinckleyTestMain {

    private   double THRESHOLD = 0.1;
    private   double ALPHA = 0.05;
    private   long MIN_INSTANCES_SEEN = 1;

    private static String CENTRALITIES_FILE_PREFIX = "/Users/fabiola/Desktop/LearnStream/dataset/draft/bipartite-betweenness-";

    private String CENTRALITIES_FILE = "/Users/fabiola/Desktop/LearnStream/dataset/draft/closeness-14594813";

    public List< Double > centralities = new ArrayList< Double >();

    public PageHinckleyTestMain() {

    }

    public PageHinckleyTestMain( String nodeId, double alpha, double threshold ) {
        this.THRESHOLD = threshold;
        this.ALPHA = alpha;
        this.CENTRALITIES_FILE = CENTRALITIES_FILE_PREFIX + nodeId;
    }

    public static void main( String... args ) throws Exception {

        PageHinckleyTestMain phtMain = new PageHinckleyTestMain();
        phtMain.run();
    }

    public void loadCentralities() throws Exception {

        BufferedReader br = FileUtil.openInputFile( CENTRALITIES_FILE );
        String line = "";
        while ( ( line = br.readLine() ) != null ) {
            centralities.add( Double.parseDouble( line ) );
        }
        br.close();

    }

    public List< Integer > run() throws Exception {

        loadCentralities();

        PageHinckleyTest phTest = new PageHinckleyTest( THRESHOLD, ALPHA, MIN_INSTANCES_SEEN );
        List< Integer > changes = new ArrayList< Integer >();

        for ( int i = 0; i < centralities.size(); i++ ) {
            
            
            if( phTest.update( centralities.get( i ) ) ) {
                //System.out.println( "1" );
                changes.add( 1 );
            } else {
                //System.out.println( "0" );
                changes.add( 0 );
            }
            
            

        }

        return changes;
    }
}

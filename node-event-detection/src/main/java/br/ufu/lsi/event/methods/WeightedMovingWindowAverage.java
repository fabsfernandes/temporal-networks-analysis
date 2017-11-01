package br.ufu.lsi.event.methods;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import br.ufu.lsi.event.utils.FileUtil;

public class WeightedMovingWindowAverage {
    
    private static  int WINDOW = 5;
    private static  double THRESHOLD = 0.6;
    private static String CENTRALITIES_FILE_PREFIX = "/Users/fabiola/Desktop/LearnStream/dataset/draft/bipartite-betweenness-";
    
    private static String CENTRALITIES_FILE = "/Users/fabiola/Desktop/LearnStream/dataset/draft/closeness-14594813";
    /*private static String CENTRALITIES_FILE = "/Users/fabiola/Desktop/LearnStream/dataset/draft/closeness-334345564";
    private static String CENTRALITIES_FILE = "/Users/fabiola/Desktop/LearnStream/dataset/draft/closeness-3145222787";
    private static String CENTRALITIES_FILE = "/Users/fabiola/Desktop/LearnStream/dataset/draft/closeness-343820098";
    private static String CENTRALITIES_FILE = "/Users/fabiola/Desktop/LearnStream/dataset/draft/closeness-122757872";
    private static String CENTRALITIES_FILE = "/Users/fabiola/Desktop/LearnStream/dataset/draft/closeness-28958495";
    private static String CENTRALITIES_FILE = "/Users/fabiola/Desktop/LearnStream/dataset/draft/closeness-260856271";
    private static String CENTRALITIES_FILE = "/Users/fabiola/Desktop/LearnStream/dataset/draft/closeness-636368737";
    private static String CENTRALITIES_FILE = "/Users/fabiola/Desktop/LearnStream/dataset/draft/closeness-279635698";
    private static String CENTRALITIES_FILE = "/Users/fabiola/Desktop/LearnStream/dataset/draft/closeness-58488491";*/
    
    public List<Double> centralities = new ArrayList<Double>();
    
    public WeightedMovingWindowAverage(){
        
    }
    public WeightedMovingWindowAverage( String nodeId, int window, double threshold ) {
        this.THRESHOLD = threshold;
        this.WINDOW = window;
        this.CENTRALITIES_FILE = CENTRALITIES_FILE_PREFIX + nodeId;
    }
    
    
    public static void main( String ... args ) throws Exception {
        
        WeightedMovingWindowAverage wmwa = new WeightedMovingWindowAverage();
        wmwa.run();
        
    }
    
    public  void loadCentralities() throws Exception {
        
        BufferedReader br = FileUtil.openInputFile( CENTRALITIES_FILE );
        String line = "";
        while( (line = br.readLine()) != null ) {
            centralities.add( Double.parseDouble( line ) );
        }
        br.close();
        
    }
    
    public  List<Integer>  run() throws Exception {

        loadCentralities();
        List<Integer> changes = new ArrayList<Integer>();
        
        List<Double> windowValues = new ArrayList<Double>();
        
        Double meanDenominator = 0.0;
        for( int i = 1; i<=WINDOW; i++ ) {
            meanDenominator += i;
        }
        
        
        for( int i = 0; i<centralities.size(); i++ ) {
            
            double newValue = centralities.get( i );
            
            
            if( windowValues.size() < WINDOW ) {
                windowValues.add( newValue );
                //System.out.println("0");
                changes.add(0);
            }
            
            else {
                Double mean = 0.0;
                for( int j = 0; j< windowValues.size(); j++ ) {
                    mean += ((j+1)*windowValues.get( j ));
                }
                
                mean = mean/meanDenominator;
                //System.out.print( mean + "," );
                
                // check change
                if( Math.abs(mean - newValue)/Math.max( mean, newValue ) > THRESHOLD ) {
                    //System.out.println( "1");
                    changes.add(1);
                }else {
                    //System.out.println("0");
                    changes.add(0);
                }
                
                // slide window
                windowValues.remove( 0 );
                windowValues.add( newValue );
            }
        }
            
        return changes;
    }

}

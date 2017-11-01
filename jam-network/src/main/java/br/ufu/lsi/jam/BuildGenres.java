package br.ufu.lsi.jam;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.Map;

import br.ufu.lsi.jam.utils.FileUtil;

public class BuildGenres {
    
    private static final String TAGS1 = "/Users/fabiola/Desktop/MLJournal/this-is-my-jam/original-dataset/msd_tagtraum_cd1.cls";
    private static final String TAGS2 = "/Users/fabiola/Desktop/MLJournal/this-is-my-jam/original-dataset/msd_tagtraum_cd2.cls";
    private static final String TAGS3 = "/Users/fabiola/Desktop/MLJournal/this-is-my-jam/original-dataset/msd_tagtraum_cd2c.cls";
    private static final String LASTFM = "/Users/fabiola/Downloads/msd_lastfm_map.cls";
    
    private static final String JAM_TO_MSD = "/Users/fabiola/Desktop/MLJournal/this-is-my-jam/jam_to_msd.tsv";
    
    private static final String JAM_TO_MSD_TO_GENRE = "/Users/fabiola/Desktop/MLJournal/this-is-my-jam/jam_to_msd_to_genre.tsv";

    private static Map<String,Tag> tags = new HashMap<String,Tag>();

    public static void main( String... args ) throws Exception {

        //System.out.println( "Loading LastFM..." );
        //loadTags( LASTFM, 4 );
        
        System.out.println( "Loading TAGS 1..." );
        loadTags( TAGS2, 1 );
        
        //System.out.println( "Loading TAGS 2..." );
        //loadTags( TAGS3, 2 );
        
        //System.out.println( "Loading TAGS 3..." );
        //loadTags( TAGS1, 3 );
        
        System.out.println( tags.size() );
        
        System.out.println( "Match with jams" );
        matchJams();

    }
    
    public static void matchJams() throws Exception {
        
        BufferedReader br = FileUtil.openInputFile( JAM_TO_MSD );
        BufferedWriter bw = FileUtil.openOutputFile( JAM_TO_MSD_TO_GENRE );
        
        String line = br.readLine();
        bw.write( "jamId\tmsdId\tgenre\n" );
        
        while ( ( line = br.readLine() ) != null ) {
            
            String jamId = line.split( "\t" )[0];
            String msdId = line.split( "\t" )[1];
            
            Tag tag = tags.get( msdId );
            if( tag != null ) {
                bw.write( jamId + "\t" + msdId + "\t" + tag.majority + "\n" );
            }
        }
        br.close();
        bw.close();
        
    }
    
    public static void loadTags( String FILE, int tagStrategy ) throws Exception {

        BufferedReader br = FileUtil.openInputFile( FILE );

        String line = br.readLine();
        int i = 0;
        int cont = 0;
        while ( ( line = br.readLine() ) != null ) {

            if ( ++ i % 100000 == 0 )
                System.out.println( i );
            
            Tag tag = new Tag();
            tag.tagStrategy = tagStrategy;
            tag.msdId = line.split( "\t" )[0];
            tag.majority = line.split( "\t" )[1];
            
            if( tags.get( tag.msdId ) == null ) {
                tags.put( tag.msdId, tag );
                ++cont;
            } else {
            }
        }
        System.out.println( "Not duplicated = " + cont );
        br.close();
    }

}

class Tag {
    
    int tagStrategy;
    String msdId;
    String majority;
    
    @Override
    public String toString() {
        return tagStrategy + "," + msdId + "," + majority;
    }
    
    
}

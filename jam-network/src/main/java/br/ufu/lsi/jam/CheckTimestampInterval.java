
package br.ufu.lsi.jam;

import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import br.ufu.lsi.jam.utils.FileUtil;

public class CheckTimestampInterval {

    private static final String JAMS = "/Users/fabiola/Desktop/MLJournal/this-is-my-jam/jams.tsv";

    private static List<Date> timestamps = new ArrayList<Date>();

    public static void main( String... args ) throws Exception {

        System.out.println( "Loading jams..." );
        loadJams();

    }

    public static void loadJams() throws Exception {

        BufferedReader br = FileUtil.openInputFile( JAMS );

        String line = br.readLine();

        String jamId = "";
        String userId = "";
        String artist = "";
        String title = "";
        String creationDate = "";
        String link = "";
        String spotifyUri = "";

        int i = 0;

        while ( ( line = br.readLine() ) != null ) {

            if ( ++ i % 100000 == 0 )
                System.out.println( i );

            int cont = 0;
            String temp = "";
            for ( int j = 0; j <= line.length(); j++ ) {

                if ( j == line.length() || line.charAt( j ) == '\t' ) {

                    switch ( cont ) {
                        case 0:
                            jamId = temp;
                            break;
                        case 1:
                            userId = temp;
                            break;
                        case 2:
                            artist = temp;
                            break;
                        case 3:
                            title = temp;
                            break;
                        case 4:
                            creationDate = temp;
                            break;
                        case 5:
                            link = temp;
                            break;
                        case 6:
                            spotifyUri = temp;
                            break;

                    }
                    cont++ ;
                    temp = "";
                } else {
                    temp += Character.toString( line.charAt( j ) );
                }
            }

            if( !creationDate.isEmpty()  ) {
                
                Date date = new SimpleDateFormat("yyyy-MM-dd").parse(creationDate);
                timestamps.add( date );
            }
        }
        Collections.sort( timestamps );
        
        System.out.println( timestamps.size() );
        System.out.println( timestamps.get( 0 ));
        System.out.println( timestamps.get( timestamps.size()-1 ));

        br.close();

    }

}


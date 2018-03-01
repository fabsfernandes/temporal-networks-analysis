package br.ufu.lsi.jam.utils;

public class StringClean {
    
    
    public static String clean(String str) {
        
        str = str.replaceAll( "\"", "" );
        str = str.trim();
        str = str.toLowerCase();
        return str;
    }
    
    public static String removeStopWords( String str ) {
        
        String [] stopWords = {",", "ïÿ", ";"};
        for( String s : stopWords ) {
            str = str.replaceAll( s, "" );
        }
        
        String [] stopWordsWithSpace = {" & ", " and ", " of "};
        for( String s : stopWordsWithSpace ) {
            str = str.replaceAll( s, " " );
        }
        
        return str;
    }

}


package br.ufu.lsi.event.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileUtil {

    public static BufferedWriter openOutputFile( String outputFile ) throws Exception {
        File file = new File( outputFile );
        
        if( !file.getParentFile().exists() )
            file.getParentFile().mkdirs();
        
        FileWriter fw = new FileWriter( file.getAbsoluteFile(), false );
        BufferedWriter bw = new BufferedWriter( fw );
        return bw;
    }

    public static BufferedWriter openOutputAppendFile( String outputFile ) throws Exception {
        File file = new File( outputFile );
        
        if( !file.getParentFile().exists() )
            file.getParentFile().mkdirs();
        
        FileWriter fw = new FileWriter( file.getAbsoluteFile(), true );
        BufferedWriter bw = new BufferedWriter( fw );
        return bw;
    }
    
    public static BufferedReader openInputFile( String inputFile ) throws Exception {
        
        File file = new File( inputFile );

        if (!file.isFile() && !file.createNewFile())
        {
            throw new IOException("Error creating new file: " + file.getAbsolutePath());
        }

        
        BufferedReader br = new BufferedReader( new FileReader( file ) );
        return br;
    }
    
    public static BufferedReader openInputFile( File inputFile ) throws Exception {
        BufferedReader br = new BufferedReader( new FileReader( inputFile ) );
        return br;
    }
    
    public static void renameFile( String oldName, String newName ) throws Exception {

        File file = new File( oldName );

        File file2 = new File( newName );

        file.renameTo(file2);
       
    }

    public static File[] getListOfFiles( String mainPath, int level ) {

        File folder = new File( mainPath + level );
        File[] listOfFiles = folder.listFiles();
        
        return listOfFiles;
    }
    
    public static void restoreFile( String oldFileName, String tempFileName ) {

        File oldFile = new File( oldFileName );
        File tempFile = new File( tempFileName );

        oldFile.delete();
        tempFile.renameTo( oldFile );
    }
    
    public static void serializeObject( Object object, String file ) {

        try {
            File f = new File( file );

            FileOutputStream out = new FileOutputStream( f );

            ObjectOutputStream stream = new ObjectOutputStream( out );

            stream.writeObject( object );

            stream.close();
            out.close();

        } catch ( Exception e ) {

            e.printStackTrace();
        }
    }

    public static Object deserializeObject( String file ) {

        try {
            File f = new File( file );

            FileInputStream out = new FileInputStream( f );

            ObjectInputStream stream = new ObjectInputStream( out );

            Object obj = stream.readObject();

            stream.close();
            out.close();

            return obj;
            
        } catch ( Exception e ) {

            e.printStackTrace();
            return null;
        }
    }

}


package com.inra.coby.deduplicator;

import entry.Main;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

/**
 *
 * @author ryahiaoui
 */
public class Commands {
    
    public static boolean mkdir(String directory ) {
      System.out.println(" Create Folder : " + directory ) ;
      File fDirectory = new File(directory) ;
      return fDirectory.mkdir() ;
    }
    
    public static void rm(String path ) throws IOException {
      System.out.println(" Remove Path : " + path ) ;
      if(new File(path).isDirectory()) {
        FileUtils.deleteDirectory(new File(path)) ;
      }
      else {
        new File(path).delete() ;
      }
    }
    
    public static void extractExec( String path , String prg , String dest ) throws IOException {
        OutputStream os ;
        try (InputStream is = Main.class.getClassLoader()
                                                   .getResource( path + "/" + prg )
                                                   .openStream())                 {
            os = new FileOutputStream( dest + File.separator + prg )  ;
            byte[] b = new byte[2048]             ;
            int length                            ;
            while ((length = is.read(b)) != -1 )  {
                  os.write(b, 0, length)          ;
            }
        }
        os.close() ;
    }
    
    public static int runCmd( String executable, String arg, String cmd ) throws IOException {
      System.out.println(" + CMD : " + cmd )                 ;
      CommandLine cmdLine = new CommandLine(executable)      ;
      cmdLine.addArguments(new String[] { arg, cmd }, false) ;
      DefaultExecutor executor = new DefaultExecutor()       ;
       executor.setExitValue(0)                              ;
      return executor.execute(cmdLine)                       ;
    }
    
}

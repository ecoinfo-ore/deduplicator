package entry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

public class Main {

    /*
     java -DFromDirectory="DATA"     \
          -DSizeFile=2000000         \
          -DToDirectory="DATA/UNIQ"  \
          -DExtension="*.txt"        \
          -jar deduplicator.jar
    */
            
    public static void main(String[] args) throws Exception     {

        if( System.getProperty("H") != null   || System.getProperty("Help") != null ) {
            System.out.println("                                                  ")  ;
            System.out.println(" #################################################")  ;
            System.out.println(" ############# Deduplicator  #####################")  ;
            System.out.println(" ------------------------------------------------ ")  ;
            System.out.println(" Total Arguments : Five                           ")  ;
            System.out.println("   FromDirectory :  Folder containing Files       ")  ;
            System.out.println("   ToDirectory   :  Output Folder result          ")  ;
            System.out.println("   SizeFile      :  Total lines per files         ")  ;
            System.out.println("   Inplace       :  Without Generating new Files  ")  ;
            System.out.println("   Suffix        :  Add Suffix to processed Files ")  ;
            System.out.println("   Extension     :  Extension Files to process    ")  ;
            System.out.println("   IgnoreFile    :  Ingoring process Files.       ")  ;
            System.out.println("   IgnoreFile    :  Ingoring process Files.       ")  ;
            System.out.println("                    Ex : -DIgnoreFile='onto/*.*'  ")  ;
            System.out.println(" #################################################")  ;
            System.out.println("                                                  ")  ;
            System.exit(0)    ;
        }
        
        String fromDirectory = getProperty("FromDirectory") ;
        String toDirectory   = getProperty("ToDirectory")   ;
        String nameFile      = getProperty("nameFile")      ;
        String ignoreFile    = getProperty("IgnoreFile")    ;
        String suffix        = getProperty("Suffix")        ;
        String sizeFile      = getProperty("SizeFile")      ;
        
        String extension     = getProperty("Extension").isEmpty() ? "*.*" :
                               getProperty("Extension")     ;
        
        String inplace       = !getProperty("Inplace").isEmpty()
                               ? " -i inplace "       :
                               toDirectory.isEmpty()  ? 
                               " -i inplace " : " "   ;
        
        if( fromDirectory.isEmpty() ) {
            System.out.println(" FromDirectory Can not be Null or Empty ! " ) ;
            System.exit(0) ;
        }
        
        if (nameFile.isEmpty()) nameFile = "Data"    ; 
        if (suffix.isEmpty())   suffix   = ".ttl"    ;
        if (sizeFile.isEmpty()) sizeFile = "1000000" ;
        
        fromDirectory = removeLastSlash(fromDirectory) ;
        toDirectory   = removeLastSlash(toDirectory)   ;

        String osName  = System.getProperty("os.name") ;

        System.out.println(" + OS-Name : " + osName)   ;

        if (  osName != null              &&
             !osName.contains("Windows")  &&
             !osName.contains("SunOS"))    {

            String awkCmd = " awk "            +
                            inplace            +
                            " '!seen[$0]++' "  +
                            ignoreFile         +
                            " "                +
                            fromDirectory      +
                            File.separator     +
                            extension          +
                            " "                ;

            if ( !inplace.trim().isEmpty() )   {
                awkCmd += " && find "    + fromDirectory +
                          File.separator + extension     +
                          " -size 0 -delete "            ;
            } else {
                awkCmd += " | split --additional-suffix=\"" + suffix + "\" -d -l  " +
                          sizeFile + " - " + toDirectory + "/" + nameFile + "_"     ;
            }
            
            runCmd("bash", "-c" , awkCmd )  ;            

        } else { // Windows
            
            String destFolder  = "gawk"     ;
            String destLibrary = "gawk" + File.separator + "lib" ;
            mkdir(destFolder)               ;
            mkdir(destLibrary)              ;
            
            extractExec("gawk-5-win"     , "gawk.exe"        , destFolder ) ;
            extractExec("gawk-5-win"     , "inplace.awk"     , destFolder ) ;
            extractExec("gawk-5-win"     , "libgmp-10.dll"   , destFolder ) ;
            extractExec("gawk-5-win"     , "libmpfr-4.dll"   , destFolder ) ;
            extractExec("gawk-5-win"     , "libncurses5.dll" , destFolder ) ;
            extractExec("gawk-5-win"     , "libreadline6.dll", destFolder ) ;
            extractExec("gawk-5-win/lib" , "inplace.dll"     , destLibrary) ;
            extractExec("gawk-5-win"     , "delEmpty.exe"    , destFolder ) ;
            
            if( ! inplace.trim().isEmpty())         {
                inplace = inplace.replace( "inplace", 
                                           destFolder + File.separator + "inplace") ;
            }
            
            /* Update AWKLIBPATH Variable ENV */
            String AWK_LIB_PATH = "set AWKLIBPATH=%cd%" + File.separator + destLibrary ;
            runCmd ("cmd", "/c", AWK_LIB_PATH ) ;
            
            String awkCmd = destFolder      + 
                            File.separator  +
                            "gawk.exe "     +
                            inplace         +
                            " !seen[$0]++ " +
                            ignoreFile      +
                            " "             +
                            fromDirectory   +
                            File.separator  +
                            extension       +
                            " "             ;

            runCmd("cmd", "/c", awkCmd )    ;
            
            if ( !inplace.trim().isEmpty() )     {

                String cmdDel = destFolder       + 
                                File.separator   +
                                "delEmpty.exe "  +
                                " -f "           +
                                " -v "           +
                                " -s "           +
                                " -y "           +
                                fromDirectory    ;
                
                runCmd("cmd", "/c", cmdDel )     ;
            }
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

    private static String getProperty(String property) {
        return System.getProperty(property) != null
               ? System.getProperty(property) : ""     ;
    }

    private static String removeLastSlash(String directoryPath )          {
        if ( ! directoryPath.isEmpty()            && 
               directoryPath.trim().endsWith(File.separator))             {
            return directoryPath.substring ( 0     ,
                                             directoryPath.length() - 1 ) ;
        }
        return directoryPath ;
    }
    
    public static boolean mkdir(String directory ) {
        System.out.println(" Create Folder : " + directory ) ;
        File fDirectory = new File(directory) ;
        return fDirectory.mkdir() ;
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

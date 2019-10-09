package entry;

import java.io.File;
import static com.inra.coby.deduplicator.Commands.rm;
import static com.inra.coby.deduplicator.Commands.mkdir;
import static com.inra.coby.deduplicator.Commands.runCmd;
import static com.inra.coby.deduplicator.Utils.getProperty;
import static com.inra.coby.deduplicator.Commands.extractExec;
import static com.inra.coby.deduplicator.Utils.removeLastSlash;

public class Main {

    /*
     java -DFromDirectory="Data"    \
          -DSizeFile=2000000        \
          -DToDirectory="Data/Uniq" \
          -DExtension="*.txt"       \
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
       
        if( fromDirectory.isEmpty() ) {
           System.out.println(" FromDirectory Can not be Null or Empty ! " ) ;
           System.exit(0) ;
        }
        
        String inplace       = ! getProperty("Inplace").isEmpty()
                               ? " -i inplace "       :
                               toDirectory.isEmpty()  ? 
                               " -i inplace " : " "   ;
               
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
            
            rm(destFolder) ;
        }
    }    
}

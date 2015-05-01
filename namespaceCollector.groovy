#! /usr/bin/env groovy

import java.util.regex.*;



if( args.length > 1 ){
  System.err.println("Error finding required file parameter!");
  System.exit(1);
}

args.each { filePath -> 
  println("Opening ${filePath}...");
  def file = new File( filePath );
  
    
    println("Starting scan of ${filePath}");
    def xmlnsList = []
    scanFile( file, xmlnsList );
    println("Finished ${filePath}.");
  
    xmlnsList = xmlnsList.sort{ url1, url2 -> 
        def url1Prefix = getNsPrefix(url1)
        def url2Prefix = getNsPrefix(url2)
        url1Prefix.compareTo(url2Prefix)
    }
    
    println("Unique XMLNS List")
    xmlnsList.each{String xmlns ->
        println( xmlns )
    }
    println("NS Elements for ConTesA Rule Set")
    xmlnsList.each{String xmlns ->
        def nsPrefix = getNsPrefix(xmlns)
        def nsUrl = xmlns.replace("xmlns:${nsPrefix}=", "")
        println("<sch:ns uri=${nsUrl} prefix=\"${nsPrefix}\"/>")
    }
}

def scanFile( File file, List<String> xmlnsList ){
    String filePath = file.getAbsolutePath();
    if( file.exists() ){
        if( file.isDirectory() ){
            File[] fileList = file.listFiles();
            fileList.each{File childFile ->
                scanFile( childFile, xmlnsList );
            }//end fileList.each( File childFile
        }//end if( file.isDirectory() ){
        else{    
            println("Starting scan of ${filePath}");

            //This is the RegEx that is used to capture the xmlns attributes
            //defined in the elements of an xml instance.&'()*+,-./:;<=>?@[\]^_`{|}~]
            def XMLNSREGEX = "xmlns:\\w+=[\"'][a-zA-Z0-9.:?;/&=#_-]+[\"']";

            if( isBinary(file) ){
              println("\tFile is most likely binary, so there will be LOTS of errors...Skipping the file.");
            }//end if( isBinary(file) ){
            else{
                String fileText = file.getText()
                def matcher = fileText =~ XMLNSREGEX
                matcher.each{
                    if( !(xmlnsList.contains(it)) ){
                        xmlnsList.add( it )
                    }//end if( !(matchedXmlns.contains(it)) ){
                }//end matcher.each
                
            }//end else
            println("Finished ${filePath}.");
        }//end else 
        
    }
    else{
        System.err.println("${filePath} does not exist.");
    }
}

def getNsPrefix( url ){
    def nsMatcher = url =~ "xmlns:\\w+"
    def nsPrefix = nsMatcher[0].replace("xmlns:", "")
    return nsPrefix
}

def isBinary( file ){
  def binary = false;

  def input = new FileInputStream( file );
  def r = new BufferedReader(new InputStreamReader(input));

  char[] cc= new char[255]; //do a peek
  r.read(cc,0,255);

  double prob_bin=0;

  for(int i=0; i<cc.length; i++){
    int j = (int)cc[i];

    if(j<32 || j>127){ //with chinese and other type languages it might flag them as binary - need another check ideaaly
      prob_bin++;
    }
  }

  double pb = prob_bin/255;
  if(pb>0.5){
    // System.out.println("probably binary at "+pb);
    binary = true;
  }

  input.close();

  return binary;
}
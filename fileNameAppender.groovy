#! /usr/bin/env groovy

import java.util.regex.*;

//This script renames the files in a directory by appending the passed in text
//to the front of it's name you call it like this
//groovy fileNameAppender directory_to_scan text_to_append 
//For example
//groovy fileNameAppender . Sample-
//this command will go through each file in the . directory and append "Sample-"
//the the their names.
if( args.length > 2 ){
  System.err.println("Too many parameters! You must pass in the path to the directory to search as the first parameter, and the string to append to all file names as the second parameter.");
  System.exit(1);
}
if( args.length < 2 ){
  System.err.println("Too few parameters! You must pass in the path to the directory to search as the first parameter, and the string to append to all file names as the second parameter.");
  System.exit(1);
}

String filePath = args[0]
String prefix = args[1]

println("Opening ${filePath}...");
def file = new File( filePath );

if( file.isDirectory() ){
    File[] fileList = file.listFiles();
    fileList.each{File childFile ->
        renameFile( childFile, prefix )
    }//end fileList.each( File childFile
}//end if( file.isDirectory() ){
else{
    //it is an individual file
//    file.renameTo(new File(prefix + file.getName()))
    renameFile( file, prefix )
}

def renameFile(File oldFile, String prefix ){
    String filePath = oldFile.getAbsolutePath()
    filePath = filePath.replace(oldFile.getName(), prefix + oldFile.getName())
    oldFile.renameTo(new File(filePath))
}
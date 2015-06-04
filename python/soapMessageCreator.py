#!/usr/bin/python

import os, sys

# This script takes in a directory and creates versions of the XML files
# it contains wrapped in SOAP Envelopes.
# usage: soapMessageCreator.py <inputdir> <outputdir>
# where inputdir is the directory containing the XML files to process
# outputdir is the directory to store the new XML files with the SOAP
# and extensionToExclue are file extensions to not process
# Envelopes

#check the number of arguments, maybe switch to use getopt module?
if not(len(sys.argv) == 4):
    print("usage: soapMessageCreator.py <inputdir> <outputdir> <extensionToExclude>" )
    sys.exit(2)
    
#Get the input directory. The first element in sys.argv is the script name, so get the second.
inputDir = sys.argv[1]
soapDir = sys.argv[2]
ignore = sys.argv[3]

if not(os.path.isdir(soapDir)):
    os.mkdir(soapDir)

print "Processing: " + inputDir
for file in os.listdir(inputDir):
    filePath = inputDir + os.sep + file
    if not(os.path.isdir(filePath)) and not(filePath.endswith(ignore)):
        soapFilePath = soapDir + os.sep + "soap_" + file
        print "Converting: " + filePath
        #open the pd file for reading
        xmlFile = open(filePath, "r")
        #no need to check for soapFile existance, using "w" will overwrite existing file content.
        soapFile = open(soapFilePath, "w")
        soapFile.write("<?xml version=\"1.0\"?>\n<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n<soapenv:Header/>\n<soapenv:Body>\n")
        for line in xmlFile:
            #do not write the root xml element from the file being read
            if line.find( "<?xml version=\"1.0\"?>" ) == -1:
                soapFile.write(line)
        soapFile.write("</soapenv:Body>\n</soapenv:Envelope>\n")
        soapFile.close()
        xmlFile.close()

#! /usr/bin/env groovy

//This file takes in an xml file and a file of xpath statements separated by new lines.
//It removes the elements the xpath statements map to from a file.
//It is called as follows
//groovy allFieldsXPathRemover <xml-file-to-process> <file-with-xpath-statements>
//The output of this script is a file called allfields-no-required.xml

import org.w3c.dom.Document
import org.w3c.dom.NodeList
import org.w3c.dom.Node
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathFactory
import java.io.File
import java.io.FileInputStream
import java.io.StringWriter;
import java.io.Writer
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.UnsupportedOperationException
import java.lang.String
import java.util.List
import java.util.Map
import javax.xml.namespace.NamespaceContext
import javax.xml.transform.OutputKeys
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory

//==============================================================================
// Script 
//==============================================================================
if( args.length > 2 ){
  System.err.println("Too many parameters! You must pass in the XML file to edit and the file containing the XPath Statements");
  System.exit(1);
}
if( args.length < 2 ){
  System.err.println("Too few parameters! You must pass in XML file to edit and the file containing the XPath Statements");
  System.exit(1);
}

//String containing the file path to the xml file
def xmlFilePath = args[0]

//String containing the file path to the xpath file
def xpathFilePath = args[1]

def xmlFile = new File(xmlFilePath)
def xpathFile = new File(xpathFilePath)

def xmlDocument = buildJDomObjects(xmlFile)
removeXPathFromFile( xpathFile, xmlDocument )
writeOutResults(xmlDocument)

//==============================================================================
// Public Methods
//==============================================================================
def buildJDomObjects(File xmlFile){
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance()
    factory.setNamespaceAware(true)
    DocumentBuilder b = factory.newDocumentBuilder()
    Document doc = b.parse(new FileInputStream(xmlFile))
    return doc
}

def removeXPathFromFile ( File xpathFile, Document xmlDocument ){
    //Evaluate XPath against Document itself
    XPath xPath = createXPathObject()
    List<String> xpathsFound = []
    List<String> xpathsNotFound = []
    xpathFile.eachLine{ line ->
        def xpathString = line.trim()
        XPathExpression expr = createXPathExpression(xPath, xpathString)
        NodeList nodes = (NodeList)expr.evaluate(xmlDocument.getDocumentElement(), XPathConstants.NODESET)
        boolean nodesFound = removeNodesFromList(nodes)
        if( !nodesFound ){
            xpathsNotFound.add(xpathString)
        }
        else{
            xpathsFound.add(xpathString)
        }
    }
    displayResults(xpathsFound, xpathsNotFound)
}

def displayResults(List<String>xpathsFound, List<String> xpathsNotFound ){
    printListResults("The following XPaths were found in the XML file:", xpathsFound)
    printListResults("The following XPaths were not found in the XML file:", xpathsNotFound)
}

def printListResults(String title, List<String> results){
    if( results.size() > 0 ){
        println title
        println "----------------------------------------------------"
        printList( results )
        println ""
    }
}
def printList(List<String> strings){
    strings.each{String s ->
        println s
    }
}

def removeNodesFromList(NodeList nodes){
    def nodeCount = nodes.getLength()
    boolean nodesFound = nodeCount > 0
    nodes.each{ Node node ->
        Node nodeRemoved = removeChildNodeFromDoc( node )
    }
    return nodesFound
}

def removeChildNodeFromDoc(Node node){
    Node parent = node.getParentNode()
    Node removedNode = parent.removeChild(node)
    return removedNode
}

def createXPathObject(){
    XPath xPath = XPathFactory.newInstance().newXPath();
    xPath.setNamespaceContext(new SampleNamespaceContext());
    return xPath
}

def createXPathExpression(XPath xPath, String xpathToUse){
    XPathExpression expr = null;
    try{
        expr= xPath.compile(xpathToUse);
    }
    catch(Exception e){
        println "Could not process XPath ${xpathToUse}."
    }
    return expr
}

def writeOutResults(Document doc){
    Transformer tf = TransformerFactory.newInstance().newTransformer();
    tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    tf.setOutputProperty(OutputKeys.INDENT, "yes");
    Writer out = new StringWriter();
    tf.transform(new DOMSource(doc), new StreamResult(out));

    File sampleTestFile = new File("allfields-no-required.xml")
    initializeFile( sampleTestFile )
    writeTextToFile( sampleTestFile, out.toString() )
    println "Created new file: ${sampleTestFile.getAbsolutePath()} with elements for the found XPaths removed."
}

def initializeFile(File file){
    if( file.exists() ){
        file.delete()
    }
    file.createNewFile();
}
def writeTextToFile(File file, String text){
    file.setText(text);
}

//==============================================================================
// internal class for defining Document namespaces
//==============================================================================
public class SampleNamespaceContext implements NamespaceContext{
    def nsMap;
    
    public SampleNamespaceContext(){
        nsMap = [:]
        nsMap.put("ansi-nist", "http://niem.gov/niem/ansi-nist/2.0")
        nsMap.put("em","http://niem.gov/niem/domains/emergencyManagement/2.1")
        nsMap.put("fs","http://niem.gov/niem/domains/familyServices/2.1")
        nsMap.put("icism","urn:us:gov:ic:ism:v2")
        nsMap.put("im","http://niem.gov/niem/domains/immigration/2.1")
        nsMap.put("intel","http://niem.gov/niem/domains/intelligence/2.1")
        nsMap.put("j","http://niem.gov/niem/domains/jxdm/4.1")
        nsMap.put("lexs","http://lexs.gov/lexs/4.0")
        nsMap.put("lexsdigest","http://lexs.gov/digest/4.0")
        nsMap.put("m","http://niem.gov/niem/domains/maritime/2.1")
        nsMap.put("nc","http://niem.gov/niem/niem-core/2.0")
        nsMap.put("ndex","http://fbi.gov/cjis/N-DEx/ndex/3.0")
        nsMap.put("s","http://niem.gov/niem/structures/2.0")
        nsMap.put("scr","http://niem.gov/niem/domains/screening/2.1")
        nsMap.put("ulex","http://ulex.gov/ulex/2.0")
        nsMap.put("ulexcodes","http://ulex.gov/codes/2.0")
        nsMap.put("ulexlib","http://ulex.gov/library/2.0")
        nsMap.put("ulexpd","http://ulex.gov/publishdiscover/2.0")
        nsMap.put("wsa","http://www.w3.org/2005/08/addressing")
    }

    public String getNamespaceURI(String prefix) {
        if( prefix == null ){
            throw new IllegalArgumentException();
        }
        else if( prefix.equals(XMLConstants.DEFAULT_NS_PREFIX) ){
            return XMLConstants.NULL_NS_URI;
        }
        else if( prefix.equals(XMLConstants.XML_NS_PREFIX) ){
            return XMLConstants.XML_NS_URI;
        }
        else if( prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)){
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        }
        else{
            String nsURI = nsMap.get(prefix);
            if( nsURI == null ){
                nsURI = XMLConstants.NULL_NS_URI;
            }
            return nsURI;
        }
    }

    public String getPrefix(String namespaceURI) {
        String prefix = null;
        if( namespaceURI == null ){
            throw new IllegalArgumentException();
        }
        else if( namespaceURI.equals(XMLConstants.XML_NS_URI)){
            prefix = XMLConstants.XML_NS_PREFIX;
        }
        else if( namespaceURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)){
            prefix = XMLConstants.XMLNS_ATTRIBUTE;
        }
        else{
            for(String key : nsMap.keySet() ){
                String nsURI = nsMap.get(key);
                if( nsURI.equals(namespaceURI.trim())){
                    prefix = key;
                    break;
                }
            }
        }
        return prefix;
    }

    public Iterator getPrefixes(String namespaceURI) {
        //Not necessary to support yet.
        throw new UnsupportedOperationException("Not supported yet."); 
    }
}
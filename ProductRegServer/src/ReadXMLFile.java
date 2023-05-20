

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ReadXMLFile {
    static Logger logger = Logger.getLogger(ReadXMLFile.class);

    static Map<String, Document> docMap = new ConcurrentHashMap<String, Document>();
    static Map<String, String> datalistmap = new ConcurrentHashMap<String, String>();
    static String[] XSLCONFIG;

    static {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            String xslParserList = Utility.getMasterValueByName("XSLPARSER");
            datalistmap.put("UNREG_AUTH", "xlsreaderconf.xml");
            datalistmap.put("BRANDMASTER", "xlsbrandmaster.xml");
            datalistmap.put("QUESTION", "xlsquestion.xml");
            datalistmap.put("UNREGMOB", "xlsunregmob.xml");
            if (xslParserList != null) {
                XSLCONFIG = xslParserList.split(",");
            }
            if (XSLCONFIG != null) {
                for (String document : XSLCONFIG) {
                    if (document != null) {
                        docMap.put(document, buildXslDocument(dBuilder, document));
                    }
                }
            }

        } catch (ParserConfigurationException e) {

            System.out.println("Error: " + e.getMessage());
            logger.debug(e.getMessage(), e);
        } catch (SAXException e) {

            System.out.println("Error: " + e.getMessage());
            logger.debug(e.getMessage(), e);
        } catch (IOException e) {

            System.out.println("Error: " + e.getMessage());
            logger.debug(e.getMessage(), e);
        }
    }

    private static Document buildXslDocument(DocumentBuilder dBuilder, String xslConfigName) throws SAXException, IOException {
        File f1 = new File(xslConfigName);
        Document doc = dBuilder.parse(f1);
        doc.getDocumentElement()
            .normalize();
        return doc;
    }

    public static String updateDOMTree(String sheetname, String key) {

        String xmlString = null;

        try {

            NodeList nList = null;
            Document doc = null;
            if (key != null) {
                doc = docMap.get(datalistmap.get(key));
            }
            if (doc == null) {
                doc = docMap.get("xlsreaderconf1.xml");
            }
            nList = doc.getElementsByTagName("worksheet");

            Node nNode = nList.item(0);

            Element eElement = (Element) nNode;
            eElement.removeAttribute("name");
            eElement.setAttribute("name", sheetname);
            logger.debug("Updated XML attribute sheet as " + sheetname);
            DOMSource source = null;
            source = new DOMSource(doc);

            StreamResult result = new StreamResult(new StringWriter());
            TransformerFactory.newInstance()
                .newTransformer()
                .transform(source, result);
            xmlString = result.getWriter()
                .toString();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            logger.debug(e.getMessage(), e);
        }

        return xmlString;

    }

}
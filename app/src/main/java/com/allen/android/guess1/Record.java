package com.allen.android.guess1;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.util.Log;
/* Android 2.2 才有 javax.xml.transform 
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
*/

public class Record {

    private static final String TAG = Const.APP_TAG;
    
    private long totalTime;
    private List<String> history;
    private String answer;
    
    private int rank;
    private int count;
    private String player;
    
    public void load(FileInputStream fos) throws Exception {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = spf.newSAXParser();
        XMLReader reader = sp.getXMLReader();
        history = new ArrayList<String>();
        ContentHandler contentHandler = new RecordContentHandler(this);
        reader.setContentHandler(contentHandler);
        reader.parse(new InputSource(fos));
    }
    
    public void save(FileOutputStream fos) throws Exception {
        try {
            /* Android 2.2 才有 javax.xml.transform, 因此改寫, 直接產生檔案. 
            DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = bf.newDocumentBuilder();
            Document doc = builder.newDocument();
            
            Element root = doc.createElement("record");
            doc.appendChild(root);
            Element timeElem = doc.createElement("total_time");
            timeElem.setTextContent(String.valueOf(totalTime));
            root.appendChild(timeElem);
            Element answerElem = doc.createElement("answer");
            answerElem.setTextContent(answer);
            root.appendChild(answerElem);
            Element histElem = doc.createElement("history");
            root.appendChild(histElem);
            for (String h : history) {
                Element itemElem = doc.createElement("item");
                itemElem.setTextContent(h);
                histElem.appendChild(itemElem);
            }
            
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(fos);
            trans.transform(source, result);
            */
            
            String xml = "<?xml version=\"1.0\"?>" +
                    "<record><total_time>TIME</total_time><answer>ANSWER</answer><history>ITEMS</history></record>";
            xml = xml.replace("TIME", String.valueOf(totalTime));
            xml = xml.replace("ANSWER", answer);
            String items = "";
            for (String h : history) {
                items += "<item>" + h + "</item>";
            }
            xml = xml.replace("ITEMS", items);
            
            Log.d(TAG, "save xml:" + xml);
            fos.write(xml.getBytes());
            
            fos.flush();
        } finally {
            fos.close();
        }
    }
    
    public void addHistoryItem(String item) {
        if (history == null) {
            history = new ArrayList<String>();
        }
        history.add(item);
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public List<String> getHistory() {
        return history;
    }

    public void setHistory(List<String> history) {
        this.history = history;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
    
    class RecordContentHandler implements ContentHandler {

        private StringBuffer buf = new StringBuffer();
        private String tag = null;
        private Record r = null;
        
        RecordContentHandler(Record r) {
            this.r = r;
        }
        
        @Override
        public void characters(char[] chars, int start, int length)
                throws SAXException {
            buf.append(chars,start,length);
        }
        
        @Override
        public void startElement(String namespaceURI, String localName, String fullName,
                Attributes attributes) throws SAXException {
            buf.setLength(0);
            tag = localName;
        }

        @Override
        public void endElement(String namespaceURI, String localName, String fullName)
                throws SAXException {
            String text = buf.toString();
            if (tag.equals("total_time")) {
                r.setTotalTime(Long.parseLong(text));
            }
            if (tag.equals("answer")) {
                r.setAnswer(text);
            }
            if (tag.equals("item")) {
                Log.d(TAG, "find item:" + text);
                r.addHistoryItem(text);
            }
            tag = "";
        }

        @Override
        public void endDocument() throws SAXException {
            
        }

        @Override
        public void endPrefixMapping(String arg0) throws SAXException {
            
        }

        @Override
        public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
                throws SAXException {
            
        }

        @Override
        public void processingInstruction(String arg0, String arg1)
                throws SAXException {
            
        }

        @Override
        public void setDocumentLocator(Locator arg0) {
            
        }

        @Override
        public void skippedEntity(String arg0) throws SAXException {
            
        }

        @Override
        public void startDocument() throws SAXException {
            
        }

        @Override
        public void startPrefixMapping(String prefix, String uri)
                throws SAXException {
            
        }
        
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
    
}

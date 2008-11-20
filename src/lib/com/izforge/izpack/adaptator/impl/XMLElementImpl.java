package com.izforge.izpack.adaptator.impl;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import com.izforge.izpack.adaptator.IXMLElement;

/**
 * Created by IntelliJ IDEA.
 * User: sora
 * Date: Nov 11, 2008
 * Time: 4:55:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class XMLElementImpl implements IXMLElement {

    private Element element;

    /**
     * Create a new root element in a new document.
     *
     * @param name Name of the root element
     */
    public XMLElementImpl(String name) {
        Document document;
        try {
            // Création d'un nouveau DOM
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder constructeur = documentFactory.newDocumentBuilder();
            document = constructeur.newDocument();
            // Propriétés du DOM
            document.setXmlVersion("1.0");
            element = document.createElement(name);
            document.appendChild(element);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * Constructor which create a root element in the given document
     *
     * @param name Name of the root element
     */
    public XMLElementImpl(String name, Document inDocument) {
        element = inDocument.createElement(name);
    }

    /**
     * Create a element in the same document of the given element
     *
     * @param name             Name of the element
     * @param elementReference Reference with an already existing document
     */
    public XMLElementImpl(String name, IXMLElement elementReference) {
        element = elementReference.getElement().getOwnerDocument().createElement(name);
    }

    public XMLElementImpl(Element inElement) {
        this.element = inElement;
    }

    public XMLElementImpl(Node node) {
        if (node instanceof Element) {
            this.element = (Element) node;
        }
    }

    public String getName() {
        return element.getNodeName();
    }

    public void addChild(IXMLElement child) {
        element.appendChild(child.getElement());
    }

    public void removeChild(IXMLElement child) {
        element.removeChild(child.getElement());
    }

    public boolean hasChildren() {
        for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == child.ELEMENT_NODE) {
                return true;
            }
        }
        return false;
    }

    public int getChildrenCount() {
        int res = 0;
        for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == child.ELEMENT_NODE) {
                res++;
            }
        }
        return res;
    }

    public Vector<IXMLElement> getChildren() {
        Vector res = new Vector();
        for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == child.ELEMENT_NODE) {
                res.add(new XMLElementImpl(child));
            }
        }
        return res;
    }

    public IXMLElement getChildAtIndex(int index) throws ArrayIndexOutOfBoundsException {
        int boucle = index;
        for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == child.ELEMENT_NODE) {
                if (index-- == 0) {
                    return new XMLElementImpl(child);
                }
            }
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public IXMLElement getFirstChildNamed(String name) {
        XMLElementImpl res = null;
        NodeList nodeList = element.getElementsByTagName(name);
        if (nodeList.getLength() > 0) {
            res = new XMLElementImpl(nodeList.item(0));
        }
        return res;
    }

    public Vector<IXMLElement> getChildrenNamed(String name) {
        NodeList nodeList = element.getElementsByTagName(name);
        Element child;
        Vector res = new Vector();
        for (int i = 0; i < nodeList.getLength(); i++) {
            child = (Element) nodeList.item(i);
            res.add(new XMLElementImpl(child));
        }
        return res;
    }

    public String getAttribute(String name) {
        return this.getAttribute(name, null);
    }

    public String getAttribute(String name, String defaultValue) {
        Node attribute = element.getAttributes().getNamedItem(name);
        if (attribute != null) {
            return attribute.getNodeValue();
        }
        return defaultValue;
    }

    public void setAttribute(String name, String value) {
        NamedNodeMap attributes = element.getAttributes();
        Attr attribute = element.getOwnerDocument().createAttribute(name);
        attribute.setValue(value);
        attributes.setNamedItem(attribute);
    }

    public void removeAttribute(String name) {
        this.element.getAttributes().removeNamedItem(name);
    }

    public Enumeration enumerateAttributeNames() {
        NamedNodeMap namedNodeMap = element.getAttributes();
        Properties properties = new Properties();
        for (int i = 0; i < namedNodeMap.getLength(); i++) {
            Node node = namedNodeMap.item(i);
            properties.put(node.getNodeName(), node.getNodeValue());
        }
        return properties.keys();
    }

    public boolean hasAttribute(String name) {
        return (this.element.getAttributes().getNamedItem(name) == null) ? false : true;
    }

    public Properties getAttributes() {
        Properties properties = new Properties();
        NamedNodeMap namedNodeMap = this.element.getAttributes();
        for (int i = 0; i < namedNodeMap.getLength(); ++i) {
            properties.put(namedNodeMap.item(i).getNodeName(), namedNodeMap.item(i).getNodeValue());
        }
        return properties;
    }

    public int getLineNr() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getContent() {
        // if only one child
        if (this.element.hasChildNodes() == true) {
            NodeList children = this.element.getChildNodes();
            if (children.getLength() == 1 && children.item(0).getNodeType() == Node.TEXT_NODE) {
                return children.item(0).getNodeValue();
            }
        }
        return null;
    }

    public void setContent(String content) {

        Node child = null;
        while ((child = this.element.getFirstChild()) != null) {
            this.element.removeChild(child);
        }

        element.appendChild(element.getOwnerDocument().createTextNode(content));
    }

    public Node getElement() {
        return element;
    }

    @Override
    public String toString() {
        return element.getNodeName() + " " + element.getNodeValue();
    }
}

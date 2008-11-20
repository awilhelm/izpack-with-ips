package com.izforge.izpack.adaptator;

import net.n3.nanoxml.XMLElement;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Created by IntelliJ IDEA.
 * User: sora
 * Date: Nov 11, 2008
 * Time: 4:53:08 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IXMLElement extends Serializable {

    /**
     * No line number defined.
     */
    int NO_LINE = -1;

    String getName();

    void addChild(IXMLElement child);

    void removeChild(IXMLElement child);

    boolean hasChildren();

    int getChildrenCount();

    Vector getChildren();

    IXMLElement getChildAtIndex(int index) throws ArrayIndexOutOfBoundsException;

    IXMLElement getFirstChildNamed(String name);

    Vector<IXMLElement> getChildrenNamed(String name);

    String getAttribute(String name);

    String getAttribute(String name, String defaultValue);

    void setAttribute(String name, String value);

    void removeAttribute(String name);

    Enumeration enumerateAttributeNames();

    boolean hasAttribute(String name);

    Properties getAttributes();

    int getLineNr();

    String getContent();

    void setContent(String content);

    Node getElement();
}

/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.izforge.izpack.adaptator;

import com.izforge.izpack.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.adaptator.impl.XMLParser;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.xml.transform.TransformerException;
import java.io.FileNotFoundException;
import java.io.IOException;

public class XMLElementTest extends TestCase {

    String lineSeparator = System.getProperty("line.separator");
    private static final String filename = "partial.xml";

    private IXMLElement root;

    public void setUp() throws FileNotFoundException {
        /* méthode DOM */
        IXMLParser parser = new XMLParser();
        root = parser.parse(XMLElementTest.class.getResourceAsStream(filename));
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(XMLElementTest.class);
    }

    public void testGetName() throws NoSuchMethodException {
        assertEquals(root.getName(), "installation");
        assertEquals(root.getChildAtIndex(0).getName(), "info");
    }

    public void testAddChild() throws NoSuchMethodException {
        IXMLElement element = new XMLElementImpl("child", root);
        root.addChild(element);
        element = root.getChildAtIndex(root.getChildrenCount() - 1);
        assertEquals(element.getName(), "child");
    }

    public void testRemoveChild() throws NoSuchMethodException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void testHasChildren() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void testGetChildrenCount() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void testGetChildren() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void testGetChildAtIndex() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void testGetFirstChildNamed() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void testGetChildrenNamed() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void testGetAttribute() throws NoSuchMethodException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void testSetAttribute() throws NoSuchMethodException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void testRemoveAttribute() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void testEnumerateAttributeNames() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void testHasAttribute() throws NoSuchMethodException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void testGetAttributes() throws NoSuchMethodException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void testGetLineNr() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void testGetContent() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void testSetContent() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void testWrite() throws TransformerException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
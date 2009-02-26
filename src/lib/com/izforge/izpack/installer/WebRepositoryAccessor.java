/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Vladimir Ralev
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

package com.izforge.izpack.installer;

import com.izforge.izpack.ExecutableFile;
import com.izforge.izpack.Pack;
import com.izforge.izpack.ParsableFile;
import com.izforge.izpack.UpdateCheck;
import com.izforge.izpack.adaptator.*;
import com.izforge.izpack.adaptator.impl.XMLParser;
import com.izforge.izpack.compiler.CompilerException;
import com.izforge.izpack.compiler.PackInfo;
import com.izforge.izpack.util.OsConstraint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * This class enumerates the availabe packs at the web repository. Parses the config files
 * - install.xml, packsinfo.xml, langpacks and is used to override the static configuration
 * in the installer jar.
 *
 * @author <a href="vralev@redhat.com">Vladimir Ralev</a>
 * @version $Revision: 1.1 $
 */
public class WebRepositoryAccessor
{
    /**
     * URL to remote install.xml
     */
    private String installXmlUrl;

    /**
     * Base repository URL
     */
    private String baseUrl;

    /**
     * install.xml
     */
    private String installXmlString;

    /**
     * packsinfo.xml contains nbytes, pack name and pack id
     */
    private String packsInfo;

    /**
     * list of PackInfo entries
     */
    private ArrayList<PackInfo> packs;

    /**
     * Constant for checking attributes.
     */
    private static boolean YES = true;

    /**
     * Constant for checking attributes.
     */
    private static boolean NO = false;

    /**
     * Files to be looked for at the repository base url
     */
    private static final String installFilename = "install.xml";

    private static final String packsinfoFilename = "packsinfo.xml";

    /**
     * Files being downloaded in the buffer, 1MB max
     */
    private static final int BUFFER_SIZE = 1000000;


    /**
     * Create a new WebRepositoryAccessor.
     *
     * @param urlbase
     */
    public WebRepositoryAccessor(String urlbase)
    {
        this.installXmlUrl = urlbase + "/" + installFilename;
        this.baseUrl = urlbase;
    }

    /**
     * Get the list of the packs from the remore install.xml
     *
     * @return the packs list
     */
    public ArrayList<PackInfo> getOnlinePacks()
    {
        readConfig();
        packs = parsePacks();
        readPacksInfo();
        parsePacksInfo();
        return packs;
    }

    /**
     * Returns the contents of a file at url as a string (must be a text file)
     *
     * @param url
     * @return the content
     */
    private String stringFromURL(String url)
    {
        int max = BUFFER_SIZE;
        byte[] raw = new byte[max];
        InputStream in = null;
        try
        {
            WebAccessor w = new WebAccessor(null);
            in = w.openInputStream(new URL(url));
            if (in == null)
            {
                throw new RuntimeException("Unable to open network stream");
            }
            int r = in.read(raw);
            int off = r;
            while (r > 0)
            {
                r = in.read(raw, off, max - off);
                off += r;
            }
            return new String(raw);
        }
        catch (Exception e)
        {
            System.out.println(e + " while trying to download " + url);
            return null;
        }
        finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
            }
            catch (Exception e)
            {
            }
        }
    }

    /**
     * Reads the install.xml into confgiString
     */
    private void readConfig()
    {
        installXmlString = stringFromURL(installXmlUrl);
    }


    /**
     * Reads packsinfo.xml
     */
    private void readPacksInfo()
    {
        String url = this.baseUrl + "/" + packsinfoFilename;
        packsInfo = stringFromURL(url);
    }

    /**
     * Parse install.xml and return the list of packs
     *
     * @return the list of packs
     */
    private ArrayList<PackInfo> parsePacks()
    {
        try
        {
            IXMLParser parser = new XMLParser();

            IXMLElement xml = parser.parse(installXmlString);
            return loadPacksList(xml);
        }
        catch (Exception e)
        {
            System.out.println("WARN: Unable to parse install.xml");
            return null;
        }
    }

    /**
     * Parse packsinfo.xml, fill the nbytes field, which is not available at runtime
     * otherwise.
     */
    private void parsePacksInfo()
    {
        try
        {
            IXMLParser parser = new XMLParser();

            IXMLElement xml = parser.parse(packsInfo);
            IXMLElement root = xml; //requireChildNamed(xml, "packs");
            for (int q = 0; q < root.getChildrenCount(); q++)
            {
                IXMLElement ch = root.getChildAtIndex(q);
                PackInfo pi = packs.get(q);
                Pack p = pi.getPack();
                p.nbytes = Long.parseLong(ch.getAttribute("nbytes"));
            }
        }
        catch (Exception e)
        {
            System.out.println("WARN: Unable to parse packsinfo.xml");
        }
    }

    /**
     * First download the jar file. The create the input stream from the
     * downloaded file. This is because the Jar connection's openInputStream
     * will blocks until the whole jar in order to unzip it (there is no way
     * to see the download progress there).
     *
     * @param url
     * @return the url
     */
    public static String getCachedUrl(String url, String tempFolder) throws Exception
    {
        int max = BUFFER_SIZE;
        byte[] raw = new byte[max];
        try
        {
            WebAccessor w = new WebAccessor(null);
            InputStream in = w.openInputStream(new URL(url));
            int r = in.read(raw);
            File tempDir = new File(tempFolder);

            tempDir.mkdirs();

            File temp = File.createTempFile("izpacktempfile", "jar", new File(tempFolder));
            FileOutputStream fos = new FileOutputStream(temp);
            String path = "file:///" + temp.getAbsolutePath();
            while (r > 0)
            {
                fos.write(raw, 0, r);
                r = in.read(raw);
            }
            in.close();
            fos.close();

            return path;
        }
        catch (SecurityException e)
        {
            System.out.println(e + " while trying to write temp file: " + tempFolder);
            throw e;
        }
        catch (Exception e)
        {
            System.out.println(e + " while trying to download " + url);
            throw e;
        }
    }


    protected ArrayList<PackInfo> loadPacksList(IXMLElement data) throws CompilerException
    {
        ArrayList<PackInfo> result = new ArrayList<PackInfo>();

        // Initialisation
        IXMLElement root = requireChildNamed(data, "packs");

        // at least one pack is required
        Vector<IXMLElement> packElements = root.getChildrenNamed("pack");
        if (packElements.isEmpty())
        {
            parseError(root, "<packs> requires a <pack>");
        }

        Iterator<IXMLElement> packIter = packElements.iterator();
        while (packIter.hasNext())
        {
            IXMLElement el = packIter.next();

            // Trivial initialisations
            String name = requireAttribute(el, "name");
            String id = el.getAttribute("id");

            boolean loose = "true".equalsIgnoreCase(el.getAttribute("loose", "false"));
            String description = requireChildNamed(el, "description").getContent();
            boolean required = requireYesNoAttribute(el, "required");
            String group = el.getAttribute("group");
            String installGroups = el.getAttribute("installGroups");
            String excludeGroup = el.getAttribute("excludeGroup");
            boolean uninstall = "yes".equalsIgnoreCase(el.getAttribute("uninstall", "yes"));
            String parent = el.getAttribute("parent");

            if (required && excludeGroup != null)
            {
                parseError(el, "Pack, which has excludeGroup can not be required.", new Exception(
                        "Pack, which has excludeGroup can not be required."));
            }

            PackInfo pack = new PackInfo(name, id, description, required, loose, excludeGroup, uninstall);
            pack.setOsConstraints(OsConstraint.getOsList(el)); // TODO:
            pack.setParent(parent);

            // unverified
            // if the pack belongs to an excludeGroup it's not preselected by default
            if (excludeGroup == null)
            {
                pack.setPreselected(validateYesNoAttribute(el, "preselected", YES));
            }
            else
            {
                pack.setPreselected(validateYesNoAttribute(el, "preselected", NO));
            }

            // Set the pack group if specified
            if (group != null)
            {
                pack.setGroup(group);
            }
            // Set the pack install groups if specified
            if (installGroups != null)
            {
                StringTokenizer st = new StringTokenizer(installGroups, ",");
                while (st.hasMoreTokens())
                {
                    String igroup = st.nextToken();
                    pack.addInstallGroup(igroup);
                }
            }

            // We get the parsables list
            Iterator<IXMLElement> iter = el.getChildrenNamed("parsable").iterator();
            while (iter.hasNext())
            {
                IXMLElement p = iter.next();
                String target = requireAttribute(p, "targetfile");
                String type = p.getAttribute("type", "plain");
                String encoding = p.getAttribute("encoding", null);
                List<OsConstraint> osList = OsConstraint.getOsList(p); // TODO: unverified

                pack.addParsable(new ParsableFile(target, type, encoding, osList));
            }

            // We get the executables list
            iter = el.getChildrenNamed("executable").iterator();
            while (iter.hasNext())
            {
                IXMLElement e = iter.next();
                ExecutableFile executable = new ExecutableFile();
                String val; // temp value

                executable.path = requireAttribute(e, "targetfile");

                // when to execute this executable
                val = e.getAttribute("stage", "never");
                if ("postinstall".equalsIgnoreCase(val))
                {
                    executable.executionStage = ExecutableFile.POSTINSTALL;
                }
                else if ("uninstall".equalsIgnoreCase(val))
                {
                    executable.executionStage = ExecutableFile.UNINSTALL;
                }

                // type of this executable
                val = e.getAttribute("type", "bin");
                if ("jar".equalsIgnoreCase(val))
                {
                    executable.type = ExecutableFile.JAR;
                    executable.mainClass = e.getAttribute("class"); // executable
                    // class
                }

                // what to do if execution fails
                val = e.getAttribute("failure", "ask");
                if ("abort".equalsIgnoreCase(val))
                {
                    executable.onFailure = ExecutableFile.ABORT;
                }
                else if ("warn".equalsIgnoreCase(val))
                {
                    executable.onFailure = ExecutableFile.WARN;
                }

                // whether to keep the executable after executing it
                val = e.getAttribute("keep");
                executable.keepFile = "true".equalsIgnoreCase(val);

                // get arguments for this executable
                IXMLElement args = e.getFirstChildNamed("args");
                if (null != args)
                {
                    Iterator<IXMLElement> argIterator = args.getChildrenNamed("arg").iterator();
                    while (argIterator.hasNext())
                    {
                        IXMLElement arg = argIterator.next();
                        executable.argList.add(requireAttribute(arg, "value"));
                    }
                }

                executable.osList = OsConstraint.getOsList(e); // TODO:
                // unverified

                pack.addExecutable(executable);
            }

            // get the updatechecks list
            iter = el.getChildrenNamed("updatecheck").iterator();
            while (iter.hasNext())
            {
                IXMLElement f = iter.next();

                String casesensitive = f.getAttribute("casesensitive");

                // get includes and excludes
                ArrayList<String> includesList = new ArrayList<String>();
                ArrayList<String> excludesList = new ArrayList<String>();

                // get includes and excludes
                Iterator<IXMLElement> include_it = f.getChildrenNamed("include").iterator();
                while (include_it.hasNext())
                {
                    IXMLElement inc_el = include_it.next();
                    includesList.add(requireAttribute(inc_el, "name"));
                }

                Iterator<IXMLElement> exclude_it = f.getChildrenNamed("exclude").iterator();
                while (exclude_it.hasNext())
                {
                    IXMLElement excl_el = exclude_it.next();
                    excludesList.add(requireAttribute(excl_el, "name"));
                }

                pack.addUpdateCheck(new UpdateCheck(includesList, excludesList, casesensitive));
            }
            // We get the dependencies
            iter = el.getChildrenNamed("depends").iterator();
            while (iter.hasNext())
            {
                IXMLElement dep = iter.next();
                String depName = requireAttribute(dep, "packname");
                pack.addDependency(depName);

            }
            result.add(pack);
        }
        return result;
    }

    /**
     * Create parse error with consistent messages. Includes file name. For use When parent is
     * unknown.
     *
     * @param message Brief message explaining error
     */
    protected void parseError(String message) throws CompilerException
    {
        throw new CompilerException(installFilename + ":" + message);
    }

    /**
     * Create parse error with consistent messages. Includes file name and line # of parent. It is
     * an error for 'parent' to be null.
     *
     * @param parent  The element in which the error occured
     * @param message Brief message explaining error
     */
    protected void parseError(IXMLElement parent, String message) throws CompilerException
    {
        throw new CompilerException(installFilename + ":" + parent.getLineNr() + ": " + message);
    }

    /**
     * Create a chained parse error with consistent messages. Includes file name and line # of
     * parent. It is an error for 'parent' to be null.
     *
     * @param parent  The element in which the error occured
     * @param message Brief message explaining error
     */
    protected void parseError(IXMLElement parent, String message, Throwable cause) throws CompilerException
    {
        throw new CompilerException(installFilename + ":" + parent.getLineNr() + ": " + message, cause);
    }

    /**
     * Create a parse warning with consistent messages. Includes file name and line # of parent. It
     * is an error for 'parent' to be null.
     *
     * @param parent  The element in which the warning occured
     * @param message Warning message
     */
    protected void parseWarn(IXMLElement parent, String message)
    {
        System.out.println(installFilename + ":" + parent.getLineNr() + ": " + message);
    }

    /**
     * Call getFirstChildNamed on the parent, producing a meaningful error message on failure. It is
     * an error for 'parent' to be null.
     *
     * @param parent The element to search for a child
     * @param name   Name of the child element to get
     */
    protected IXMLElement requireChildNamed(IXMLElement parent, String name) throws CompilerException
    {
        IXMLElement child = parent.getFirstChildNamed(name);
        if (child == null)
        {
            parseError(parent, "<" + parent.getName() + "> requires child <" + name + ">");
        }
        return child;
    }

    /**
     * Call getContent on an element, producing a meaningful error message if not present, or empty,
     * or a valid URL. It is an error for 'element' to be null.
     *
     * @param element The element to get content of
     */
    protected URL requireURLContent(IXMLElement element) throws CompilerException
    {
        URL url = null;
        try
        {
            url = new URL(requireContent(element));
        }
        catch (MalformedURLException x)
        {
            parseError(element, "<" + element.getName() + "> requires valid URL", x);
        }
        return url;
    }

    /**
     * Call getContent on an element, producing a meaningful error message if not present, or empty.
     * It is an error for 'element' to be null.
     *
     * @param element The element to get content of
     */
    protected String requireContent(IXMLElement element) throws CompilerException
    {
        String content = element.getContent();
        if (content == null || content.length() == 0)
        {
            parseError(element, "<" + element.getName() + "> requires content");
        }
        return content;
    }

    /**
     * Call getAttribute on an element, producing a meaningful error message if not present, or
     * empty. It is an error for 'element' or 'attribute' to be null.
     *
     * @param element   The element to get the attribute value of
     * @param attribute The name of the attribute to get
     */
    protected String requireAttribute(IXMLElement element, String attribute) throws CompilerException
    {
        String value = element.getAttribute(attribute);
        if (value == null)
        {
            parseError(element, "<" + element.getName() + "> requires attribute '" + attribute + "'");
        }
        return value;
    }

    /**
     * Get a required attribute of an element, ensuring it is an integer. A meaningful error message
     * is generated as a CompilerException if not present or parseable as an int. It is an error for
     * 'element' or 'attribute' to be null.
     *
     * @param element   The element to get the attribute value of
     * @param attribute The name of the attribute to get
     */
    protected int requireIntAttribute(IXMLElement element, String attribute) throws CompilerException
    {
        String value = element.getAttribute(attribute);
        if (value == null || value.length() == 0)
        {
            parseError(element, "<" + element.getName() + "> requires attribute '" + attribute + "'");
        }
        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException x)
        {
            parseError(element, "'" + attribute + "' must be an integer");
        }
        return 0; // never happens
    }

    /**
     * Call getAttribute on an element, producing a meaningful error message if not present, or one
     * of "yes" or "no". It is an error for 'element' or 'attribute' to be null.
     *
     * @param element   The element to get the attribute value of
     * @param attribute The name of the attribute to get
     */
    protected boolean requireYesNoAttribute(IXMLElement element, String attribute) throws CompilerException
    {
        String value = requireAttribute(element, attribute);
        if ("yes".equalsIgnoreCase(value))
        {
            return true;
        }
        if ("no".equalsIgnoreCase(value))
        {
            return false;
        }

        parseError(element, "<" + element.getName() + "> invalid attribute '" + attribute + "': Expected (yes|no)");

        return false; // never happens
    }

    /**
     * Call getAttribute on an element, producing a meaningful warning if not "yes" or "no". If the
     * 'element' or 'attribute' are null, the default value is returned.
     *
     * @param element      The element to get the attribute value of
     * @param attribute    The name of the attribute to get
     * @param defaultValue Value returned if attribute not present or invalid
     */
    protected boolean validateYesNoAttribute(IXMLElement element, String attribute, boolean defaultValue)
    {
        if (element == null)
        {
            return defaultValue;
        }

        String value = element.getAttribute(attribute, (defaultValue ? "yes" : "no"));
        if ("yes".equalsIgnoreCase(value))
        {
            return true;
        }
        if ("no".equalsIgnoreCase(value))
        {
            return false;
        }

        // TODO: should this be an error if it's present but "none of the
        // above"?
        parseWarn(element, "<" + element.getName() + "> invalid attribute '" + attribute
                + "': Expected (yes|no) if present");

        return defaultValue;
    }

}

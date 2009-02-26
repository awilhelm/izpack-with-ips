/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2003 Jonathan Halliday
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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import com.izforge.izpack.CustomData;
import com.izforge.izpack.ExecutableFile;
import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.Panel;
import com.izforge.izpack.adaptator.*;
import com.izforge.izpack.installer.DataValidator.Status;
import com.izforge.izpack.adaptator.impl.XMLParser;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.OsConstraint;
import com.sun.pkg.client.Image;

/**
 * Runs the install process in text only (no GUI) mode.
 * 
 * @author Jonathan Halliday <jonathan.halliday@arjuna.com>
 * @author Julien Ponge <julien@izforge.com>
 * @author Johannes Lehtinen <johannes.lehtinen@iki.fi>
 */
public class AutomatedInstaller extends InstallerBase
{

    // there are panels which can be instantiated multiple times
    // we therefore need to select the right XML section for each
    // instance
    private TreeMap<String, Integer> panelInstanceCount;

    /**
     * The automated installation data.
     */
    private AutomatedInstallData idata = new AutomatedInstallData();

    /**
     * The result of the installation.
     */
    private boolean result = false;

    /**
     * Constructing an instance triggers the install.
     * 
     * @param inputFilename Name of the file containing the installation data.
     * @throws Exception Description of the Exception
     */
    public AutomatedInstaller(String inputFilename) throws Exception
    {
        super();

        File input = new File(inputFilename);

        // Loads the installation data
        loadInstallData(this.idata);

        // Loads the xml data
        this.idata.xmlData = getXMLData(input);

        // Loads the langpack
        this.idata.localeISO3 = this.idata.xmlData.getAttribute("langpack", "eng");
        InputStream in = getClass().getResourceAsStream(
                "/langpacks/" + this.idata.localeISO3 + ".xml");
        this.idata.langpack = new LocaleDatabase(in);
        this.idata.setVariable(ScriptParser.ISO3_LANG, this.idata.localeISO3);

        // create the resource manager singleton
        ResourceManager.create(this.idata);

        // Load custom langpack if exist.
        addCustomLangpack(this.idata);

        this.panelInstanceCount = new TreeMap<String, Integer>();
        // load conditions
        loadConditions(this.idata);

        // loads installer conditions
        loadInstallerRequirements();

        // load dynamic variables
        loadDynamicVariables();
    }

    /**
     * Writes the uninstalldata. <p/> Unfortunately, Java doesn't allow multiple inheritance, so
     * <code>AutomatedInstaller</code> and <code>InstallerFrame</code> can't share this code ...
     * :-/ <p/> TODO: We should try to fix this in the future.
     */
    private boolean writeUninstallData()
    {
        try
        {
            // We get the data
            UninstallData udata = UninstallData.getInstance();
            List files = udata.getUninstalableFilesList();
            ZipOutputStream outJar = this.idata.uninstallOutJar;

            if (outJar == null) { return true; // it is allowed not to have an installer
            }

            System.out.println("[ Writing the uninstaller data ... ]");

            // We write the files log
            outJar.putNextEntry(new ZipEntry("install.log"));
            BufferedWriter logWriter = new BufferedWriter(new OutputStreamWriter(outJar));
            logWriter.write(this.idata.getInstallPath());
            logWriter.newLine();
            Iterator iter = files.iterator();
            while (iter.hasNext())
            {
                logWriter.write((String) iter.next());
                if (iter.hasNext())
                {
                    logWriter.newLine();
                }
            }
            logWriter.flush();
            outJar.closeEntry();

            // We write the ips packages log
            outJar.putNextEntry(new ZipEntry("ips-install.log"));
            logWriter = new BufferedWriter(new OutputStreamWriter(outJar));
            logWriter.newLine();

            for (Image.FmriState fmristate: this.idata.installedIPSPackages)
            {
                logWriter.write(fmristate.fmri.getName());
                logWriter.newLine();
            }

            logWriter.flush();
            outJar.closeEntry();
    

            // We write the uninstaller jar file log
            outJar.putNextEntry(new ZipEntry("jarlocation.log"));
            logWriter = new BufferedWriter(new OutputStreamWriter(outJar));
            logWriter.write(udata.getUninstallerJarFilename());
            logWriter.newLine();
            logWriter.write(udata.getUninstallerPath());
            logWriter.flush();
            outJar.closeEntry();

            // Write out executables to execute on uninstall
            outJar.putNextEntry(new ZipEntry("executables"));
            ObjectOutputStream execStream = new ObjectOutputStream(outJar);
            iter = udata.getExecutablesList().iterator();
            execStream.writeInt(udata.getExecutablesList().size());
            while (iter.hasNext())
            {
                ExecutableFile file = (ExecutableFile) iter.next();
                execStream.writeObject(file);
            }
            execStream.flush();
            outJar.closeEntry();

            // *** ADDED code bellow
            // Write out additional uninstall data
            // Do not "kill" the installation if there is a problem
            // with custom uninstall data. Therefore log it to Debug,
            // but do not throw.
            Map<String, Object> additionalData = udata.getAdditionalData();
            if (additionalData != null && !additionalData.isEmpty())
            {
                Iterator<String> keys = additionalData.keySet().iterator();
                HashSet<String> exist = new HashSet<String>();
                while (keys != null && keys.hasNext())
                {
                    String key = keys.next();
                    Object contents = additionalData.get(key);
                    if ("__uninstallLibs__".equals(key))
                    {
                        Iterator nativeLibIter = ((List) contents).iterator();
                        while (nativeLibIter != null && nativeLibIter.hasNext())
                        {
                            String nativeLibName = (String) ((List) nativeLibIter.next()).get(0);
                            byte[] buffer = new byte[5120];
                            long bytesCopied = 0;
                            int bytesInBuffer;
                            outJar.putNextEntry(new ZipEntry("native/" + nativeLibName));
                            InputStream in = getClass().getResourceAsStream(
                                    "/native/" + nativeLibName);
                            while ((bytesInBuffer = in.read(buffer)) != -1)
                            {
                                outJar.write(buffer, 0, bytesInBuffer);
                                bytesCopied += bytesInBuffer;
                            }
                            outJar.closeEntry();
                        }
                    }
                    else if ("uninstallerListeners".equals(key) || "uninstallerJars".equals(key))
                    { // It is a ArrayList of ArrayLists which contains the
                        // full
                        // package paths of all needed class files.
                        // First we create a new ArrayList which contains only
                        // the full paths for the uninstall listener self; thats
                        // the first entry of each sub ArrayList.
                        ArrayList<String> subContents = new ArrayList<String>();

                        // Secound put the class into uninstaller.jar
                        Iterator listenerIter = ((List) contents).iterator();
                        while (listenerIter.hasNext())
                        {
                            byte[] buffer = new byte[5120];
                            long bytesCopied = 0;
                            int bytesInBuffer;
                            CustomData customData = (CustomData) listenerIter.next();
                            // First element of the list contains the listener
                            // class path;
                            // remind it for later.
                            if (customData.listenerName != null)
                            {
                                subContents.add(customData.listenerName);
                            }
                            Iterator<String> liClaIter = customData.contents.iterator();
                            while (liClaIter.hasNext())
                            {
                                String contentPath = liClaIter.next();
                                if (exist.contains(contentPath))
                                {
                                    continue;
                                }
                                exist.add(contentPath);
                                try
                                {
                                    outJar.putNextEntry(new ZipEntry(contentPath));
                                }
                                catch (ZipException ze)
                                { // Ignore, or ignore not ?? May be it is a
                                    // exception because
                                    // a doubled entry was tried, then we should
                                    // ignore ...
                                    Debug.trace("ZipException in writing custom data: "
                                            + ze.getMessage());
                                    continue;
                                }
                                InputStream in = getClass().getResourceAsStream("/" + contentPath);
                                if (in != null)
                                {
                                    while ((bytesInBuffer = in.read(buffer)) != -1)
                                    {
                                        outJar.write(buffer, 0, bytesInBuffer);
                                        bytesCopied += bytesInBuffer;
                                    }
                                }
                                else
                                {
                                    Debug.trace("custom data not found: " + contentPath);
                                }
                                outJar.closeEntry();

                            }
                        }
                        // Third we write the list into the
                        // uninstaller.jar
                        outJar.putNextEntry(new ZipEntry(key));
                        ObjectOutputStream objOut = new ObjectOutputStream(outJar);
                        objOut.writeObject(subContents);
                        objOut.flush();
                        outJar.closeEntry();

                    }
                    else
                    {
                        outJar.putNextEntry(new ZipEntry(key));
                        if (contents instanceof ByteArrayOutputStream)
                        {
                            ((ByteArrayOutputStream) contents).writeTo(outJar);
                        }
                        else
                        {
                            ObjectOutputStream objOut = new ObjectOutputStream(outJar);
                            objOut.writeObject(contents);
                            objOut.flush();
                        }
                        outJar.closeEntry();
                    }
                }
            }

            // write the script files, which will
            // perform several complement and unindependend uninstall actions
            ArrayList<String> unInstallScripts = udata.getUninstallScripts();
            Iterator<String> unInstallIter = unInstallScripts.iterator();
            ObjectOutputStream rootStream;
            int idx = 0;
            while (unInstallIter.hasNext())
            {
                outJar.putNextEntry(new ZipEntry(UninstallData.ROOTSCRIPT + Integer.toString(idx)));
                rootStream = new ObjectOutputStream(outJar);
                String unInstallScript = (String) unInstallIter.next();
                rootStream.writeUTF(unInstallScript);
                rootStream.flush();
                outJar.closeEntry();
            }

            // Cleanup
            outJar.flush();
            outJar.close();
            return true;
        }
        catch (Exception err)
        {
            err.printStackTrace();
            return false;
        }
    }

    /**
     * Runs the automated installation logic for each panel in turn.
     * 
     * @throws Exception
     */
    protected void doInstall() throws Exception
    {
        // check installer conditions
        if (!checkInstallerRequirements(this.idata))
        {
            Debug.log("not all installerconditions are fulfilled.");
            System.exit(-1);
            return;
        }

        // TODO: i18n
        System.out.println("[ Starting automated installation ]");
        Debug.log("[ Starting automated installation ]");

        try
        {
            // assume that installation will succeed
            this.result = true;

            // walk the panels in order
            Iterator panelsIterator = this.idata.panelsOrder.iterator();
            while (panelsIterator.hasNext())
            {
                Panel p = (Panel) panelsIterator.next();
                if (p.hasCondition()
                        && !this.idata.getRules().isConditionTrue(p.getCondition(),
                                this.idata.variables))
                {
                    Debug.log("Condition for panel " + p.getPanelid() + "is not fulfilled, skipping panel!");
                    if (this.panelInstanceCount.containsKey(p.className))
                    {
                       // get number of panel instance to process
                        this.panelInstanceCount.put(p.className, this.panelInstanceCount
                                .get(p.className) + 1);
                    }
                    continue;
                }

                String praefix = "com.izforge.izpack.panels.";
                if (p.className.compareTo(".") > -1)
                // Full qualified class name
                {
                    praefix = "";
                }
                if (!OsConstraint.oneMatchesCurrentSystem(p.osConstraints))
                {
                    continue;
                }

                String panelClassName = p.className;
                String automationHelperClassName = praefix + panelClassName + "AutomationHelper";
                Class<PanelAutomation> automationHelperClass = null;

                Debug.log("AutomationHelper:" + automationHelperClassName);
                // determine if the panel supports automated install
                try
                {

                    automationHelperClass = (Class<PanelAutomation>) Class
                            .forName(automationHelperClassName);

                }
                catch (ClassNotFoundException e)
                {
                    // this is OK - not all panels have/need automation support.
                    Debug.log("ClassNotFoundException-skip :" + automationHelperClassName);
                    // but run actions and validate it anyway
                    executePreConstructActions(p, null);
                    executePreValidateActions(p, null);
                    validatePanel(p);
                    executePostValidateActions(p, null);
                    continue;
                }

                // instantiate the automation logic for the panel
                PanelAutomation automationHelperInstance = null;
                if (automationHelperClass != null)
                {
                    try
                    {
                        executePreConstructActions(p, null);
                        Debug.log("Instantiate :" + automationHelperClassName);
                        automationHelperInstance = automationHelperClass.newInstance();
                    }
                    catch (Exception e)
                    {
                        Debug.log("ERROR: no default constructor for " + automationHelperClassName
                                + ", skipping...");
                        // but run actions and validate it anyway
                        executePreValidateActions(p, null);
                        validatePanel(p);
                        executePostValidateActions(p, null);
                        continue;
                    }
                }

                // We get the panels root xml markup
                Vector<IXMLElement> panelRoots = this.idata.xmlData.getChildrenNamed(panelClassName);
                int panelRootNo = 0;

                if (this.panelInstanceCount.containsKey(panelClassName))
                {
                    // get number of panel instance to process
                    panelRootNo = this.panelInstanceCount.get(panelClassName);
                }

                IXMLElement panelRoot = panelRoots.elementAt(panelRootNo);

                this.panelInstanceCount.put(panelClassName, panelRootNo + 1);

                // execute the installation logic for the current panel, if it has
                // any:
                if (automationHelperInstance != null)
                {
                    try
                    {
                        executePreActivateActions(p, null);
                        Debug.log("automationHelperInstance.runAutomated :"
                                + automationHelperClassName + " entered.");
                        if (!automationHelperInstance.runAutomated(this.idata, panelRoot))
                        {
                            // make installation fail instantly
                            this.result = false;
                            return;
                        }
                        else
                        {
                            Debug.log("automationHelperInstance.runAutomated :"
                                    + automationHelperClassName + " successfully done.");
                        }
                    }
                    catch (Exception e)
                    {
                        Debug.log("ERROR: automated installation failed for panel "
                                + panelClassName);
                        e.printStackTrace();
                        this.result = false;
                    }

                }
                executePreValidateActions(p, null);
                validatePanel(p);
                executePostValidateActions(p, null);            }

            // this does nothing if the uninstaller was not included
            writeUninstallData();

            if (this.result)
            {
                System.out.println("[ Automated installation done ]");
            }
            else
            {
                System.out.println("[ Automated installation FAILED! ]");
            }
        }
        catch (Exception e)
        {
            this.result = false;
            System.err.println(e.toString());
            e.printStackTrace();
            System.out.println("[ Automated installation FAILED! ]");
        }
        finally
        {
            // Bye
            Housekeeper.getInstance().shutDown(this.result ? 0 : 1);
        }
    }

    /**
     * @param p
     */
    private void validatePanel(final Panel p) throws InstallerException
    {
        String dataValidator = p.getValidator();
        if (dataValidator != null)
        {
            DataValidator validator = DataValidatorFactory.createDataValidator(dataValidator);
            com.izforge.izpack.installer.DataValidator.Status validationResult = validator
                    .validateData(idata);
            if (validationResult != DataValidator.Status.OK)
            {
                // make installation fail instantly
                this.result = false;
                // if defaultAnswer is true, result is ok
                if (validationResult == Status.WARNING && validator.getDefaultAnswer())
                {
                    System.out
                            .println("Configuration said, it's ok to go on, if validation is not successfull");
                    this.result = true;
                    return;
                }
                throw new InstallerException("Validating data was not successfull");
            }
        }
        return;
    }

    /**
     * Loads the xml data for the automated mode.
     * 
     * @param input The file containing the installation data.
     * @return The root of the XML file.
     * @throws Exception thrown if there are problems reading the file.
     */
    public IXMLElement getXMLData(File input) throws Exception
    {
        FileInputStream in = new FileInputStream(input);

        // Initialises the parser
        IXMLParser parser = new XMLParser();
        IXMLElement rtn = parser.parse(in);
        in.close();

        return rtn;
    }

    /**
     * Get the result of the installation.
     * 
     * @return True if the installation was successful.
     */
    public boolean getResult()
    {
        return this.result;
    }
    
    private final List<PanelAction> createPanelActionsFromStringList(Panel panel, List<String> actions)
    {
        List<PanelAction> actionList = null;
        if (actions != null)
        {
            actionList = new ArrayList<PanelAction>();
            for (String actionClassName : actions){
                PanelAction action = PanelActionFactory.createPanelAction(actionClassName);
                action.initialize(panel.getPanelActionConfiguration(actionClassName));
            }            
        }
        return actionList;
    }

    private final void executePreConstructActions(Panel panel, AbstractUIHandler handler)
    {
        List<PanelAction> preConstructActions = createPanelActionsFromStringList(panel, panel
                .getPreConstructionActions());
        if (preConstructActions != null)
        {
            for (int actionIndex = 0; actionIndex < preConstructActions.size(); actionIndex++)
            {
                preConstructActions.get(actionIndex).executeAction(idata, handler);
            }
        }
    }

    private final void executePreActivateActions(Panel panel, AbstractUIHandler handler)
    {
        List<PanelAction> preActivateActions = createPanelActionsFromStringList(panel, panel
                .getPreActivationActions());
        if (preActivateActions != null)
        {
            for (int actionIndex = 0; actionIndex < preActivateActions.size(); actionIndex++)
            {
                preActivateActions.get(actionIndex).executeAction(idata, handler);
            }
        }
    }

    private final void executePreValidateActions(Panel panel, AbstractUIHandler handler)
    {
        List<PanelAction> preValidateActions = createPanelActionsFromStringList(panel, panel
                .getPreValidationActions());
        if (preValidateActions != null)
        {
            for (int actionIndex = 0; actionIndex < preValidateActions.size(); actionIndex++)
            {
                preValidateActions.get(actionIndex).executeAction(idata, handler);
            }
        }
    }

    private final void executePostValidateActions(Panel panel, AbstractUIHandler handler)
    {
        List<PanelAction> postValidateActions = createPanelActionsFromStringList(panel,panel
                .getPostValidationActions());
        if (postValidateActions != null)
        {
            for (int actionIndex = 0; actionIndex < postValidateActions.size(); actionIndex++)
            {
                postValidateActions.get(actionIndex).executeAction(idata, handler);
            }
        }
    }
}

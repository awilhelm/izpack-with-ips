// IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
//
// http://izpack.org/
// http://izpack.codehaus.org/
// 
// Licensed under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License. You may obtain a copy of
// the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
//    
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// License for the specific language governing permissions and limitations under
// the License.

package com.izforge.izpack.installer;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipOutputStream;
import com.izforge.izpack.IPSPack;
import com.izforge.izpack.Info;
import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.Pack;
import com.izforge.izpack.rules.RulesEngine;
import com.sun.pkg.client.Fmri;
import com.sun.pkg.client.Image.FmriState;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.impl.XMLElementImpl;

/**
 * Encloses information about the install process. This implementation is not
 * thread safe.
 * 
 * @author Julien Ponge <julien@izforge.com>
 * @author Johannes Lehtinen <johannes.lehtinen@iki.fi>
 */
public class AutomatedInstallData {

	// --- Static members -------------------------------------------------
	public static final String MODIFY_INSTALLATION = "modify.izpack.install";

	public static final String INSTALLATION_INFORMATION = ".installationinformation";

	/**
	 * Names of the custom actions types with which they are stored in the
	 * installer jar file. These names are also used to identify the type of
	 * custom action in the customData map. Slashes as first char are needed to
	 * use the names as "file" name in the installer jar.
	 */
	/*
	 * Attention !! Do not change the existent names and the order. Add a / as
	 * first char at new types. Add new type handling in Unpacker.
	 */
	static final String[] CUSTOM_ACTION_TYPES = new String[] {
			"/installerListeners", "/uninstallerListeners", "/uninstallerLibs",
			"/uninstallerJars" };

	public static final int INSTALLER_LISTENER_INDEX = 0;

	public static final int UNINSTALLER_LISTENER_INDEX = 1;

	public static final int UNINSTALLER_LIBS_INDEX = 2;

	public static final int UNINSTALLER_JARS_INDEX = 3;

	// --- Instance members -----------------------------------------------
	private RulesEngine rules;

	/**
	 * The language code.
	 */
	public String localeISO3;

	/**
	 * The used locale.
	 */
	public Locale locale;

	/**
	 * The language pack.
	 */
	public LocaleDatabase langpack;

	/**
	 * The uninstaller jar stream.
	 */
	public ZipOutputStream uninstallOutJar;

	/**
	 * The inforamtions.
	 */
	public Info info;

	/**
	 * The complete list of packs.
	 */
	public List<Pack> allPacks;

	/**
	 * The available packs.
	 */
	public List<Pack> availablePacks;

	/**
	 * The selected packs.
	 */
	public List<Pack> selectedPacks;

	/**
	 * The available IPS packs.
	 */
	public List<IPSPack> IPSPacks = new ArrayList<IPSPack>();

	/**
	 * The IPS packs selected for installation.
	 */
	public List<IPSPack> selectedIPSPacks = new ArrayList<IPSPack>();

	/**
	 * A list holding the state of every installed packages in the current
	 * image.
	 */
	public List<FmriState> installedIPSPackages = new ArrayList<FmriState>();

	/**
	 * The IPS packages selected for upgrade.
	 */
	public List<Fmri> selectedIPSPackages = new ArrayList<Fmri>();

	/**
	 * The IPS packages selected for removal.
	 */
	public List<Fmri> unwantedIPSPackages = new ArrayList<Fmri>();

	/**
	 * Whether we want this installer to stay there after the installation ends.
	 */
	public boolean keepInstallerWhenDone = false;

	/**
	 * Whether we want the Sun Update center to get installed.
	 */
	public boolean installUpdateCenter = false;

	/**
	 * The URL of the proxy we use to download files or packages.
	 */
	public Proxy proxy = Proxy.NO_PROXY;

	/**
	 * The panels list.
	 */
	public List<IzPanel> panels;

	/**
	 * The panels order.
	 */
	public List panelsOrder;

	/**
	 * The current panel.
	 */
	public int curPanelNumber;

	/**
	 * Can we close the installer ?
	 */
	public boolean canClose = false;

	/**
	 * Did the installation succeed ?
	 */
	public boolean installSuccess = true;

	/**
	 * The xmlData for automated installers.
	 */
    public IXMLElement xmlData;

	/**
	 * Custom data.
	 */
	public Map<String, List> customData;

	/**
	 * Maps the variable names to their values
	 */
	protected Properties variables;

	/**
	 * The attributes used by the panels
	 */
	protected Map<String, Object> attributes;

	/**
	 * This class should be a singleton. Therefore the one possible object will
	 * be stored in this static member.
	 */
	private static AutomatedInstallData self = null;

	/**
	 * Returns the one possible object of this class.
	 * 
	 * @return the one possible object of this class
	 */
	public static AutomatedInstallData getInstance () {
		return (self);
	}

	/**
	 * Constructs a new instance of this class. Only one should be possible, at
	 * a scound call a RuntimeException will be raised.
	 */
	public AutomatedInstallData () {
		availablePacks = new ArrayList<Pack>();
		selectedPacks = new ArrayList<Pack>();
		panels = new ArrayList<IzPanel>();
		panelsOrder = new ArrayList();
        xmlData = new XMLElementImpl("AutomatedInstallation");
		variables = new Properties();
		attributes = new HashMap<String, Object>();
		customData = new HashMap<String, List>();
		if (self != null) throw new RuntimeException(
				"Panic!! second call of the InstallData Ctor!!");
		self = this;
	}

	/**
	 * Returns the map of variable values. Modifying this will directly affect
	 * the current value of variables.
	 * 
	 * @return the map of variable values
	 */
	public Properties getVariables () {
		return variables;
	}

	/**
	 * Sets a variable to the specified value. This is short hand for
	 * <code>getVariables().setProperty(var, val)</code>.
	 * 
	 * @param var the name of the variable
	 * @param val the new value of the variable
	 * @see #getVariable
	 */
	public void setVariable (String var, String val) {
		variables.setProperty(var, val);
	}

	/**
	 * Returns the current value of the specified variable. This is short hand
	 * for <code>getVariables().getProperty(var)</code>.
	 * 
	 * @param var the name of the variable
	 * @return the value of the variable or null if not set
	 * @see #setVariable
	 */
	public String getVariable (String var) {
		return variables.getProperty(var);
	}

	/**
	 * Sets the install path.
	 * 
	 * @param path the new install path
	 * @see #getInstallPath
	 */
	public void setInstallPath (String path) {
		setVariable(ScriptParser.INSTALL_PATH, path);
	}

	/**
	 * Returns the install path.
	 * 
	 * @return the current install path or null if none set yet
	 * @see #setInstallPath
	 */
	public String getInstallPath () {
		return getVariable(ScriptParser.INSTALL_PATH);
	}

	/**
	 * Returns the value of the named attribute.
	 * 
	 * @param attr the name of the attribute
	 * @return the value of the attribute or null if not set
	 * @see #setAttribute
	 */
	public Object getAttribute (String attr) {
		return attributes.get(attr);
	}

	/**
	 * Sets a named attribute. The panels and other IzPack components can attach
	 * custom attributes to InstallData to communicate with each other. For
	 * example, a set of co-operating custom panels do not need to implement a
	 * common data storage but can use InstallData singleton. The name of the
	 * attribute should include the package and class name to prevent name space
	 * collisions.
	 * 
	 * @param attr the name of the attribute to set
	 * @param val the value of the attribute or null to unset the attribute
	 * @see #getAttribute
	 */
	public void setAttribute (String attr, Object val) {
		if (val == null) attributes.remove(attr);
		else attributes.put(attr, val);
	}

	public RulesEngine getRules () {
		return rules;
	}

	public void setRules (RulesEngine rules) {
		this.rules = rules;
	}
}

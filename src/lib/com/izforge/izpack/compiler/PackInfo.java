/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2004 Chadwick McHenry
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

package com.izforge.izpack.compiler;

import com.izforge.izpack.*;
import com.izforge.izpack.util.OsConstraint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.*;

/**
 * Temporary holding place for Pack information as the Packager is built. The packager is used by
 * the compiler to collect info about an installer, and finally create the actual installer files.
 *
 * @author Chadwick McHenry
 */
public class PackInfo implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = -3604642858885697783L;

    /**
     * The pack object serialized in the installer.
     */
    private Pack pack;

    /**
     * The color of the node. This is used for the dependency graph algorithms
     */
    public int colour;

    /**
     * white colour
     */
    public final static int WHITE = 0;

    /**
     * grey colour
     */
    public final static int GREY = 1;

    /**
     * black colour
     */
    public final static int BLACK = 2;

    /**
     * Files of the Pack.
     */
    private Map files = new HashMap();

    /**
     * Parsables files in this Pack.
     */
    private List parsables = new ArrayList();

    /**
     * Executable files in this Pack.
     */
    private List executables = new ArrayList();

    /**
     * Update check specifications in this Pack.
     */
    private List updateChecks = new ArrayList();

    /**
     * Constructor with required info.
     *
     * @param name         name of the pack
     * @param id           id of the pack e.g. to resolve I18N
     * @param description  descripton in English
     * @param required     pack is required or not
     * @param loose        files of pack should be stored separatly or not
     * @param excludegroup name of the exclude group
     * @param uninstall    pack must be uninstalled
     */
    public PackInfo(String name, String id, String description, boolean required, boolean loose, String excludegroup, boolean uninstall)
    {
        boolean ispreselected = (excludegroup == null);
        pack = new Pack(name, id, description, null, null, required, ispreselected, loose, excludegroup, uninstall);
        colour = PackInfo.WHITE;
    }

    /**
     * ********************************************************************************************
     * Attributes of the Pack
     * ********************************************************************************************
     */

    public void setDependencies(List<String> dependencies)
    {
        pack.dependencies = dependencies;
    }

    /**
     * Set the name of the group which contains the packs which exludes mutual.
     *
     * @param group name of the mutal exclude group
     */
    public void setExcludeGroup(String group)
    {
        pack.excludeGroup = group;
    }

    public void setOsConstraints(List<OsConstraint> osConstraints)
    {
        pack.osConstraints = osConstraints;
    }

    public List<OsConstraint> getOsConstraints(List osConstraints)
    {
        return pack.osConstraints;
    }

    public void setPreselected(boolean preselected)
    {
        pack.preselected = preselected;
    }

    public boolean isPreselected()
    {
        return pack.preselected;
    }

    /**
     * Get the pack group.
     *
     * @return Get the pack group, null if there is no group.
     */
    public String getGroup()
    {
        return pack.group;
    }

    /**
     * Set the pack group.
     *
     * @param group the group to associate the pack with.
     */
    public void setGroup(String group)
    {
        pack.group = group;
    }

    /**
     * Add an install group to the pack.
     *
     * @param group the install group to associate the pack with.
     */
    public void addInstallGroup(String group)
    {
        pack.installGroups.add(group);
    }

    /**
     * See if the pack is associated with the given install group.
     *
     * @param group the install group name to check
     * @return true if the given group is associated with the pack.
     */
    public boolean hasInstallGroup(String group)
    {
        return pack.installGroups.contains(group);
    }

    /**
     * Get the install group names.
     *
     * @return Set<String> for the install groups
     */
    public Set<String> getInstallGroups()
    {
        return pack.installGroups;
    }

    public Pack getPack()
    {
        return pack;
    }
    
    public boolean isHidden()
    {
        return pack.isHidden();
    }

    
    public void setHidden(boolean hidden)
    {
        pack.setHidden(hidden);
    }

    /***********************************************************************************************
     * Public methods to add data to the Installer being packed
     **********************************************************************************************/

    /**
     * Add a file or directory to be installed.
     *
     * @param file       the file or basedir to be installed.
     * @param targetfile path file will be installed to.
     * @param osList     the target operation system(s) of this pack.
     * @param override   what to do if the file already exists when installing
     * @param condition
     * @throws FileNotFoundException if the file specified does not exist. The file is not read
     *                               until the {@link Packager#createInstaller} is invoked, thus a FileNotFoundEception will occur
     *                               then, if the file is deleted in between.
     */
    /*
     * public void addFile(File file, String targetfile, List osList, int override) throws
     * FileNotFoundException { addFile( file,targetfile, osList, override, null); }
     * 
     * 
     * /** Add a file or directory to be installed.
     * 
     * @param file the file or basedir to be installed. @param targetfile path file will be
     * installed to. @param osList the target operation system(s) of this pack. @param override what
     * to do if the file already exists when installing @param additionals Map which contains
     * additional data
     * 
     * @throws FileNotFoundException if the file specified does not exist. The file is not read
     * until the {@link Packager#createInstaller} is invoked, thus a FileNotFoundEception will occur
     * then, if the file is deleted in between.
     */
    public void addFile(File baseDir, File file, String targetfile, List<OsConstraint> osList, int override, Map additionals, String condition)
            throws FileNotFoundException
    {
        if (!file.exists())
        {
            throw new FileNotFoundException(file.toString());
        }

        PackFile packFile = new PackFile(baseDir, file, targetfile, osList, override, additionals);
        packFile.setLoosePackInfo(pack.loose);
        packFile.setCondition(condition);
        files.put(packFile, file);
    }

    /**
     * Set of PackFile objects for this Pack.
     */
    public Set getPackFiles()
    {
        return files.keySet();
    }

    /**
     * The file described by the specified PackFile. Returns <tt>null</tt> if the PackFile did not
     * come from the set returned by {@link #getPackFiles()}.
     */
    public File getFile(PackFile packFile)
    {
        return (File) files.get(packFile);
    }

    /**
     * Parsable files have variables substituted after installation.
     */
    public void addParsable(ParsableFile parsable)
    {
        parsables.add(parsable);
    }

    /**
     * List of parsables for this Pack.
     */
    public List getParsables()
    {
        return parsables;
    }

    /**
     * Executables files have their executable flag set, may be executed, and optionally, deleted
     * when finished executing.
     */
    public void addExecutable(ExecutableFile executable)
    {
        executables.add(executable);
    }

    /**
     * List of parsables for this Pack.
     */
    public List getExecutables()
    {
        return executables;
    }

    /**
     * Executables files have their executable flag set, may be executed, and optionally, deleted
     * when finished executing.
     */
    public void addUpdateCheck(UpdateCheck updateCheck)
    {
        updateChecks.add(updateCheck);
    }

    /**
     * List of update checks for this Pack.
     */
    public List getUpdateChecks()
    {
        return updateChecks;
    }

    /**
     * The packs that this file depends on
     */
    public void addDependency(String dependency)
    {
        if (pack.dependencies == null)
        {
            pack.dependencies = new ArrayList<String>();
        }
        pack.dependencies.add(dependency);
    }

    public List<String> getDependencies()
    {
        return pack.dependencies;
    }

    public String getParent()
    {
        return pack.parent;
    }

    public void setParent(String p)
    {
        pack.parent = p;
    }

    public String toString()
    {
        return pack.name;
    }

    public void setPackImgId(String packImgId)
    {
        pack.packImgId = packImgId;
    }


    /**
     * @return the condition
     */
    public String getCondition()
    {
        return this.pack.getCondition();
    }


    /**
     * @param condition the condition to set
     */
    public void setCondition(String condition)
    {
        this.pack.setCondition(condition);
    }

    public void addValidator(String validatorClassName)
    {
        pack.addValidator(validatorClassName);
    }
}

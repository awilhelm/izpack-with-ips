package com.izforge.izpack;

import java.io.Serializable;

/**
 * This class keeps the information about an IPS Pack.
 *
 */
public class IPSPack implements Serializable
{

    // Address of the remote IPS repository
    private String repositoryUri;

    // Version that the installer will try to get, if available
    private String version;

    // True if the pack will be checked in the list of the installer
    private boolean checkedByDefault;


    // Name of the pack
    private String name;

    // Description of the pack
    private String description;

    // Files from the repository we want to include
    private String includes[];

    // Files from the repository we want to exclude
    private String excludes[];


    // Constructor
    public IPSPack(String inRepositoryAddress, String inName, String inDescription, String inVersion, boolean inCheckedByDefault, String inIncludes[], String inExcludes[])
    {
        this.repositoryUri = inRepositoryAddress;
        this.name = inName;
        this.description = inDescription;
        this.version = inVersion;
        this.checkedByDefault = inCheckedByDefault;

        this.includes = inIncludes;
        this.excludes = inExcludes;
    }

    public String getVersion()
    {
        return version;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
       return description;
    }

    public boolean isCheckedByDefault()
    {
       return checkedByDefault;
    }




}

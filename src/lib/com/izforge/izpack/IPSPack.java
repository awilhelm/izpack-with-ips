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

    // Description of the pack
    private String description;

    // Files from the repository we want to include
    private String includes[];

    // Files from the repository we want to exclude
    private String excludes[];


    // Constructor
    public IPSPack(String inRepositoryAddress, String inDescription, String inVersion, String inIncludes[], String inExcludes[])
    {
        this.repositoryUri = inRepositoryAddress;
        this.description = inDescription;
        this.version = inVersion;

        this.includes = inIncludes;
        this.excludes = inExcludes;
    }

    public String getDescription()
    {
       return description;
    }







}

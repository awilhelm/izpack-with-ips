package com.izforge.izpack.installer;

import com.izforge.izpack.util.OsVersion;

import java.io.*;
import java.net.URL;
import java.net.URI;

public class PrivilegedRunner
{
    public boolean isPlatformSupported()
    {
        return OsVersion.IS_MAC;
    }

    public boolean isElevationNeeded()
    {
        if (OsVersion.IS_WINDOWS)
        {
            return true;
        }
        else
        {
            return !System.getProperty("user.name").equals("root");
        }
    }

    public void relaunchWithElevatedRights() throws IOException, InterruptedException
    {
        String javaCommand = getJavaCommand();
        String installer = getInstallerJar();
        ProcessBuilder builder = new ProcessBuilder(getElevator(), javaCommand, "-jar", installer);
        builder.environment().put("izpack.mode", "privileged");
        builder.start().waitFor();
    }

    private String getElevator() throws IOException, InterruptedException
    {
        if (OsVersion.IS_OSX)
        {
            return extractMacElevator().getCanonicalPath();
        }

        return null;
    }

    private File extractMacElevator() throws IOException, InterruptedException
    {
        String path = System.getProperty("java.io.tmpdir") + File.separator + "Installer";
        File elevator = new File(path);

        FileOutputStream out = new FileOutputStream(elevator);
        InputStream in = getClass().getResourceAsStream("/com/izforge/izpack/installer/run-with-privileges-on-osx");
        copyStream(out, in);
        in.close();
        out.close();

        makeExecutable(path);

        elevator.deleteOnExit();
        return elevator;
    }

    private void makeExecutable(String path) throws InterruptedException, IOException
    {
        new ProcessBuilder("/bin/chmod", "+x", path).start().waitFor();
    }

    private void copyStream(OutputStream out, InputStream in) throws IOException
    {
        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        while ((bytesRead = in.read(buffer)) >= 0)
        {
            out.write(buffer, 0, bytesRead);
        }
    }

    private String getInstallerJar()
    {
        String res = PrivilegedRunner.class.getName().replace('.', '/') + ".class";
        URL url = ClassLoader.getSystemResource(res);
        String path = url.getFile();
        path = path.substring(0, path.lastIndexOf('!'));
        try
        {
            return new File(URI.create(path)).getCanonicalPath();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private String getJavaCommand()
    {
        return new StringBuilder(System.getProperty("java.home"))
                .append(File.separator)
                .append("bin")
                .append(File.separator)
                .append(getJavaExecutable())
                .toString();
    }

    private String getJavaExecutable()
    {
        if (OsVersion.IS_WINDOWS)
        {
            return "java.exe";
        }
        else
        {
            return "java";
        }
    }
}

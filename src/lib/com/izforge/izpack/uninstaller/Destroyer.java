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

package com.izforge.izpack.uninstaller;

import com.izforge.izpack.ExecutableFile;
import com.izforge.izpack.event.UninstallerListener;
import com.izforge.izpack.installer.UninstallData;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.os.unix.ShellScript;
import com.sun.pkg.client.Image;

import java.io.*;
import java.util.*;


/**
 * The files destroyer class.
 *
 * @author Julien Ponge
 */
public class Destroyer extends Thread
{

    /**
     * True if the destroyer must force the recursive deletion.
     */
    private boolean forceDestroy;

    /**
     * The installation path.
     */
    private String installPath;

    /**
     * the destroyer listener.
     */
    private AbstractUIProgressHandler handler;

    /**
     * The constructor.
     *
     * @param installPath  The installation path.
     * @param forceDestroy Shall we force the recursive deletion.
     * @param handler      The destroyer listener.
     */
    public Destroyer(String installPath, boolean forceDestroy, AbstractUIProgressHandler handler)
    {
        super("IzPack - Destroyer");

        this.installPath = installPath;
        this.forceDestroy = forceDestroy;
        this.handler = handler;
    }

    /**
     * The run method.
     */
    @Override
	public void run()
    {
        try
        {
            // We get the list of uninstaller listeners
            List[] listeners = getListenerLists();
            // We get the list of the files to delete
            ArrayList<ExecutableFile> executables = getExecutablesList();

            FileExecutor executor = new FileExecutor(executables);
            executor.executeFiles(ExecutableFile.UNINSTALL, this.handler);

            ArrayList<File> files = getFilesList();
            int size = files.size();

            // Custem action listener stuff --- beforeDeletion ----
            informListeners(listeners[0], UninstallerListener.BEFORE_DELETION, files, handler);

            handler.startAction("destroy", size);

            // We destroy the files
            for (int i = 0; i < size; i++)
            {
                File file = files.get(i);
                // Custem action listener stuff --- beforeDelete ----
                informListeners(listeners[1], UninstallerListener.BEFORE_DELETE, file, handler);

                file.delete();

                // Custem action listener stuff --- afterDelete ----
                informListeners(listeners[1], UninstallerListener.AFTER_DELETE, file, handler);

                handler.progress(i, file.getAbsolutePath());
            }

            // Custom action listener stuff --- afterDeletion ----
            informListeners(listeners[0], UninstallerListener.AFTER_DELETION, files, handler);

            if (OsVersion.IS_UNIX)
            {
                ArrayList<String> rootScripts = getRootScripts();
                Iterator<String> rsi = rootScripts.iterator();
                while (rsi.hasNext())
                {
                  execRootScript(rsi.next());
                }
            }
            // We make a complementary cleanup
            handler.progress(size, "[ cleanups ]");
            cleanup(new File(installPath));

            handler.stopAction();

			/*
			 * deletion of IPS Packs
			 */
			try
			{
				Image img = new Image(new File(installPath));
				List<Image.FmriState> fmrilist = img.getInventory(
						new String[0], false);
				if (!fmrilist.isEmpty())
				{
					handler.startAction("IPSDestroyer", fmrilist.size() - 1);
					int i = 0;
					/*
					 * Deletion pack after pack to be able to have a progress
					 * bar
					 */
					for (Image.FmriState b: fmrilist)
					{
						img.uninstallPackages(new String[] { b.fmri.getName() });
						handler.progress(i++, "Removing " + b.fmri.getName());
					}
					handler.stopAction();
				}
			}
			catch (Exception e)
			{
				handler.emitWarning("Error", e.getMessage());
			}
        }
        catch (Exception err)
        {
            handler.stopAction();
            err.printStackTrace();

            StackTraceElement str[] = err.getStackTrace();
            for (StackTraceElement aStr : str)
            {
            	// TODO Write something in there.
            }

            StringWriter trace = new StringWriter();
            // err.printStackTrace(new PrintStream);
            err.printStackTrace(new PrintWriter(trace));

            handler.emitError("exception caught", err.toString() + "\n" + trace.toString());
        }
    }

    /**
     * Asks the JVM for the uninstaller deletion.
     *
     * @exception Exception Description of the Exception
     */
    // private void askUninstallerRemoval() throws Exception
    // {
    // // Initialisations
    // InputStream in = Destroyer.class.getResourceAsStream("/jarlocation.log");
    // InputStreamReader inReader = new InputStreamReader(in);
    // BufferedReader reader = new BufferedReader(inReader);
    //
    // // We delete
    // File jar = new File(reader.readLine());
    // File path = new File(reader.readLine());
    // File inst = new File(installPath);
    // jar.deleteOnExit();
    // path.deleteOnExit();
    // inst.deleteOnExit();
    // }
    /**
     * Returns an ArrayList of the files to delete.
     *
     * @return The files list.
     * @throws Exception Description of the Exception
     */
    private ArrayList<File> getFilesList() throws Exception
    {
        // Initializations
        TreeSet<File> files = new TreeSet<File>(Collections.reverseOrder());
        InputStream in = Destroyer.class.getResourceAsStream("/install.log");
        InputStreamReader inReader = new InputStreamReader(in);
        BufferedReader reader = new BufferedReader(inReader);

        // We skip the first line (the installation path)
        reader.readLine();

        // We read it
        String read = reader.readLine();
        while (read != null)
        {
            files.add(new File(read));
            read = reader.readLine();
        }

        // We return it
        return new ArrayList<File>(files);
    }

    /**
     * Gets the List of all Executables
     *
     * @return The ArrayList of the Executables
     * @throws Exception
     */
    private ArrayList<ExecutableFile> getExecutablesList() throws Exception
    {
        ArrayList<ExecutableFile> executables = new ArrayList<ExecutableFile>();
        ObjectInputStream in = new ObjectInputStream(Destroyer.class
                .getResourceAsStream("/executables"));
        int num = in.readInt();
        for (int i = 0; i < num; i++)
        {
            ExecutableFile file = (ExecutableFile) in.readObject();
            executables.add(file);
        }
        return executables;
    }

    /**
     * Gets the root files.
     *
     * @return The files which should remove by root for another user
     */
    private ArrayList<String> getRootScripts()
    {
        ArrayList<String> result = new ArrayList<String>();
        
        int idx=0;
        while(true)
        {
          try
          {
            ObjectInputStream in = new ObjectInputStream(Destroyer.class.getResourceAsStream("/"
                  + UninstallData.ROOTSCRIPT+Integer.toString(idx)));
        
            result.add( in.readUTF() );
          }
          catch (Exception e) 
          {
             Debug.log("Last RootScript Index=" + idx);
             break;
          }        
          idx++;
        }
        return result;
    }

    /**
     * Removes the given files as root for the given Users
     *
     * @param aRootScript The Script to exec as uninstall time by root.
     */
    private void execRootScript(String aRootScript)
    {
        if (!"".equals(aRootScript))
        {
            Debug.log("Will Execute: " + aRootScript);

            try
            {
                String result = ShellScript.execAndDelete(new StringBuffer(aRootScript), File.createTempFile(
                        this.getClass().getName(),
                        Long.toString(System.currentTimeMillis()) + ".sh").toString());
                Debug.log("Result: " + result);
            }
            catch (Exception ex)
            {
                Debug.log("Exeption during su remove: " + ex.getMessage());
            }
        }
    }

    /**
     * Makes some recursive cleanups.
     *
     * @param file The file to wipe.
     * @throws Exception Description of the Exception
     */
    private void cleanup(File file) throws Exception
    {
        if (file.isDirectory())
        {
            File[] files = file.listFiles();
            int size = files.length;
            for (int i = 0; i < size; i++)
            {
                cleanup(files[i]);
            }
            file.delete();
        }
        else if (forceDestroy)
        {
            file.delete();
        }

    }

    // CUSTOM ACTION STUFF -------------- start -----------------

    /**
     * Load the defined uninstall listener objects.
     *
     * @return a list with the defined uninstall listeners
     * @throws Exception
     */
    private List[] getListenerLists() throws Exception
    {
        ArrayList[] uninstaller = new ArrayList[]{new ArrayList(), new ArrayList()};
        // Load listeners if exist
        InputStream in;
        ObjectInputStream objIn;
        in = Destroyer.class.getResourceAsStream("/uninstallerListeners");
        if (in != null)
        {
            objIn = new ObjectInputStream(in);
            List listeners = (List) objIn.readObject();
            objIn.close();
            Iterator iter = listeners.iterator();
            while (iter != null && iter.hasNext())
            {
                Class<UninstallerListener> clazz = (Class<UninstallerListener>) Class.forName(((String) iter.next()));
                UninstallerListener ul = clazz.newInstance();
                if (ul.isFileListener())
                {
                    uninstaller[1].add(ul);
                }
                uninstaller[0].add(ul);
            }
        }
        return uninstaller;
    }

    /**
     * Informs all listeners.
     *
     * @param listeners list with the listener objects
     * @param action    identifier which callback should be called
     * @param param     parameter for the call
     * @param handler   the current progress handler
     */

    private void informListeners(List listeners, int action, Object param,
                                 AbstractUIProgressHandler handler)
    {
        // Iterate the action list.
        Iterator iter = listeners.iterator();
        UninstallerListener il = null;
        while (iter.hasNext())
        {
            try
            {
                il = (UninstallerListener) iter.next();
                switch (action)
                {
                    case UninstallerListener.BEFORE_DELETION:
                        il.beforeDeletion((List) param, handler);
                        break;
                    case UninstallerListener.AFTER_DELETION:
                        il.afterDeletion((List) param, handler);
                        break;
                    case UninstallerListener.BEFORE_DELETE:
                        il.beforeDelete((File) param, handler);
                        break;
                    case UninstallerListener.AFTER_DELETE:
                        il.afterDelete((File) param, handler);
                        break;
                }
            }
            catch (Throwable e)
            { // Catch it to prevent for a block of uninstallation.
                handler.emitError("Skipping custom action because exception caught during "
                        + il.getClass().getName(), e.toString());
            }
        }
    }

    // CUSTOM ACTION STUFF -------------- end -----------------

}

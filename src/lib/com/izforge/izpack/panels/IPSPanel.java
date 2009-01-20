package com.izforge.izpack.panels;

import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.LayoutConstants;
import com.izforge.izpack.Info;
import com.izforge.izpack.IPSPack;
import com.izforge.izpack.util.IoHelper;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: aoyama
 * Date: 12 janv. 2009
 * Time: 21:48:59
 * To change this template use File | Settings | File Templates.
 */
public class IPSPanel extends IzPanel
{

    /**
     * The tip label.
     */
    protected JTextArea descriptionArea;

     /**
     * The packs table.
     */
    protected JTable packsTable;

    protected JScrollPane tableScroller;

     /**
     * The constructor.
     *
     * @param parent The parent.
     * @param idata  The installation data.
     */
    public IPSPanel(InstallerFrame parent, InstallData idata)
    {
        this(parent, idata, new IzPanelLayout());
    }


     /**
     * Creates the table for the packs. All parameters are required. The table will be returned.
     *
     * @param width       of the table
     * @param scroller    the scroller to be used
     * @param layout      layout to be used
     * @param constraints constraints to be used
     * @return the created table
     */
    protected JTable createPacksTable(int width, JScrollPane scroller, GridBagLayout layout,
                                      GridBagConstraints constraints)
    {
        JTable table = new JTable();
        table.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(Color.white);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //table.getSelectionModel().addListSelectionListener(this);
        table.setShowGrid(false);
        scroller.setViewportView(table);
        scroller.setAlignmentX(LEFT_ALIGNMENT);
        scroller.getViewport().setBackground(Color.white);
        scroller.setPreferredSize(new Dimension(width, (idata.guiPrefs.height / 3 + 30)));

        if (layout != null && constraints != null)
        {
            layout.addLayoutComponent(scroller, constraints);
        }
        add(scroller);
        return (table);
    }

    /**
     * Creates a new IPSPanel object with the given layout manager.
     *
     * @param parent The parent IzPack installer frame.
     * @param idata  The installer internal data.
     * @param layout layout manager to be used with this IzPanel
     */

    public IPSPanel(InstallerFrame parent, InstallData idata, LayoutManager2 layout)
    {

        super(parent, idata, layout);

        //tableScroller = new JScrollPane();
        //packsTable = createPacksTable(300, tableScroller, null, null);

        Iterator<IPSPack> packIter = idata.IPSPacks.iterator();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel select=new JLabel("Sélectionnez les packs IPS à installer :");


        int i=0;

        add(select, NEXT_LINE);


         while (packIter.hasNext())
         {


            IPSPack ipsPack = packIter.next();

            Checkbox atemp=new Checkbox(ipsPack.getDescription());

            add(atemp, NEXT_LINE);

         }


        // At end of layouting we should call the completeLayout method also they do nothing.
        getLayoutHelper().completeLayout();
    }


    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return Always true.
     */
    public boolean isValidated()
    {
        return true;
    }

}


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
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.CellRendererPane;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: aoyama
 * Date: 12 janv. 2009
 * Time: 21:48:59
 * To change this template use File | Settings | File Templates.
 */
public class IPSPanel extends IzPanel implements ListSelectionListener
{

    /**
     * The tip label.
     */
    protected JTextArea descriptionArea;

     /**
     * The packs table.
     */
    protected JTable packsTable;

    /*
     * The scrollers components
     */
    protected JScrollPane tableScroller;
    protected JScrollPane descriptionScroller;

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

    /*
     * JTable Table Model
     */
    class IPSTableModel extends AbstractTableModel
    {
            private String[] columnNames;
            private Object[][] data;

            public IPSTableModel(String[] inCol, Object[][] inData)
            {
                columnNames=inCol;
                data=inData;
            }

            public int getColumnCount()
            {
                return columnNames.length;
            }

            public int getRowCount()
            {
                return data.length;
            }

            public String getColumnName(int col)
            {
                return columnNames[col];
            }

            public Object getValueAt(int row, int col)
            {
                return data[row][col];
            }

            /*
             * Used for the default renderer (JCheckBox for boolean)
             */
            public Class getColumnClass(int c)
            {
                return getValueAt(0, c).getClass();
            }

            public boolean isCellEditable(int row, int col)
            {
                // Only the first column is editable
                if (col == 0)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }

            public void setValueAt(Object value, int row, int col)
            {
                data[row][col] = value;
                fireTableCellUpdated(row, col);
            }
    }

    /*
     * Make the layout
     */
    public void createLayout(Object[][] data)
    {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Display the Headline label
        headLineLabel=new JLabel(parent.langpack.getString("IPSPanel.headline"));
        headLineLabel.setAlignmentX(LEFT_ALIGNMENT);

        add(headLineLabel);

        // Display the packs list
        packsTable = new JTable(new IPSTableModel(new String[] { "", "Name" }, data));
        packsTable.setIntercellSpacing(new Dimension(0, 0));
        packsTable.setShowGrid(false);
        packsTable.getColumnModel().getColumn(0).setMaxWidth(30); // checkboxes column
        packsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        packsTable.getSelectionModel().addListSelectionListener(this);
        packsTable.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));

        tableScroller = new JScrollPane();
        tableScroller.setViewportView(packsTable);

        add(tableScroller);

        // Display the description of a pack
        descriptionArea = new JTextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionScroller = new JScrollPane();
        descriptionScroller.setMinimumSize(new Dimension(this.getWidth(), 150));
        descriptionScroller.setBorder(BorderFactory.createTitledBorder(parent.langpack.getString("IPSPanel.description")));
        descriptionScroller.setViewportView(descriptionArea);

        add(descriptionScroller);

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

        Object[][] data = new Object[idata.IPSPacks.size()][2];

        // We make the data for the packs table
        Iterator<IPSPack> packIter = idata.IPSPacks.iterator();
        for (int i=0;packIter.hasNext();i++)
        {
            IPSPack ipsPack = packIter.next();

            data[i][0] = ipsPack.isCheckedByDefault();
            data[i][1] = ipsPack.getName();
        }

        createLayout(data);


        // At end of layouting we should call the completeLayout method also they do nothing.
        getLayoutHelper().completeLayout();
    }

     /*
     * (non-Javadoc)
     *
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e)
    {
        // Get the description
        IPSPack selectedPack = idata.IPSPacks.get(packsTable.getSelectedRow());
        String desc=selectedPack.getDescription();

        // If a version number is available, we add it before the description
        if (selectedPack.getVersion()!=null)
        {
            desc="Version : " + selectedPack.getVersion() + "\n" + desc;
        }

        // Put the description in the Textarea
        descriptionArea.setText(desc);
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

    /*
     * Called when the user goes to the next panel
     */
    public void panelDeactivate()
    {
        // if the user has pressed the previous button, some packs may have been added yet, so we clear the list
        idata.selectedIPSPacks.clear();

        // for each pack, we check if it has been selected and add it to the selected list or not
        Iterator<IPSPack> packIter = idata.IPSPacks.iterator();
        for (int i=0;packIter.hasNext();i++)
        {

            IPSPack ipsPack = packIter.next();

            if (packsTable.getValueAt(i,0).equals(true))
            {
                idata.selectedIPSPacks.add(ipsPack);
            }

        }

    }

}


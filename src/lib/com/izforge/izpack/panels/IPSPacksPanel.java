
package com.izforge.izpack.panels;

import java.awt.Dimension;
import java.awt.LayoutManager2;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import com.izforge.izpack.IPSPack;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;

/**
 * @author Romain Tertiaux
 */
public class IPSPacksPanel extends IzPanel implements ListSelectionListener
{
	/**
	 * JTable Table Model
	 */
	private static final class IPSTableModel extends AbstractTableModel
	{
		/**
		 * This allows this model to get serialized.
		 */
		private static final long serialVersionUID = 719679513919461813L;

		/**
		 * The columns' headers.
		 */
		private final String[] columnNames;

		/**
		 * A list of (checked, name) pairs.
		 */
		private final Object[][] data;

		/**
		 * @param inCol
		 * @param inData
		 */
		public IPSTableModel (String[] inCol, Object[][] inData)
		{
			columnNames = inCol;
			data = inData;
		}

		/**
		 * Used for the default renderer (JCheckBox for a boolean).
		 * 
		 * @see AbstractTableModel#getColumnClass(int)
		 */
		@Override
		public Class<?> getColumnClass (int c)
		{
			return getValueAt(0, c).getClass();
		}

		/**
		 * {@inheritDoc}
		 */
		public int getColumnCount ()
		{
			return columnNames.length;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getColumnName (int col)
		{
			return columnNames[col];
		}

		/**
		 * {@inheritDoc}
		 */
		public int getRowCount ()
		{
			return data.length;
		}

		/**
		 * {@inheritDoc}
		 */
		public Object getValueAt (int row, int col)
		{
			return data[row][col];
		}

		/**
		 * Only the first column is editable.
		 * 
		 * @see AbstractTableModel#isCellEditable(int, int)
		 */
		@Override
		public boolean isCellEditable (int row, int col)
		{
			return col == 0;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void setValueAt (Object value, int row, int col)
		{
			data[row][col] = value;
			fireTableCellUpdated(row, col);
		}
	}

	/**
	 * This allows this panel to get serialized.
	 */
	private static final long serialVersionUID = 6932399414745838289L;

	/**
	 * The tip label.
	 */
	protected JTextArea descriptionArea;

	/**
	 * The packs table.
	 */
	protected JTable packsTable;

	/**
	 * The scroller component containing the table.
	 */
	protected JScrollPane tableScroller;

	/**
	 * The scroller component containing the descriptive text.
	 */
	protected JScrollPane descriptionScroller;

	/**
	 * The constructor.
	 * 
	 * @param parent The parent.
	 * @param idata The installation data.
	 */
	public IPSPacksPanel (InstallerFrame parent, InstallData idata)
	{
		this(parent, idata, new IzPanelLayout());
	}

	/**
	 * Creates a new IPSPanel object with the given layout manager.
	 * 
	 * @param parent The parent IzPack installer frame.
	 * @param idata The installer internal data.
	 * @param layout layout manager to be used with this IzPanel.
	 */
	public IPSPacksPanel (InstallerFrame parent, InstallData idata,
			LayoutManager2 layout)
	{
		super(parent, idata, layout);
		Object[][] data = new Object[idata.ipsPacks.size()][2];
		/*
		 * We make the data for the packs table
		 */
		int i = 0;
		for (IPSPack pack: idata.ipsPacks)
		{
			data[i][0] = pack.isCheckedByDefault();
			data[i][1] = pack.getName();
			++i;
		}
		createLayout(data);
	}

	/**
	 * Make the layout.
	 * 
	 * @param data
	 */
	public void createLayout (Object[][] data)
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		/*
		 * Display the Headline label.
		 */
		headLineLabel = new JLabel(
				parent.langpack.getString("IPSPacksPanel.headline"));
		headLineLabel.setAlignmentX(LEFT_ALIGNMENT);
		add(headLineLabel);
		/*
		 * Display the packs list.
		 */
		packsTable = new JTable(new IPSTableModel(new String[] { "",
				parent.langpack.getString("IPSPacksPanel.name") }, data));
		packsTable.setIntercellSpacing(new Dimension(0, 0));
		packsTable.setShowGrid(false);
		packsTable.getColumnModel().getColumn(0).setMaxWidth(30);
		packsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		packsTable.getSelectionModel().addListSelectionListener(this);
		packsTable.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		tableScroller = new JScrollPane();
		tableScroller.setViewportView(packsTable);
		add(tableScroller);
		/*
		 * Display the description of a pack.
		 */
		descriptionArea = new JTextArea();
		descriptionArea.setEditable(false);
		descriptionArea.setLineWrap(true);
		descriptionScroller = new JScrollPane();
		descriptionScroller.setMinimumSize(new Dimension(getWidth(), 150));
		descriptionScroller.setBorder(BorderFactory.createTitledBorder(parent.langpack.getString("IPSPacksPanel.description")));
		descriptionScroller.setViewportView(descriptionArea);
		add(descriptionScroller);
		/*
		 * At end of layouting we should call the completeLayout method also
		 * they do nothing.
		 */
		getLayoutHelper().completeLayout();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void panelDeactivate ()
	{
		/*
		 * If the user has pressed the previous button, some packs may have been
		 * added yet, so we clear the list.
		 */
		idata.selectedIPSPacks.clear();
		/*
		 * For each pack, we check if it has been selected and add it to the
		 * selected list or not.
		 */
		int i = 0;
		for (IPSPack pack: idata.ipsPacks)
		{
			if ((Boolean) packsTable.getValueAt(i++, 0))
			{
				idata.selectedIPSPacks.add(pack);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void valueChanged (ListSelectionEvent e)
	{
		/*
		 * Get the description.
		 */
		IPSPack selectedPack = idata.ipsPacks.get(packsTable.getSelectedRow());
		String desc = selectedPack.getDescription();
		/*
		 * If a version number is available, we add it before the description.
		 */
		if (selectedPack.getVersion() != null)
		{
			desc = parent.langpack.getString("IPSPacksPanel.version") + " "
					+ selectedPack.getVersion() + "\n" + desc;
		}
		/*
		 * Put the description in the Textarea.
		 */
		descriptionArea.setText(desc);
	}
}

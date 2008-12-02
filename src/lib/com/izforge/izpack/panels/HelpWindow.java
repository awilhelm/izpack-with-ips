package com.izforge.izpack.panels;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.izforge.izpack.util.Debug;

/**
 * Common HTML Help Window (modal)
 * 
 * @version $Id: HelpWindow.java,v 1.1.2.3 2008/10/07 05:21:58 zbp Exp $
 */
public class HelpWindow extends JDialog implements HyperlinkListener, ActionListener
{

    /**
     * Helps information
     */
    public final static String HELP_TAG = "help";

    public final static String ISO3_ATTRIBUTE = "iso3";

    public final static String SRC_ATTRIBUTE = "src";

    /**
     * 
     */
    private static final long serialVersionUID = -357544689286217809L;

    private JPanel jContentPane = null;

    private JEditorPane _htmlHelp = null;

    private JButton _btnClose = null;

    private JScrollPane _scrollPane = null;

    private String _closeButtonText = "Close";

    /**
     * This is the default constructor
     * 
     * @param owner - owner Frame
     * @param closeButtonText - Button Text for Close button
     */
    public HelpWindow(Frame owner, String closeButtonText)
    {
        super(owner, true);
        _closeButtonText = closeButtonText;
        initialize();
    }

    /**
     * This method initializes Help Dialog
     * 
     */
    private void initialize()
    {
        this.setSize(600, 400);
        this.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        this.setContentPane(getJContentPane());
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane()
    {
        if (jContentPane == null)
        {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BoxLayout(getJContentPane(), BoxLayout.Y_AXIS));
            jContentPane.add(get_scrollPane(), null);
            jContentPane.add(get_btnClose(), null);
        }
        return jContentPane;
    }

    /**
     * This method initializes _htmlHelp
     * 
     * @return javax.swing.JEditorPane
     */
    private JEditorPane get_htmlHelp()
    {
        if (_htmlHelp == null)
        {
            try
            {
                _htmlHelp = new JEditorPane();
                _htmlHelp.setContentType("text/html"); // Generated
                _htmlHelp.setEditable(false);
                _htmlHelp.addHyperlinkListener(this);
            }
            catch (java.lang.Throwable e)
            {
                Debug.log(e.getLocalizedMessage());
            }
        }
        return _htmlHelp;
    }

    private JScrollPane get_scrollPane()
    {
        if (_scrollPane == null)
        {
            try
            {
                _scrollPane = new JScrollPane(get_htmlHelp());
            }
            catch (java.lang.Throwable e)
            {
                Debug.log(e.getLocalizedMessage());
            }
        }
        return _scrollPane;
    }

    /**
     * This method initializes _btnClose
     * 
     * @return javax.swing.JButton
     */
    private JButton get_btnClose()
    {
        if (_btnClose == null)
        {
            try
            {
                _btnClose = new JButton(_closeButtonText);
                _btnClose.setAlignmentX(CENTER_ALIGNMENT);
                _btnClose.addActionListener(this);
            }
            catch (java.lang.Throwable e)
            {
                Debug.log(e.getLocalizedMessage());
            }
        }
        return _btnClose;
    }

    public void hyperlinkUpdate(HyperlinkEvent e)
    {
        try
        {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
            {
                get_htmlHelp().setPage(e.getURL());
            }
        }
        catch (Exception err)
        {
            // Ignore exceptions
        }
    }

    /**
     * displays Help Text in a modal window
     * 
     * @param title
     * @param helpDocument
     */
    public void showHelp(String title, URL helpDocument)
    {
        this.setTitle(title);
        try
        {
            get_htmlHelp().setPage(helpDocument);
        }
        catch (IOException e)
        {
            Debug.log(e.getLocalizedMessage());
        }
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e)
    {
        // Close button pressed
        this.setVisible(false);
    }
}

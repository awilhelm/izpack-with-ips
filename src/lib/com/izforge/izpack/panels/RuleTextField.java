/*---------------------------------------------------------------------------*
 *                      (C) Copyright 2002 by Elmar Grom
 *                                       
 *                           - All Rights Reserved -             
 *                                       
 *                  THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE        
 *                                       
 * This copyright notice does not evidence any actual or intended publication
 *---------------------------------------------------------------------------*/

package   com.izforge.izpack.panels;

import    java.awt.*;
import    java.awt.event.*;
import    java.util.*;

import    javax.swing.*;
import    javax.swing.event.*;
import    javax.swing.text.*;
  
/*---------------------------------------------------------------------------*/
/**
 * One line synopsis.
 * <BR><BR>
 * Enter detailed class description here.
 *
 * @see      
 *
 * @version  0.0.1 / 10/20/02
 * @author   Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public class RuleTextField extends JTextField
{
  /** Used to specify numeric input only */
  public  static int     N       = 1;
  /** Used to specify hexadecimal input only */
  public  static int     H       = 2;
  /** Used to specify alphabetic input only */
  public  static int     A       = 3;
  /** Used to specify open input (no restrictions) */
  public  static int     O       = 4;
  /** Used to specify alpha-numeric input only */
  public  static int     AN      = 5;

  private int       columns;
  private int       editLength;
  private int       type;
  private boolean   unlimitedEdit;
  private Toolkit   toolkit;
  private Rule      rule;

  public RuleTextField (int      digits,
                        int      editLength,
                        int      type,
                        boolean  unlimitedEdit,
                        Toolkit  toolkit) 
  {
    super (digits + 1);
    
    columns             = digits;
    this.toolkit        = toolkit;
    this.type           = type;
    this.editLength     = editLength;
    this.unlimitedEdit  = unlimitedEdit;
    rule.setRuleType (type, editLength, unlimitedEdit);
  }

  protected Document createDefaultModel () 
  {
    rule = new Rule ();
    return (rule);
  }
 
  public int getColumns ()
  {
    return (columns);
  }

  public int getEditLength ()
  {
    return (editLength);
  }
  
  public boolean unlimitedEdit ()
  {
    return (unlimitedEdit);
  }

  public void setColumns (int columns)
  {
    super.setColumns (columns + 1);
    this.columns = columns;
  }


// --------------------------------------------------------------------------
//
// --------------------------------------------------------------------------
 
  class Rule extends PlainDocument 
  {
    private int       editLength;
    private int       type;
    private boolean   unlimitedEdit;
  
    public void setRuleType (int      type,
                             int      editLength,
                             boolean  unlimitedEdit)
    {
      this.type           = type;
      this.editLength     = editLength;
      this.unlimitedEdit  = unlimitedEdit;
    }
    
    public void insertString (int           offs, 
                              String        str, 
                              AttributeSet  a)      throws BadLocationException 
    {
      // --------------------------------------------------
      // don't process if we get a null reference
      // --------------------------------------------------
      if (str == null) 
      {
        return;
      }
      
      // --------------------------------------------------
      // Compute the total length the string would become
      // if the insert request were be honored. If this
      // size is within the specified limits, apply further
      // rules, otherwise give an error signal and return.
      // --------------------------------------------------
      int totalSize = getLength () + str.length ();
      
      if ((totalSize <= editLength) || (unlimitedEdit))
      {
        boolean error = false;
        
        // test for numeric type
        if (type == N)
        {
          for (int i = 0; i < str.length (); i++)
          {
            if (!Character.isDigit (str.charAt (i)))
            {
              error = true;
            }
          }
        }
        // test for hex type
        else if (type == H)
        {
          for (int i = 0; i < str.length (); i++)
          {
            char focusChar = Character.toUpperCase (str.charAt (i));
            if (!Character.isDigit (focusChar) &&
                (focusChar != 'A')             &&
                (focusChar != 'B')             &&
                (focusChar != 'C')             &&
                (focusChar != 'D')             &&
                (focusChar != 'E')             &&
                (focusChar != 'F')               )
            {
              error = true;
            }
          }
        }
        // test for alpha type
        else if (type == A)
        {
          for (int i = 0; i < str.length (); i++)
          {
            if (!Character.isLetter (str.charAt (i)))
            {
              error = true;
            }
          }
        }
        // test for alpha-numeric type
        else if (type == AN)
        {
          for (int i = 0; i < str.length (); i++)
          {
            if (!Character.isLetterOrDigit (str.charAt (i)))
            {
              error = true;
            }
          }
        }
        // test for 'open' -> no limiting rule at all
        else if (type == O)
        {
          // let it slide...
        }
        else
        {
          System.out.println ("type = " + type);
        }
        
        // ------------------------------------------------
        // if we had no error when applying the rules, we
        // are ready to insert the string, otherwise give
        // an error signal.
        // ------------------------------------------------
        if (!error)
        {
          super.insertString (offs, str, a);
        }
        else
        {
          toolkit.beep ();
        }
      }
      else
      {
        toolkit.beep ();
      }
 	  }
  }
}
/*---------------------------------------------------------------------------*/
/*
 * $Id: FullScreenWindow.java,v 1.3 2007/12/20 18:33:33 rbair Exp $
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.sun.pdfview.main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A window that takes over the full screen.  You can put exactly one
 * JComponent into the window.  If there are multiple screens attached
 * to the computer, this class will display buttons on each screen so
 * that the user can select which one receives the full-screen window.
 */
public class FullScreenWindow {
    /**
     * The screen that the user last chose for displaying a
     * FullScreenWindow
     */
    private static GraphicsDevice defaultScreen;

    /**
     * The current screen for the FullScreenWindow
     */
    private GraphicsDevice screen;

    /**
     * The JFrame filling the screen
     */
    private JFrame jf;

    /**
     * Whether this FullScreenWindow has been used.  Each FullScreenWindow
     * can only be displayed once.
     */
    private boolean dead = false;

    /**
     * Create a full screen window containing a JComponent, and ask the
     * user which screen they'd like to use if more than one is present.
     *
     * @param part        the JComponent to display
     * @param forcechoice true if you want force the display of the screen
     *                    choice buttons.  If false, buttons will only display if the user
     *                    hasn't previously picked a screen.
     */
    public FullScreenWindow(JComponent part, boolean forcechoice) {
        //	super();
        init(part, forcechoice);
    }

    /**
     * Create a full screen window containing a JComponent.  The user
     * will only be asked which screen to display on if there are multiple
     * monitors attached and the user hasn't already made a choice.
     *
     * @param part the JComponent to display
     */
    public FullScreenWindow(JComponent part) {
        //	super();
        init(part, false);
    }

    /**
     * Close the full screen window.  This particular FullScreenWindow
     * object cannot be used again.
     */
    public void close() {
        dead = true;
        flag.set();
        screen.setFullScreenWindow(null);
        if (jf != null) {
            jf.dispose();
        }
    }

    /**
     * Create the window, asking for which screen to use if there are
     * multiple monitors and either forcechoice is true, or the user
     * hasn't already picked a screen.
     *
     * @param part        the JComponent to display
     * @param forcechoice false if user shouldn't be asked twice which
     *                    of several monitors to use.
     */
    private void init(JComponent part, boolean forcechoice) {
        if (forcechoice) {
            defaultScreen = null;
        }
        screen = null;

        GraphicsEnvironment ge =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice screens[] = ge.getScreenDevices();
        if (defaultScreen != null) {
            for (int i = 0; i < screens.length; i++) {
                if (screens[i] == defaultScreen) {
                    screen = defaultScreen;
                }
            }
        }

        if (screens.length == 1) {
            screen = screens[0];
        }
        if (screen == null) {
            screen = pickScreen(screens);
        }
        if (dead) {
            return;
        }
        defaultScreen = screen;
        DisplayMode dm = screen.getDisplayMode();
        GraphicsConfiguration gc = screen.getDefaultConfiguration();
        jf = new JFrame(gc);
        jf.setUndecorated(true);
        jf.setBounds(gc.getBounds());
        jf.getContentPane().add(part);
        jf.show();
        screen.setFullScreenWindow(jf);
    }

    /**
     * A button that appears on a particular graphics device, asking
     * whether that device should be used for multiple-monitor choices.
     */
    class PickMe extends JFrame {
        GraphicsDevice mygd;

        /**
         * Creates the PickMe button on a particular display.
         *
         * @param gd the GraphicsDevice (display) to use for this button
         */
        public PickMe(GraphicsDevice gd) {
            super(gd.getDefaultConfiguration());
            //	    super((java.awt.Frame)null, false);
            setUndecorated(true);
            mygd = gd;
            JButton jb = new JButton("Click here to use this screen");
            jb.setBackground(Color.yellow);
            jb.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    pickDevice(mygd);
                }
            });
            Dimension sz = jb.getPreferredSize();
            sz.width += 30;
            sz.height = 200;
            jb.setPreferredSize(sz);
            getContentPane().add(jb);
            pack();
            Rectangle bounds = gd.getDefaultConfiguration().getBounds();
            int x = bounds.width / 2 - sz.width / 2 + bounds.x;
            int y = bounds.height / 2 - sz.height / 2 + bounds.y;
//	    System.out.println("Opening picker at "+x+","+y);
            setLocation(x, y);
            show();
        }
    }

    /**
     * Flag indicating whether the user has selected a screen or not.
     */
    private Flag flag = new Flag();
    private GraphicsDevice pickedDevice;

    /**
     * Select a particular screen for display of this window, and set
     * the flag.
     */
    private void pickDevice(GraphicsDevice gd) {
        pickedDevice = gd;
        flag.set();
    }

    /**
     * Displays a button on each attached monitor, and returns the
     * GraphicsDevice object associated with that monitor.
     *
     * @param scrns a list of GraphicsDevices on which to display buttons
     * @return the GraphicsDevice selected.
     */
    private GraphicsDevice pickScreen(GraphicsDevice scrns[]) {
        flag.clear();
        int count = 0;
        PickMe pickers[] = new PickMe[scrns.length];
        for (int i = 0; i < scrns.length; i++) {
            if (scrns[i].isFullScreenSupported()) {
                count++;
            }
            pickers[i] = new PickMe(scrns[i]);
        }
        flag.waitForFlag();
        for (int i = 0; i < pickers.length; i++) {
            if (pickers[i] != null) {
                pickers[i].dispose();
            }
        }
        return pickedDevice;
    }
}

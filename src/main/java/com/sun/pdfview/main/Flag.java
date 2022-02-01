/*
 * $Id: Flag.java,v 1.2 2007/12/20 18:33:33 rbair Exp $
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

/**
 * A generic synchronized flag, because Java doesn't have one.
 */
public class Flag {
    private boolean isSet;

    /**
     * Sets the flag.  Any pending waitForFlag calls will now return.
     */
    public synchronized void set() {
        isSet = true;
        notifyAll();
    }

    /**
     * Clears the flag.  Do this before calling waitForFlag.
     */
    public synchronized void clear() {
        isSet = false;
    }

    /**
     * Waits for the flag to be set, if it is not set already.
     * This method catches InterruptedExceptions, so if you want
     * notification of interruptions, use interruptibleWaitForFlag
     * instead.
     */
    public synchronized void waitForFlag() {
        if (!isSet) {
            try {
                wait();
            } catch (InterruptedException ie) {
            }
        }
    }

    /**
     * Waits for the flag to be set, if it is not set already.
     */
    public synchronized void interruptibleWaitForFlag()
            throws InterruptedException {
        if (!isSet) {
            wait();
        }
    }
}


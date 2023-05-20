/*
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
package org.loboevolution.pdfview;

/**
 * An interface for rendering or parsing, which can be stopped and started.
 *
  *
  *
 */
public interface Watchable {

    /** the possible statuses */
    int UNKNOWN = 0;
    /** Constant <code>NOT_STARTED=1</code> */
    int NOT_STARTED = 1;
    /** Constant <code>PAUSED=2</code> */
    int PAUSED = 2;
    /** Constant <code>NEEDS_DATA=3</code> */
    int NEEDS_DATA = 3;
    /** Constant <code>RUNNING=4</code> */
    int RUNNING = 4;
    /** Constant <code>STOPPED=5</code> */
    int STOPPED = 5;
    /** Constant <code>COMPLETED=6</code> */
    int COMPLETED = 6;
    /** Constant <code>ERROR=7</code> */
    int ERROR = 7;

    /**
     * Get the status of this watchable
     *
     * @return one of the well-known statuses
     */
    int getStatus();

    /**
     * Stop this watchable.  Stop will cause all processing to cease,
     * and the watchable to be destroyed.
     */
    void stop();

    /**
     * Start this watchable and run until it is finished or stopped.
     * Note the watchable may be stopped if go() with a
     * different time is called during execution.
     */
    void go();

    /**
     * Start this watchable and run for the given number of steps or until
     * finished or stopped.
     *
     * @param steps the number of steps to run for
     */
    void go(int steps);

    /**
     * Start this watchable and run for the given amount of time, or until
     * finished or stopped.
     *
     * @param millis the number of milliseconds to run for
     */
    void go(long millis);
}

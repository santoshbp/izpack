/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2003 Jonathan Halliday
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

package com.izforge.izpack.panels.process;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.handler.AbstractUIProcessHandler;
import com.izforge.izpack.installer.automation.PanelAutomation;
import com.izforge.izpack.installer.automation.PanelAutomationHelper;
import com.izforge.izpack.util.Housekeeper;

/**
 * Functions to support automated usage of the CompilePanel
 *
 * @author Jonathan Halliday
 * @author Tino Schwarze
 */
public class ProcessPanelAutomationHelper extends PanelAutomationHelper implements PanelAutomation,
        AbstractUIProcessHandler
{

    private int noOfJobs = 0;

    private int currentJob = 0;
    private ProcessPanelWorker processPanelWorker;

    /**
     * Constructs a <tt>ProcessPanelAutomationHelper</tt>.
     *
     * @param processPanelWorker the process panel worker
     * @param housekeeper        the house-keeper
     */
    public ProcessPanelAutomationHelper(ProcessPanelWorker processPanelWorker, Housekeeper housekeeper)
    {
        super(housekeeper);
        this.processPanelWorker = processPanelWorker;
        processPanelWorker.setHandler(this);
    }

    /**
     * Save installDataGUI for running automated.
     *
     * @param installData installation parameters
     * @param panelRoot   unused.
     */
    public void makeXMLData(AutomatedInstallData installData, IXMLElement panelRoot)
    {
        // not used here - during automatic installation, no automatic
        // installation information is generated
    }

    /**
     * Perform the installation actions.
     *
     * @param panelRoot The panel XML tree root.
     */
    public void runAutomated(AutomatedInstallData idata, IXMLElement panelRoot) throws InstallerException
    {
        processPanelWorker.run();
        if (!processPanelWorker.getResult())
        {
            throw new InstallerException("The work done by the ProcessPanel (line " + panelRoot.getLineNr() + ") failed");
        }
    }

    public void logOutput(String message, boolean stderr)
    {
        if (stderr)
        {
            System.err.println(message);
        }
        else
        {
            System.out.println(message);
        }
    }

    /**
     * Reports progress on System.out
     *
     * @see com.izforge.izpack.api.handler.AbstractUIProcessHandler#startProcessing(int)
     */
    public void startProcessing(int noOfJobs)
    {
        System.out.println("[ Starting processing ]");
        this.noOfJobs = noOfJobs;
    }

    /**
     * @see com.izforge.izpack.api.handler.AbstractUIProcessHandler#finishProcessing
     */
    public void finishProcessing(boolean unlockPrev, boolean unlockNext)
    {
        /* FIXME: maybe we should abort if unlockNext is false...? */
        System.out.println("[ Processing finished ]");
    }

    /**
     *
     */
    public void startProcess(String name)
    {
        this.currentJob++;
        System.out.println("Starting process " + name + " (" + Integer.toString(this.currentJob)
                + "/" + Integer.toString(this.noOfJobs) + ")");
    }

    public void finishProcess()
    {
    }
}

/*
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

package com.izforge.izpack.panels.compile;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.installer.automation.PanelAutomation;
import com.izforge.izpack.installer.automation.PanelAutomationHelper;
import com.izforge.izpack.util.Housekeeper;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Functions to support automated usage of the CompilePanel
 *
 * @author Jonathan Halliday
 * @author Tino Schwarze
 */
public class CompilePanelAutomationHelper extends PanelAutomationHelper implements PanelAutomation,
        CompileHandler
{

    private CompileWorker worker = null;

    private int job_max = 0;

    private String job_name = null;

    private int last_line_len = 0;

    // when using the eclipse compiler, we're capturing System.out and System.err...
    private PrintStream stdout;
    private PrintStream stderr;
    private VariableSubstitutor variableSubstitutor;

    /**
     * The resource manager.
     */
    private final ResourceManager resources;

    /**
     * Constructs a <tt>CompilePanelAutomationHelper</tt>.
     *
     * @param variableSubstitutor the variable substituter
     * @param resources           the resource manager
     * @param housekeeper         the house-keeper
     */
    public CompilePanelAutomationHelper(VariableSubstitutor variableSubstitutor, ResourceManager resources,
                                        Housekeeper housekeeper)
    {
        super(housekeeper);
        this.variableSubstitutor = variableSubstitutor;
        this.resources = resources;
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
     * @throws InstallerException if something went wrong.
     */
    public void runAutomated(AutomatedInstallData idata, IXMLElement panelRoot) throws InstallerException
    {
        IXMLElement compiler_xml = panelRoot.getFirstChildNamed("compiler");

        String compiler = null;

        if (compiler_xml != null)
        {
            compiler = compiler_xml.getContent();
        }

        if (compiler == null)
        {
            throw new InstallerException("invalid automation installDataGUI: could not find compiler");
        }

        IXMLElement args_xml = panelRoot.getFirstChildNamed("arguments");

        String args = null;

        if (args_xml != null)
        {
            args = args_xml.getContent();
        }

        if (args_xml == null)
        {
            throw new InstallerException("invalid automation installDataGUI: could not find compiler arguments");
        }

        try
        {

            this.worker = new CompileWorker(idata, this, variableSubstitutor, resources);
            this.worker.setCompiler(compiler);
            this.worker.setCompilerArguments(args);

            this.stdout = System.out;
            this.stderr = System.err;

            this.worker.run();

            if (this.worker.getResult().isSuccess())
            {
                throw new InstallerException("Compilation failed (xml line " + panelRoot.getLineNr() + ")");
            }
        }
        catch (IOException e)
        {
            throw new InstallerException(e);
        }
    }

    /**
     * Reports progress on System.out
     *
     * @see com.izforge.izpack.api.handler.AbstractUIProgressHandler#startAction(String, int)
     */
    public void startAction(String name, int noOfJobs)
    {
        this.stdout.println("[ Starting compilation ]");
        this.job_name = "";
    }

    /**
     * Reports the error to System.err
     *
     * @param error the error
     * @see CompileHandler#handleCompileError(CompileResult)
     */
    public void handleCompileError(CompileResult error)
    {
        this.stdout.println();
        this.stdout.println("[ Compilation failed ]");
        this.stderr.println("Command line: " + error.getCmdline());
        this.stderr.println();
        this.stderr.println("stdout of compiler:");
        this.stderr.println(error.getStdout());
        this.stderr.println("stderr of compiler:");
        this.stderr.println(error.getStderr());
        // abort instantly and make installation fail
        error.setAction(CompileResult.ACTION_ABORT);
    }

    /**
     * Sets state variable for thread sync.
     *
     * @see com.izforge.izpack.api.handler.AbstractUIProgressHandler#stopAction()
     */
    public void stopAction()
    {
        if ((this.job_name != null) && (this.last_line_len > 0))
        {
            String line = this.job_name + ": done.";
            this.stdout.print("\r" + line);
            for (int i = line.length(); i < this.last_line_len; i++)
            {
                this.stdout.print(' ');
            }
            this.stdout.println();
        }

        if (this.worker.getResult().isSuccess())
        {
            this.stdout.println("[ Compilation successful ]");
        }
    }

    /**
     * Tell about progress.
     *
     * @param val
     * @param msg
     * @see com.izforge.izpack.api.handler.AbstractUIProgressHandler#progress(int, String)
     */
    public void progress(int val, String msg)
    {
        double percentage = ((double) val) * 100.0d / (double) this.job_max;

        String percent = (new Integer((int) percentage)).toString() + '%';
        String line = this.job_name + ": " + percent;

        int line_len = line.length();

        this.stdout.print("\r" + line);
        for (int i = line_len; i < this.last_line_len; i++)
        {
            this.stdout.print(' ');
        }

        this.last_line_len = line_len;
    }

    /**
     * Reports progress to System.out
     *
     * @param jobName The next job's name.
     * @param max     unused
     * @param jobNo   The next job's number.
     * @see com.izforge.izpack.api.handler.AbstractUIProgressHandler#nextStep(String, int, int)
     */
    public void nextStep(String jobName, int max, int jobNo)
    {
        if ((this.job_name != null) && (this.last_line_len > 0))
        {
            String line = this.job_name + ": done.";
            this.stdout.print("\r" + line);
            for (int i = line.length(); i < this.last_line_len; i++)
            {
                this.stdout.print(' ');
            }
            this.stdout.println();
        }

        this.job_max = max;
        this.job_name = jobName;
        this.last_line_len = 0;
    }

    /**
     * {@inheritDoc}
     */
    public void setSubStepNo(int no_of_substeps)
    {
        this.job_max = no_of_substeps;
    }
}

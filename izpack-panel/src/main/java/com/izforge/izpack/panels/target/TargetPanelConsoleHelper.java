/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Jan Blok
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

package com.izforge.izpack.panels.target;

import java.io.PrintWriter;
import java.util.Properties;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.installer.console.PanelConsole;
import com.izforge.izpack.installer.console.PanelConsoleHelper;
import com.izforge.izpack.panels.path.PathInputPanel;
import com.izforge.izpack.util.Console;

/**
 * The Target panel console helper class.
 *
 * @author Mounir El Hajj
 */
public class TargetPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole
{

    /**
     * The resource manager.
     */
    private final ResourceManager resources;

    /**
     * Constructs a <tt>TargetPanelConsoleHelper</tt>.
     *
     * @param resources the resource manager
     */
    public TargetPanelConsoleHelper(ResourceManager resources)
    {
        this.resources = resources;
    }

    public boolean runGeneratePropertiesFile(AutomatedInstallData installData, PrintWriter printWriter)
    {
        printWriter.println(AutomatedInstallData.INSTALL_PATH + "=");
        return true;
    }

    public boolean runConsoleFromProperties(AutomatedInstallData installData, Properties properties)
    {
        String strTargetPath = properties.getProperty(AutomatedInstallData.INSTALL_PATH);
        if (strTargetPath == null || "".equals(strTargetPath.trim()))
        {
            System.err.println("Missing mandatory target path!");
            return false;
        }
        else
        {
            strTargetPath = installData.getVariables().replace(strTargetPath);
            installData.setInstallPath(strTargetPath);
            return true;
        }
    }

    /**
     * Runs the panel using the specified console.
     *
     * @param installData the installation data
     * @param console     the console
     * @return <tt>true</tt> if the panel ran successfully, otherwise <tt>false</tt>
     */
    @Override
    public boolean runConsole(AutomatedInstallData installData, Console console)
    {
        String strDefaultPath = PathInputPanel.loadDefaultInstallDir(resources, installData);

        String strTargetPath = console.prompt("Select target path [" + strDefaultPath + "] ", null);
        if (strTargetPath != null)
        {
            strTargetPath = strTargetPath.trim();
            if (strTargetPath.isEmpty())
            {
                strTargetPath = strDefaultPath;
            }
            strTargetPath = installData.getVariables().replace(strTargetPath);

            installData.setInstallPath(strTargetPath);
            return promptEndPanel(installData, console);
        }
        else
        {
            return false;
        }
    }
}

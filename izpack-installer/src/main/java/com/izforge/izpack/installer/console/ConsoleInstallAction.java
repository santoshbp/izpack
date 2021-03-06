package com.izforge.izpack.installer.console;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.util.Console;

/**
 * Performs interactive console installation.
 *
 * @author Tim Anderson
 */
class ConsoleInstallAction extends AbstractInstallAction
{

    /**
     * Constructs a <tt>ConsoleInstallAction</tt>.
     *
     * @param factory       the panel console factory
     * @param installData   the installation date
     * @param objectFactory the factory for {@link DataValidator} instances
     * @param rules         the rules engine
     * @param writer        the uninstallation data writer
     */
    public ConsoleInstallAction(PanelConsoleFactory factory, AutomatedInstallData installData,
                                ObjectFactory objectFactory, RulesEngine rules, UninstallDataWriter writer)
    {
        super(factory, installData, objectFactory, rules, writer);
    }

    /**
     * Runs the action for the console panel associated with the specified panel.
     *
     * @param panel        the panel
     * @param panelConsole the console implementation of the panel
     * @param console      the console
     * @return <tt>true</tt> if the action was successful, otherwise <tt>false</tt>
     * @throws InstallerException for any installer error
     */
    @Override
    protected boolean run(Panel panel, PanelConsole panelConsole, Console console) throws InstallerException
    {
        boolean result;
        do
        {
            result = panelConsole.runConsole(getInstallData(), console);
            if (!result)
            {
                break;
            }
        }
        while (!validatePanel(panel, console));
        return result;
    }
}

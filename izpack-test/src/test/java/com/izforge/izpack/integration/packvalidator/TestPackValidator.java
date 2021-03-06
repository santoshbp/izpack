package com.izforge.izpack.integration.packvalidator;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.panels.treepacks.PackValidator;


/**
 * Test {@link PackValidator} implementation.
 *
 * @author Tim Anderson
 */
public class TestPackValidator implements PackValidator
{

    /**
     * Marks a pack valid/invalid.
     *
     * @param pack        the pack name
     * @param valid       determines if the pack is valid or not
     * @param installData the installation data
     */
    public static void setValid(String pack, boolean valid, AutomatedInstallData installData)
    {
        installData.setVariable(pack + ".valid", Boolean.toString(valid));
    }

    /**
     * Validates the selected pack.
     * <p/>
     * This returns the value of a <em>&lt;pack&gt;</em>.valid variable.
     *
     * @param handler     the handler
     * @param installData the installation data
     * @param packId      the pack identifier
     * @param isSelected  determines if the pack is selected
     * @return <tt>true</tt> if the pack is valid, otherwise <tt>false</tt>
     */
    @Override
    public boolean validate(AbstractUIHandler handler, GUIInstallData installData, String packId, boolean isSelected)
    {
        String name = packId + ".valid";
        String value = installData.getVariable(name);
        return Boolean.valueOf(value);
    }
}

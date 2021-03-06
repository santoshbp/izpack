package com.izforge.izpack.installer.requirement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.filters.StringInputStream;
import org.junit.Test;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallerRequirement;
import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.rules.RulesEngineImpl;
import com.izforge.izpack.core.rules.logic.NotCondition;
import com.izforge.izpack.core.rules.process.JavaCondition;
import com.izforge.izpack.installer.console.ConsolePrompt;
import com.izforge.izpack.installer.data.InstallData;
import com.izforge.izpack.test.util.TestConsole;

/**
 * Tests the {@link InstallerRequirementChecker} class.
 *
 * @author Tim Anderson
 */
public class InstallerRequirementCheckerTest
{
    /**
     * The rules.
     */
    private RulesEngine rules;

    /**
     * The installation data.
     */
    private AutomatedInstallData installData;

    /**
     * Constructs a <tt>InstallerRequirementCheckerTest</tt>.
     */
    public InstallerRequirementCheckerTest()
    {
        DefaultVariables variables = new DefaultVariables();
        installData = new InstallData(variables);
        installData.setInstallerrequirements(new ArrayList<InstallerRequirement>());
        installData.setLangpack(new LocaleDatabase(new StringInputStream("<langpack/>")));
        rules = new RulesEngineImpl(installData, null);
        variables.setRules(rules);

        Map<String, Condition> conditionsmap = new HashMap<String, Condition>();
        Condition alwaysFalse = new JavaCondition();
        conditionsmap.put("false", alwaysFalse);

        Condition alwaysTrue = NotCondition.createFromCondition(alwaysFalse, rules);
        conditionsmap.put("true", alwaysTrue);

        rules.readConditionMap(conditionsmap);
    }

    /**
     * Tests the {@link InstallerRequirementChecker}.
     */
    @Test
    public void testInstallerRequirementChecker()
    {
        Prompt prompt = new ConsolePrompt(new TestConsole());
        InstallerRequirementChecker checker = new InstallerRequirementChecker(installData, rules, prompt);

        // no requirements - should evaluate true
        assertTrue(checker.check());

        // add a requirement that always evaluates false
        InstallerRequirement req1 = new InstallerRequirement();
        req1.setCondition("false");
        req1.setMessage("requirement1 = always false");
        installData.getInstallerrequirements().add(req1);

        // should evaluate false
        assertFalse(checker.check());

        // add a requirement that always evaluates true
        InstallerRequirement req2 = new InstallerRequirement();
        req2.setCondition("true");
        req2.setMessage("requirement2 = always true");
        installData.getInstallerrequirements().add(req2);

        // should still evaluate false, due to presence of req1
        assertFalse(checker.check());

        // remove req1 and verify evaluates true
        installData.getInstallerrequirements().remove(req1);
        assertTrue(checker.check());
    }
}

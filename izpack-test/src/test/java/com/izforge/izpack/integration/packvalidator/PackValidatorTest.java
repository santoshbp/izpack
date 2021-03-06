package com.izforge.izpack.integration.packvalidator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.compiler.container.TestInstallationContainer;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerController;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.integration.HelperTestMethod;
import com.izforge.izpack.panels.hello.HelloPanel;
import com.izforge.izpack.panels.install.InstallPanel;
import com.izforge.izpack.panels.packs.PacksPanel;
import com.izforge.izpack.panels.simplefinish.SimpleFinishPanel;
import com.izforge.izpack.panels.treepacks.PackValidator;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.test.util.TestHousekeeper;


/**
 * Tests that {@link PackValidator}s are invoked during installation.
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@Container(TestInstallationContainer.class)
public class PackValidatorTest
{
    /**
     * Temporary folder to perform installations to.
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * Install data.
     */
    private final GUIInstallData installData;

    /**
     * The installer frame.
     */
    private final InstallerFrame frame;

    /**
     * The installer controller.
     */
    private final InstallerController controller;

    /**
     * The house-keeper.
     */
    private final TestHousekeeper housekeeper;

    /**
     * The frame fixture.
     */
    private FrameFixture frameFixture;

    /**
     * Constructs an <tt>PanelActionValidatorTest</tt>.
     *
     * @param installData the install data
     * @param frame       the installer frame
     * @param controller  the installer controller
     * @param housekeeper the house-keeper
     */
    public PackValidatorTest(GUIInstallData installData, InstallerFrame frame, InstallerController controller,
                             TestHousekeeper housekeeper)
    {
        this.installData = installData;
        this.frame = frame;
        this.controller = controller;
        this.housekeeper = housekeeper;
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp()
    {
        // write to temporary folder so the test doesn't need to be run with elevated permissions
        File installPath = new File(temporaryFolder.getRoot(), "izpackTest");
        assertTrue(installPath.mkdirs());
        installData.setInstallPath(installPath.getAbsolutePath());
    }

    /**
     * Tears down the test case.
     */
    @After
    public void tearDown()
    {
        if (frameFixture != null)
        {
            frameFixture.cleanUp();
        }
    }

    /**
     * Verifies that registered {@link PackValidator}s are invoked by {@link PacksPanel}.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/packvalidators.xml")
    public void testPackValidator() throws Exception
    {
        assertEquals(4, installData.getPanelsOrder().size());

        frameFixture = HelperTestMethod.prepareFrameFixture(frame, controller);

        // HelloPanel
        Thread.sleep(2000);
        checkCurrentPanel(HelloPanel.class);
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // PacksPanel
        Thread.sleep(1000);
        checkCurrentPanel(PacksPanel.class);

        //set the Base pack as invalid, and verify clicking next has no effect
        TestPackValidator.setValid("Base", false, installData);
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();
        Thread.sleep(1000);
        checkCurrentPanel(PacksPanel.class);

        // now set it valid. Should be able to go to next panel
        TestPackValidator.setValid("Base", true, installData);
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // InstallPanel
        Thread.sleep(1000);
        checkCurrentPanel(InstallPanel.class);
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // SimpleFinishPanel
        Thread.sleep(1000);
        checkCurrentPanel(SimpleFinishPanel.class);
        frameFixture.button(GuiId.BUTTON_QUIT.id).click();

        // verify the installer has terminated
        housekeeper.waitShutdown(2 * 60 * 1000);
        assertTrue(housekeeper.hasShutdown());
        assertEquals(0, housekeeper.getExitCode());
        assertFalse(housekeeper.getReboot());
    }

    /**
     * Verifies that the current panel is an instance of the specified type.
     *
     * @param type the expected panel type
     */
    private void checkCurrentPanel(Class<? extends IzPanel> type)
    {
        Panel panel = installData.getPanelsOrder().get(installData.getCurPanelNumber());
        assertEquals(type.getName(), panel.getClassName());
    }

}


package com.sun.identity.config.upgrade;

import com.iplanet.sso.SSOToken;
import com.sun.identity.config.util.AjaxPage;
import com.sun.identity.security.AdminTokenAction;
import com.sun.identity.shared.debug.Debug;
import java.security.AccessController;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponseWrapper;
import net.sf.click.control.ActionLink;
import org.forgerock.openam.upgrade.ServiceUpgradeWrapper;
import org.forgerock.openam.upgrade.UpgradeException;
import org.forgerock.openam.upgrade.UpgradeServices;
import org.forgerock.openam.upgrade.UpgradeUtils;

/**
 *
 * @author Peter Major
 */
public class Upgrade extends AjaxPage {

    private UpgradeServices upgrade;
    private ServiceUpgradeWrapper wrapper;
    private SSOToken adminToken;
    private Debug debug = UpgradeUtils.debug;
    public ActionLink doUpgradeLink = new ActionLink("doUpgrade", this, "doUpgrade");
    public ActionLink saveReportLink = new ActionLink("saveReport", this, "saveReport");

    public Upgrade() {
        try {
            adminToken = (SSOToken) AccessController.doPrivileged(
                    AdminTokenAction.getInstance());
            upgrade = new UpgradeServices();
            wrapper = upgrade.preUpgrade(adminToken);
            addModel("currentVersion", UpgradeUtils.getCurrentVersion());
            addModel("newVersion", UpgradeUtils.getWarFileVersion());
            addModel("changelist", upgrade.generateShortUpgradeReport(adminToken, wrapper, true));
        } catch (UpgradeException ue) {
            debug.error("An error occured, while initializing Upgrade page", ue);
        }
    }

    public boolean doUpgrade() {
        HttpServletRequestWrapper request =
                new HttpServletRequestWrapper(getContext().getRequest());
        HttpServletResponseWrapper response =
                new HttpServletResponseWrapper(getContext().getResponse());

        try {
            upgrade.upgrade(adminToken, wrapper);
            writeToResponse("true");
            setPath(null);
        } catch (UpgradeException ue) {
            writeToResponse(ue.getMessage());
            setPath(null);
            debug.error("Error occured while upgrading OpenAM", ue);
        }
        return false;
    }

    public boolean saveReport() {
        try {
            String report = upgrade.
                    generateDetailedUpgradeReport(adminToken, wrapper, false);
            writeToResponse(report);
            getContext().getResponse().setContentType("application/force-download; charset=\"UTF-8\"");
            getContext().getResponse().setHeader(
                    "Content-Disposition", "attachment; filename=\"upgradereport." + System.currentTimeMillis() + "\"");
            getContext().getResponse().setHeader("Content-Description", "File Transfer");
            
            setPath(null);
        } catch (UpgradeException ue) {
            debug.error("Error occured while generating detailed report", ue);
        }
        return false;
    }
}

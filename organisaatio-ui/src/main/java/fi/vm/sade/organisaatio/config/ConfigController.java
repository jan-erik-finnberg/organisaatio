package fi.vm.sade.organisaatio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author simok
 */
@Controller
public class ConfigController {
    @Value("${organisaatio-ui.organisaatio-ui-url}")
    private String organisaatioUiURL;

    @Value("${organisaatio-ui.organisaatio-service-url.rest}")
    private String organisaatioServiceRestURL;

    @Value("${organisaatio-ui.koodisto-service-url.rest}")
    private String koodistoServiceRestURL;

    @Value("${organisaatio-ui.lokalisaatio-service-url.rest}")
    private String lokalisaatioServiceRestURL;

    @Value("${organisaatio-ui.authentication-service-url.rest}")
    private String authenticationServiceRestURL;

    @Value("${root.organisaatio.oid}")
    private String rootOrganisaatioOid;

    @Value("${auth.mode:}")
    private String authMode;

    @Value("${organisaatio-ui.cas.url:/cas/myroles}")
    private String casUrl;

    @Value("${organisaatio-ui.cas-me.url:/cas/me}")
    private String casMeUrl;

    @Value("${organisaatio-ui.session-keepalive-interval.seconds:30}")
    private Integer sessionKeepaliveIntervalInSeconds;

    @Value("${organisaatio-ui.max-session-idle-time.seconds:1800}")
    private Integer maxSessionIdleTimeInSeconds;

    @RequestMapping(value = "/configuration.js", method = RequestMethod.GET, produces = "text/javascript")
    @ResponseBody
    public String index() {
        StringBuilder b = new StringBuilder();
        append(b, "UI_URL_BASE", organisaatioUiURL);
        append(b, "SERVICE_URL_BASE", organisaatioServiceRestURL);
        append(b, "KOODISTO_URL_BASE", koodistoServiceRestURL);
        append(b, "LOKALISAATIO_URL_BASE", lokalisaatioServiceRestURL);
        append(b, "AUTHENTICATION_URL_BASE", authenticationServiceRestURL);
        append(b, "ROOT_ORGANISAATIO_OID", rootOrganisaatioOid);

        append(b, "TEMPLATE_URL_BASE", "");

        append(b, "CAS_URL", casUrl);
        append(b, "CAS_ME_URL", casMeUrl);

        append(b, "SESSION_KEEPALIVE_INTERVAL_IN_SECONDS", Integer.toString(sessionKeepaliveIntervalInSeconds));
        append(b, "MAX_SESSION_IDLE_TIME_IN_SECONDS", Integer.toString(maxSessionIdleTimeInSeconds));

        if (!authMode.isEmpty()) {
            append(b, "AUTH_MODE", authMode);
            if (authMode.trim().equalsIgnoreCase("dev")) {
                b.append("$('head').append('<script type=\"text/javascript\" src=\"/servers/cas/myroles\"></script>')");
            }
        }

        return b.toString();
    }

    private void append(StringBuilder b, String key, String value) {
        b.append(key);
        b.append(" = \"");
        b.append(value);
        b.append("\";\n");
    }

}

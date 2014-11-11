package org.dbg4j.example.webapp.spring.debugintegration;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.dbg4j.core.context.DebugAllowanceStrategy;
import org.dbg4j.core.context.DebugContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Very simple allowance strategy. When url contains "debug=true" attribute, it allows debugging.
 */
public class MyWebAppAllowanceStrategy implements DebugAllowanceStrategy {

    private static final Logger log = LoggerFactory.getLogger(MyWebAppAllowanceStrategy.class);

    public static final String DEBUG_URL_ATTRIBUTE = "debug";
    public static final String DEBUG_URL_VALUE = "true";

    private boolean disabled;

    @Override
    public boolean isAllowed(@Nullable DebugContext context, @Nullable Object... params) {
        if(context != null) {
            return true;
        }
        try {
            if(!disabled) {
                HttpServletRequest httpRequest;
                httpRequest = (HttpServletRequest)params[0];

                if(StringUtils.isNotBlank(httpRequest.getQueryString())
                        && httpRequest.getQueryString().contains(DEBUG_URL_ATTRIBUTE + "=" + DEBUG_URL_VALUE)){
                    return true;
                }
            }
        } catch (Exception e) {
            log.debug("isAllowed: error", e);
        }
        return false;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
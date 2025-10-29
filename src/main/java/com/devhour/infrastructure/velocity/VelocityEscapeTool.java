package com.devhour.infrastructure.velocity;

import io.micrometer.core.instrument.util.StringEscapeUtils;

public class VelocityEscapeTool extends org.apache.velocity.tools.generic.EscapeTool {
    public String json(String str) {
        return StringEscapeUtils.escapeJson(str);
    }
}

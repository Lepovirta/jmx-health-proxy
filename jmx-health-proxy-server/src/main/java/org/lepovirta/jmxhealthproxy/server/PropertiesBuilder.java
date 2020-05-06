package org.lepovirta.jmxhealthproxy.server;

import java.util.Properties;

public final class PropertiesBuilder {
    public static Properties fromString(String paramsStr) {
        final String[] paramsArr = paramsStr.split("\\s+");
        return fromStrings(paramsArr);
    }

    public static Properties fromStrings(String[] paramsArr) {
        final Properties paramsMap = new Properties();
        for (String paramStr : paramsArr) {
            final String[] paramKV = paramStr.split("=", 2);
            if (paramKV.length == 2) {
                final String key = paramKV[0].trim();
                final String value = paramKV[1].trim();
                if (!key.isEmpty()) {
                    paramsMap.setProperty(key, value);
                }
            }
        }
        return paramsMap;
    }
}

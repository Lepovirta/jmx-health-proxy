package org.lepovirta.jmxhealthproxy;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.Properties;

public final class JmxConnectionBuilder {
    public static final String PROPERTY_JMX_URL = "jmxUrl";
    public static final String PROPERTY_JMX_HOSTNAME = "jmxHostName";
    public static final String PROPERTY_JMX_PORT = "jmxPort";

    private String jmxUrl = "";
    private String jmxHostName = "";
    private int jmxPort = -1;
    private Map<String, Object> env;

    public String getJmxUrl() {
        return jmxUrl;
    }

    public void setJmxUrl(String jmxUrl) {
        this.jmxUrl = jmxUrl;
    }

    public String getJmxHostName() {
        return jmxHostName;
    }

    public void setJmxHostName(String jmxHostName) {
        this.jmxHostName = jmxHostName;
    }

    public int getJmxPort() {
        return jmxPort;
    }

    public void setJmxPort(int jmxPort) {
        this.jmxPort = jmxPort;
    }

    public Map<String, Object> getEnv() {
        return env;
    }

    public void setEnv(Map<String, Object> env) {
        this.env = env;
    }

    private boolean isJmxUrlSpecified() {
        return this.jmxUrl != null && !this.jmxUrl.isEmpty();
    }

    private boolean isJmxHostAndPortSpecified() {
        return this.jmxHostName != null && !this.jmxHostName.isEmpty() && this.jmxPort > 0;
    }

    private String buildUrlString() {
        if (this.isJmxUrlSpecified()) {
            return this.jmxUrl;
        }
        if (this.isJmxHostAndPortSpecified()) {
            return "service:jmx:rmi:///jndi/rmi://" + this.jmxHostName + ":" + this.jmxPort + "/jmxrmi";
        }
        return null;
    }

    public MBeanServerConnection build() throws IOException {
        final String urlString = this.buildUrlString();
        if (urlString == null) {
            return ManagementFactory.getPlatformMBeanServer();
        }
        final JMXServiceURL url = new JMXServiceURL(urlString);
        return JMXConnectorFactory.connect(url, this.getEnv()).getMBeanServerConnection();
    }

    public static JmxConnectionBuilder fromProperties(Properties properties) {
        final JmxConnectionBuilder builder = new JmxConnectionBuilder();
        builder.setJmxUrl(properties.getProperty(PROPERTY_JMX_URL));
        builder.setJmxHostName(properties.getProperty(PROPERTY_JMX_HOSTNAME));
        builder.setJmxPort(Integer.parseInt(properties.getProperty(PROPERTY_JMX_PORT, "-1")));
        return builder;
    }
}

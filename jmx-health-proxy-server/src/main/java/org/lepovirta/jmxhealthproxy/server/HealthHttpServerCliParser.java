package org.lepovirta.jmxhealthproxy.server;

import org.lepovirta.jmxhealthproxy.HealthTarget;
import org.lepovirta.jmxhealthproxy.JmxConnectionBuilder;

import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class HealthHttpServerCliParser {

    public static final String PARAMETER_SEPARATOR = "--";

    private Properties serverProperties;
    private Stream<Properties> healthProperties;

    public static HealthHttpServerCliParser parse(String[] args) {
        final HealthHttpServerCliParser cliParser = new HealthHttpServerCliParser();
        cliParser.parseArgs(args);
        return cliParser;
    }

    private void parseArgs(String[] args) {
        final int separatorIndex = indexOfSeparator(args);
        if (separatorIndex < 0) {
            this.serverProperties = PropertiesBuilder.fromStrings(args);
            this.healthProperties = Stream.empty();
            return;
        }

        this.serverProperties = PropertiesBuilder.fromStrings(
                Arrays.stream(args).limit(separatorIndex).toArray(String[]::new)
        );
        this.healthProperties = Arrays.stream(args)
                .skip(separatorIndex + 1)
                .map(PropertiesBuilder::fromString);
    }

    public InetSocketAddress getHttpServerAddress() {
        final String host = this.serverProperties.getProperty(
                HealthHttpServer.PROPERTY_HTTP_SERVER_HOST,
                "0.0.0.0"
        );
        final int port = Integer.parseInt(this.serverProperties.getProperty(
                HealthHttpServer.PROPERTY_HTTP_SERVER_PORT,
                "9999")
        );
        return new InetSocketAddress(host, port);
    }

    public MBeanServerConnection getMBeanServerConnection() throws IOException {
        return JmxConnectionBuilder.fromProperties(this.serverProperties).build();
    }

    public String getHttpPath() {
        return this.serverProperties.getProperty(HealthHttpServer.PROPERTY_HTTP_SERVER_PATH, "");
    }

    public Map<String, HealthTarget> getHealthTargets() {
        return this.healthProperties
                .flatMap(HealthHttpServerCliParser::propsToHealthTarget)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Stream<Map.Entry<String, HealthTarget>> propsToHealthTarget(Properties p) {
        final String key = p.getProperty(HealthHttpServer.PROPERTY_TARGET_NAME, "");
        final HealthTarget target = HealthTarget.fromProperties(p);
        if (key.isEmpty()) {
            return Stream.empty();
        }
        return Stream.of(new AbstractMap.SimpleEntry<>(key, target));
    }

    private static int indexOfSeparator(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(PARAMETER_SEPARATOR)) {
                return i;
            }
        }
        return -1;
    }
}

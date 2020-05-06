package org.lepovirta.jmxhealthproxy.server;

import com.sun.net.httpserver.HttpServer;
import org.lepovirta.jmxhealthproxy.HealthTarget;
import org.lepovirta.jmxhealthproxy.Response;

import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class HealthHttpServer {
    public static final String PROPERTY_HTTP_SERVER_HOST = "httpServerHost";
    public static final String PROPERTY_HTTP_SERVER_PORT = "httpServerPort";
    public static final String PROPERTY_HTTP_SERVER_PATH = "httpServerPath";
    public static final String PROPERTY_TARGET_NAME = "name";

    private final MBeanServerConnection conn;
    private final InetSocketAddress serverAddress;
    private final String httpPath;
    private final Map<String, HealthTarget> targets;
    private HttpServer server;

    HealthHttpServer(
            MBeanServerConnection conn,
            InetSocketAddress httpServerAddress,
            String httpPath,
            Map<String, HealthTarget> targets
    ) {
        this.conn = conn;
        this.serverAddress = httpServerAddress;
        this.httpPath = httpPath;
        this.targets = targets;
    }

    public void start() {
        try {
            this.server = HttpServer.create(this.serverAddress, 0);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to set up a health endpoint HTTP server", ex);
        }

        for (Map.Entry<String, HealthTarget> target : this.targets.entrySet()) {
            String path = this.targetPath(target.getKey());
            this.server.createContext(path, exchange -> {
                try {
                    final Response response = target.getValue().isHealthy(this.conn);
                    exchange.sendResponseHeaders(response.getHttpCode(), -1);
                } catch (Exception ex) {
                    // TODO: log
                    exchange.sendResponseHeaders(500, -1);
                }
                exchange.close();
            });
        }

        this.server.start();
    }

    private String targetPath(final String targetName) {
        return Stream.<String>builder()
                .add(this.httpPath)
                .add(targetName)
                .build()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("/", "/", ""));
    }

    public void stop() {
        if (this.server != null) {
            this.server.stop(0);
        }
    }

    public static void main(String[] args) throws Exception {
        final HealthHttpServerCliParser cliParser = HealthHttpServerCliParser.parse(args);
        final HealthHttpServer server = new HealthHttpServer(
                cliParser.getMBeanServerConnection(),
                cliParser.getHttpServerAddress(),
                cliParser.getHttpPath(),
                cliParser.getHealthTargets()
        );
        final CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread("shutdown-hook") {
            @Override
            public void run() {
                server.stop();
                latch.countDown();
            }
        });

        try {
            server.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }
}

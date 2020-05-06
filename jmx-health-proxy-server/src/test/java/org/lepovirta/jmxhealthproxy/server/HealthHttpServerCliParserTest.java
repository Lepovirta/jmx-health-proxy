package org.lepovirta.jmxhealthproxy.server;

import org.junit.Assert;
import org.junit.Test;
import org.lepovirta.jmxhealthproxy.HealthTarget;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class HealthHttpServerCliParserTest {

    @Test
    public void testParser() {
        String[] args = new String[]{
                "httpServerHost=localhost",
                "httpServerPort=8888",
                "httpServerPath=health",
                "--",
                "name=live objectName=demo.health:type=live attributeName=Status expectedExp=~OK.* matchCount=any",
                "name=ready objectName=demo.health:type=ready attributeName=State expectedExp=OK",
        };
        Map<String, HealthTarget> expectedTargets = new HashMap<>();
        expectedTargets.put("live", new HealthTarget(
                "demo.health:type=live",
                "Status",
                "~OK.*",
                HealthTarget.MatchCount.ANY
        ));
        expectedTargets.put("ready", new HealthTarget(
                "demo.health:type=ready",
                "State",
                "OK",
                HealthTarget.MatchCount.ALL
        ));

        final HealthHttpServerCliParser cliParser = HealthHttpServerCliParser.parse(args);

        Assert.assertEquals(new InetSocketAddress("localhost", 8888), cliParser.getHttpServerAddress());
        Assert.assertEquals("health", cliParser.getHttpPath());
        Assert.assertEquals(expectedTargets, cliParser.getHealthTargets());
    }
}

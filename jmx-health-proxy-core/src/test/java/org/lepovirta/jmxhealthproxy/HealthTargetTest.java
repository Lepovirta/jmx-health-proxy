package org.lepovirta.jmxhealthproxy;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import org.junit.Assert;

public class HealthTargetTest {

    private static MBeanServerConnection mBeanConn;
    private static DemoAttributes demoBean1;
    private static DemoAttributes demoBean2;

    @BeforeClass
    public static void setupClass() throws Exception {
        demoBean1 = new DemoAttributes("first", 1);
        demoBean2 = new DemoAttributes("second", 2);
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        mBeanConn = server;
        server.registerMBean(demoBean1, new ObjectName("demo1:type=health"));
        server.registerMBean(demoBean2, new ObjectName("demo2:type=health"));
    }

    @Test
    public void testUnknownObjectName() throws Exception {
        final HealthTarget unknownTarget = new HealthTarget(
                "does-not-exist:type=health",
                "Attribute1",
                "first",
                HealthTarget.MatchCount.ALL
        );

        Assert.assertEquals(Response.FAIL, unknownTarget.isHealthy(mBeanConn));
    }

    @Test
    public void testUnknownAttributeName() throws Exception {
        final HealthTarget unknownTarget = new HealthTarget(
                "does-not-exist:type=health",
                "DoesNotExist",
                "first",
                HealthTarget.MatchCount.ALL
        );

        Assert.assertEquals(Response.FAIL, unknownTarget.isHealthy(mBeanConn));
    }

    @Test
    public void testExactMatch() throws Exception {
        final HealthTarget healthyTarget = new HealthTarget(
                "demo1:type=health",
                "Attribute2",
                String.valueOf(demoBean1.getAttribute2()),
                HealthTarget.MatchCount.ALL
        );
        final HealthTarget unhealthyTarget = new HealthTarget(
                "demo1:type=health",
                "Attribute2",
                "2",
                HealthTarget.MatchCount.ALL
        );

        Assert.assertEquals(Response.OK, healthyTarget.isHealthy(mBeanConn));
        Assert.assertEquals(Response.FAIL, unhealthyTarget.isHealthy(mBeanConn));
    }

    @Test
    public void testRegexMatch() throws Exception {
        final HealthTarget healthyTarget = new HealthTarget(
                "demo1:type=health",
                "Attribute1",
                "~fi.*",
                HealthTarget.MatchCount.ALL
        );
        final HealthTarget unhealthyTarget = new HealthTarget(
                "demo1:type=health",
                "Attribute1",
                "~x.*",
                HealthTarget.MatchCount.ALL
        );

        Assert.assertEquals(Response.OK, healthyTarget.isHealthy(mBeanConn));
        Assert.assertEquals(Response.FAIL, unhealthyTarget.isHealthy(mBeanConn));
    }

    @Test
    public void testMatchAll() throws Exception {
        final HealthTarget healthyTarget = new HealthTarget(
                "demo*:type=health",
                "Attribute3",
                demoBean1.getAttribute3(),
                HealthTarget.MatchCount.ALL
        );
        final HealthTarget unhealthyTarget = new HealthTarget(
                "demo*:type=health",
                "Attribute1",
                demoBean1.getAttribute1(),
                HealthTarget.MatchCount.ALL
        );
        Assert.assertEquals(Response.OK, healthyTarget.isHealthy(mBeanConn));
        Assert.assertEquals(Response.FAIL, unhealthyTarget.isHealthy(mBeanConn));
    }

    @Test
    public void testMatchAny() throws Exception {
        final HealthTarget healthyTarget = new HealthTarget(
                "demo*:type=health",
                "Attribute1",
                demoBean1.getAttribute1(),
                HealthTarget.MatchCount.ANY
        );
        final HealthTarget unhealthyTarget = new HealthTarget(
                "demo*:type=health",
                "Attribute1",
                "does-not-exist",
                HealthTarget.MatchCount.ANY
        );
        Assert.assertEquals(Response.OK, healthyTarget.isHealthy(mBeanConn));
        Assert.assertEquals(Response.FAIL, unhealthyTarget.isHealthy(mBeanConn));
    }
}

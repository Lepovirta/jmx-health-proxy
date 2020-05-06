package org.lepovirta.jmxhealthproxy.server;

import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class PropertiesBuilderTest {

    @Test
    public void testEmptyParameters() {
        final Properties result = PropertiesBuilder.fromString("");
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testSingleParameter() {
        final Properties expected = new Properties();
        expected.setProperty("foobar", "1");

        final Properties result = PropertiesBuilder.fromString("foobar=1");

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testManyParameters() {
        final Properties expected = new Properties();
        expected.setProperty("foobar", "1");
        expected.setProperty("hello", "world");

        final Properties result = PropertiesBuilder.fromString("hello=world foobar=1");

        Assert.assertEquals(expected, result);
    }
}

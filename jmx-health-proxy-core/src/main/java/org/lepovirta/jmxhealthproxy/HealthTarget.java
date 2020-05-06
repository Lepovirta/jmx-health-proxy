package org.lepovirta.jmxhealthproxy;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

public final class HealthTarget {
    public static final String PROPERTY_OBJECT_NAME = "objectName";
    public static final String PROPERTY_ATTRIBUTE_NAME = "attributeName";
    public static final String PROPERTY_EXPECTED_EXP = "expectedExp";
    public static final String PROPERTY_MATCH_COUNT = "matchCount";
    public final String objectName;
    public final String attributeName;
    public final ExpectationEval expectedExp;
    public final MatchCount matchCount;

    public enum MatchCount {
        ALL,
        ANY
    }

    public HealthTarget(
            String objectName,
            String attributeName,
            String expectedExp,
            MatchCount matchCount
    ) {
        this.objectName = objectName;
        this.attributeName = attributeName;
        this.expectedExp = new ExpectationEval(expectedExp);
        this.matchCount = matchCount;
    }

    public static HealthTarget fromProperties(Properties properties) {
        return new HealthTarget(
                properties.getProperty(PROPERTY_OBJECT_NAME),
                properties.getProperty(PROPERTY_ATTRIBUTE_NAME),
                properties.getProperty(PROPERTY_EXPECTED_EXP),
                MatchCount.valueOf(properties.getProperty(PROPERTY_MATCH_COUNT, MatchCount.ALL.name()).toUpperCase())
        );
    }

    public Response isHealthy(final MBeanServerConnection conn) throws Exception {
        final Set<ObjectName> objectNames = conn.queryNames(new ObjectName(this.objectName), null);

        if (objectNames.isEmpty()) {
            return Response.FAIL;
        }

        if (this.matchCount.equals(MatchCount.ALL)) {
            for (ObjectName objectName : objectNames) {
                if (!this.checkAttribute(conn, objectName)) {
                    return Response.FAIL;
                }
            }
            return Response.OK;
        }

        for (ObjectName objectName : objectNames) {
            if (this.checkAttribute(conn, objectName)) {
                return Response.OK;
            }
        }
        return Response.FAIL;
    }

    private boolean checkAttribute(final MBeanServerConnection conn, final ObjectName objectName) throws Exception {
        try {
            final String attribute = conn.getAttribute(objectName, this.attributeName).toString();
            return this.expectedExp.test(attribute);
        } catch (AttributeNotFoundException|InstanceNotFoundException ex) {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HealthTarget that = (HealthTarget) o;
        return Objects.equals(objectName, that.objectName) &&
                Objects.equals(attributeName, that.attributeName) &&
                Objects.equals(expectedExp, that.expectedExp) &&
                matchCount == that.matchCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectName, attributeName, expectedExp, matchCount);
    }

    @Override
    public String toString() {
        return "HealthTarget{" +
                "objectName='" + objectName + '\'' +
                ", attributeName='" + attributeName + '\'' +
                ", expectedValue='" + expectedExp + '\'' +
                ", matchCount=" + matchCount +
                '}';
    }

    public static void main(String[] args) throws Exception {
        final Properties properties = System.getProperties();
        final MBeanServerConnection conn = JmxConnectionBuilder.fromProperties(properties).build();
        final HealthTarget healthTarget = fromProperties(properties);
        final Response response = healthTarget.isHealthy(conn);
        System.out.println(response.toString());
        System.exit(response.getExitCode());
    }
}

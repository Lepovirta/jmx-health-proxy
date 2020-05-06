package org.lepovirta.jmxhealthproxy;

public interface DemoAttributesMBean {
    String getAttribute1();
    int getAttribute2();
    String getAttribute3();
}

class DemoAttributes implements DemoAttributesMBean {
    private String attribute1;
    private int attribute2;

    public DemoAttributes(String attribute1, int attribute2) {
        this.attribute1 = attribute1;
        this.attribute2 = attribute2;
    }

    public String getAttribute1() {
        return attribute1;
    }

    public int getAttribute2() {
        return attribute2;
    }

    public String getAttribute3() {
        return "same";
    }
}

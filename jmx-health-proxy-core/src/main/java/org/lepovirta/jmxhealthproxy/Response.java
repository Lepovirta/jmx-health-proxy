package org.lepovirta.jmxhealthproxy;

public enum Response {
    OK(0, 204),
    FAIL(1, 503);

    private final int exitCode;
    private final int httpCode;

    Response(int exitCode, int httpCode) {
        this.exitCode = exitCode;
        this.httpCode = httpCode;
    }

    public int getExitCode() {
        return exitCode;
    }

    public int getHttpCode() {
        return httpCode;
    }
}

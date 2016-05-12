package org.ck.maven.plugins.pom.versions.common;

public class NotLockVersionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NotLockVersionException(String message) {
        super(message);
    }

}

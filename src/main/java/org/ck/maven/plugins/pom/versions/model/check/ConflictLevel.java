package org.ck.maven.plugins.pom.versions.model.check;

public enum ConflictLevel {
    ok(0), warn(1), error(2), fatal(3);

    private int code;

    private ConflictLevel(int code) {
        this.code = code;
    }

    public int getValue() {
        return this.code;
    }
}

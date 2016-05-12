package org.ck.maven.plugins.pom.versions.model.check;

public class ProcessConflictResult {
    private ConflictLevel level;
    private int warnLevelCount;
    private int errorLevelCount;
    private int fatalLevelCount;

    public ConflictLevel getLevel() {
        return level;
    }

    public void setLevel(ConflictLevel level) {
        this.level = level;
    }

    public int getWarnLevelCount() {
        return warnLevelCount;
    }

    public void setWarnLevelCount(int warnLevelCount) {
        this.warnLevelCount = warnLevelCount;
    }

    public int getErrorLevelCount() {
        return errorLevelCount;
    }

    public void setErrorLevelCount(int errorLevelCount) {
        this.errorLevelCount = errorLevelCount;
    }

    public int getFatalLevelCount() {
        return fatalLevelCount;
    }

    public void setFatalLevelCount(int fatalLevelCount) {
        this.fatalLevelCount = fatalLevelCount;
    }

}

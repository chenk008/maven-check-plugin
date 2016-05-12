package org.ck.maven.plugins.pom.versions.model.check;

import java.util.List;

/**
 * 扫描结果
 *
 */
public class CheckConflictResult {
    /**
     * 警告级别的冲突，这种冲突需要仔细甄别，并不一定会影响应用，也并不一定不影响应用
     */
    private List<Conflict> warnConflictList;
    /**
     * 错误级别的冲突，这种冲突不影响应用稳定和安全，因为冲突的jars MD5一致，是同一个包不能的名称而已，只是打进war包会比较多余
     */
    private List<Conflict> errorConflictList;
    /**
     * 这种冲突很有可能会影响系统稳定性，强烈建议排除
     */
    private List<Conflict> fatalConflictList;

    public List<Conflict> getWarnConflictList() {
        return warnConflictList;
    }

    public void setWarnConflictList(List<Conflict> warnConflictList) {
        this.warnConflictList = warnConflictList;
    }

    public List<Conflict> getErrorConflictList() {
        return errorConflictList;
    }

    public void setErrorConflictList(List<Conflict> errorConflictList) {
        this.errorConflictList = errorConflictList;
    }

    public List<Conflict> getFatalConflictList() {
        return fatalConflictList;
    }

    public void setFatalConflictList(List<Conflict> fatalConflictList) {
        this.fatalConflictList = fatalConflictList;
    }

    

}

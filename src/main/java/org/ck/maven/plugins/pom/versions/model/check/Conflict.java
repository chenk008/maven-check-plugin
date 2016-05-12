package org.ck.maven.plugins.pom.versions.model.check;

import java.util.List;

/**
 *
 *
 */
public class Conflict {
    /**
     * 冲突的Jar
     */
    private List<Jar> JarList;
    /**
     * 冲突的Class
     */
    private List<String> conflictClassList;
    /**
     * 最终判定的冲突数目，尤其冲突会有交际所以可能存在合并的情况。最终以此为准
     */
    private int finalConflictcount;
    /**
     * 冲突Id
     */
    private String id;

    /**
     * 冲突级别
     */
    private ConflictLevel level;
    /**
     * 冲突的描述
     */
    private String desc = "";

    public List<String> getConflictClassList() {
        return conflictClassList;
    }

    public void setConflictClassList(List<String> conflictClassList) {
        this.conflictClassList = conflictClassList;
    }

    public List<Jar> getJarList() {
        return JarList;
    }

    public void setJarList(List<Jar> jarList) {
        JarList = jarList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ConflictLevel getLevel() {
        return level;
    }

    public void setLevel(ConflictLevel level) {
        this.level = level;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getFinalConflictcount() {
        return finalConflictcount;
    }

    public void setFinalConflictcount(int finalConflictcount) {
        this.finalConflictcount = finalConflictcount;
    }


    

}

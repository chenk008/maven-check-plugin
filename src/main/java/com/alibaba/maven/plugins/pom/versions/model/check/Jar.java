package com.alibaba.maven.plugins.pom.versions.model.check;

import java.util.List;

/**
 * Jar包
 * 
 * 
 */
public class Jar {
    /**
     * jar包的物理路径
     */
    private String jarFilePath;
    /**
     * jar包jar协议路径
     */
    private String jarFileUrl;
    /**
     * jar包的物理名称 * 当取依赖树的时候，这个名称用gid前缀一下。避免重命名
     */
    private String jarFileName;
    /**
     * jar包的md5码
     */
    private String MD5 = "";
    /**
     * maven中表示的Artifact
     */
    private MyArtifact artifact;
    /**
     * jar包中包含的所有class
     */
    private List<String> classList;

    public String getJarFilePath() {
        return jarFilePath;
    }

    public void setJarFilePath(String jarFilePath) {
        this.jarFilePath = jarFilePath;
    }

    public String getJarFileUrl() {
        return jarFileUrl;
    }

    public void setJarFileUrl(String jarFileUrl) {
        this.jarFileUrl = jarFileUrl;
    }

    public String getJarFileName() {
        return jarFileName;
    }

    public void setJarFileName(String jarFileName) {
        this.jarFileName = jarFileName;
    }

    public List<String> getClassList() {
        return classList;
    }

    public void setClassList(List<String> classList) {
        this.classList = classList;
    }

    public String getMD5() {
        return MD5;
    }

    public void setMD5(String mD5) {
        MD5 = mD5;
    }

    public MyArtifact getArtifact() {
        return artifact;
    }

    public void setArtifact(MyArtifact artifact) {
        this.artifact = artifact;
    }
}

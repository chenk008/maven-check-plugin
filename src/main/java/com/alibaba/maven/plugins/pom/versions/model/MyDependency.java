package com.alibaba.maven.plugins.pom.versions.model;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;

public class MyDependency extends Dependency {

    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    private String toVersion;
    /**
     * 
     */
    private ArtifactRepository fromRepository;
    /**
     * 
     */
    private boolean isLocalRepoLatest;
    /**
     * 
     */
    private boolean isInDependencyManagementElement;

    private VersionType versionType;

    private VersionPosition versionPosition;

    private String propertiesBinding;
    
    private boolean isCanLock;

    @Override
    public int hashCode() {
        String key = this.getGroupId() + this.getArtifactId() + this.getVersion();
        int hash, i;
        for (hash = key.length(), i = 0; i < key.length(); i++)
            hash += key.charAt(i);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            String thisKey = this.getGroupId() + this.getArtifactId() + this.getVersion();
            String objKey = ((MyDependency) obj).getGroupId() + ((MyDependency) obj).getArtifactId()
                    + ((MyDependency) obj).getVersion();
            return thisKey.equals(objKey);
        } catch (Exception e) {
            return false;
        }

    }

    public String getToVersion() {
        return toVersion;
    }

    public void setToVersion(String toVersion) {
        this.toVersion = toVersion;
    }

    public boolean isLocalRepoLatest() {
        return isLocalRepoLatest;
    }

    public void setLocalRepoLatest(boolean isLocalRepoLatest) {
        this.isLocalRepoLatest = isLocalRepoLatest;
    }

    public boolean isInDependencyManagementElement() {
        return isInDependencyManagementElement;
    }

    public void setInDependencyManagementElement(boolean isInDependencyManagementElement) {
        this.isInDependencyManagementElement = isInDependencyManagementElement;
    }



    public VersionType getVersionType() {
        return versionType;
    }

    public void setVersionType(VersionType versionType) {
        this.versionType = versionType;
    }

    public VersionPosition getVersionPosition() {
        return versionPosition;
    }

    public void setVersionPosition(VersionPosition versionPosition) {
        this.versionPosition = versionPosition;
    }

    public String toStringAll() {
        return "groupId=" + this.getGroupId() + ",artifactId=" + this.getArtifactId() + ",version=" + this.getVersion()
                + ",versionType=" + this.getVersionType() + ",isInDependencyManagementElement="
                + this.isInDependencyManagementElement + ",versionPosition=" + this.getVersionPosition()
                + ",propertiesBinding=" + this.getPropertiesBinding();
    }

    public String getPropertiesBinding() {
        return propertiesBinding;
    }

    public void setPropertiesBinding(String propertiesBinding) {
        this.propertiesBinding = propertiesBinding;
    }

    public ArtifactRepository getFromRepository() {
        return fromRepository;
    }

    public void setFromRepository(ArtifactRepository fromRepository) {
        this.fromRepository = fromRepository;
    }

    public boolean isCanLock() {
        return isCanLock;
    }

    public void setCanLock(boolean isCanLock) {
        this.isCanLock = isCanLock;
    }

   

}

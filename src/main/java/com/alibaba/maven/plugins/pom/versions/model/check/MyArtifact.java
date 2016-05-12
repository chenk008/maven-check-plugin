package com.alibaba.maven.plugins.pom.versions.model.check;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.logging.AbstractLogEnabled;

public class MyArtifact extends AbstractLogEnabled implements Comparable<MyArtifact> {

    private File file;

    private String groupId;

    private String artifactId;

    private String baseVersion;

    private String version;

    private String classifier;

    private String scope = Artifact.SCOPE_COMPILE;

    private String type = "jar";

    private String name;
    
    private Artifact artifact;

    public int compareTo(MyArtifact o) {
        String one = this.artifactId + "-" + this.groupId + "-" + this.version + "-" + this.classifier + "."
                + this.type + ":" + this.scope;
        String two = o.getArtifactId() + "-" + o.getGroupId() + "-" + o.getVersion() + "-" + o.getClassifier() + "."
                + o.getType() + ":" + o.getScope();
        return one.compareTo(two);
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getBaseVersion() {
        return baseVersion;
    }

    public void setBaseVersion(String baseVersion) {
        this.baseVersion = baseVersion;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public String getName(boolean isUseBaseVersion) {
        if (StringUtils.isBlank(this.name)) {
            StringBuilder sb = new StringBuilder();
            sb.append(this.artifactId);
            sb.append("-");
            if (isUseBaseVersion) {
                sb.append(this.baseVersion);
            } else {
                sb.append(this.version);
            }

            if (StringUtils.isNotBlank(this.classifier)) {
                sb.append("-");
                sb.append(this.classifier);
            }

            sb.append(".");
            sb.append(this.type);
            sb.append(" (");
            sb.append(this.scope);
            sb.append(")");
            this.name = sb.toString();
        }
        return this.name;
    }

    public static MyArtifact createMyArtifact(Artifact artifact) {
        if ("jar".equals(artifact.getType())) {
            MyArtifact myArtifact = new MyArtifact();
            myArtifact.setFile(artifact.getFile());
            myArtifact.setGroupId(artifact.getGroupId());
            myArtifact.setArtifactId(artifact.getArtifactId());
            myArtifact.setBaseVersion(artifact.getBaseVersion());
            myArtifact.setVersion(artifact.getVersion());
            myArtifact.setScope(artifact.getScope());
            myArtifact.setType(artifact.getType());
            myArtifact.setClassifier(artifact.getClassifier());
            myArtifact.setArtifact(artifact);
            return myArtifact;
        } else {
            return null;
        }
    }

    public static String getArtifactName(Artifact artifact, boolean isUseBaseVersion) {
        StringBuilder sb = new StringBuilder();
        sb.append(artifact.getArtifactId());
        sb.append("-");
        if (isUseBaseVersion) {
            sb.append(artifact.getBaseVersion());
        } else {
            sb.append(artifact.getVersion());
        }

        if (StringUtils.isNotBlank(artifact.getClassifier())) {
            sb.append("-");
            sb.append(artifact.getClassifier());
        }

        sb.append(".");
        sb.append(artifact.getType());
        sb.append(" (");
        sb.append(artifact.getScope());
        sb.append(")");
        return sb.toString();
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

}

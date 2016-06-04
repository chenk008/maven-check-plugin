package org.ck.maven.plugins.pom.versions.mojo.check.impl;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.ck.maven.plugins.pom.versions.model.check.MyArtifact;
import org.ck.maven.plugins.pom.versions.mojo.check.AbstractCheckMojo;

/**
 * 
 * check dependencies conflict
 * 打包过程中，检查依赖冲突
 * 
 * @goal check
 * @phase package
 * @requiresProject true
 * @requiresDependencyResolution test
 * 
 * 
 * 
 */
public class CheckMojo extends AbstractCheckMojo {

    /**
     * checkPackaging war,jar
     * 
     * @parameter expression="${checkPackaging}" default-value=""
     */
    private String checkPackaging;

    /**
     * checkWithoutScope provided,test
     * 
     * @parameter expression="${checkWithoutScope}" default-value=""
     */
    private String checkWithoutScope;

    @SuppressWarnings("unchecked")
    @Override
    protected Set<MyArtifact> queryArtifacts() {
        Set<MyArtifact> realArtifacts = new TreeSet<MyArtifact>();
        Set<Artifact> artifacts = this.project.getArtifacts();
        this.getLog().info("Query to " + artifacts.size() + " dependencies");
        MyArtifact myArtifact;
        if (StringUtils.isBlank(checkWithoutScope)) {
            for (Artifact artifact : artifacts) {
                myArtifact = MyArtifact.createMyArtifact(artifact);
                if (myArtifact != null) {
                    boolean contains = realArtifacts.contains(myArtifact);
                    if (contains) {
                        this.getLog().info("-* " + MyArtifact.getArtifactName(artifact, true));
                    } else {
                        realArtifacts.add(myArtifact);
                    }
                } else {
                    this.getLog().info("-- " + MyArtifact.getArtifactName(artifact, true));
                }
            }
        } else {
            this.getLog().info("Check dependency without scope type : " + checkWithoutScope);
            String[] scopes = checkWithoutScope.split(",");
            boolean isWithoutScope = false;
            for (Artifact artifact : artifacts) {
                isWithoutScope = false;
                for (String string : scopes) {
                    if (artifact.getScope().equals(string)) {
                        isWithoutScope = true;
                        break;
                    }
                }
                if (!isWithoutScope) {
                    myArtifact = MyArtifact.createMyArtifact(artifact);
                    if (myArtifact != null) {
                        boolean contains = realArtifacts.contains(myArtifact);
                        if (contains) {
                            this.getLog().info("-* " + MyArtifact.getArtifactName(artifact, true));
                        } else {
                            realArtifacts.add(myArtifact);
                        }
                    } else {
                        this.getLog().info("-- " + MyArtifact.getArtifactName(artifact, true));
                    }
                } else {
                    this.getLog().info("-- " + MyArtifact.getArtifactName(artifact, true));
                }
            }
        }
        return realArtifacts;
    }

    @Override
    protected boolean customMethod() {
        boolean isRun = false;
        if (StringUtils.isBlank(checkPackaging)) {
            isRun = true;
        } else {
            this.getLog().info("Check project packaging type : " + checkPackaging);
            String projectPackaging = project.getPackaging();
            this.getLog().info("This project packaging is : " + projectPackaging);
            String[] packagings = checkPackaging.split(",");
            for (String string : packagings) {
                if (projectPackaging.equals(string)) {
                    isRun = true;
                    break;
                }
            }
        }
        if (!isRun) {
            this.getLog().info("This project does not check.");
        }
        return isRun;
    }

    @Override
    protected void deletePackage() {
        String basePath = project.getBasedir().getPath() + "/" + target + project.getBuild().getFinalName();
        String jarPath = basePath + ".jar";
        String sourcesPath = basePath + "-sources.jar";
        FileUtils.deleteQuietly(new File(jarPath));
        FileUtils.deleteQuietly(new File(sourcesPath));
    }

}

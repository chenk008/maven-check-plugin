package org.ck.maven.plugins.pom.versions.mojo;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.ck.maven.plugins.pom.versions.model.check.MyArtifact;

/**
 * 
 * check-war
 * 
 * @goal check-war
 * @phase package
 * @requiresProject true
 * 
 * 
 * 
 */
public class CheckWarMojo extends AbstractCheckMojo {

    /**
     * warDirPath
     * 
     * @parameter expression="${warDirPath}" default-value=""
     */
    private String warDirPath = "";
    /**
     * warPath
     * 
     * @parameter expression="${warPath}" default-value=""
     */
    private String warPath = "";

    @Override
    protected boolean customMethod() {
        if (!"war".equals(project.getPackaging())) {
            this.getLog().info("This project is not a webapp.");
            return false;
        } else {
            return true;
        }
    }

    private static final String libDirPath = "/WEB-INF/lib";

    @Override
    protected Set<MyArtifact> queryArtifacts() throws MojoExecutionException {

        warDirPath = warDirPath.trim();
        if (StringUtils.isBlank(warDirPath)) {
            warDirPath = project.getBasedir().getPath() + "/" + this.target + project.getBuild().getFinalName();
        }
        if (this.getLog().isDebugEnabled()) {
            this.getLog().debug("warDirPath : " + warDirPath);
        }

        warPath = warPath.trim();
        if (StringUtils.isBlank(warPath)) {
            warPath = warDirPath + ".war";
        }
        if (this.getLog().isDebugEnabled()) {
            this.getLog().debug("warPath : " + warPath);
        }

        warDirPath = warDirPath.replace("\\", "/");
        File warDir = new File(warDirPath);
        if (!warDir.exists()) {
            throw new MojoExecutionException(warDirPath + " not found");
        }
        if (!warDir.isDirectory()) {
            throw new MojoExecutionException(warDirPath + " is not a directory");
        }

        String jarDirPath = warDirPath + libDirPath;
        warDir = new File(jarDirPath);
        if (!warDir.exists()) {
            throw new MojoExecutionException(jarDirPath + " not found");
        }

        this.getLog().info("Search jarDirPath : " + jarDirPath);

        File jarDir = new File(jarDirPath);
        File[] listFiles = jarDir.listFiles(new FileFilter() {
       
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".jar")) {
                    return true;
                }
                return false;
            }
        });
        Set<MyArtifact> artifacts = new TreeSet<MyArtifact>();
        MyArtifact artifact;
        for (File file : listFiles) {
            artifact = new MyArtifact();
            artifact.setFile(file);
            artifact.setArtifactId(file.getName());
            artifacts.add(artifact);
        }
        return artifacts;
    }

    @Override
    protected void deletePackage() throws MojoExecutionException {
        FileUtils.deleteQuietly(new File(warPath));
        try {
            FileUtils.deleteDirectory(new File(warDirPath));
        } catch (IOException e) {
            throw new MojoExecutionException("", e);
        }

    }

}

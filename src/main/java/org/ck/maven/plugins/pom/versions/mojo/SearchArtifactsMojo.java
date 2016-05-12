package org.ck.maven.plugins.pom.versions.mojo;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.ck.maven.plugins.pom.versions.model.check.MyArtifact;

/**
 * 
 * search Artifacts
 * 
 * @goal sa
 * @phase process-resources
 * @requiresProject true
 * @requiresDependencyResolution test
 * 
 * 
 * 
 */
public class SearchArtifactsMojo extends AbstractMojo {
    /**
     * 
     * @parameter expression="${project}"
     * @readonly
     * @required
     * 
     */
    protected MavenProject project;

    /**
     * checkWithoutScope provided,test
     * 
     * @parameter expression="${checkWithoutScope}" default-value=""
     */
    private String checkWithoutScope;

    public void execute() throws MojoExecutionException, MojoFailureException {
        @SuppressWarnings("unchecked")
        Set<Artifact> artifacts = this.project.getArtifacts();
        this.getLog().info("Query to " + artifacts.size() + " dependencies");

        System.out.println("=========================================");
        for (Artifact artifact : artifacts) {
            this.getLog().info(artifact.toString());
        }
        System.out.println("=========================================");

        Set<MyArtifact> realArtifacts = new TreeSet<MyArtifact>();
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
        this.getLog().info("Scan to " + realArtifacts.size() + " useful dependencies");

        System.out.println("*****************************************");
        for (MyArtifact artifact : realArtifacts) {
            this.getLog().info("+- "+artifact.getName(false)+"   " + artifact.getArtifact().toString());
        }
        System.out.println("*****************************************");

    }

}

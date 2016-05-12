package com.alibaba.maven.plugins.pom.versions.mojo;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.dom4j.DocumentException;

import com.alibaba.maven.plugins.pom.versions.model.MyDependency;
import com.alibaba.maven.plugins.pom.versions.service.PomXmlService;
import com.alibaba.maven.plugins.pom.versions.service.RemoteArtifactRepositoriesReadService;

public abstract class AbstractVersionsMojo extends AbstractMojo {
    /**
     * 
     * @parameter expression="${project}"
     * @readonly
     * 
     */
    private MavenProject project;
    /**
     * @parameter expression="${localRepository}"
     * @readonly
     * 
     */
    private ArtifactRepository localRepository;

    /**
     * @component
     * 
     */
    private ArtifactFactory artifactFactory;

    /**
     * @component roleHint="pv"
     * 
     */
    private RepositoryMetadataManager repositoryMetadataManager;
    /**
     * @component
     */
    private RemoteArtifactRepositoriesReadService remoteArtifactRepositoriesReadService;


    public void execute() throws MojoExecutionException, MojoFailureException {
        checkRemoteArtifactRepositories();
        PomXmlService pomService = null;
        try {
            pomService = new PomXmlService(this.getProject().getFile(), this.getLog());
        } catch (DocumentException e) {
            throw new MojoExecutionException("pom.xml resolve error", e);
        }
        run(pomService);
        if (pomService.isChange()) {
            try {
                pomService.backupOldPomFile();
                pomService.writePomFile(true);
            } catch (IOException e) {
                throw new MojoExecutionException("write or backup  pom.xml  error", e);
            }
        } else {
            this.getLog().info("pom.xml not changed");
        }
    }

    protected abstract void run(PomXmlService pomService) throws MojoExecutionException, MojoFailureException;

    /**
     * 将Dependency字符串化
     * 
     * @param d
     * @return
     */
    protected String dependencyToString(Dependency d) {
        StringBuffer buf = new StringBuffer();
        buf.append(d.getGroupId());
        buf.append(":");
        buf.append(d.getArtifactId());
        buf.append(":");
        buf.append(d.getVersion());
        return buf.toString();
    }

    /**
     * 对象转换
     * 
     * @param d
     * @return
     */
    protected MyDependency dependencyToMyDependency(Dependency d) {
        MyDependency myDependency = new MyDependency();
        myDependency.setGroupId(d.getGroupId());
        myDependency.setArtifactId(d.getArtifactId());
        myDependency.setVersion(d.getVersion());
        myDependency.setScope(d.getScope());
        myDependency.setType(d.getType());
        return myDependency;
    }

    /**
     * 在控制台直接打印
     * 
     * @param msg
     */
    protected void print(String msg) {
        if (StringUtils.isNotBlank(msg)) {
            System.out.println(msg);
        }
    }

    /**
     * 
     * @throws MojoExecutionException
     */
    private void checkRemoteArtifactRepositories() throws MojoExecutionException {
        String localRepositoryId = "local";
        @SuppressWarnings("unchecked")
        List<ArtifactRepository> remoteArtifactRepositories = this.project.getRemoteArtifactRepositories();
        for (ArtifactRepository artifactRepository : remoteArtifactRepositories) {
            if (localRepositoryId.equals(artifactRepository.getId())) {
                throw new MojoExecutionException("remote repository id cannot be  'local'");
            }
            artifactRepository.getSnapshots().setUpdatePolicy(ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS);
        }
    }

    public MavenProject getProject() {
        return project;
    }

    public ArtifactRepository getLocalRepository() {
        return localRepository;
    }

    public ArtifactFactory getArtifactFactory() {
        return artifactFactory;
    }

    public RepositoryMetadataManager getRepositoryMetadataManager() {
        return repositoryMetadataManager;
    }

    public RemoteArtifactRepositoriesReadService getRemoteArtifactRepositoriesReadService() {
        return remoteArtifactRepositoriesReadService;
    }

}

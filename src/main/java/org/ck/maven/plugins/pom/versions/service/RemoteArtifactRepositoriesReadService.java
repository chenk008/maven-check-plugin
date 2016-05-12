package org.ck.maven.plugins.pom.versions.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.plugin.MojoFailureException;
import org.ck.maven.plugins.pom.versions.common.PropertiesLoaderUtil;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * 
 * 加载使用到的maven库
 * 
 */
public final class RemoteArtifactRepositoriesReadService extends AbstractLogEnabled {
    private final String FILE_NAME = "RemoteArtifactRepositories.properties";

    @SuppressWarnings("unchecked")
    private List<ArtifactRepository> list = Collections.EMPTY_LIST;

    private ArtifactRepositoryLayout artifactRepositoryLayout;

    private synchronized void load() throws IOException {
        Properties properties = PropertiesLoaderUtil.getProperties(FILE_NAME);
        this.getLogger().debug("loading " + FILE_NAME);
        if (properties != null && properties.size() > 0) {
            list = new ArrayList<ArtifactRepository>();
            ArtifactRepository artifactRepository;
            for (Entry<Object, Object> propertie : properties.entrySet()) {
                artifactRepository = new DefaultArtifactRepository(propertie.getKey().toString(), propertie.getValue()
                        .toString(), artifactRepositoryLayout);
                list.add(artifactRepository);
                this.getLogger().info("loading remote repo : [" + propertie.getKey().toString()+"] "+propertie.getValue().toString());
            }
        }

    }

    public List<ArtifactRepository> getRemoteArtifactRepositories() throws MojoFailureException {
        if (list == null || list.size() == 0) {
            try {
                load();
            } catch (IOException e) {
                throw new MojoFailureException("init remoteArtifactRepositories error");
            }
        }
        return list;
    }

}

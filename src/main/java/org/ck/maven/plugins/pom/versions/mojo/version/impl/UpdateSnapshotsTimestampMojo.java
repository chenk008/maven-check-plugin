package org.ck.maven.plugins.pom.versions.mojo.version.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataResolutionException;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotArtifactRepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ck.maven.plugins.pom.versions.common.NotLockVersionException;
import org.ck.maven.plugins.pom.versions.model.MyDependency;
import org.ck.maven.plugins.pom.versions.model.VersionType;
import org.ck.maven.plugins.pom.versions.mojo.version.AbstractVersionsMojo;
import org.ck.maven.plugins.pom.versions.service.version.PomXmlService;

/**
 * 
 * update snapshot dependencies
 * 更新snapshot到最新的snapshot
 * 
 * @goal update-snapshots
 * @requiresProject true
 * @requiresOnline true
 * 
 * 
 * 
 */
public class UpdateSnapshotsTimestampMojo extends AbstractVersionsMojo {

    /**
     * exclude dependencies regex
     * 
     * @parameter expression="${excludeRegex}" default-value=""
     */
    private String excludeRegex;
    /**
     * include dependencies regex
     * 
     * @parameter expression="${includeRegex}" default-value=""
     */
    private String includeRegex;

    private Pattern excludeRegexPattern = null;

    private boolean isExclude(MyDependency myDependency) {
        String myDependencyTemp = myDependency.getGroupId() + ":" + myDependency.getArtifactId();
        if (excludeRegexPattern == null) {
            excludeRegexPattern = Pattern.compile(excludeRegex);
        }
        return excludeRegexPattern.matcher(myDependencyTemp).find();
    }

    private Pattern includeRegexPattern = null;

    private boolean isInclude(MyDependency myDependency) {
        String myDependencyTemp = myDependency.getGroupId() + ":" + myDependency.getArtifactId();
        if (includeRegexPattern == null) {
            includeRegexPattern = Pattern.compile(includeRegex);
        }
        return includeRegexPattern.matcher(myDependencyTemp).find();
    }

    @Override
    protected void run(PomXmlService pomService) throws MojoExecutionException, MojoFailureException {
        if (StringUtils.isNotBlank(excludeRegex)) {
            this.getLog().info("excludeRegex : " + excludeRegex);
        }
        if (StringUtils.isNotBlank(includeRegex)) {
            this.getLog().info("includeRegex : " + includeRegex);
        }

        Set<MyDependency> allMyDependencySet = pomService.getMyDependencys();
        Set<MyDependency> needMyDependencySet = new HashSet<MyDependency>();
        if (allMyDependencySet.size() > 0) {
            boolean isInInclude = true;
            boolean isOutExclude = true;
            for (MyDependency myDependency : allMyDependencySet) {
                if (this.getLog().isDebugEnabled()) {
                    this.getLog().debug("allMyDependencySet : " + myDependency.toStringAll());
                }
                isInInclude = true;
                isOutExclude = true;
                if (myDependency.getVersionType() == VersionType.SNAPSHOT_TIMESTAMP
                        || myDependency.getVersionType() == VersionType.SNAPSHOT) {

                    if (StringUtils.isNotBlank(includeRegex)) {
                        isInInclude = isInclude(myDependency);
                    }
                    if (StringUtils.isNotBlank(excludeRegex)) {
                        isOutExclude = !isExclude(myDependency);
                    }
                    if (isInInclude && isOutExclude) {
                        needMyDependencySet.add(myDependency);
                    }
                }
            }

        } else {
            getLog().info("no dependencies needed to update");
            return;
        }

        if (needMyDependencySet.size() > 0) {
            for (MyDependency myDependency : needMyDependencySet) {
                checkDependencyMetadata(myDependency);
            }
            System.out.println("");
            System.out.println("********************** Update Snapshot Dependencies For ["
                    + this.getProject().getArtifactId() + "] ********************");
            System.out.println("");
            Map<Integer, MyDependency> needDependencyMap = new TreeMap<Integer, MyDependency>();
            int key = 1;
            for (MyDependency myDependency : needMyDependencySet) {
                if (myDependency.isLocalRepoLatest()) {
                    System.out.println("WARNING: " + this.dependencyToString(myDependency)
                            + " in local repository is the latest, so does not support updates");
                } else if (!myDependency.isCanLock()) {
                    System.out.println("WARNING: " + this.dependencyToString(myDependency)
                            + " updates unavailable in :");
                    for (ArtifactRepository artifactRepository : this.getRemoteArtifactRepositoriesReadService()
                            .getRemoteArtifactRepositories()) {
                        System.out.println("  [" + artifactRepository.getUrl() + "]  ");
                    }
                    System.out.println();
                } else if (myDependency.getVersion().equals(myDependency.getToVersion())) {
                    this.getLog().debug("Ignore update, because they versions as well.");
                } else {
                    needDependencyMap.put(key, myDependency);
                    key++;
                }

            }
            if (needDependencyMap.size() > 0) {
                System.out.println("");
                MyDependency needMyDependency;
                Integer mapKey;
                for (Map.Entry<Integer, MyDependency> entry : needDependencyMap.entrySet()) {
                    mapKey = entry.getKey();
                    needMyDependency = entry.getValue();
                    System.out.println(MessageFormat.format(template, mapKey,
                            this.dependencyToString(needMyDependency), needMyDependency.getToVersion(),
                            needMyDependency.getFromRepository().getId()));
                    System.out.println("");
                }
                System.out.println("");
                setDependencysVersion(needDependencyMap, pomService);
            } else {
                System.out.println(" * snapshot  dependencies  all latest");
            }
            System.out.println("");
            System.out
                    .println("*******************************************************************************************");
            System.out.println("");

        } else {
            getLog().info("no snapshot  dependencies needed to update");
        }

    }

    private final static String template = "* {0} | {1} >> {2} | from repository : {3}";

    /**
     * 更新snapshot的版本
     * @param needDependencyMap
     * @param pomService
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    private void setDependencysVersion(Map<Integer, MyDependency> needDependencyMap, PomXmlService pomService)
            throws MojoExecutionException, MojoFailureException {
        InputStreamReader stdin = new InputStreamReader(System.in);
        BufferedReader bufin = new BufferedReader(stdin);
        try {
            System.out
                    .print("Press [Enter] to update all items or choose items to update, e.g., [1] or [1 2] or [Q|q] to quit: ");
            String str = bufin.readLine();
            str = str.toLowerCase().trim();
            if (StringUtils.isBlank(str)) {
                str = "all";
            } else {
                if ("q".equals(str)) {
                    System.out.println("");
                    System.out.println(" * quit update");
                    return;
                }
            }

            System.out.println("");
            System.out.println("Choice is : [ " + str + " ]");
            System.out.println("");
            System.out.println("");
            String[] items = str.split(" ");
            MyDependency myDependency;
            for (String key : items) {
                if ("all".equals(key)) {
                    for (Map.Entry<Integer, MyDependency> entry : needDependencyMap.entrySet()) {
                        pomService.setDependencyVersion(entry.getValue());
                    }
                    System.out.println(" * Update all dependencies");
                    break;
                }
                Integer iKey = null;
                try {
                    iKey = Integer.parseInt(key);
                } catch (Exception e) {
                    throw new MojoFailureException("Choice  : [" + key + "] is invalid.");
                }
                myDependency = needDependencyMap.get(iKey);
                if (myDependency != null) {
                    pomService.setDependencyVersion(myDependency);
                    System.out.println(" * update " + this.dependencyToString(myDependency) + " >> "
                            + myDependency.getToVersion());
                } else {
                    throw new MojoFailureException("Choice  : [" + key + "] is invalid..");
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Choice item is invalid.", e);
        }
    }

    /**
     * 检查某个依赖的元数据
     * @param myDependency
     * @throws MojoFailureException
     */
    private void checkDependencyMetadata(MyDependency myDependency) throws MojoFailureException {
        try {
            Artifact artifact = this.getArtifactFactory().createArtifact(myDependency.getGroupId(),
                    myDependency.getArtifactId(), myDependency.getVersion(), myDependency.getScope(),
                    myDependency.getType());
            SnapshotArtifactRepositoryMetadata snapshotArtifactRepositoryMetadata = new SnapshotArtifactRepositoryMetadata(
                    artifact);
            this.getRepositoryMetadataManager().resolve(snapshotArtifactRepositoryMetadata,
                    this.getRemoteArtifactRepositoriesReadService().getRemoteArtifactRepositories(),
                    this.getLocalRepository());

            String toVersion;
            boolean isLocalRepoLatest;
            ArtifactRepository fromRepository;
            boolean isCanLock;
            try {
                Metadata metadata = snapshotArtifactRepositoryMetadata.getMetadata();
                Versioning versioning = metadata.getVersioning();
                if (versioning == null) {
                    throw new NotLockVersionException("Failed to resolve artifact : "
                            + snapshotArtifactRepositoryMetadata.getKey());
                }
                Snapshot snapshot = versioning.getSnapshot();
                if (snapshot == null) {
                    throw new NotLockVersionException("Failed to resolve artifact : "
                            + snapshotArtifactRepositoryMetadata.getKey());
                }

                if (snapshot.isLocalCopy()) {
                    this.getLog().debug("local repository is  the latest snapshot , so not lock");
                    toVersion = myDependency.getVersion();
                    fromRepository = this.getLocalRepository();
                    isLocalRepoLatest = true;
                } else {
                    isLocalRepoLatest = false;
                    toVersion = removeSnapshotQualifier(metadata.getVersion());
                    toVersion += "-" + snapshot.getTimestamp() + "-" + snapshot.getBuildNumber();
                    fromRepository = artifact.getRepository() == null ? this.getLocalRepository() : artifact
                            .getRepository(); // 如果远程仓库id定义为local了，那么此时就会返回null
                }
                isCanLock = true;
            } catch (NotLockVersionException ne) {
                toVersion = myDependency.getVersion();
                isLocalRepoLatest = false;
                fromRepository = null;
                isCanLock = false;
            }

            myDependency.setLocalRepoLatest(isLocalRepoLatest);
            myDependency.setToVersion(toVersion);
            myDependency.setFromRepository(fromRepository);
            myDependency.setCanLock(isCanLock);
        } catch (RepositoryMetadataResolutionException e) {
            throw new MojoFailureException("resolve artifact error", e);
        }
    }

    private String removeSnapshotQualifier(String version) {
        Matcher matcher = PomXmlService.matchSnapshotTimestampRegex.matcher(version);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            int index = version.lastIndexOf("-SNAPSHOT");
            return index > 0 ? version.substring(0, index) : version;
        }

    }
}

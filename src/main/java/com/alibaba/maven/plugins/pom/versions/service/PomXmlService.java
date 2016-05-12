package com.alibaba.maven.plugins.pom.versions.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.alibaba.maven.plugins.pom.versions.model.MyDependency;
import com.alibaba.maven.plugins.pom.versions.model.VersionPosition;
import com.alibaba.maven.plugins.pom.versions.model.VersionType;

/**
 * pom.xml
 * 
 * 
 */
public class PomXmlService {
    private final Log log;

    public PomXmlService(File pomXmlFile, Log log) throws DocumentException {
        this.log = log;
        this.pomXmlFile = pomXmlFile;
        this.document = readPomFile();
        this.MavenProjectProperties = getProperties();
    }

    private Properties getProperties() {
        Properties p = new Properties();
        Element propertiesElement = this.document.getRootElement().element(elementNames[6]);// properties
        if (propertiesElement != null) {
            @SuppressWarnings("unchecked")
            List<Element> elements = propertiesElement.elements();
            for (Element e : elements) {
                p.setProperty(e.getName(), e.getText());
            }
        }
        return p;
    }

    private Document readPomFile() throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(pomXmlFile);
        log.debug("readPomFile for Document");
        return document;
    }

    private final File pomXmlFile;
    private final Properties MavenProjectProperties;
    private final Document document;
    private final String[] elementNames = new String[] { "dependencyManagement", "dependencies", "dependency",
            "groupId", "artifactId", "version", "properties" };
    private boolean isChange = false;

    @Deprecated
    public void setPomVersion(String pomVersion) {
        Element projectElement = this.document.getRootElement();
        Element pomVersionElement = projectElement.element(elementNames[5]);
        if (pomVersionElement != null) {
            String version = pomVersionElement.getText();
            if (!pomVersion.equals(version)) {
                pomVersionElement.setText(pomVersion);
                isChange = true;
            } else {
                log.warn("pom.version no change");
            }
        }
    }

    public Set<MyDependency> getMyDependencys() {
        Set<MyDependency> myDependencySet = new HashSet<MyDependency>();
        Element projectElement = this.document.getRootElement();
        Element dependencyManagementElement = projectElement.element(elementNames[0]); // dependencyManagement
        if (dependencyManagementElement != null) {
            myDependencySet.addAll(getMyDependencysByDependenciesElement(
                    dependencyManagementElement.element(elementNames[1]), true));
        }
        myDependencySet.addAll(getMyDependencysByDependenciesElement(projectElement.element(elementNames[1]), false));
        return myDependencySet;
    }

    private Set<MyDependency> getMyDependencysByDependenciesElement(Element dependenciesElement,
            boolean isInDependencyManagementElement) {
        Set<MyDependency> myDependencySet = new HashSet<MyDependency>();
        if (dependenciesElement != null) {
            @SuppressWarnings("unchecked")
            List<Element> dependencyElements = dependenciesElement.elements(elementNames[2]);// dependency
            String gid, aid, version;
            MyDependency myDependency;
            for (Element dependencyElement : dependencyElements) {
                gid = dependencyElement.elementText(elementNames[3]);// groupId
                aid = dependencyElement.elementText(elementNames[4]);// artifactId
                version = dependencyElement.elementText(elementNames[5]);// version
                if (StringUtils.isNotBlank(gid) && StringUtils.isNotBlank(aid) && StringUtils.isNotBlank(version)) {
                    myDependency = new MyDependency();
                    myDependency.setGroupId(gid);
                    myDependency.setArtifactId(aid);
                    manageVersion(version, myDependency);
                    myDependency.setInDependencyManagementElement(isInDependencyManagementElement);
                    myDependencySet.add(myDependency);
                }
            }
        }
        return myDependencySet;
    }

    private final Pattern versionByProperties = Pattern.compile("^\\$\\{(.+)\\}$");

    private void manageVersion(String srcVersion, MyDependency myDependency) {
        String resultVersion;
        VersionPosition versionPosition;
        VersionType versionType;
        String propertiesBinding;
        Matcher matcher = versionByProperties.matcher(srcVersion);
        if (matcher.matches()) { // ${sping.version}
            String temp = matcher.group(1);
            propertiesBinding = srcVersion;
            resultVersion = MavenProjectProperties.getProperty(temp, "");
            versionPosition = VersionPosition.Properties;
            if (isSnapshot(resultVersion)) {
                versionType = VersionType.SNAPSHOT;
            } else if (isSnapshotTimestamp(resultVersion)) {
                versionType = VersionType.SNAPSHOT_TIMESTAMP;
            } else if ("".equals(resultVersion)) {
                versionType = VersionType.UNKNOWN;
                resultVersion = srcVersion;
            } else {
                versionType = VersionType.RELEASE;
            }
        } else { // 1.23
            resultVersion = srcVersion;
            propertiesBinding = "";
            versionPosition = VersionPosition.self;
            if (isSnapshot(resultVersion)) {
                versionType = VersionType.SNAPSHOT;
            } else if (isSnapshotTimestamp(resultVersion)) {
                versionType = VersionType.SNAPSHOT_TIMESTAMP;
            } else {
                versionType = VersionType.RELEASE;
            }
        }
        myDependency.setVersion(resultVersion);
        myDependency.setPropertiesBinding(propertiesBinding);
        myDependency.setVersionPosition(versionPosition);
        myDependency.setVersionType(versionType);
    }

    private boolean isSnapshot(String version) {
        return version.endsWith(Artifact.SNAPSHOT_VERSION);
    }

    private boolean isSnapshotTimestamp(String version) {
        return matchSnapshotTimestampRegex.matcher(version).matches();
    }

    // Artifact.VERSION_FILE_PATTERN is error
    public final static Pattern matchSnapshotTimestampRegex = Pattern.compile("^(.+)\\-(\\d{8})\\.(\\d{6})\\-(\\d+)$");

    public void setDependencyVersion(MyDependency myDependency) {
        Element projectElement = this.document.getRootElement();
        Element propertiesElement = projectElement.element(elementNames[6]);// properties
        Element dependencyManagementElement = projectElement.element(elementNames[0]); // dependencyManagement

        if (dependencyManagementElement != null) {
            setArtifactVersion(dependencyManagementElement.element(elementNames[1]), myDependency, propertiesElement);// dependencies
        }
        setArtifactVersion(projectElement.element(elementNames[1]), myDependency, propertiesElement);// dependencies
    }

    @SuppressWarnings("unchecked")
    private void setArtifactVersion(Element dependenciesElement, MyDependency myDependency, Element propertiesElement) {
        Element element = dependenciesElement;
        List<Element> elements = null;
        if (element != null) {
            elements = element.elements(elementNames[2]);// dependency
            if (elements.size() > 0) {
                String gid, aid, version, toVersion;
                Matcher matcher;
                for (Element e : elements) {
                    gid = e.elementText(elementNames[3]);// groupId
                    aid = e.elementText(elementNames[4]);// artifactId
                    version = e.elementText(elementNames[5]);// version
                    if (myDependency.getGroupId().equals(gid) && myDependency.getArtifactId().equals(aid)
                            && version != null) {
                        toVersion = myDependency.getToVersion();
                        if (myDependency.getVersion().equals(version)) { // 直接定义
                            e.element(elementNames[5]).setText(toVersion);
                            isChange = true;
                        } else if (myDependency.getPropertiesBinding().equals(version)) { // 属性定义
                            matcher = versionByProperties.matcher(myDependency.getPropertiesBinding());
                            matcher.matches();
                            propertiesElement.element(matcher.group(1)).setText(toVersion);
                            isChange = true;
                        }
                    } // 无veriosn
                }
            }
        }
    }

    public void writePomFile(boolean isCover) throws IOException {
        File destFile;
        if (isCover) {
            destFile = pomXmlFile;
            log.debug("rewrite pom.xml");
        } else {
            String path = pomXmlFile.getPath();
            path += ".c.xml";
            log.debug("writePomFile for " + path);
            destFile = new File(path);
        }
        XMLWriter writer = new XMLWriter(new FileOutputStream(destFile));
        writer.write(document);
        writer.close();
        log.info("write "+destFile.getPath()+" successful.");

    }

    public void backupOldPomFile() throws IOException {
        String path = pomXmlFile.getPath();
        path += ".pv";
        log.debug("backupOldPomFile for " + path);
        File destFile = new File(path);
        FileUtils.copyFile(pomXmlFile, destFile);
        log.info("backup "+destFile.getPath()+" successful.");
    }
    

    public boolean isChange() {
        return isChange;
    }

    public Properties getMavenProjectProperties() {
        return MavenProjectProperties;
    }
}

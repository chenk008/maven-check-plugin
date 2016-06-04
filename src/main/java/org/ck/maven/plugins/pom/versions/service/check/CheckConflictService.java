package org.ck.maven.plugins.pom.versions.service.check;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.ck.maven.plugins.pom.versions.model.check.CheckConflictResult;
import org.ck.maven.plugins.pom.versions.model.check.Conflict;
import org.ck.maven.plugins.pom.versions.model.check.ConflictLevel;
import org.ck.maven.plugins.pom.versions.model.check.Jar;
import org.ck.maven.plugins.pom.versions.model.check.MyArtifact;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * 检查多个jar包是否包含冲突
 * 
 * 
 */
public class CheckConflictService extends AbstractLogEnabled {

    /**
     * 扫描所有artifact分析它们彼此间的冲突
     * 
     * @param artifacts
     * @return null表示没有冲突
     * @throws Exception
     */
    public CheckConflictResult searchArtifacts(Set<MyArtifact> artifacts) throws Exception {
        this.getLogger().info("Scan to " + artifacts.size() + " useful dependencies");
        if (artifacts == null || artifacts.size() == 0) {
            return null;
        }

        String jarFilePath;
        Jar jar;
        File file;
        Map<String, Jar> allJar = new HashMap<String, Jar>();
        List<String> allClassList = new ArrayList<String>();

        for (MyArtifact artifact : artifacts) {

            if (StringUtils.isBlank(artifact.getGroupId())) {
                this.getLogger().info("+- " + artifact.getArtifactId() + ":" + artifact.getScope());
            } else {
                 this.getLogger().info("+- "+artifact.getName(false)); //TODO
                //this.getLogger().info("+- " + artifact.getName(false) + "   " + artifact.getArtifact().toString());
            }

            jarFilePath = artifact.getFile().getPath();
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug(jarFilePath + " " + artifact.getScope());
            }

            if (!jarFilePath.endsWith(".jar")) {
                this.getLogger().warn(jarFilePath + " is not .jar end");
                continue;
            }

            file = new File(jarFilePath);
            if (!file.exists()) {
                this.getLogger().warn(jarFilePath + " is not found");
                continue;
            }

            jar = searchJarAndStuff(file, artifact, allClassList);
            if (jar != null) {
                allJar.put(jar.getJarFileName(), jar);
            }
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("all class size : " + allClassList.size());
        }
        if (allClassList.size() == 0) {
            return null;
        }

        return processConflictJar(allClassList, allJar);

    }

    /**************************************************/

    /**
     * 分析class冲突
     * 
     * @param allClassList 内容格式:"jar文件路径||类名"
     * @param allJar
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    private CheckConflictResult processConflictJar(List<String> allClassList, Map<String, Jar> allJar) throws Exception {

        String[] splitStr;
        String mapValue;
        Map<String, String> conflictClassMap = new HashMap<String, String>();

        //找到冲突的类
        for (String clazz : allClassList) {
            splitStr = clazz.split("\\|\\|");
            mapValue = conflictClassMap.get(splitStr[0]);
            if (StringUtils.isNotBlank(mapValue)) {
            	//用||分隔所有包含该类的jar包文件路径
                conflictClassMap.put(splitStr[0], mapValue + "||" + splitStr[1]);
            } else {
                conflictClassMap.put(splitStr[0], splitStr[1]);
            }
        }

        if (conflictClassMap.size() == 0) {
            return null;
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("conflictClassMap : " + conflictClassMap.size());
        }

        //conflictClassMap 转换成 conflictJarCountMap
        Map<String, List<String>> conflictJarCountMap = new HashMap<String, List<String>>();
        String key;
        List<String> value;
        for (Map.Entry<String, String> entry : conflictClassMap.entrySet()) {
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug(entry.getKey() + " : " + entry.getValue());
            }
            key = entry.getValue();
            if (key.indexOf("||") < 0) {
                continue;
            }
            value = conflictJarCountMap.get(key);
            if (value == null) {
                value = new ArrayList<String>();
            }
            value.add(entry.getKey());
            conflictJarCountMap.put(key, value);
        }

        if (conflictJarCountMap.size() == 0) {
            return null;
        }

        return processResult(conflictJarCountMap, allJar);

    }

    /**
     * 
     * @param conflictJarCountMap key:类名，value：jar包的list
     * @param allJar
     * @return
     * @throws Exception
     */
    private CheckConflictResult processResult(Map<String, List<String>> conflictJarCountMap, Map<String, Jar> allJar)
            throws Exception {
        List<Jar> jarList;
        String[] splitStr;
        List<Conflict> allConflictList = new ArrayList<Conflict>();
        Conflict conflict;
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("conflictJarCountMap : " + conflictJarCountMap.size());
        }
        for (Map.Entry<String, List<String>> entry : conflictJarCountMap.entrySet()) {
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug(entry.getKey() + " : " + entry.getValue());
            }
            splitStr = entry.getKey().split("\\|\\|");
            jarList = new ArrayList<Jar>();
            for (String jarName : splitStr) {
                jarList.add(allJar.get(jarName));
            }
            conflict = createConflict(jarList, entry.getValue());
            allConflictList.add(conflict);

        }

        return returnResult(allConflictList);
    }

    private Conflict createConflict(List<Jar> jarList, List<String> conflictClassList) throws Exception {
        Conflict conflict = new Conflict();
        String id = "";
        for (Jar jar : jarList) {
            id += "," + jar.getJarFileName();
        }
        id = id.substring(id.indexOf(",") + 1, id.length());
        conflict.setJarList(jarList);
        conflict.setId(id);
        conflict.setConflictClassList(conflictClassList);
        conflict.setFinalConflictcount(conflictClassList.size());
        return conflict;
    }

    private CheckConflictResult returnResult(List<Conflict> allConflictList) throws Exception {
        List<Conflict> warnConflictList = new ArrayList<Conflict>();
        List<Conflict> errorConflictList = new ArrayList<Conflict>();
        List<Conflict> fatalConflictList = new ArrayList<Conflict>();

        for (Conflict conflict : allConflictList) {

            for (Conflict conflict2 : allConflictList) {
                if (conflict.getJarList().size() < conflict2.getJarList().size()) {
                    processConflict(conflict, conflict2);
                }
            }
            ConflictLevel level = ConflictLevel.warn;
            for (Jar jar : conflict.getJarList()) {
                if (conflict.getFinalConflictcount() == jar.getClassList().size()) {
                    level = ConflictLevel.error;
                }
            }
            conflict.setLevel(level);
            putDescAndUpLevel(conflict);
            if (ConflictLevel.warn == conflict.getLevel()) {
                warnConflictList.add(conflict);
            } else if (ConflictLevel.error == conflict.getLevel()) {
                errorConflictList.add(conflict);
            } else {
                fatalConflictList.add(conflict);
            }
        }
        CheckConflictResult r = new CheckConflictResult();
        r.setErrorConflictList(errorConflictList);
        r.setWarnConflictList(warnConflictList);
        r.setFatalConflictList(fatalConflictList);
        return r;
    }

    private void processConflict(Conflict conflict1, Conflict conflict2) {
        List<Jar> jarList1 = conflict1.getJarList();
        List<String> jarNames1 = new ArrayList<String>();
        for (Jar jar : jarList1) {
            jarNames1.add(jar.getJarFileName());
        }

        List<Jar> jarList2 = conflict2.getJarList();
        List<String> jarNames2 = new ArrayList<String>();
        for (Jar jar : jarList2) {
            jarNames2.add(jar.getJarFileName());
        }

        List<Boolean> bl = new ArrayList<Boolean>();
        for (int i = 0; i < jarNames1.size(); i++) {
            String name1 = jarNames1.get(i);
            for (int j = 0; j < jarNames2.size(); j++) {
                String name2 = jarNames2.get(j);
                if (name1.equals(name2)) {
                    bl.add(true);
                }
            }
        }

        int trueCount = 0;

        for (Boolean boolean1 : bl) {
            if (boolean1) {
                trueCount++;
            }
        }

        if (trueCount == jarList1.size()) {
            int count1 = conflict1.getFinalConflictcount();
            int count2 = conflict2.getFinalConflictcount();
            conflict1.setFinalConflictcount(count1 + count2);
        }
    }

    private void putDescAndUpLevel(Conflict conflict) throws Exception {
        if (ConflictLevel.warn == conflict.getLevel()) {
            conflict.setDesc("The jars cannot determine the logic of conflict.");
        } else {
            conflict.setDesc("The conflict jars has the same MD5 value.");
            List<Jar> jarList = conflict.getJarList();
            int[] counts = new int[jarList.size() + 1];
            counts[0] = conflict.getFinalConflictcount();
            int num = 1;
            for (Jar jar : jarList) {
                jar.setMD5(DigestUtils.md5Hex(new FileInputStream(new File(jar.getJarFilePath()))));
                counts[num] = jar.getClassList().size();
                num++;
            }
            boolean m = true;
            for (int i = 0; i < counts.length - 1; i++) {
                if (counts[i] != counts[i + 1]) {
                    m = false;
                    break;
                }
            }

            if (m) {
                for (int i = 0; i < jarList.size() - 1; i++) {
                    if (!jarList.get(i).getMD5().equals(jarList.get(i + 1).getMD5())) {
                        conflict.setDesc("The jars conflict.");
                        conflict.setLevel(ConflictLevel.fatal); // up
                                                                // level
                        break;
                    }
                }
            } else {
                conflict.setDesc("The jars may be the inclusion relationship conflict.");
                conflict.setLevel(ConflictLevel.fatal); // up level
            }
        }

    }

    /**************************************************/

    /**
     * 扫描jar中所有类
     * 
     * @param jarFilePath jar文件物理路径
     * @param artifact
     * @param allClassList class收集器，钩子出参，如果不需要请传递null
     * @return 如果这个jar包中不包含class，则返回null
     * @throws Exception
     */
    public Jar searchJarAndStuff(File file, MyArtifact artifact, List<String> allClassList) throws Exception {

        String jarFilePath = file.getPath();
        String jarFileUrl = "jar:file:" + jarFilePath + "!/";
        String jarFileName = "";
        if (StringUtils.isBlank(artifact.getGroupId())) {
            jarFileName = file.getName();
        } else {
            jarFileName = artifact.getGroupId() + "-" + file.getName();
        }

        Jar jar = new Jar();
        jar.setJarFilePath(jarFilePath);
        jar.setJarFileUrl(jarFileUrl);
        // snapshot
        if (StringUtils.isNotBlank(artifact.getBaseVersion()) && StringUtils.isNotBlank(artifact.getVersion())
                && !artifact.getBaseVersion().equals(artifact.getVersion())) {
            jarFileName = artifact.getGroupId() + "-" + artifact.getArtifactId() + "-" + artifact.getVersion() + "."
                    + artifact.getType();
        }
        jar.setJarFileName(jarFileName);
        jar.setArtifact(artifact);

        List<String> classList = new ArrayList<String>();
        URL url = new URL(jar.getJarFileUrl());
        JarURLConnection conn = ((JarURLConnection) url.openConnection());
        conn.setUseCaches(false);
        JarFile jarFile = conn.getJarFile();
        Enumeration<JarEntry> entries = jarFile.entries();
        String fileName;
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            fileName = entry.getName();
            if (fileName.endsWith(".class")) {
                fileName = fileName.replace('/', '.');
                classList.add(fileName);
                if (allClassList != null) {
                    allClassList.add(fileName + "||" + jar.getJarFileName());
                }
            }
        }
        jarFile.close();
        if (classList.size() == 0) {
            return null;
        }
        jar.setClassList(classList);
        return jar;
    }

}

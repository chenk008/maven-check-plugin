package org.ck.maven.plugins.pom.versions.service.check;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoFailureException;
import org.ck.maven.plugins.pom.versions.model.check.CheckConflictResult;
import org.ck.maven.plugins.pom.versions.model.check.Conflict;
import org.ck.maven.plugins.pom.versions.model.check.ConflictLevel;
import org.ck.maven.plugins.pom.versions.model.check.Jar;
import org.ck.maven.plugins.pom.versions.model.check.ProcessConflictResult;
import org.codehaus.plexus.logging.AbstractLogEnabled;

public class ProcessConflictService extends AbstractLogEnabled {
    /**
     * 处理并输出扫描结果
     * 
     * @param r
     * @param isCreateDetailDoc
     * @param excludes
     * @return
     * @throws MojoFailureException
     */
    public ProcessConflictResult printResult(CheckConflictResult r, boolean isCreateDetailDoc, String[] excludes) throws MojoFailureException {

        List<Conflict> warnConflictList = r.getWarnConflictList();

        List<Conflict> errorConflictList = r.getErrorConflictList();

        List<Conflict> fatalConflictList = r.getFatalConflictList();

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("warnConflictList : " + warnConflictList.size());
            this.getLogger().debug("errorConflictList : " + errorConflictList.size());
            this.getLogger().debug("fatalConflictList : " + fatalConflictList.size());
        }

        if (excludes != null && excludes.length != 0 && warnConflictList.size() > 0) {
            warnConflictList = filter(warnConflictList, excludes);
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("filter warnConflictList : " + warnConflictList.size());
            }
        }

        System.out.println("");
        System.out.println("*****************************************");
        System.out.println("conflict output begin");
        System.out.println("");
        System.out.println("");
        System.out.println("--------------------------");
        System.out.println("");

        ConflictLevel level = ConflictLevel.ok;
        if (fatalConflictList.size() > 0) {
            level = ConflictLevel.fatal;
        } else {
            if (errorConflictList.size() > 0) {
                level = ConflictLevel.error;
            } else if(warnConflictList.size() > 0){
                level = ConflictLevel.warn;
            }
        }

        print(fatalConflictList);
        print(errorConflictList);
        print(warnConflictList);

        System.out.println("");
        System.out.println("conflict output end");
        System.out.println("*****************************************");
        System.out.println("");

        ProcessConflictResult result = new ProcessConflictResult();
        result.setLevel(level);
        result.setWarnLevelCount(warnConflictList.size());
        result.setErrorLevelCount(errorConflictList.size());
        result.setFatalLevelCount(fatalConflictList.size());

        return result;

    }

    private List<Conflict> filter(List<Conflict> warnConflictList, String[] excludes) {
        List<Conflict> newConflictlist = new ArrayList<Conflict>();
        boolean isFilter;
        for (Conflict conflict : warnConflictList) {
            isFilter = false;
            for (String exclude : excludes) {
                if (conflict.getId().equals(exclude)) {
                    isFilter = true;
                    break;
                }
            }

            if (!isFilter) {
                newConflictlist.add(conflict);
            }
        }
        return newConflictlist;

    }

    private void print(List<Conflict> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Conflict conflict : list) {
            List<Jar> jarList = conflict.getJarList();
            System.out.println(conflict.getDesc());
            System.out.println("Id     : " + conflict.getId());
            System.out.println("Level  : " + conflict.getLevel());
            System.out.println("Count  : " + conflict.getFinalConflictcount());
            System.out.println("Detail : ");
            for (Jar jar : jarList) {
                String artifactStr = "";
                if (jar.getArtifact().getArtifact() != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("         ");
                    sb.append(jar.getArtifact().getArtifact().toString());
                    artifactStr = sb.toString();
                }
                if (!StringUtils.isBlank(artifactStr)) {
                    artifactStr += " >> ";
                } else {
                    artifactStr += "         ";
                }
                artifactStr += jar.getJarFileName() + " (" + jar.getClassList().size() + ")  ";
                System.out.println(artifactStr);
                System.out.println("               -> " + jar.getJarFilePath());
                System.out.println();
            }
            System.out.println("");
            System.out.println("--------------------------");
            System.out.println("");
        }
    }
}

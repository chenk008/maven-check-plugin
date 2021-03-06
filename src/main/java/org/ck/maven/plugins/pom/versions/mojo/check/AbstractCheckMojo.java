package org.ck.maven.plugins.pom.versions.mojo.check;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.ck.maven.plugins.pom.versions.model.check.CheckConflictResult;
import org.ck.maven.plugins.pom.versions.model.check.ConflictLevel;
import org.ck.maven.plugins.pom.versions.model.check.MyArtifact;
import org.ck.maven.plugins.pom.versions.model.check.ProcessConflictResult;
import org.ck.maven.plugins.pom.versions.service.check.CheckConflictService;
import org.ck.maven.plugins.pom.versions.service.check.ProcessConflictService;

/**
 * 检查包冲突
 * 
 */
public abstract class AbstractCheckMojo extends AbstractMojo {
    /**
     * 
     * @parameter expression="${project}"
     * @readonly
     * @required
     * 
     */
    protected MavenProject project;
    /**
     * level: warn, error, fatal
     * 
     * @parameter expression="${level}" default-value="fatal"
     */
    private String level;
    /**
     * exclude id list
     * 
     * @parameter alias="excludes"
     */
    private String[] excludes;
    /**
     * @parameter expression="${target}" default-value="target/"
     */
    protected String target;
    /**
     * isDeleteFailurePackage 如果发生冲突，是否要删除生成的包
     * 
     * @parameter expression="${isDeleteFailurePackage}" default-value="false"
     * 
     */
    private boolean isDeleteFailurePackage;
    /**
     * isCreateDetailDoc
     * 
     * @parameter expression="${isCreateDetailDoc}" default-value="false"
     */
    private boolean isCreateDetailDoc;
    /**
     * Found conflict whether to terminate packaging  如果发生冲突，是否要中断打包
     * 
     * @parameter expression="${isTerminatePackaging}" default-value="false"
     */
    private boolean isTerminatePackaging;
    /**
     * @component
     */
    private CheckConflictService checkConflictService;
    /**
     * @component
     */
    private ProcessConflictService processConflictService;

    public void execute() throws MojoExecutionException, MojoFailureException {
        boolean isRun = customMethod();
        if (!isRun) {
            return;
        }
        if (StringUtils.isBlank(level)) {
            level = "fatal";
        }

        this.getLog().info("---------------------------");
        this.getLog().info("level : " + level);
        this.getLog().info("isTerminatePackaging : " + isTerminatePackaging);
        this.getLog().info("isDeleteFailurePackage : " + isDeleteFailurePackage);
        this.getLog().info("---------------------------");

        if (excludes != null && excludes.length != 0) {
            for (String exclude : excludes) {
                this.getLog().info("exclude : " + exclude);
            }
        }
        Set<MyArtifact> artifacts = queryArtifacts();
        CheckConflictResult r = null;
        try {
            r = checkConflictService.searchArtifacts(artifacts);
        } catch (Exception e) {
            throw new MojoExecutionException("plugin error.", e);
        }
        if (r == null) {
            this.getLog().info("Scan the dependencies not found conflict.");
        } else {
            ProcessConflictResult result = processConflictService.printResult(r, isCreateDetailDoc, excludes);
            ConflictLevel conflictLevel = result.getLevel();
            boolean isThrow = false;

            int value = conflictLevel.getValue();
            if ("warn".equals(level) && value >= ConflictLevel.warn.getValue()) {
                isThrow = true;
            } else if ("error".equals(level) && value >= ConflictLevel.error.getValue()) {
                isThrow = true;
            } else if ("fatal".equals(level) && value >= ConflictLevel.fatal.getValue()) {
                isThrow = true;
            } else {
                isThrow = false;
            }

            this.getLog().info(
                    "[pv*-" + conflictLevel + "] : Scan the dependencies found conflict. ( "
                            + (result.getFatalLevelCount() + result.getErrorLevelCount() + result.getWarnLevelCount())
                            + " )");
            this.getLog().info("fatal level : " + result.getFatalLevelCount());
            this.getLog().info("error level : " + result.getErrorLevelCount());
            this.getLog().info("warn  level : " + result.getWarnLevelCount());

            if (isThrow) {
                if (isTerminatePackaging) {
                    if (isDeleteFailurePackage) {
                        deletePackage();
                        this.getLog().info("delete target package.");
                    }
                    throw new MojoFailureException("[ " + project.getArtifactId() + " ]"
                            + " Note: Scan the dependencies found conflict.");
                }
            }
        }

    }

    /**
     * 得到所有的atrifact
     * @return
     * @throws MojoExecutionException
     */
    protected abstract Set<MyArtifact> queryArtifacts() throws MojoExecutionException;

    /**
     * 判断是否需要执行检查
     * @return
     * @throws MojoExecutionException
     */
    protected abstract boolean customMethod() throws MojoExecutionException;

    protected abstract void deletePackage() throws MojoExecutionException;

    public void setExcludes(String[] excludes) {
        this.excludes = excludes;
    }

}

package com.github.donalddu.hotfix


import org.apache.commons.io.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class HotFixPlugin implements Plugin<Project> {
    private Project project = null

    @Override
    void apply(Project project) {
        this.project = project
        project.extensions.create("hotFixUploader", UploaderSetting)
        createUpdateLogFile()
        if (project.android.hasProperty("applicationVariants")) {
            project.android.applicationVariants.all { variant ->
                String variantName = variant.name.capitalize()
                createUploadApkTask(variant).dependsOn project.tasks["assemble${variantName}"]
                createUploadHotfixTask(variant).dependsOn project.tasks["assemble${variantName}"]
                createGenHotfixTask(variant)
            }
        }
    }

    private Task createUploadHotfixTask(Object variant) {
        String variantName = variant.name.capitalize()
        Task task = project.tasks.create("upload${variantName}Hotfix").doLast {
            File apkFile = variant.outputs[0].outputFile
            File patch = HotFixPatch.genHotFixPatch(apkFile, variant.applicationId, variant.versionCode)
            println 'HotFixPatch: ' + patch.name
            startScript(variant, patch)
        }
        task.group = 'hotFix'
        return task
    }

    private Task createUploadApkTask(Object variant) {
        String variantName = variant.name.capitalize()
        Task task = project.tasks.create("upload${variantName}RawApk").doLast {
            File apkFile = variant.outputs[0].outputFile
            startScript(variant, apkFile)
        }
        task.group = 'hotFix'
        return task
    }

    private Task createGenHotfixTask(Object variant) {
        String variantName = variant.name.capitalize()
        Task task = project.tasks.create("gen${variantName}Hotfix").doLast {
            File apkFile = variant.outputs[0].outputFile
            if (apkFile.exists()) {
                File patch = HotFixPatch.genHotFixPatch(apkFile, variant.applicationId, variant.versionCode)
                println 'HotFixPatch: ' + patch.name
            } else println '****************** apkFile is not exists ******************'
        }
        task.group = 'hotFix'
        return task
    }

    private void startScript(Object variant, File apkFile) {
        BatParams params = new BatParams(variant.applicationId, variant.versionName, variant.versionCode, isDebug(variant))
        params.apkFilePath = apkFile.absolutePath
        params.updateLog = getUpdateLog()
        CmdUtil.excuteCMD(new File(setting.postmanScriptPath), params, setting.extras)
    }

    private void createUpdateLogFile() {
        def setting = getSetting()
        if (setting.enable && setting.updateLogFileName != null) {
            def logFile = new File(project.projectDir, setting.updateLogFileName)
            if (!logFile.exists()) logFile.createNewFile()
        }
    }

    private String getUpdateLog() {
        def setting = getSetting()
        if (setting.updateLogFileName != null) {
            def logFile = new File(project.projectDir, setting.updateLogFileName)
            def log = FileUtils.readFileToString(logFile, setting.updateLogFileEncoding)
            if (log.length() == 0) log = "empty log"
            return log
        } else {
            return null
        }
    }

    private static boolean isDebug(Object variant) {
        return variant.name.contains("debug")
    }

    UploaderSetting getSetting() {
        return project.hotFixUploader
    }
}

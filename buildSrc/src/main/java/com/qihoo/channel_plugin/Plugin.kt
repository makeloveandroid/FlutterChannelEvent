package com.qihoo.channel_plugin


import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class CorePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // 1.只能是一个 AppPlugin 插件的model,才可以使用此插件
        var isApp = project.plugins.hasPlugin(AppPlugin::class.java)
        if (isApp) {
            // 注册一个Transform
            var android = project.extensions.getByType(AppExtension::class.java)
            // 自定义 RegisterTransform
            var transformImpl = RegisterTransform(project)
            // 注册
            android.registerTransform(transformImpl)

        }
    }
}
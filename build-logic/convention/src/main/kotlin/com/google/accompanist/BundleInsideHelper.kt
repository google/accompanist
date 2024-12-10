/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.accompanist

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.Transformer
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext
import org.apache.tools.zip.ZipOutputStream
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Usage
import org.gradle.api.file.FileTreeElement
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

/**
 * Originally from https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:buildSrc/public/src/main/kotlin/androidx/build/BundleInsideHelper.kt
 * Small modifications based on gradle version
 */

/** Allow java and Android libraries to bundle other projects inside the project jar/aar. */
object BundleInsideHelper {
    val CONFIGURATION_NAME = "bundleInside"
    val REPACKAGE_TASK_NAME = "repackageBundledJars"

    /**
     * Creates a configuration for the users to use that will be used to bundle these dependency
     * jars inside of libs/ directory inside of the aar.
     *
     * ```
     * dependencies {
     *   bundleInside(project(":foo"))
     * }
     * ```
     *
     * Used project are expected
     *
     * @param relocations a list of package relocations to apply
     * @param dropResourcesWithSuffix used to drop Java resources if they match this suffix, null
     *   means no filtering
     * @receiver the project that should bundle jars specified by this configuration
     * @see forInsideAar(String, String)
     */
    @JvmStatic
    fun Project.forInsideAar(relocations: List<Relocation>?, dropResourcesWithSuffix: String?) {
        val bundle = createBundleConfiguration()
        val repackage = configureRepackageTaskForType(relocations, bundle, dropResourcesWithSuffix)
        // Add to AGP's configuration so this jar get packaged inside of the aar.
        dependencies.add("implementation", files(repackage.flatMap { it.archiveFile }))
    }

    /**
     * Creates 3 configurations for the users to use that will be used bundle these dependency jars
     * inside of libs/ directory inside of the aar.
     *
     * ```
     * dependencies {
     *   bundleInside(project(":foo"))
     * }
     * ```
     *
     * Used project are expected
     *
     * @param from specifies from which package the rename should happen
     * @param to specifies to which package to put the renamed classes
     * @param dropResourcesWithSuffix used to drop Java resources if they match this suffix, null
     *   means no filtering
     * @receiver the project that should bundle jars specified by these configurations
     */
    @JvmStatic
    fun Project.forInsideAar(from: String, to: String, dropResourcesWithSuffix: String?) {
        forInsideAar(listOf(Relocation(from, to)), dropResourcesWithSuffix)
    }

    /**
     * Creates a configuration for users to use that will bundle the dependency jars inside of this
     * lint check's jar. This is required because lintPublish does not currently support
     * dependencies, so instead we need to bundle any dependencies with the lint jar manually.
     * (b/182319899)
     *
     * ```
     * dependencies {
     *     if (rootProject.hasProperty("android.injected.invoked.from.ide")) {
     *         compileOnly(LINT_API_LATEST)
     *     } else {
     *         compileOnly(LINT_API_MIN)
     *     }
     *     compileOnly(KOTLIN_STDLIB)
     *     // Include this library inside the resulting lint jar
     *     bundleInside(project(":foo-lint-utils"))
     * }
     * ```
     *
     * @receiver the project that should bundle jars specified by these configurations
     */
    @JvmStatic
    fun Project.forInsideLintJar(): Configuration {
        val bundle = createBundleConfiguration()
        val compileOnly = configurations.getByName("compileOnly")
        val testImplementation = configurations.getByName("testImplementation")

        compileOnly.extendsFrom(bundle)
        testImplementation.extendsFrom(bundle)

        // Relocation needed to avoid classpath conflicts with Android Studio (b/337980250)
        // Can be removed if we migrate from using kotlin-metadata-jvm inside of lint checks
        val relocations = listOf(Relocation("kotlin.metadata", "androidx.lint.kotlin.metadata"))
        val repackage = configureRepackageTaskForType(relocations, bundle, null)
        val sourceSets = extensions.getByType(SourceSetContainer::class.java)
        repackage.configure {
            this.from(sourceSets.findByName("main")?.output)
            // kotlin-metadata-jvm has a service descriptor that needs transformation
            this.mergeServiceFiles()
            // Exclude Kotlin metadata files from kotlin-metadata-jvm
            this.exclude(
                "META-INF/kotlin-metadata-jvm.kotlin_module",
                "META-INF/kotlin-metadata.kotlin_module",
                "META-INF/metadata.jvm.kotlin_module",
                "META-INF/metadata.kotlin_module"
            )
        }

        listOf("apiElements", "runtimeElements").forEach { config ->
            configurations.getByName(config).apply {
                outgoing.artifacts.clear()
                outgoing.artifact(repackage)
            }
        }

        return bundle
    }

    data class Relocation(val from: String, val to: String)

    private fun Project.configureRepackageTaskForType(
        relocations: List<Relocation>?,
        configuration: Configuration,
        dropResourcesWithSuffix: String?
    ): TaskProvider<ShadowJar> {
        val action = Action<ShadowJar> {
            configurations = listOf(configuration)
            if (relocations != null) {
                for (relocation in relocations) {
                    println("Relocating ${relocation.from} to ${relocation.to}")
                    relocate(relocation.from, relocation.to)
                }
            }
            val dontIncludeResourceTransformer = DontIncludeResourceTransformer()
            dontIncludeResourceTransformer.dropResourcesWithSuffix = dropResourcesWithSuffix
            transformers.add(dontIncludeResourceTransformer)
            archiveBaseName.set("repackaged")
            archiveVersion.set("")
            destinationDirectory.set(layout.buildDirectory.dir("repackaged"))
        }
        return tasks.register(REPACKAGE_TASK_NAME, ShadowJar::class.java, action)
    }

    private fun Project.createBundleConfiguration(): Configuration {
        val bundle =
            configurations.create(CONFIGURATION_NAME) {
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, objects.named<Usage>(Usage.JAVA_RUNTIME))
                }
                isCanBeConsumed = false
            }
        return bundle
    }

    class DontIncludeResourceTransformer : Transformer {
        @Optional @Input var dropResourcesWithSuffix: String? = null

        override fun getName(): String {
            return "DontIncludeResourceTransformer"
        }

        override fun canTransformResource(element: FileTreeElement?): Boolean {
            val path = element?.relativePath?.pathString
            return dropResourcesWithSuffix != null &&
                (path?.endsWith(dropResourcesWithSuffix!!) == true)
        }

        override fun transform(context: TransformerContext?) {
            // no op
        }

        override fun hasTransformedResource(): Boolean {
            return true
        }

        override fun modifyOutputStream(zipOutputStream: ZipOutputStream?, b: Boolean) {
            // no op
        }
    }
}

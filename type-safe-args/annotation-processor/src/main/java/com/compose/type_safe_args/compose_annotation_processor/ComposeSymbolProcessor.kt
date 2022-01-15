package com.compose.type_safe_args.compose_annotation_processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate

class ComposeSymbolProcessor(
    private val options: Map<String, String>,
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation("com.compose.type_safe_args.annotation.ComposeDestination")
            .filterIsInstance<KSClassDeclaration>()

        if (!symbols.iterator().hasNext()) return emptyList()

        val argumentProviders = resolver
            .getSymbolsWithAnnotation("com.compose.type_safe_args.annotation.ArgumentProvider")
            .filterIsInstance<KSClassDeclaration>()

        val argumentProviderMap = mutableMapOf<KSClassDeclaration, KSClassDeclaration>()
        argumentProviders.forEach { argumentProvider ->
            symbols.forEach { composeDestination ->
                if (argumentProvider.superTypes.map { it.toString() }
                        .contains("I${composeDestination.simpleName.asString()}Provider")) {
                    argumentProviderMap[composeDestination] = argumentProvider
                }
            }
        }

        symbols.forEach {
            val packageName = it.packageName.asString()

            val file = codeGenerator.createNewFile(
                dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray()),
                packageName = packageName,
                fileName = it.simpleName.asString()
            )
            tabs = 0

            val propertyMap = getPropertyMap(it.getAllProperties(), logger, resolver) ?: run {
                logger.error("invalid argument found")
                return@forEach
            }

            var singletonClass: KSClassDeclaration? = null
            it.declarations.forEach {
                if (it is KSClassDeclaration && it.classKind == ClassKind.OBJECT) {
                    singletonClass = it
                }
            }

            file addLine "package $packageName"
            file addLine "import androidx.navigation.*"
            file addLine "import android.net.Uri"
            file addLine "import android.os.Bundle"
            file addLine "import com.google.gson.reflect.TypeToken"
            file addLine "import com.compose.type_safe_args.annotation.*"
            addImports(file, propertyMap.values)
            argumentProviderMap[it]?.qualifiedName?.asString()?.let {
                file addLine "import $it"
            }
            if (argumentProviderMap[it]?.qualifiedName != singletonClass?.qualifiedName) {
                singletonClass?.qualifiedName?.asString()?.let {
                    file addLine "import $it"
                }
            }
            file addLine ""

            it.accept(DefaultArgumentVisitor(file, resolver, logger, options, propertyMap), Unit)
            it.accept(NavTypeVisitor(file, resolver, logger, options, propertyMap), Unit)
            it.accept(
                ComposeDestinationVisitor(
                    file,
                    resolver,
                    logger,
                    options,
                    argumentProviderMap,
                    propertyMap,
                    singletonClass
                ), Unit
            )
            file.close()
        }

        return emptyList()
    }
}

package com.compose.type_safe_args.compose_annotation_processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Nullability
import java.io.OutputStream

fun getPropertyMap(
    properties: Sequence<KSPropertyDeclaration>,
    logger: KSPLogger,
    resolver: Resolver
): Map<KSPropertyDeclaration, PropertyInfo>? {
    val propertyMap = mutableMapOf<KSPropertyDeclaration, PropertyInfo>()
    properties.forEach { property ->
        val resolvedType = property.type.resolve()
        val resolvedClassDeclarationName = resolver.getClassDeclarationByName(
            resolvedType.declaration.qualifiedName ?: run {
                logger.error("Invalid type argument", property)
                return null
            })
            ?.toString() ?: ""
        val resolvedClassQualifiedName = (resolvedType.declaration.qualifiedName?.asString()
            ?: run {
                logger.error("Invalid type argument", property)
                return null
            })
        val typeArguments = property.type.element?.typeArguments ?: emptyList()
        propertyMap[property] = PropertyInfo(
            propertyName = property.simpleName.asString(),
            resolvedType = resolvedType,
            resolvedClassDeclarationName = resolvedClassDeclarationName,
            resolvedClassQualifiedName = resolvedClassQualifiedName,
            typeArguments = typeArguments,
            isNullable = resolvedType.nullability == Nullability.NULLABLE,
            composeArgumentType = when (resolvedClassDeclarationName) {
                "Boolean" -> ComposeArgumentType.BOOLEAN
                "String" -> ComposeArgumentType.STRING
                "Float" -> ComposeArgumentType.FLOAT
                "Int" -> ComposeArgumentType.INT
                "Long" -> ComposeArgumentType.LONG
                "IntArray" -> ComposeArgumentType.INT_ARRAY
                "BooleanArray" -> ComposeArgumentType.BOOLEAN_ARRAY
                "LongArray" -> ComposeArgumentType.LONG_ARRAY
                "FloatArray" -> ComposeArgumentType.FLOAT_ARRAY
                else -> when {
                    resolvedClassQualifiedName == "kotlin.collections.ArrayList" -> {
                        var isParcelable = false
                        var isSerializable = false
                        for (argument in typeArguments) {
                            val resolvedArgument = argument.type?.resolve()
                            if ((resolvedArgument?.declaration as? KSClassDeclaration)?.superTypes?.map { it.toString() }
                                    ?.contains("Parcelable") == true) {
                                isParcelable = true
                            }
                            if ((resolvedArgument?.declaration as? KSClassDeclaration)?.superTypes?.map { it.toString() }
                                    ?.contains("Serializable") == true) {
                                isSerializable = true
                            }
                        }
                        if (isParcelable) {
                            ComposeArgumentType.PARCELABLE_ARRAY
                        } else if (isSerializable) {
                            ComposeArgumentType.SERIALIZABLE
                        } else {
                            logger.error(
                                "invalid property type, cannot pass it in bundle",
                                property
                            )
                            return null
                        }
                    }
                    (resolvedType.declaration as KSClassDeclaration).superTypes.map { it.toString() }
                        .contains("Parcelable") -> {
                        ComposeArgumentType.PARCELABLE
                    }
                    (resolvedType.declaration as KSClassDeclaration).superTypes.map { it.toString() }
                        .contains("Serializable") -> {
                        ComposeArgumentType.SERIALIZABLE
                    }
                    else -> {
                        logger.error(
                            "invalid property type, cannot pass it in bundle",
                            property
                        )
                        return null
                    }
                }
            },
            hasDefaultValue = property.annotations.map { it.shortName.asString() }.any { it == "HasDefaultValue" }
        )
    }
    return propertyMap
}

var tabs = 0

infix fun OutputStream.addLine(line: String) {
    this.write("\n".toByteArray())
    repeat(tabs) {
        this.write("\t".toByteArray())
    }
    this.write(line.toByteArray())
}

infix fun OutputStream.addPhrase(line: String) {
    this.write(line.toByteArray())
}

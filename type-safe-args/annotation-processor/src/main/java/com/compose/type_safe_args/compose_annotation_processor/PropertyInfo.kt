package com.compose.type_safe_args.compose_annotation_processor

import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument

data class PropertyInfo(
    val propertyName: String,
    val resolvedType: KSType,
    val resolvedClassDeclarationName: String,
    val resolvedClassQualifiedName: String,
    val resolvedClassSimpleName: String,
    val typeArguments: List<KSTypeArgument>,
    val isNullable: Boolean,
    val composeArgumentType: ComposeArgumentType,
    val hasDefaultValue: Boolean
)

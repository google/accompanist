package com.compose.type_safe_args.compose_annotation_processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Nullability
import com.google.devtools.ksp.symbol.Variance
import java.io.OutputStream

class NavTypeVisitor(private val file: OutputStream, private val resolver: Resolver, private val logger: KSPLogger, private val options: Map<String, String>, ) :
    KSVisitorVoid() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val properties: Sequence<KSPropertyDeclaration> = classDeclaration.getAllProperties()

        val route = classDeclaration.simpleName.asString()

        val propertyMap = getPropertyMap(properties, logger, resolver) ?: run {
            logger.error("invalid argument found")
            return
        }
        properties.forEach { property ->
            val propertyInfo = propertyMap[property] ?: run {
                logger.error("invalid argument found")
                return
            }
            if (!(propertyInfo.composeArgumentType == ComposeArgumentType.PARCELABLE ||
                    propertyInfo.composeArgumentType == ComposeArgumentType.PARCELABLE_ARRAY ||
                    propertyInfo.composeArgumentType == ComposeArgumentType.SERIALIZABLE

            )) {
                return@forEach
            }

            file addLine "val ${route}_${propertyInfo.propertyName.replaceFirstChar { it.uppercase() }}NavType: NavType<"
            addVariableType(file, propertyInfo)

            file addPhrase "> = object : NavType<"
            addVariableType(file, propertyInfo)
            file addPhrase ">(${propertyInfo.isNullable}) {"
            tabs++

            file addLine "override val name: String"
            tabs++
            file addLine "get() = "
            file addPhrase "\"${propertyInfo.propertyName}\""
            tabs--

            file addLine "override fun get(bundle: Bundle, key: String): "
            addVariableType(file, propertyInfo)
            file addPhrase " {"
            tabs++

            when (propertyInfo.composeArgumentType) {
                ComposeArgumentType.PARCELABLE -> {
                    file addLine "return bundle.getParcelable(key)"
                    if (!propertyInfo.isNullable) {
                        file addPhrase "!!"
                    }
                }
                ComposeArgumentType.PARCELABLE_ARRAY -> {
                    file addLine "return bundle.getParcelableArrayList(key)"
                    if (!propertyInfo.isNullable) {
                        file addPhrase "!!"
                    }
                }
                ComposeArgumentType.SERIALIZABLE -> {
                    file addLine "return bundle.getSerializable(key)"
                    file addPhrase " as"
                    if (propertyInfo.isNullable) {
                        file addPhrase "?"
                    }
                    file addPhrase " "
                    addVariableType(file, propertyInfo)
                }
                ComposeArgumentType.INT -> {}
                ComposeArgumentType.LONG -> {}
                ComposeArgumentType.FLOAT -> {}
                ComposeArgumentType.BOOLEAN -> {}
                ComposeArgumentType.STRING -> {}
                ComposeArgumentType.INT_ARRAY -> {}
                ComposeArgumentType.LONG_ARRAY -> {}
                ComposeArgumentType.FLOAT_ARRAY -> {}
                ComposeArgumentType.BOOLEAN_ARRAY -> {}
            }

            tabs--
            file addLine "}"

            file addLine "override fun parseValue(value: String): "
            addVariableType(file, propertyInfo)
            file addPhrase " {"
            tabs++
            file addLine "return gson.fromJson(value, object : TypeToken<"
            addVariableType(file, propertyInfo)
            file addPhrase ">() {}.type)"
            tabs--
            file addLine "}"

            file addLine "override fun put(bundle: Bundle, key: String, value: "
            addVariableType(file, propertyInfo)
            file addPhrase ") {"
            tabs++

            when (propertyInfo.composeArgumentType) {
                ComposeArgumentType.PARCELABLE -> file addLine "bundle.putParcelable(key, value)"
                ComposeArgumentType.PARCELABLE_ARRAY -> file addLine "bundle.putParcelableArrayList(key, value)"
                ComposeArgumentType.SERIALIZABLE -> file addLine "bundle.putSerializable(key, value)"
                ComposeArgumentType.INT -> {}
                ComposeArgumentType.LONG -> {}
                ComposeArgumentType.FLOAT -> {}
                ComposeArgumentType.BOOLEAN -> {}
                ComposeArgumentType.STRING -> {}
                ComposeArgumentType.INT_ARRAY -> {}
                ComposeArgumentType.LONG_ARRAY -> {}
                ComposeArgumentType.FLOAT_ARRAY -> {}
                ComposeArgumentType.BOOLEAN_ARRAY -> {}
            }

            tabs--
            file addLine "}"

            tabs--
            file addLine "}"
        }
    }

    private fun addVariableType(file: OutputStream, propertyInfo: PropertyInfo) {
        file addPhrase propertyInfo.resolvedClassQualifiedName
        visitChildTypeArguments(propertyInfo.typeArguments)
        file addPhrase if (propertyInfo.isNullable) "?" else ""
    }

    private fun visitChildTypeArguments(typeArguments: List<KSTypeArgument>) {
        if (typeArguments.isNotEmpty()) {
            file addPhrase "<"
            typeArguments.forEachIndexed { i, arg ->
                visitTypeArgument(arg, data = Unit)
                if (i < typeArguments.lastIndex) file addLine ", "
            }
            file addPhrase ">"
        }
    }

    override fun visitTypeArgument(typeArgument: KSTypeArgument, data: Unit) {
        if (options["ignoreGenericArgs"] == "true") {
            file addPhrase "*"
            return
        }

        when (val variance: Variance = typeArgument.variance) {
            Variance.STAR -> {
                file addPhrase "*"
                return
            }
            Variance.COVARIANT, Variance.CONTRAVARIANT -> {
                file addPhrase variance.label
                file addPhrase " "
            }
            Variance.INVARIANT -> {
                // do nothing
            }
        }
        val resolvedType: KSType? = typeArgument.type?.resolve()
        file addPhrase (resolvedType?.declaration?.qualifiedName?.asString() ?: run {
            logger.error("Invalid type argument", typeArgument)
            return
        })
        file addPhrase if (resolvedType.nullability == Nullability.NULLABLE) "?" else ""

        val genericArguments: List<KSTypeArgument> =
            typeArgument.type?.element?.typeArguments ?: emptyList()
        visitChildTypeArguments(genericArguments)
    }
}
package com.compose.type_safe_args.annotation

import android.os.Bundle
import androidx.navigation.NavType
import com.google.gson.Gson

val gson = Gson()

val BoolArrayType: NavType<BooleanArray?> = object : NavType<BooleanArray?>(true) {
    override val name: String
        get() = "boolean[]"

    override fun put(bundle: Bundle, key: String, value: BooleanArray?) {
        bundle.putBooleanArray(key, value)
    }

    override fun get(bundle: Bundle, key: String): BooleanArray? {
        return bundle[key] as BooleanArray?
    }

    override fun parseValue(value: String): BooleanArray {
        return gson.fromJson(value, BooleanArray::class.java)
    }
}

val FloatArrayType: NavType<FloatArray?> = object : NavType<FloatArray?>(true) {
    override val name: String
        get() = "float[]"

    override fun put(bundle: Bundle, key: String, value: FloatArray?) {
        bundle.putFloatArray(key, value)
    }

    override fun get(bundle: Bundle, key: String): FloatArray? {
        return bundle[key] as FloatArray?
    }

    override fun parseValue(value: String): FloatArray {
        return gson.fromJson(value, FloatArray::class.java)
    }
}

val IntArrayType: NavType<IntArray?> = object : NavType<IntArray?>(true) {
    override val name: String
        get() = "integer[]"

    override fun put(bundle: Bundle, key: String, value: IntArray?) {
        bundle.putIntArray(key, value)
    }

    override fun get(bundle: Bundle, key: String): IntArray? {
        return bundle[key] as IntArray?
    }

    override fun parseValue(value: String): IntArray {
        return gson.fromJson(value, IntArray::class.java)
    }
}

val LongArrayType: NavType<LongArray?> = object : NavType<LongArray?>(true) {
    override val name: String
        get() = "long[]"

    override fun put(bundle: Bundle, key: String, value: LongArray?) {
        bundle.putLongArray(key, value)
    }

    override fun get(bundle: Bundle, key: String): LongArray? {
        return bundle[key] as LongArray?
    }

    override fun parseValue(value: String): LongArray {
        return gson.fromJson(value, LongArray::class.java)
    }
}

val StringArrayType: NavType<Array<String>?> = object : NavType<Array<String>?>(true) {
    override val name: String
        get() = "string[]"

    override fun put(bundle: Bundle, key: String, value: Array<String>?) {
        bundle.putStringArray(key, value)
    }

    @Suppress("UNCHECKED_CAST")
    override fun get(bundle: Bundle, key: String): Array<String>? {
        return bundle[key] as Array<String>?
    }

    override fun parseValue(value: String): Array<String>? {
        return gson.fromJson(value, Array<String>::class.java)
    }
}

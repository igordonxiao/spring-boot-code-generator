package io.github.igordonxiao.codegen

import java.io.File

/**
 * 将字符串对应的文件夹地址创建为文件夹
 */
fun String.toDirs() = File(this).apply { if (!this.exists()) this.mkdirs() }

/**
 * 将字符串对应的文件地址生成文件并写入内容
 */
fun String.toFile(context: String = "") = File(this).apply { writeText(context) }

/**
 * 首字母大写
 */
fun String.beginWithUpperCase(): String {
    return when (this.length) {
        0 -> ""
        1 -> this.toUpperCase()
        else -> this[0].toUpperCase() + this.substring(1)
    }
}

/**
 * 转换为驼峰形式
 */
fun String.toCamelCase(): String {
    return this.split('_').map {
        it.beginWithUpperCase()
    }.joinToString("")
}

/**
 *  首字母小写
 */
fun String.beginWithLowerCase(): String {
    return when (this.length) {
        0 -> ""
        1 -> this.toUpperCase()
        else -> this[0].toLowerCase() + this.substring(1)
    }
}
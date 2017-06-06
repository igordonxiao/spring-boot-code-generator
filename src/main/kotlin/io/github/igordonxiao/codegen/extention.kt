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
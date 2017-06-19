package io.github.igordonxiao.codegen

/**
 * 数据表
 */
data class Ptable(val schema: String, val name: String)

/**
 * 数据列
 */
data class Pcolumn(val name: String, val type: String, val comment: String?)


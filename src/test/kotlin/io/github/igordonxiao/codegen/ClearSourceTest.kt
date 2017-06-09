package io.github.igordonxiao.codegen

import io.github.igordonxiao.DESTINATION_PARENT_DIR

fun main(args: Array<String>) {
    Runtime.getRuntime().exec("rm -rf ${DESTINATION_PARENT_DIR}")
}


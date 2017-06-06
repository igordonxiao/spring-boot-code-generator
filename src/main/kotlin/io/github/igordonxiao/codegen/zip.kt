package io.github.igordonxiao.codegen

import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.CRC32
import java.util.zip.CheckedOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


val EXT = ".zip"
private val BASE_DIR = ""

private val PATH = "/"
private val BUFFER = 1024

/**
 * @param srcFile
 * *
 * @throws Exception
 */
@Throws(Exception::class)
fun compress(srcFile: File) {
    val name = srcFile.getName()
    val basePath = srcFile.parent + "/"
    val destPath = basePath + name + EXT
    compress(srcFile, destPath)
}

/**
 * @param srcFile
 * @param destPath
 * @throws Exception
 */
@Throws(Exception::class)
fun compress(srcFile: File, destFile: File) {
    val cos = CheckedOutputStream(FileOutputStream(
            destFile), CRC32())
    val zos = ZipOutputStream(cos)
    compress(srcFile, zos, BASE_DIR)
    zos.flush()
    zos.close()
}

/**
 * @param srcFile
 * *
 * @param destPath
 * *
 * @throws Exception
 */
@Throws(Exception::class)
fun compress(srcFile: File, destPath: String) {
    compress(srcFile, File(destPath))
}

/**
 * @param srcFile
 * @param zos
 * @param basePath
 * @throws Exception
 */
@Throws(Exception::class)
private fun compress(srcFile: File, zos: ZipOutputStream,
                     basePath: String) {
    if (srcFile.isDirectory()) {
        compressDir(srcFile, zos, basePath)
    } else {
        compressFile(srcFile, zos, basePath)
    }
}

/**
 * @param srcPath
 * @throws Exception
 */
@Throws(Exception::class)
fun compress(srcPath: String) {
    val srcFile = File(srcPath)
    compress(srcFile)
}

/**
 * @param srcPath
 * @param destPath
 */
@Throws(Exception::class)
fun compress(srcPath: String, destPath: String) {
    val srcFile = File(srcPath)
    compress(srcFile, destPath)
}

/**
 * @param dir
 * @param zos
 * @param basePath
 * @throws Exception
 */
@Throws(Exception::class)
private fun compressDir(dir: File, zos: ZipOutputStream,
                        basePath: String) {
    val files = dir.listFiles()
    if (files.isEmpty()) {
        val entry = ZipEntry(basePath + dir.getName() + PATH)
        zos.putNextEntry(entry)
        zos.closeEntry()
    }

    for (file in files) {
        compress(file, zos, basePath + dir.getName() + PATH)
    }
}

/**
 * @param file
 * @param zos
 * @param dir
 * @throws Exception
 */
@Throws(Exception::class)
private fun compressFile(file: File, zos: ZipOutputStream, dir: String) {
    val entry = ZipEntry(dir + file.getName())
    zos.putNextEntry(entry)
    val bis = BufferedInputStream(FileInputStream(
            file))
    var count: Int
    val data = ByteArray(BUFFER)
    count = bis.read(data, 0, BUFFER)
    while (count != -1) {
        zos.write(data, 0, count)
        count = bis.read(data, 0, BUFFER)
    }
    bis.close()
    zos.closeEntry()
}
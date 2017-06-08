package io.github.igordonxiao

import io.github.igordonxiao.codegen.Generator
import io.github.igordonxiao.codegen.compress
import spark.ModelAndView
import spark.Spark.*
import spark.template.thymeleaf.ThymeleafTemplateEngine
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * 压缩目标文件夹
 */
val DESTINATION_PARENT_DIR = "/Users/gordon/Desktop"
/**
 * 生成代码的目标文件夹
 */
val DESTINATION_DIR = "${DESTINATION_PARENT_DIR}/application/"

/**
 * 打包的ZIP文件名
 */
val ZIP_FILE_NAME = "application.zip"

/**
 * 打包的ZIP文件路径
 */
val ZIP_FILE_PATH = DESTINATION_PARENT_DIR + "/" + ZIP_FILE_NAME

/**
 * 运行主方法
 */
fun main(args: Array<String>) {
    port(8080)
    staticFileLocation("/static")
    File(DESTINATION_DIR).apply { if (!this.exists()) this.mkdirs() }

    get("/") { _, _ -> ThymeleafTemplateEngine().render(ModelAndView(HashMap<String, Any>(), "index")) }
    get("/notSupport") { _, _ -> ThymeleafTemplateEngine().render(ModelAndView(HashMap<String, Any>(), "notSupport")) }

    post("/gen", { req, res ->
        val params = req.queryMap()
        try {
            Runtime.getRuntime().exec("rm -rf ${DESTINATION_DIR}")
            Runtime.getRuntime().exec("rm -f ${ZIP_FILE_PATH}")
            val generator = Generator(DESTINATION_DIR, DB_IP = params["DB_IP"].value(), DB_PORT = params["DB_PORT"].value(), DB_NAME = params["DB_NAME"].value(), DB_USERNAME = params["DB_USERNAME"].value(), DB_PASSWORD = params["DB_PASSWORD"].value(), GROUP_ID = params["GROUP_ID"].value(), ARTIFACT_ID = params["ARTIFACT_ID"].value(), PROJECT_NAME = params["PROJECT_NAME"].value(), PROJECT_VERSION = params["PROJECT_VERSION"].value(), PROJECT_DESCRIPTION = params["PROJECT_DESCRIPTION"].value())

            when (params["LANGUAGE"].value().toUpperCase()) {
                "JAVA" -> generator.genJava()
                else -> res.redirect("/notSupport")
            }
            compress(DESTINATION_DIR)
            val path = Paths.get(ZIP_FILE_PATH)
            var data: ByteArray? = null
            try {
                data = Files.readAllBytes(path)
            } catch (e: Exception) {
                e.printStackTrace()
                e.message
            }
            val raw = res.raw()
            res.header("Content-Disposition", "attachment; filename=${ZIP_FILE_NAME}")
            res.type("application/force-download")
            try {
                raw.outputStream.write(data)
                raw.outputStream.flush()
                raw.outputStream.close()
                raw
            } catch (e: Exception) {
                e.printStackTrace()
                e.message
            }
        } catch (e: Exception) {
            e.message
        }
    })
}


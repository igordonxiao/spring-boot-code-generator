package io.github.igordonxiao

import io.github.igordonxiao.codegen.Generator
import io.github.igordonxiao.codegen.compress
import spark.ModelAndView
import spark.Spark.*
import spark.template.thymeleaf.ThymeleafTemplateEngine
import java.nio.file.Files
import java.nio.file.Paths

/**
 * 压缩目标文件夹
 */
val DESTINATION_PARENT_DIR = "/Users/gordon/Desktop"
/**
 * 生成代码的目标文件夹
 */
val DESTINATION_DIR = "${DESTINATION_PARENT_DIR}/gen/"

fun main(args: Array<String>) {
    port(8080)
    staticFileLocation("/static")

    get("/") { req, res ->
        val model = HashMap<String, Any>()
        ThymeleafTemplateEngine().render(ModelAndView(model, "index"))
    }

    post("/gen", { req, res ->
        val params = req.queryMap()
        try {
            Generator(DESTINATION_DIR, DB_IP = params["DB_IP"].value(), DB_PORT = params["DB_PORT"].value(), DB_NAME = params["DB_NAME"].value(), DB_USERNAME = params["DB_USERNAME"].value(), DB_PASSWORD = params["DB_PASSWORD"].value(), GROUP_ID = params["GROUP_ID"].value(), ARTIFACT_ID = params["ARTIFACT_ID"].value(), PROJECT_NAME = params["PROJECT_NAME"].value(), PROJECT_VERSION = params["PROJECT_VERSION"].value(), PROJECT_DESCRIPTION = params["PROJECT_DESCRIPTION"].value()).genProject()
            compress(DESTINATION_DIR)
            val path = Paths.get(DESTINATION_PARENT_DIR + "/gen.zip")
            var data: ByteArray? = null
            try {
                data = Files.readAllBytes(path)
            } catch (e1: Exception) {
                e1.printStackTrace()
            }
            val raw = res.raw()
            res.header("Content-Disposition", "attachment; filename=application.zip")
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

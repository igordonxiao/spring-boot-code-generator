package io.github.igordonxiao.codegen

import com.squareup.javapoet.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.*
import javax.lang.model.element.Modifier
import javax.persistence.Entity
import javax.persistence.GenerationType
import javax.persistence.Table

/**
 * 生成器
 */
class Generator(
        /**
         * 生成代码的目标文件夹
         */
        val DESTINATION_DIR: String = "/Users/gordon/Desktop/gen/",
        /**
         * 数据库IP
         */
        val DB_IP: String = "localhost",
        /**
         * 数据库端口
         */
        DB_PORT: String = "5432",
        /**
         * 数据库名称
         */
        DB_NAME: String = "springbootdemo",
        /**
         * 数据库用户名
         */
        val DB_USERNAME: String = "postgres",
        /**
         * 数据库用户密码
         */
        val DB_PASSWORD: String = "root",
        /**
         * 代码包名
         */
        val GROUP_ID: String = "com.demo",
        /**
         * 项目打包名称
         */
        val ARTIFACT_ID: String = "demo",
        /**
         * 项目名
         */
        val PROJECT_NAME: String = "demo project",

        /**
         * 项目版本号
         */
        val PROJECT_VERSION: String = "1.0.0",
        /**
         * 项目描述
         */
        val PROJECT_DESCRIPTION: String = "test demo") {

    /**
     * 数据库连接地址
     */
    val DB_URL = "jdbc:postgresql://${DB_IP}:${DB_PORT}/${DB_NAME}"

    /**
     * 数据库驱动
     */
    val DB_DRIVER: String = "org.postgresql.Driver"

    /**
     * maven标准文件结构
     */
    val SOURCE_DIRS = arrayOf<String>("src/main/java/", "src/main/resources/", "src/test/java/")

    /**
     * 代码文件夹
     */
    val CODE_BASE_DIR = (DESTINATION_DIR + SOURCE_DIRS[0] + GROUP_ID.replace(".", "/"))

    /**
     * 资源文件夹
     */
    val RESOURCE_DIR = (DESTINATION_DIR + SOURCE_DIRS[1])

    /**
     * 模型类文件夹
     */
    val MODEL_PACKAGE_DIR = CODE_BASE_DIR + "/model"

    /**
     * 模型文件包名
     */
    val MODEL_PACKAGE = GROUP_ID + ".model"

    /**
     * 数据库操作类文件夹
     */
    val DAO_PACKAGE_DIR = CODE_BASE_DIR + "/dao"

    /**
     * 数据库操作类包名
     */
    val DAO_PACKAGE = GROUP_ID + ".dao"

    /**
     * 服务类文件夹
     */
    val SERVICE_PACKAGE_DIR = CODE_BASE_DIR + "/service"

    /**
     * 服务类包名
     */
    val SERVICE_PACKAGE = GROUP_ID + ".service"

    /**
     * 控制类文件夹
     */
    val CONTROLLER_PACKAGE_DIR = CODE_BASE_DIR + "/controller"
    /**
     * 控制类包名
     */
    val CONTROLLER_PACKAGE = GROUP_ID + ".controller"

    /**
     * 生成Java项目
     */
    fun genJava() {
        makeDirs()
        genResourceFiles()
        genPackageDirs()
        genSpringBootApp()
        Database.connect(url = DB_URL, user = DB_USERNAME, password = DB_PASSWORD, driver = DB_DRIVER)
        dbTables().forEach {
            genModel(it, tableMeta(it.name))
            genDao(it)
            genService(it)
            genController(it)
        }
    }

    /**
     * 生成源代码文件夹
     */
    private fun makeDirs() = SOURCE_DIRS.forEach { (DESTINATION_DIR + it).toDirs() }

    /**
     * 生成各种项目资源文件
     */
    private fun genResourceFiles() {
        // 生成.gitignore文件
        (DESTINATION_DIR + ".gitignore").toFile("""target/
!.mvn/wrapper/maven-wrapper.jar

### STS ###
.apt_generated
.classpath
.factorypath
.project
.settings
.springBeans

### IntelliJ IDEA ###
.idea
*.iws
*.iml
*.ipr

### NetBeans ###
nbproject/private/
build/
nbbuild/
dist/
nbdist/
.nb-gradle/
""")
        // 生成POM文件
        (DESTINATION_DIR + "pom.xml").toFile("""<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>${GROUP_ID}</groupId>
    <artifactId>${ARTIFACT_ID}</artifactId>
    <version>${PROJECT_VERSION}</version>
    <packaging>jar</packaging>

    <name>${PROJECT_NAME}</name>
    <description>${PROJECT_DESCRIPTION}</description>

    <repositories>
        <repository>
            <id>aliyun</id>
            <name>aliyun Central Repo</name>
            <url>http://maven.aliyun.com/nexus/content/groups/static</url>
        </repository>
    </repositories>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>1.5.3.RELEASE</version>
        <relativePath/>
    </parent>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-static</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>

""")
        // 生成SpringBoot配置文件
        (RESOURCE_DIR + "application.yml").toFile("""
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    platform: POSTGRESQL
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
server:
  port: 8080

""")
    }

    /**
     * 生成Spring Boot 主运行文件
     */
    private fun genSpringBootApp() {
        (CODE_BASE_DIR + "/MainApplication.java").toFile("""
package ${GROUP_ID};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
static class MainApplication {
	static static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}
}

 """)
    }

    /**
     * 生成代码包文件名
     */
    private fun genPackageDirs() = arrayListOf<String>(MODEL_PACKAGE_DIR, DAO_PACKAGE_DIR, SERVICE_PACKAGE_DIR, CONTROLLER_PACKAGE_DIR).forEach { it.toDirs() }

    /**
     * 获取数据库所有的表名
     */
    private fun dbTables(): List<Ptable> {
        val tables = arrayListOf<Ptable>()
        transaction {
            val conn = TransactionManager.current().connection
            val statement = conn.createStatement()
            val query = "SELECT schemaname, tablename    FROM   pg_tables WHERE   tablename   NOT   LIKE   'pg%' AND tablename NOT LIKE 'sql_%'"
            val rs = statement.executeQuery(query)
            while (rs.next()) tables.add(Ptable(rs.getString(1), rs.getString(2)))
        }
        return tables
    }

    /**
     * 获取数据表元数据
     */
    private fun tableMeta(tableName: String): List<Pcolumn> {
        val columns = arrayListOf<Pcolumn>()
        transaction {
            val conn = TransactionManager.current().connection
            val statement = conn.createStatement()
            val query = "select column_name, data_type from information_schema.columns where table_name = '${tableName}'"
            val rs = statement.executeQuery(query)
            while (rs.next()) columns.add(Pcolumn(rs.getString(1), rs.getString(2)))
        }
        return columns
    }

    /**
     * id field
     */
    private fun idField(): FieldSpec {
        val generatedValueAnnotation = com.squareup.javapoet.AnnotationSpec.builder(javax.persistence.GeneratedValue::class.java).addMember("strategy", "\$T.AUTO", GenerationType::class.java).build()
        val idColumnAnnotation = com.squareup.javapoet.AnnotationSpec.builder(javax.persistence.Column::class.java).addMember("name", "\"id\"").build()
        return FieldSpec.builder(java.lang.Long::class.java, "id", Modifier.PRIVATE).addAnnotation(javax.persistence.Id::class.java).addAnnotation(generatedValueAnnotation).addAnnotation(idColumnAnnotation).build()
    }

    /**
     * 获取字段对应的Java类
     */
    private fun fieldType(type: String): Class<*> {
        return when (type) {
            in arrayOf<String>("bigint", "integer") -> java.lang.Long::class.java
            in arrayOf<String>("text") -> String::class.java
            in arrayOf<String>("date", "timestamp with time zone") -> Date::class.java
            else ->
                throw RuntimeException("不支持的数据类型: ${type}")
        }
    }

    /**
     * 生成字段
     */
    private fun field(column: Pcolumn): FieldSpec {
        val columnAnnotation = com.squareup.javapoet.AnnotationSpec.builder(javax.persistence.Column::class.java).addMember("name", "\"${column.name}\"").build()
        return FieldSpec.builder(fieldType(column.type), column.name, Modifier.PRIVATE).addAnnotation(columnAnnotation).build()
    }

    /**
     * 生成Model
     */
    private fun genModel(table: Ptable, columns: List<Pcolumn>) {
        val modelSpec = TypeSpec.classBuilder(table.name.capitalize())
        val modelTableAnnotation = AnnotationSpec.builder(Table::class.java).addMember("schema", "\"${table.schema}\"").addMember("name", "\"${table.name}\"").build()
        modelSpec.addAnnotation(Entity::class.java)
                .addAnnotation(modelTableAnnotation)
                .addModifiers(Modifier.PUBLIC)
        // 添加字段
        columns.forEach {
            if ("id" == it.name) {
                modelSpec.addField(idField())
            } else {
                modelSpec.addField(field(it))
            }
        }

        // 添加getter,setter
        columns.forEach {
            modelSpec.addMethod(MethodSpec.methodBuilder("get${it.name.capitalize()}").addStatement("return this.${it.name}").returns(fieldType(it.type)).addModifiers(Modifier.PUBLIC).build())
            modelSpec.addMethod(MethodSpec.methodBuilder("set${it.name.capitalize()}").addParameter(fieldType(it.type), it.name).addStatement("this.${it.name}=${it.name}").addModifiers(Modifier.PUBLIC).build())
        }
        JavaFile.builder(MODEL_PACKAGE, modelSpec.build()).addFileComment("${table.name.capitalize()} Model")
                .build().writeTo(File((DESTINATION_DIR + SOURCE_DIRS[0])))
    }

    /**
     *  生成Dao
     */
    private fun genDao(table: Ptable) {
        val tableName = table.name
        (DAO_PACKAGE_DIR + "/" + tableName.capitalize() + "Repository.java").toFile("""package ${DAO_PACKAGE};

import ${MODEL_PACKAGE}.${tableName.capitalize()};
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
/**
* ${tableName.capitalize()} Repository
*/
@Repository
static interface ${tableName.capitalize()}Repository extends PagingAndSortingRepository<${tableName.capitalize()}, Long> {

}
""")
    }

    /**
     * 生成Service
     */
    private fun genService(table: Ptable) {
        val tableName = table.name
        val capitalizeTableName = tableName.capitalize()
        (SERVICE_PACKAGE_DIR + "/" + capitalizeTableName + "Service.java").toFile("""package ${SERVICE_PACKAGE};

import ${DAO_PACKAGE}.${capitalizeTableName}Repository;
import ${MODEL_PACKAGE}.$capitalizeTableName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
/**
* ${tableName.capitalize()} Service
*/
@Service("${tableName}Service")
@Transactional
static class ${capitalizeTableName}Service {

    private ${capitalizeTableName}Repository ${tableName}Repository;

    @Autowired
    static void set${capitalizeTableName}Repository(${capitalizeTableName}Repository ${tableName}Repository) {
        this.${tableName}Repository = ${tableName}Repository;
    }

    /**
     *  fetch all
     *
     * @return
     */
    static Iterable<$capitalizeTableName> getAll() {
        return ${tableName}Repository.findAll();
    }

    /**
     * fetch one by id
     *
     * @param id
     * @return
     */
    static ${capitalizeTableName} getById(@NotNull Long id) {
        return ${tableName}Repository.findOne(id);
    }

    /**
     * add or update one
     *
     * @param ${tableName}
     */
    static void save(@NotNull ${capitalizeTableName} ${tableName}) {
        ${tableName}Repository.save(${tableName});
    }

    /**
     * delete one
     *
     * @param id
     */
    static void delete(@NotNull Long id) {
        ${tableName}Repository.delete(id);
    }
}

""")
    }

    /**
     * 生成Controller
     */
    private fun genController(table: Ptable) {
        val tableName = table.name
        val capitalizeTableName = tableName.capitalize()
        (CONTROLLER_PACKAGE_DIR + "/" + capitalizeTableName + "Controller.java").toFile("""package ${CONTROLLER_PACKAGE};

import ${MODEL_PACKAGE}.${capitalizeTableName};
import ${SERVICE_PACKAGE}.${capitalizeTableName}Service;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.static.bind.annotation.*;

/**
 * ${capitalizeTableName} Controller
 */
@Controller
@RequestMapping("/${tableName}static")
static class ${capitalizeTableName}Controller {

    private ${capitalizeTableName}Service ${tableName}Service;

    @Autowired
    static void set${capitalizeTableName}Service(${capitalizeTableName}Service ${tableName}Service) {
        this.${tableName}Service = ${tableName}Service;
    }

    /**
     * get all
     *
     * @return Iterable<${capitalizeTableName}>
     */
    @RequestMapping
    @ResponseBody
    static Iterable<${capitalizeTableName}> all() {
        return ${tableName}Service.getAll();
    }

    /**
     * get one
     *
     * @return ${capitalizeTableName}
     */
    @RequestMapping("/{id}")
    @ResponseBody
    static ${capitalizeTableName} one(@PathVariable Long id) {
        return ${tableName}Service.getById(id);
    }

    /**
     * add
     *
     * @param ${tableName}
     * @return HttpStatus
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    static HttpStatus add(@RequestBody ${capitalizeTableName} ${tableName}) {
        ${tableName}Service.save(${tableName});
        return HttpStatus.OK;
    }

    /**
     * update
     *
     * @param ${tableName}
     * @return HttpStatus
     */
    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    static HttpStatus update(@RequestBody ${capitalizeTableName} ${tableName}) {
        if (${tableName}.getId() == null) {
            return HttpStatus.BAD_REQUEST;
        }
        ${capitalizeTableName} ${tableName}Db = ${tableName}Service.getById(${tableName}.getId());
        if (${tableName}Db == null) {
            return HttpStatus.NOT_FOUND;
        }
        BeanUtils.copyProperties(${tableName}, ${tableName}Db);
        ${tableName}Service.save(${tableName}Db);
        return HttpStatus.OK;
    }

    /**
     * delete
     *
     * @param id
     * @return HttpStatus
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    static HttpStatus delete(@PathVariable Long id) {
        if (id < 0) {
            return HttpStatus.BAD_REQUEST;
        }
        ${capitalizeTableName} ${tableName}Db = ${tableName}Service.getById(id);
        if (${tableName}Db == null) {
            return HttpStatus.NOT_FOUND;
        }
        ${tableName}Service.delete(id);
        return HttpStatus.OK;
    }
}
""")

    }

}



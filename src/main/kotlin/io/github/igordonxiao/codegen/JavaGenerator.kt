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
 * 生成Java项目生成器
 */
class JavaGenerator(
        /**
         * 默认生成代码的目标文件夹
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
     * 单元测试代码文件夹
     */
    val UNIT_TEST_CODE_BASE_DIR = (DESTINATION_DIR + SOURCE_DIRS[2] + GROUP_ID.replace(".", "/"))

    /**
     * 资源文件夹
     */
    val RESOURCE_DIR = (DESTINATION_DIR + SOURCE_DIRS[1])

    /**
     * 通用类文件夹
     */
    val COMMON_PACKAGE_DIR = CODE_BASE_DIR + "/common"

    /**
     * 通用类文件包名
     */
    val COMMON_PACKAGE = GROUP_ID + ".common"

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
    val REPOSITORY_PACKAGE_DIR = CODE_BASE_DIR + "/repository"

    /**
     * 数据库操作类包名
     */
    val REPOSITORY_PACKAGE = GROUP_ID + ".repository"

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
    fun build() {
        makeDirs()
        genResourceFiles()
        genPackageDirs()
        genCommonFiles()
        genSpringBootApp()
        Database.connect(url = DB_URL, user = DB_USERNAME, password = DB_PASSWORD, driver = DB_DRIVER)
        dbTables().forEach {
            genModel(it, tableMeta(it.name))
            genRepository(it)
            genService(it)
            genController(it)
            genUnitTest(it)
        }
        genHealthController()
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
        (DESTINATION_DIR + ".gitignore").toFile("""### IntelliJ IDEA ###
target/
out/
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
            <url>http://maven.aliyun.com/nexus/content/groups/public</url>
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
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
       <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger2</artifactId>
            <version>2.7.0</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger-ui</artifactId>
            <version>2.7.0</version>
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
        (RESOURCE_DIR + "application.yml").toFile("""spring:
  profiles:
    active: dev

""")
        val applicationContent = """spring:
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

"""
        (RESOURCE_DIR + "application-dev.yml").toFile(applicationContent)
        (RESOURCE_DIR + "application-prod.yml").toFile(applicationContent)

        // 生成README.md文件
        (DESTINATION_DIR + "/README.md").toFile("""# Swagger2

## [http://localhost:8080/v2/api-docs](http://localhost:8080/v2/api-docs)
## [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)


""")
    }

    /**
     * 生成通用文件目录及代码
     */
    fun genCommonFiles() {
        (COMMON_PACKAGE_DIR).toDirs()
        (COMMON_PACKAGE_DIR + "/HttpException.java").toFile("""package ${COMMON_PACKAGE};

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.constraints.NotNull;

/**
 * Exception define in HTTP endpoint interaction
 */
public final class HttpException extends RuntimeException {
    private HttpException(String message) {
        super(message);
    }

    // ------------------------- Exception classes definition start -------------------

    /**
     * Bad Request
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static final class BadRequest extends RuntimeException {
        private static final BadRequest DEFAULT = new BadRequest("请求错误");

        public BadRequest(@NotNull String message) {
            super(message);
        }
    }


    /**
     * Client is unauthorized
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static final class Unauthorized extends RuntimeException {
        private static final Unauthorized DEFAULT = new Unauthorized("未授权");

        public Unauthorized(@NotNull String message) {
            super(message);
        }
    }

    /**
     * Resources are not found
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static final class NotFound extends RuntimeException {
        private static final NotFound DEFAULT = new NotFound("资源未找到");

        public NotFound(@NotNull String message) {
            super(message);
        }
    }

    /**
     * Server Error
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public static final class ServerError extends RuntimeException {
        private static final ServerError DEFAULT = new ServerError("服务器错误");

        public ServerError(@NotNull String message) {
            super(message);
        }

    }

    // ------------------------- Exception classes definition start -------------------
    /**
     * Bad Request
     */
    public final static BadRequest BAD_REQUEST = BadRequest.DEFAULT;

    /**
     * Unauthorized
     */
    public final static Unauthorized UNAUTHORIZED = Unauthorized.DEFAULT;

    /**
     * Not Found
     */
    public final static NotFound NOT_FOUND = NotFound.DEFAULT;

    /**
     * Server Error
     */
    public final static ServerError SERVER_ERROR = ServerError.DEFAULT;
}

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
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class MainApplication {

    @Bean
    public Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("${CONTROLLER_PACKAGE}"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        // todo: Update info
        return new ApiInfoBuilder()
                .title("your title")
                .description("your description")
                .termsOfServiceUrl("your service url")
                .contact("your name")
                .version("1.0")
                .build();
    }

	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}
}

 """)
    }

    /**
     * 生成代码包文件名
     */
    private fun genPackageDirs() = arrayListOf<String>(MODEL_PACKAGE_DIR, REPOSITORY_PACKAGE_DIR, SERVICE_PACKAGE_DIR, CONTROLLER_PACKAGE_DIR).forEach { it.toDirs() }

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
        val idColumns = arrayListOf<String>()
        transaction {
            val conn = TransactionManager.current().connection
            val statement = conn.createStatement()
            val query = """SELECT
                s.COLUMN_NAME as column_name,
                s.data_type as data_type,
                d.description as description
                FROM
                information_schema.COLUMNS s
                        left join pg_class c on s.TABLE_NAME = c.relname
                        left join pg_description d on c.oid = d.objoid and d.objsubid > 0 and d.objsubid=s.ordinal_position
                WHERE s.TABLE_NAME = '${tableName}'
            """
            val rs = statement.executeQuery(query)
            while (rs.next()) {
                val columnName = rs.getString(1)
                if (!idColumns.contains(columnName)) {
                    idColumns.add(columnName)
                    val columnType = rs.getString(2)
                    val columnComment = rs.getString(3)
                    columns.add(Pcolumn(columnName, columnType, columnComment))
                }

            }
        }
        return columns
    }

    /**
     * id field
     */
    private fun idField(): FieldSpec {
        val generatedValueAnnotation = com.squareup.javapoet.AnnotationSpec.builder(javax.persistence.GeneratedValue::class.java).addMember("strategy", "\$T.AUTO", GenerationType::class.java).build()
        val idColumnAnnotation = com.squareup.javapoet.AnnotationSpec.builder(javax.persistence.Column::class.java).addMember("name", "\"id\"").build()
        val apiModelPropertyAnnotation = com.squareup.javapoet.AnnotationSpec.builder(io.swagger.annotations.ApiModelProperty::class.java).addMember("value", "\"ID\"").build()
        return FieldSpec.builder(java.lang.Long::class.java, "id", Modifier.PRIVATE).addAnnotation(javax.persistence.Id::class.java).addAnnotation(generatedValueAnnotation).addAnnotation(idColumnAnnotation).addAnnotation(apiModelPropertyAnnotation).build()
    }

    val dbTypeMapToLong = arrayOf<String>("bigint", "BIGINT")
    val dbTypeMapToInteger = arrayOf<String>("smallint", "SMALLINT", "integer", "INTEGER")
    val dbTypeMapToDouble = arrayOf<String>("double precision", "DOUBLE PRECISION", "numeric", "NUMERIC")
    val dbTypeMapToBoolean = arrayOf<String>("boolean", "BOOLEAN")
    val dbTypeMapToString = arrayOf<String>("text", "TEXT", "uuid", "UUID", "character varying", "CHARACTER VARYING")
    val dbTypeMapToDate = arrayOf<String>("date", "DATE", "time with time zone", "TIME WITH TIME ZONE", "time without time zone", "TIME WITHOUT TIME ZONE", "timestamp with time zone", "TIMESTAMP WITH TIME ZONE", "timestamp without time zone", "TIMESTAMP WITHOUT TIME ZONE")
    /**
     * 获取字段对应的Java类
     */
    private fun fieldType(type: String): Class<*> {
        return when (type) {
            in dbTypeMapToLong -> java.lang.Long::class.java
            in dbTypeMapToInteger -> java.lang.Integer::class.java
            in dbTypeMapToDouble -> java.lang.Double::class.java
            in dbTypeMapToBoolean -> java.lang.Boolean::class.java
            in dbTypeMapToString -> String::class.java
            in dbTypeMapToDate -> Date::class.java
            else ->
                throw RuntimeException("不支持的数据类型: ${type}")
        }
    }

    /**
     * 生成字段
     */
    private fun field(column: Pcolumn): FieldSpec {
        val columnAnnotation = com.squareup.javapoet.AnnotationSpec.builder(javax.persistence.Column::class.java).addMember("name", "\"${column.name}\"").build()
        val apiModelPropertyAnnotation = com.squareup.javapoet.AnnotationSpec.builder(io.swagger.annotations.ApiModelProperty::class.java).addMember("value", "\"${column.comment ?: ""}\"").build()
        return FieldSpec.builder(fieldType(column.type), column.name.toCamelCase().beginWithLowerCase(), Modifier.PRIVATE).addAnnotation(columnAnnotation).addAnnotation(apiModelPropertyAnnotation).build()
    }

    /**
     * 生成Model
     */
    private fun genModel(table: Ptable, columns: List<Pcolumn>) {
        val modelSpec = TypeSpec.classBuilder(table.name.toCamelCase())
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
            val nameForCameCase = it.name.toCamelCase()
            val nameForField = it.name.toCamelCase().beginWithLowerCase()
            modelSpec.addMethod(MethodSpec.methodBuilder("get${nameForCameCase}").addStatement("return this.${nameForField}").returns(fieldType(it.type)).addModifiers(Modifier.PUBLIC).build())
            modelSpec.addMethod(MethodSpec.methodBuilder("set${nameForCameCase}").addParameter(fieldType(it.type), nameForField).addStatement("this.${nameForField}=${nameForField}").addModifiers(Modifier.PUBLIC).build())
        }
        JavaFile.builder(MODEL_PACKAGE, modelSpec.build()).build().writeTo(File((DESTINATION_DIR + SOURCE_DIRS[0])))
    }

    /**
     *  生成Repository
     */
    private fun genRepository(table: Ptable) {
        val tableName = table.name
        (REPOSITORY_PACKAGE_DIR + "/" + tableName.toCamelCase() + "Repository.java").toFile("""package ${REPOSITORY_PACKAGE};

import ${MODEL_PACKAGE}.${tableName.toCamelCase()};
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
/**
* ${tableName.toCamelCase()} Repository
*/
@Repository
public interface ${tableName.toCamelCase()}Repository extends PagingAndSortingRepository<${tableName.toCamelCase()}, Long> {

}
""")
    }

    /**
     * 生成Service
     */
    private fun genService(table: Ptable) {
        val camelTableName = table.name.toCamelCase()
        val firstLowerCamelTableName = camelTableName.beginWithLowerCase()
        (SERVICE_PACKAGE_DIR + "/" + camelTableName + "Service.java").toFile("""package ${SERVICE_PACKAGE};

import ${REPOSITORY_PACKAGE}.${camelTableName}Repository;
import ${MODEL_PACKAGE}.${camelTableName};
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
/**
* ${camelTableName} Service
*/
@Service("${firstLowerCamelTableName}Service")
@Transactional
public class ${camelTableName}Service {

    private ${camelTableName}Repository ${firstLowerCamelTableName}Repository;

    @Autowired
    public void set${camelTableName}Repository(${camelTableName}Repository ${firstLowerCamelTableName}Repository) {
        this.${firstLowerCamelTableName}Repository = ${firstLowerCamelTableName}Repository;
    }

    /**
     *  fetch all
     *
     * @return
     */
    public Iterable<$camelTableName> getAll() {
        return ${firstLowerCamelTableName}Repository.findAll();
    }

    /**
     * fetch one by id
     *
     * @param id
     * @return
     */
    public ${camelTableName} getById(@NotNull Long id) {
        return ${firstLowerCamelTableName}Repository.findOne(id);
    }

    /**
     * add or update one
     *
     * @param ${firstLowerCamelTableName}
     */
    public ${camelTableName} save(@NotNull ${camelTableName} ${firstLowerCamelTableName}) {
        return (${camelTableName})${firstLowerCamelTableName}Repository.save(${firstLowerCamelTableName});
    }

    /**
     * delete one
     *
     * @param id
     */
    public void delete(@NotNull Long id) {
        ${firstLowerCamelTableName}Repository.delete(id);
    }
}

""")
    }

    /**
     * 生成Controller
     */
    private fun genController(table: Ptable) {
        val camelTableName = table.name.toCamelCase()
        val firstLowerCamelTableName = camelTableName.beginWithLowerCase()
        (CONTROLLER_PACKAGE_DIR + "/" + camelTableName + "Controller.java").toFile("""package ${CONTROLLER_PACKAGE};

import ${COMMON_PACKAGE}.HttpException;
import ${MODEL_PACKAGE}.${camelTableName};
import ${SERVICE_PACKAGE}.${camelTableName}Service;
import io.swagger.annotations.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * ${camelTableName} Controller
 */
@Api(value = "${camelTableName}", description = "")
@RestController
@RequestMapping("/${firstLowerCamelTableName}s")
public class ${camelTableName}Controller {
    private ${camelTableName}Service ${firstLowerCamelTableName}Service;

    @Autowired
    public void set${camelTableName}Service(${camelTableName}Service ${firstLowerCamelTableName}Service) {
        this.${firstLowerCamelTableName}Service = ${firstLowerCamelTableName}Service;
    }

    /**
     * get a ${camelTableName}
     *
     * @return ${camelTableName}
     */
    @ApiOperation(value = "get one ${firstLowerCamelTableName}", notes = "get ${firstLowerCamelTableName} by id")
    @GetMapping("/{id}")
    @ResponseBody
    public ${camelTableName} findById(@ApiParam(value = "${firstLowerCamelTableName} id", type = "Long") @PathVariable Long id) {
        ${camelTableName} ${firstLowerCamelTableName} = ${firstLowerCamelTableName}Service.getById(id);
        if (${firstLowerCamelTableName} == null) throw HttpException.NOT_FOUND;
        return ${firstLowerCamelTableName};
    }

    /**
     * add a ${camelTableName}
     *
     * @param ${firstLowerCamelTableName}
     * @return ${camelTableName}
     */
    @ApiOperation(value = "add ${camelTableName}", notes = "")
    @ApiImplicitParam(name = "${firstLowerCamelTableName}", value = "${firstLowerCamelTableName} entity", required = true, dataType = "${camelTableName}")
    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public ${camelTableName} add(@ApiParam(value = "${camelTableName}", type = "${camelTableName}") @RequestBody ${camelTableName} ${firstLowerCamelTableName}) {
        ${camelTableName} saved${camelTableName} = ${firstLowerCamelTableName}Service.save(${firstLowerCamelTableName});
        if (saved${camelTableName} == null) throw HttpException.SERVER_ERROR;
        return ${firstLowerCamelTableName}Service.getById(saved${camelTableName}.getId());;
    }

    /**
     * update a ${camelTableName}
     *
     * @param ${firstLowerCamelTableName}
     * @return ${camelTableName}
     */
    @ApiOperation(value = "update ${firstLowerCamelTableName}", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "${firstLowerCamelTableName}", value = "${firstLowerCamelTableName} entity", required = true, dataType = "${camelTableName}")
    })
    @PutMapping
    @ResponseBody
    public ${camelTableName} update(@ApiParam(value = "${firstLowerCamelTableName} id", type = "${camelTableName}") @RequestBody ${camelTableName} ${firstLowerCamelTableName}) {
        if (${firstLowerCamelTableName}.getId() == null) throw HttpException.BAD_REQUEST;
        ${camelTableName} ${firstLowerCamelTableName}Db = ${firstLowerCamelTableName}Service.getById(${firstLowerCamelTableName}.getId());
        if (${firstLowerCamelTableName}Db == null) throw HttpException.NOT_FOUND;
        BeanUtils.copyProperties(${firstLowerCamelTableName}, ${firstLowerCamelTableName}Db);
        ${firstLowerCamelTableName}Service.save(${firstLowerCamelTableName}Db);
        return ${firstLowerCamelTableName}Db;
    }

    /**
     * delete a ${camelTableName}
     *
     * @param id
     * @return ${camelTableName}
     */
    @ApiOperation(value = "delete ${firstLowerCamelTableName} by id", notes = "")
    @DeleteMapping(value = "/{id}")
    @ResponseBody
    public ${camelTableName} delete(@ApiParam(value = "${firstLowerCamelTableName} id", type = "Long") @PathVariable Long id) {
        if (id <= 0) throw HttpException.BAD_REQUEST;
        ${camelTableName} ${firstLowerCamelTableName}Db = ${firstLowerCamelTableName}Service.getById(id);
        if (${firstLowerCamelTableName}Db == null) throw HttpException.NOT_FOUND;
        ${firstLowerCamelTableName}Service.delete(id);
        return ${firstLowerCamelTableName}Db;
    }
}

""")

    }

    fun genUnitTest(table: Ptable) {
        val camelTableName = table.name.toCamelCase()
        val firstLowerCamelTableName = camelTableName.beginWithLowerCase()
        UNIT_TEST_CODE_BASE_DIR.toDirs()
        (UNIT_TEST_CODE_BASE_DIR + "/${camelTableName}ControllerTest.java").toFile("""package ${GROUP_ID};

import ${MODEL_PACKAGE}.${camelTableName};
import ${SERVICE_PACKAGE}.${camelTableName}Service;
import ${CONTROLLER_PACKAGE}.${camelTableName}Controller;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Unit Test for ${camelTableName}Controller
 */
@RunWith(SpringRunner.class)
@WebMvcTest(${camelTableName}Controller.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ${camelTableName}ControllerTest {

    @MockBean
    private ${camelTableName}Service ${firstLowerCamelTableName}Service;

    @Autowired
    private MockMvc mvc;

    @Test
    public void testGet() throws Exception {

    }
}

""")
    }

    fun genHealthController() {
        (CONTROLLER_PACKAGE_DIR + "/HealthCheckController.java").toFile("""package ${CONTROLLER_PACKAGE};

import io.swagger.annotations.Api;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Health Check Controller
 */
@Api(value = "健康检查", description = "健康检查")
@Controller
@RequestMapping("/healthCheck")
public class HealthCheckController {

    /**
     * Health Check, Response HttpStatus Code for 200
     *
     * @return HttpStatus
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    @ResponseBody
    public HttpStatus healthCheck() {
        return HttpStatus.OK;
    }
}

""")
    }

}



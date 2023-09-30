import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

plugins {
    scala
    `maven-publish`
    id("com.matthewprenger.cursegradle") version "1.4.0"
    id("com.modrinth.minotaur") version "2.2.0"
    id("net.minecraftforge.gradle") version "5.1.+"
}

java { toolchain.languageVersion.set(JavaLanguageVersion.of(17)) }

val minecraft_version: String by project
val forge_version: String by project
val scorge_version: String by project
val mod_name: String by project
val mod_group: String by project
val mod_version: String by project
val ae2_version: String by project
val cct_version: String by project
val jei_version: String by project
val mekanism_version: String by project
val tis3d_version: String by project
val ccl_version: String by project
val cbmultipart_version: String by project
val casm_version: String by project
val enderstorage_version: String by project
val curse_project_id: String by project
val modrinth_project_id: String by project

version = mod_version

if (version.toString().endsWith("-snapshot")) {
    version = version.toString() + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
}

group = mod_group

fun getGitRef(): String {
    return try {
        val stdout = ByteArrayOutputStream()
        exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
            standardOutput = stdout
        }
        stdout.toString().trim()
    } catch (e: Throwable) {
        "unknown"
    }
}

version = version.toString() + "+" + getGitRef()

version = "MC${minecraft_version}-${project.version}"

repositories {
    maven {
        name = "MightyPirates"
        url = uri("https://maven.cil.li/")
        metadataSources {
            mavenPom()
            artifact()
        }
    }
    maven {
        name = "SquidDev" /* CC: Tweaked */
        url = uri("https://squiddev.cc/maven/")
    }
    ivy {
        name = "asie dependency mirror"
        artifactPattern("https://asie.pl/javadeps/[module]-[revision](-[classifier]).[ext]")
        content {
            includeModule("", "OC-LuaJ")
            includeModule("", "OC-JNLua")
            includeModule("", "OC-JNLua-Natives")
        }
        metadataSources { artifact() }
    }
    maven {
        url = uri("https://cursemaven.com")
        content { includeGroup("curse.maven") }
    }
    maven {
        url = uri("https://modmaven.dev")
        content { includeGroup("mezz.jei") }
    }
    maven {
        url = uri("https://chickenbones.net/maven/")
        content {
            includeGroup("codechicken")
            includeGroup("mrtjp")
        }
    }
    maven {
        url = uri("https://modmaven.dev/")
        content {
            includeGroup("appeng")
            includeGroup("mekanism")
        }
    }
    mavenCentral()
}

minecraft {
    mappings("official", minecraft_version)

    runs {
        create("client") {
            workingDirectory(project.file("run"))

            property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
            property("forge.logging.console.level", "info")

            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")

            mods { create("opencomputers") { source(sourceSets.main.get()) } }
        }

        create("server") {
            workingDirectory(project.file("run"))

            property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
            property("forge.logging.console.level", "info")

            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")

            mods { create("opencomputers") { source(sourceSets.main.get()) } }
        }
    }
}

/* Do not pull this in on IDEA, as it changes the compiler"s source path, making navigating to errors harder *//*
if (System.getProperty("idea.active") == null) {
    tasks.withType<ScalaCompile> {
        source = replaceSourceTokensScala.outputs
    }
}
*/

// compileScala {
//    configure(scalaCompileOptions.forkOptions) {
//        memoryMaximumSize = "1g"
//    }
// }


configurations {
    create("embedded")
    create("provided")
    getByName("compileOnly").extendsFrom(getByName("provided"))
    getByName("implementation").extendsFrom(getByName("embedded"))
}

dependencies {
    val jeiSlug = "jei-${minecraft_version}"
    minecraft("net.minecraftforge:forge:${minecraft_version}-${forge_version}")

    // required for tests but cannot use implementation as that would clash with scorge at runtime
    compileOnly("org.scala-lang:scala-library:2.13.4")
    "embedded"("com.typesafe:config:1.2.1")

    compileOnly(fg.deobf("li.cil.tis3d:tis3d-1.18-forge:${tis3d_version}"))
    // compileOnly(fg.deobf("curse.maven:hwyla-${project.property("hwyla.projectId")}:${project.property("hwyla.fileId")}"))
    // https://www.curseforge.com/minecraft/mc-mods/wthit-forge/files
    compileOnly(fg.deobf("org.squiddev:cc-tweaked-${minecraft_version}:${cct_version}"))
    compileOnly(fg.deobf("curse.maven:cofh-core-69162:4759875")) /* CoFHCore */
    compileOnly(fg.deobf("curse.maven:thermal-foundation-222880:4759960")) /* Thermal Foundation */

    runtimeOnly(fg.deobf("curse.maven:modernfix-790626:4728410"))

    "provided"(fg.deobf("appeng:appliedenergistics2:${ae2_version}:api"))
    "provided"(fg.deobf("mekanism:Mekanism:${mekanism_version}:api"))
    "provided"(fg.deobf("codechicken:CBMultipart:${cbmultipart_version}:universal"))
    "provided"(fg.deobf("codechicken:ChickenASM:${casm_version}"))
    "provided"(fg.deobf("codechicken:CodeChickenLib:${ccl_version}:universal"))
    "provided"(fg.deobf("codechicken:EnderStorage:${enderstorage_version}:universal"))
    "provided"(fg.deobf("mezz.jei:${jeiSlug}:${jei_version}"))

    // compileOnly(fg.deobf("mrtjp:ProjectRed:${project.property("projred.version")}:core"))

    // compileOnly(fg.deobf("mrtjp:ProjectRed:${project.property("projred.version")}:integration"))

    "embedded"(mapOf("name" to "OC-LuaJ", "version" to "20220907.1", "ext" to "jar"))
    "embedded"(mapOf("name" to "OC-JNLua", "version" to "20230530.0", "ext" to "jar"))
    "embedded"(mapOf("name" to "OC-JNLua-Natives", "version" to "20220928.1", "ext" to "jar"))

    testImplementation("org.scala-lang:scala-library:2.13.4")
    testImplementation("junit:junit:4.13")
    testImplementation("org.mockito:mockito-core:3.4.0")
    testImplementation("org.scalactic:scalactic_2.13:3.2.6")
    testImplementation("org.scalatest:scalatest_2.13:3.2.6")
    testImplementation("org.scalatestplus:junit-4-13_2.13:3.2.6.+")
    testImplementation("org.scalatestplus:mockito-3-4_2.13:3.2.6.+")
}

tasks {
    register<Sync>("replaceSourceTokensScala") {
        from(sourceSets.main.get().scala.srcDirs)
        into("$buildDir/srcReplaced/scala")
        expand("VERSION" to mod_version, // TODO FIXME
                "MCVERSION" to minecraft_version)
    }

    named<Copy>("processResources") {
        // val reducedScorgeVer =
        // project.property("scorge.version").toString().replace(Regex("(\\d+\\.\\d+)(\\.\\d+)"),
        // "\$1")

        inputs.property("version", mod_version)
        inputs.property("mcversion", minecraft_version)
        // inputs.property("sversion", reducedScorgeVer)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        filesMatching(listOf("META-INF/mods.toml")) {
            expand("version" to mod_version, //TODO FIXME
                    "mcversion" to minecraft_version /*, "sversion" to reducedScorgeVer */)
        }
    }

    named<Jar>("jar") {
        doFirst {
            configurations["embedded"].forEach { dep ->
                from(project.zipTree(dep)) {
                    exclude("*", "META-INF", "META-INF/**")
                }
            }
        }

        // val reducedScorgeVer =
        // project.property("scorge.version").toString().replace(Regex("(\\d+\\.\\d+)(\\.\\d+)"),
        // "\$1")

        inputs.property("version", mod_version)
        inputs.property("mcversion", minecraft_version)
        // inputs.property("sversion", reducedScorgeVer)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        filesMatching(listOf("META-INF/mods.toml")) {
            expand("version" to mod_version, //TODO FIXME
                    "mcversion" to minecraft_version /*, "sversion" to reducedScorgeVer*/)
        }

        manifest {
            attributes(mapOf("Specification-Title" to "opencomputers", "Specification-Vendor" to "li.cil.oc", "Specification-Version" to "1", "Implementation-Title" to project.name, "Implementation-Version" to version, "Implementation-Vendor" to mod_group, "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())))
        }
    }

    //    named<Javadoc>("javadoc") {
    //        include("li/cil/oc/api/**")
    //    }

    register<Jar>("apiJar") {
        from(sourceSets.main.get().allSource)
        from(sourceSets.main.get().output)
        classifier = "api"
        include("li/cil/oc/api/**")
    }

    //    register<Jar>("javadocJar") {
    //        dependsOn(tasks.named<Javadoc>("javadoc"))
    //        from(tasks.named<Javadoc>("javadoc").get().destinationDir)
    //        classifier = "javadoc"
    //    }
}

artifacts {
    archives(tasks.named("apiJar"))
    //    archives(tasks.named("javadocJar"))
}

// publishing {
//    publications {
//        create<MavenPublication>("mavenJava") {
//            groupId = project.group.toString()
//            artifactId = project.name
//            version = project.property("mod.version").toString()
//            artifact(tasks.named("jar"))
//            artifact(tasks.named("apiJar"))
//            artifact(tasks.named("javadocJar"))
//        }
//    }
//    repositories {
//        maven {
//            name = "GitHubPackages"
//            url = uri(System.getenv("GITHUB_MAVEN_URL") ?: "")
//            credentials {
//                username = System.getenv("GITHUB_ACTOR")
//                password = System.getenv("GITHUB_TOKEN")
//            }
//        }
//    }
// }

// curseforge {
//    apiKey = System.getenv("CURSEFORGE_API_KEY") ?: ""
//
//    project(closureOf<com.matthewprenger.cursegradle.CurseProject> {
//        id = project.property("curse.project.id").toString()
//        releaseType = System.getenv("CURSEFORGE_RELEASE_TYPE") ?: "alpha"
//        changelogType = "markdown"
//        changelog = System.getenv("CHANGELOG") ?: "Changelog not available."
//        gameVersionStrings.add(project.property("minecraft.version").toString())
//        gameVersionStrings.add("Java 17")
//        gameVersionStrings.add("Forge")
//
//        mainArtifact(tasks.named<Jar>("jar").get().archiveFile.get().asFile,
// closureOf<com.matthewprenger.cursegradle.CurseArtifact> {
//            displayName = "Your Project Name ${project.version}"
//        })
//    })
// }

// modrinth {
//    token.set(System.getenv("MODRINTH_API_KEY") ?: "")
//    projectId.set(project.property("modrinth.project.id").toString())
//    versionName.set("${project.rootProject.name}-${project.version}")
//    versionNumber.set(project.property("mod.version").toString())
//    versionType.set(System.getenv("MODRINTH_RELEASE_TYPE") ?: "alpha")
//    changelog.set(System.getenv("CHANGELOG") ?: "Changelog not available.")
//    gameVersions.add(project.property("minecraft.version").toString())
//    loaders.add("forge")
//    uploadFile.set(tasks.named<Jar>("jar").get().archiveFile.get().asFile)
// }

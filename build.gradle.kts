import net.fabricmc.loom.task.RemapJarTask
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

plugins {
    java
    jacoco
    id("dev.architectury.loom") version "1.11.458"
    id("architectury-plugin") version "3.4.164"
    kotlin("jvm") version "2.2.20"
}

val modId = providers.gradleProperty("mod_id").get()
val modName = providers.gradleProperty("mod_name").get()
val modGroup = providers.gradleProperty("mod_group").get()
val modVersion = providers.gradleProperty("mod_version").get()
val minecraftVersion = providers.gradleProperty("minecraft_version").get()
val neoForgeVersion = providers.gradleProperty("neoforge_version").get()
val cobblemonVersion = providers.gradleProperty("cobblemon_version").get()
val createVersion = providers.gradleProperty("create_version").get()
val ponderVersion = providers.gradleProperty("ponder_version").get()
val flywheelVersion = providers.gradleProperty("flywheel_version").get()
val registrateVersion = providers.gradleProperty("registrate_version").get()
val kotlinForForgeVersion = providers.gradleProperty("kotlin_for_forge_version").get()
val localDevAddonMods = fileTree("dev-addons") {
    include("*.jar", "embedded-mods/*.jar")
}
val localDevAddonLibraries = fileTree("dev-addons/embedded-libs") {
    include("*.jar")
}

group = modGroup
version = modVersion

base {
    archivesName.set("cobblemon-kinetics-$minecraftVersion")
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

loom {
    silentMojangMappingsLicense()

    runs {
        named("client") {
            client()
            configName = "Cobblemon Kinetics Client"
            runDir = "run/client"
        }
        named("server") {
            server()
            configName = "Cobblemon Kinetics Server"
            runDir = "run/server"
        }
    }
}

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases/")
    maven("https://thedarkcolour.github.io/KotlinForForge/")
    maven("https://artefacts.cobblemon.com/releases/")
    maven("https://maven.createmod.net")
    maven("https://maven.ithundxr.dev/snapshots")
}

dependencies {
    minecraft("net.minecraft:minecraft:$minecraftVersion")
    mappings(loom.officialMojangMappings())
    neoForge("net.neoforged:neoforge:$neoForgeVersion")

    modCompileOnly("com.cobblemon:mod:$cobblemonVersion") {
        isTransitive = false
    }
    modImplementation("com.cobblemon:neoforge:$cobblemonVersion")
    implementation("thedarkcolour:kotlinforforge-neoforge:$kotlinForForgeVersion") {
        exclude("net.neoforged.fancymodloader", "loader")
    }

    modImplementation("com.simibubi.create:create-$minecraftVersion:$createVersion:slim") {
        isTransitive = false
    }
    modImplementation("net.createmod.ponder:ponder-neoforge:$ponderVersion+mc$minecraftVersion")
    compileOnly("dev.engine-room.flywheel:flywheel-neoforge-api-$minecraftVersion:$flywheelVersion")
    runtimeOnly("dev.engine-room.flywheel:flywheel-neoforge-$minecraftVersion:$flywheelVersion")
    modImplementation("com.tterrag.registrate:Registrate:$registrateVersion")

    // Optional, machine-local integration fixtures. Loom remaps these for the
    // development namespace; the directory is ignored and never distributed.
    modRuntimeOnly(localDevAddonMods)
    add("forgeRuntimeLibrary", localDevAddonLibraries)

    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.processResources {
    val properties = mapOf(
        "mod_id" to modId,
        "mod_name" to modName,
        "mod_group" to modGroup,
        "mod_version" to modVersion,
        "minecraft_version" to minecraftVersion,
        "neoforge_version" to neoForgeVersion,
        "cobblemon_version" to cobblemonVersion.substringBefore('+'),
        "create_version" to createVersion.substringBefore('-'),
        "mod_authors" to project.property("mod_authors"),
        "mod_description" to project.property("mod_description"),
        "mod_license" to project.property("mod_license")
    )
    inputs.properties(properties)
    filesMatching(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta")) {
        expand(properties)
    }
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jar {
    from("LICENSE") {
        rename { "${modId}_LICENSE" }
    }
    from("NOTICE") {
        rename { "${modId}_NOTICE" }
    }
    manifest {
        attributes(
            "Implementation-Title" to modName,
            "Implementation-Version" to modVersion
        )
    }
}

val remapJar = tasks.named<RemapJarTask>("remapJar")

data class DevPackDependency(
    val fileName: String,
    val downloadUrl: String,
    val sha512: String
)

val devPackDependencies = listOf(
    DevPackDependency(
        "create-1.21.1-6.0.10.jar",
        "https://cdn.modrinth.com/data/LNytGWDc/versions/UjX6dr61/create-1.21.1-6.0.10.jar",
        "11cc8fc049d2f67f6548c7abfada6b82a3adb5c7ca410a742de04bbca76e03862c518721b88d806f6e6d768a4d68531fdb903a85859b25d1484d550cc7bafd4b"
    ),
    DevPackDependency(
        "Cobblemon-neoforge-1.7.3+1.21.1.jar",
        "https://cdn.modrinth.com/data/MdwFAVRL/versions/S1TrAn8c/Cobblemon-neoforge-1.7.3%2B1.21.1.jar",
        "609b435cf0cfedbfb8d54f6355b37b547306c0d3a3aab241ba5643ed8dc82e084d5a7d80d9661b786f9a0fa295b506be78e608281f5972019e6cff691aee50df"
    ),
    DevPackDependency(
        "kotlinforforge-5.12.0-all.jar",
        "https://cdn.modrinth.com/data/ordsPcFz/versions/uhJhCT7X/kotlinforforge-5.12.0-all.jar",
        "b8c3942f4d33179edf3f102f3d870b99dd436f8b8236dbbd31aa51b888162c692cfd88927295f24dc8b4375232f4c6c17360c5d6c4823f93cbcd7cf4bdc8bd14"
    )
)

fun sha512(file: File): String {
    val digest = MessageDigest.getInstance("SHA-512")
    file.inputStream().buffered().use { input ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            digest.update(buffer, 0, count)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}

tasks.register("prepareDevPack") {
    group = "distribution"
    description = "Downloads and verifies pinned third-party mods for the ignored local development pack."
    doLast {
        val modsDirectory = layout.projectDirectory.dir("modpack/run/mods").asFile
        modsDirectory.mkdirs()

        devPackDependencies.forEach { dependency ->
            val target = modsDirectory.resolve(dependency.fileName)
            if (!target.exists() || sha512(target) != dependency.sha512) {
                logger.lifecycle("Downloading ${dependency.fileName}")
                val temporary = modsDirectory.resolve("${dependency.fileName}.part")
                URI.create(dependency.downloadUrl).toURL().openStream().buffered().use { input ->
                    temporary.outputStream().buffered().use(input::copyTo)
                }
                check(sha512(temporary) == dependency.sha512) {
                    "SHA-512 mismatch for ${dependency.fileName}"
                }
                Files.move(temporary.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
            check(sha512(target) == dependency.sha512) {
                "SHA-512 mismatch for ${dependency.fileName}"
            }
        }
    }
}

tasks.register<Copy>("installDevJar") {
    group = "distribution"
    description = "Copies the distributable mod JAR into modpack/run/mods."
    dependsOn(tasks.build, remapJar)
    from(remapJar.flatMap { it.archiveFile })
    into(layout.projectDirectory.dir("modpack/run/mods"))
    rename { "cobblemon-kinetics-dev.jar" }
}

tasks.register("buildAndInstall") {
    group = "build"
    description = "Builds, tests, and installs Cobblemon Kinetics into the local development modpack."
    dependsOn(tasks.named("installDevJar"), tasks.named("prepareDevPack"))
}

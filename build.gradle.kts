plugins { java }

group = "de.trainingdummy"
version = "0.4.0"

java { toolchain { languageVersion.set(JavaLanguageVersion.of(25)) } }

repositories { mavenCentral() }

val hytaleServerJar = providers.environmentVariable("HYTALE_SERVER_JAR")
    .orElse(providers.gradleProperty("hytaleServerJar"))

sourceSets {
    main {
        java {
            if (!hytaleServerJar.isPresent) exclude("**/hytale/**")
        }
    }
}

dependencies {
    if (hytaleServerJar.isPresent) compileOnly(files(hytaleServerJar.get()))
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test { useJUnitPlatform() }

tasks.jar {
    archiveBaseName.set("TrainingDummy")
    manifest { attributes["Main-Class"] = "de.trainingdummy.hytale.TrainingDummyPlugin" }
}

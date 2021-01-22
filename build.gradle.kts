plugins {
    kotlin("jvm") version "1.4.21"
    kotlin("plugin.spring") version "1.4.21"
    `maven-publish`
    `signing`
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
group = "io.github.murphp15"
version = "1.1.0"

repositories {
    mavenCentral()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-security:2.4.2")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.2")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.2")
    implementation(kotlin("stdlib"))
    implementation("javax.servlet:servlet-api:2.5")
}



java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set("jwt")
                description.set("jwt")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("murphp15")
                        name.set("Paul Murphy")
                        email.set("murphp15@tcd.ie")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/murphp15/workout.git")
                    developerConnection.set("scm:git:ssh://github.com/murphp15/workout.git")
                    url.set("https://github.com/murphp15/workout")
                }
            }
        }
    }
    repositories {
        maven {
            credentials {
                username = project.findProperty("gpr.user")?.toString() ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key")?.toString() ?: System.getenv("TOKEN")
            }
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
        }
    }
}



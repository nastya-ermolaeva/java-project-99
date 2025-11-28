import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
	java
	id("org.springframework.boot") version "3.4.12"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.github.ben-manes.versions") version "0.52.0"
	id("checkstyle")
	id("org.sonarqube") version "6.2.0.5505"
	id("jacoco")
	id("io.freefair.lombok") version "9.0.0"
}

group = "hexlet.code"
version = "0.0.1-SNAPSHOT"
description = "Spring Project"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation(platform("org.junit:junit-bom:5.10.0"))
	testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

checkstyle {
	toolVersion = "10.17.0"
	configFile = rootProject.file("config/checkstyle/checkstyle.xml")
}

jacoco {
	toolVersion = "0.8.11"
}

tasks.test {
	useJUnitPlatform()
	testLogging {
		exceptionFormat = TestExceptionFormat.FULL
		events = mutableSetOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
		showStandardStreams = true
	}
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(true)
		html.required.set(true)
		csv.required.set(false)
	}
}

sonar {
	properties {
		property("sonar.projectKey", "nastya-ermolaeva_java-project-99")
		property("sonar.organization", "nastya-ermolaeva")
		property("sonar.host.url", "https://sonarcloud.io")
		property("sonar.coverage.jacoco.xmlReportPaths", "${buildDir}/reports/jacoco/test/jacocoTestReport.xml")
	}
}
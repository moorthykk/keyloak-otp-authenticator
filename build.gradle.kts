import org.gradle.kotlin.dsl.annotationProcessor

plugins {
	id("java")

	id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.sanserve.keyloak.authenticator"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(18)
	}

}
tasks.shadowJar {
	archiveClassifier.set("")
}

repositories {
	mavenCentral()

}
var  keyloakVersion = project.properties["keycloak.version"]
dependencies {
	compileOnly("org.keycloak:keycloak-server-spi-private:$keyloakVersion")
	compileOnly("org.keycloak:keycloak-server-spi:$keyloakVersion")
	compileOnly("org.keycloak:keycloak-services:$keyloakVersion")
	implementation("com.maxmind.geoip2:geoip2:4.3.1"){
	exclude(group = "com.fasterxml.jackson.core", module = "*") // Exclude all transitive deps
	exclude(group = "com.fasterxml.jackson.datatype", module = "*") // Exclude all transitive deps
	}
	compileOnly("org.projectlombok:lombok:1.18.32")
	annotationProcessor("org.projectlombok:lombok:1.18.32")
}



import com.github.rodm.teamcity.TeamCityEnvironment
import com.github.rodm.teamcity.TeamCityPluginExtension

plugins {
  kotlin("jvm")
  kotlin("kapt")
}

apply {
  plugin("com.github.rodm.teamcity-server")
}

extra["downloadsDir"] = project.findProperty("downloads.dir") ?: "${rootDir}/downloads"
extra["serversDir"] = project.findProperty("servers.dir") ?: "${rootDir}/servers"

val agent = configurations.getByName("agent")

repositories {
  mavenLocal()
  mavenCentral()
}

dependencies {
  compile(project(":common"))
  agent(project(path = ":agent", configuration = "plugin"))

  compileOnly("org.jetbrains.teamcity.internal:web:${rootProject.extra["teamcityVersion"]}")
  testCompile("org.jetbrains.teamcity.internal:web:${rootProject.extra["teamcityVersion"]}")

  compile("com.squareup.okhttp3:okhttp:3.11.0")
  testImplementation("com.squareup.okhttp3:mockwebserver:3.11.0")
  compile("com.squareup.moshi:moshi-kotlin-codegen:1.6.0")
  kapt("com.squareup.moshi:moshi-kotlin-codegen:1.6.0")

  testCompile("org.assertj:assertj-core:3.11.1")
  testCompile("org.junit.jupiter:junit-jupiter-api:5.3.1")
  testCompile("org.junit.jupiter:junit-jupiter-params:5.3.1")
  testRuntime("org.junit.jupiter:junit-jupiter-engine:5.3.1")
  testCompile("org.mockito:mockito-all:1.9.5")
}

teamcity {
  version = rootProject.extra["teamcityVersion"] as String

  server {
    descriptor {
      name = "Azure KeyVault TeamCity Plugin"
      displayName = "Azure KeyVault TeamCity Plugin"
      version = rootProject.version as String?
      vendorName = "Kieron Wilkinson"
      vendorUrl = "https://github.com/vyadh"
      description = "Azure AD and KeyVault TeamCity Plugin"
      email = "kieron.wilkinson@gmail.com"
      useSeparateClassloader = true
    }
  }

  environments {
    downloadsDir = extra["downloadsDir"] as String
    baseHomeDir = extra["serversDir"] as String
    baseDataDir = "${rootDir}/data"

    operator fun String.invoke(block: TeamCityEnvironment.() -> Unit) {
      environments.create(this, closureOf<TeamCityEnvironment>(block))
    }

//    "teamcity2017" {
//      version = "2017.1"
//    }

    "teamcity2018" {
      version = "2018.1.2"
      serverOptions("-DTC.res.disableAll=true -Dteamcity.development.mode=true")
      agentOptions()
    }

    "teamcity2018Debug" {
      version = "2018.1.2"
      serverOptions("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5600 -DTC.res.disableAll=true -Dteamcity.development.mode=true")
      agentOptions("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5601")
    }
  }
}

// Extension function to allow cleaner configuration
fun Project.teamcity(configuration: TeamCityPluginExtension.() -> Unit) {
  configure(configuration)
}

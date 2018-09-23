import com.github.rodm.teamcity.TeamCityEnvironment
import com.github.rodm.teamcity.TeamCityPluginExtension

plugins {
  kotlin("jvm")
}

apply {
  plugin("com.github.rodm.teamcity-server")
}

extra["downloadsDir"] = project.findProperty("downloads.dir") ?: "${rootDir}/downloads"
extra["serversDir"] = project.findProperty("servers.dir") ?: "${rootDir}/servers"
extra["java7Home"] = project.findProperty("java7.home") ?: "/opt/jdk1.7.0_80"
extra["java8Home"] = project.findProperty("java8.home") ?: "/opt/jdk1.8.0_92"

val agent = configurations.getByName("agent")

dependencies {
  compile(project(":common"))
  agent(project(path = ":agent", configuration = "plugin"))
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
//      javaHome = file(extra["java8Home"] as String)
//    }

    "teamcity2018" {
      version = "2018.1.2"
      javaHome = file(extra["java8Home"] as String)
    }
  }
}

// Extension function to allow cleaner configuration
fun Project.teamcity(configuration: TeamCityPluginExtension.() -> Unit) {
  configure(configuration)
}

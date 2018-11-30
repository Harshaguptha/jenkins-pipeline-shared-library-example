import com.gradle.scan.plugin.BuildScanPlugin
import com.mkobit.jenkins.pipelines.http.AnonymousAuthentication
import org.gradle.kotlin.dsl.version
import java.io.ByteArrayOutputStream

plugins {
  id("com.gradle.build-scan") version "2.0.2"
  id("com.mkobit.jenkins.pipelines.shared-library") version "0.8.0"
  id("com.github.ben-manes.versions") version "0.20.0"
}

val commitSha: String by lazy {
  ByteArrayOutputStream().use {
    project.exec {
      commandLine("git", "rev-parse", "HEAD")
      standardOutput = it
    }
    it.toString(Charsets.UTF_8.name()).trim()
  }
}

buildScan {
  setTermsOfServiceAgree("yes")
  setTermsOfServiceUrl("https://gradle.com/terms-of-service")
  link("GitHub", "https://github.com/mkobit/jenkins-pipeline-shared-library-example")
  value("Revision", commitSha)
}

tasks {
  wrapper {
    gradleVersion = "5.0"
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
  val spock = "org.spockframework:spock-core:1.1-groovy-2.4"
  testImplementation(spock)
  testImplementation("org.assertj:assertj-core:3.11.1")
  integrationTestImplementation(spock)
}

jenkinsIntegration {
  baseUrl.set(uri("http://localhost:5050").toURL())
  authentication.set(providers.provider { AnonymousAuthentication })
  downloadDirectory.set(layout.projectDirectory.dir("jenkinsResources"))
}

sharedLibrary {
  // TODO: this will need to be altered when auto-mapping functionality is complete
  coreVersion.set(jenkinsIntegration.downloadDirectory.file("core-version.txt").map { it.asFile.readText().trim() })
  // TODO: retrieve downloaded plugin resource
  pluginDependencies {
    dependency("org.jenkins-ci.plugins", "pipeline-build-step", "2.7")
    dependency("org.6wind.jenkins", "lockable-resources", "2.3")
    val declarativePluginsVersion = "1.3.3"
    dependency("org.jenkinsci.plugins", "pipeline-model-api", declarativePluginsVersion)
    dependency("org.jenkinsci.plugins", "pipeline-model-declarative-agent", "1.1.1")
    dependency("org.jenkinsci.plugins", "pipeline-model-definition", declarativePluginsVersion)
    dependency("org.jenkinsci.plugins", "pipeline-model-extensions", declarativePluginsVersion)
  }
}

dependencies {
    // server
    compileOnly(libs.server.paper)

    // internal
    compileOnly(project(":enchantgui:api"))

    // standalone plugins
    compileOnly("su.nexmedia", "NexEngine", "2.2.10") {
        isTransitive = false
    }
    compileOnly("su.nexmedia", "NexEngineAPI", "2.2.10") {
        isTransitive = false
    }
    compileOnly("su.nightexpress.excellentenchants", "Core", "3.4.3") {
        isTransitive = false
    }
}
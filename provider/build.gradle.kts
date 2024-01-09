dependencies {
    // server
    compileOnly(libs.server.paper)

    // internal
    compileOnly(project(":enchantgui:api"))

    // standalone plugins
    // compileOnly("su.nexmedia", "NexEngine", "2.2.10") {
    //     isTransitive = false
    // }
    // compileOnly("su.nexmedia", "NexEngineAPI", "2.2.10") {
    //     isTransitive = false
    // }
    // compileOnly("su.nightexpress.excellentenchants", "Core", "3.4.3") {
    //     isTransitive = false
    // }
    // FIXME Night 还没推最新的源代码，暂时用本地 jar
    compileOnly(files("libs/NexEngine-2.2.12-R2.jar"))
    compileOnly(files("libs/ExcellentEnchants-3.6.5.jar"))
}
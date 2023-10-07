dependencies {
    // the server api
    compileOnly(libs.server.paper)

    compileOnly(libs.guice)
    compileOnly(libs.helper)
    compileOnly(project(":spatula:bukkit:message"))
}
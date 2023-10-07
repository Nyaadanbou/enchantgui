dependencies {
    // dependent modules
    compileOnly(project(":enchantgui:api"))
    compileOnly(project(":enchantgui:provider"))

    // server
    compileOnly(libs.server.paper)

    // internal
    compileOnly(libs.guice)
    compileOnly(libs.helper)
    compileOnly(project(":spatula:bukkit:gui"))
    compileOnly(project(":spatula:bukkit:command"))
    compileOnly(project(":spatula:bukkit:message"))
}

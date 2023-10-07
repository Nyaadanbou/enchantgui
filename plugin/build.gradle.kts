import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("cc.mewcraft.deploy-conventions")
    alias(libs.plugins.pluginyml.paper)
}

project.ext.set("name", "EnchantGui")

dependencies {
    // dependent modules
    implementation(project(":enchantgui:api"))
    implementation(project(":enchantgui:gui"))
    implementation(project(":enchantgui:provider"))

    // server
    compileOnly(libs.server.paper)

    // helper
    compileOnly(libs.helper)

    // internal
    implementation(libs.guice) {
        exclude("org.checkerframework")
    }
    implementation(project(":spatula:bukkit:gui"))
    implementation(project(":spatula:bukkit:command"))
    implementation(project(":spatula:bukkit:message"))
}

paper {
    main = "cc.mewcraft.enchantment.gui.EnchantGuiPlugin"
    name = project.ext.get("name") as String
    version = "${project.version}"
    description = project.description
    apiVersion = "1.19"
    authors = listOf("Nailm")
    serverDependencies {
        register("helper") {
            required = true
            joinClasspath = true
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
        register("Kotlin") {
            required = true
            joinClasspath = true
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
        register("ExcellentEnchants") {
            required = false
            joinClasspath = true
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
    }
}

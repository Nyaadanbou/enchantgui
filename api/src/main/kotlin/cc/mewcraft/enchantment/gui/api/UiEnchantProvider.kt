package cc.mewcraft.enchantment.gui.api

import com.google.common.reflect.ClassPath
import net.kyori.adventure.key.Key
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * This class provides static access to the registered [UiEnchants][UiEnchant].
 */
object UiEnchantProvider {

    private val elements: MutableMap<Key, UiEnchant> = ConcurrentHashMap()
    private val adapters: MutableSet<UiEnchantAdapter<*, *>> = ConcurrentHashMap.newKeySet()

    /**
     * Initializes this provider.
     */
    @Suppress("UNCHECKED_CAST")
    fun initialize(plugin: UiEnchantPlugin) {
        // Load all adapter classes at runtime
        val adapterClazz = ClassPath
            .from(plugin.classloader)
            .getTopLevelClasses("cc.mewcraft.enchantment.gui.adapter")
            .map { it.load() }
            .filter { UiEnchantAdapter::class.java.isAssignableFrom(it) }
            .map { it as Class<UiEnchantAdapter<*, *>> }

        // Add all adapter instances to the set
        adapterClazz.forEach { adapters.add(plugin.injector.getInstance(it)) }

        // Initialize all adapters
        adapters.forEach { it.initialize() }
    }

    /**
     * @return a modifiable map of [EnchantmentElements][UiEnchant]
     */
    fun asMap(): Map<Key, UiEnchant> {
        return elements.toMutableMap()
    }

    // --- Contains
    private fun containsKey(key: Key): Boolean {
        return elements.containsKey(key)
    }

    private fun containsKey(key: String): Boolean {
        return containsKey(Key.key(Key.MINECRAFT_NAMESPACE, key))
    }

    // --- Get
    operator fun get(key: Key): UiEnchant? {
        return elements[key]
    }

    operator fun get(key: String): UiEnchant? {
        return elements[Key.key(Key.MINECRAFT_NAMESPACE, key)]
    }

    // --- Filter
    fun all(): Collection<UiEnchant> {
        return elements.values
    }

    fun filter(predicate: (UiEnchant) -> Boolean): Collection<UiEnchant> {
        return elements.values.filter(predicate)
    }

    // --- Register/unregister
    fun register(element: UiEnchant) {
        elements[element.key()] = element
    }

    fun unregister(key: Key) {
        elements.remove(key)
    }
}

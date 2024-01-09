package cc.mewcraft.enchantment.gui.api

/**
 * Represents an adapter than converts arbitrary enchantment type to [UiEnchant].
 *
 * All implementations should be put in the package: cc.mewcraft.enchantment.gui.adapter.
 *
 * @param <E> enchantment type
 * @param <T> enchantment target type
 */
interface UiEnchantAdapter<E, T> {
    /**
     * Initialize this adapter.
     *
     * The implementation should register all available enchantments via [UiEnchantProvider.register].
     */
    fun initialize()

    /**
     * Checks whether this adapter can be initialized or not.
     *
     * @return true if this adapter can be initialized; false otherwise
     */
    fun canInitialize(): Boolean

    fun adaptEnchantment(providedEnchant: E): UiEnchant
    fun adaptEnchantmentTarget(providedTarget: T): UiEnchantTarget
}

package cc.mewcraft.enchantment.gui.util

import com.google.common.collect.ListMultimap
import com.google.common.collect.MultimapBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.TranslationArgument

class I18nLore(
    private val format: List<String>,
) {
    private val sanitizers: HashMap<String, () -> Boolean> = HashMap()
    private val registry: HashMap<String, TranslatableComponent> = HashMap() // FIXME 如果这里用 ListMultimap，那就写个扩展函数
    private val replaced: ListMultimap<String, TranslatableComponent> = MultimapBuilder.ListMultimapBuilder.hashKeys().arrayListValues().build()

    /**
     * 注册一个 [TranslatableComponent]，并将其 [TranslatableComponent.key] 设置为 [key]。
     *
     * @param key 将作为 [TranslatableComponent.key]
     */
    fun register(key: String) {
        registry[key] = Component.translatable(key)
    }

    /**
     * 跟 [register] 一样，只不过是一次性注册多个。
     */
    fun register(keys: Iterable<String>) {
        keys.forEach { key -> register(key) }
    }

    /**
     * 为指定的 [TranslatableComponent] 添加 [TranslationArgument]。
     *
     * @param key 翻译的键
     * @param args 翻译的变量
     * @throws IllegalArgumentException 如果 [key] 还未注册
     */
    fun arguments(key: String, args: List<ComponentLike>) {
        val raw = requireNotNull(registry[key])
        val new = raw.arguments(args)
        replaced.put(key, new)
    }

    // FIXME resolve function ambiguity
    // fun arguments(key: String, args: () -> List<ComponentLike>) = arguments(key, args())
    fun arguments(key: String, vararg args: ComponentLike) = arguments(key, args.toList())
    fun arguments(key: String, args: List<String>) = arguments(key, args.map(String::miniMessage))

    // FIXME resolve function ambiguity
    // fun arguments(key: String, args: () -> List<String>) = arguments(key, args())
    fun arguments(key: String, vararg args: String) = arguments(key, args.toList())
    fun arguments(key: String, args: () -> String) = arguments(key, args())

    /**
     * 为指定的 [TranslatableComponent] 添加 [TranslationArgument]。该函数会逐个将 [items]
     * 中的元素作为 [TranslationArgument] 添加到 [key] 所对应的 [TranslatableComponent]
     * 上，最终形成等同于 [items] 长度的一个列表 ([List]<[TranslatableComponent]>)。
     *
     * 例如，如果 [items] 中有3个元素，那么最终同一个 [key] 将对应3个 [TranslatableComponent]。这其中：
     * - 第一个 [TranslatableComponent] 的变量为 [items] 中的第一个元素
     * - 第二个 [TranslatableComponent] 的变量为 [items] 中的第二个元素
     * - 第三个 [TranslatableComponent] 的变量为 [items] 中的第三个元素
     *
     * 最后，这3个 [TranslatableComponent] 都将在替换模板的时候被展开，也就是这3个
     * [TranslatableComponent] 将替换掉模板中 [key] 所对应的内容。
     *
     * @param key 翻译的键
     * @param items 翻译的参数
     * @throws IllegalArgumentException 如果 [key] 还未注册
     */
    fun argumentsMany(key: String, items: List<ComponentLike>) {
        // Hints: items.size 就是最终要复制的行数
        val raw: TranslatableComponent = requireNotNull(registry[key]) // 要复制的 component
        val map: List<TranslatableComponent> = items.map { raw.arguments(it) } // 只替换第一个遇到的 argument
        replaced.putAll(key, map)
    }

    // FIXME resolve function ambiguity
    // fun argumentsMany(key: String, items: () -> List<ComponentLike>) = argumentsMany(key, items())
    fun argumentsMany(key: String, items: List<String>) = argumentsMany(key, items.map(String::miniMessage))
    fun argumentsMany(key: String, items: () -> List<String>) = argumentsMany(key, items())

    /**
     * 注册一个清理逻辑。
     *
     * @param pattern 要移除的字符串模式（包含就算）
     * @param predicate 该清理逻辑的执行前提条件
     */
    fun sanitize(pattern: String, predicate: () -> Boolean) {
        sanitizers[pattern] = predicate
    }

    fun build(): List<TranslatableComponent> {
        val format1 = format.toMutableList()

        // 清理 format，隐藏没必要呈现的内容
        for ((pattern, predicate) in sanitizers.entries) {
            if (predicate.invoke()) {
                format1.removeIf { it.contains(pattern) }
            }
        }

        // 根据清理后的 format，生成最终的 List<TranslatableComponent>
        val componentLore = format1.flatMap { replaced[it] }
        return componentLore
    }
}
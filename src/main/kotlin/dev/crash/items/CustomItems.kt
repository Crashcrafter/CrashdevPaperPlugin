package dev.crash.items

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.crash.INSTANCE
import dev.crash.customItemsMap
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.io.File

object CustomItems {

    data class CustomItem(val material: String, val displayName: String, val lore: List<String>? = null, val cmd: Int? = null, val enchantments: HashMap<String, Int>? = null, val data: HashMap<String, String>? = null)

    internal fun loadItems(){
        val file = File(INSTANCE.dataFolder.path + "/ci.json")
        if(file.exists()){
            val customItems = jacksonObjectMapper().readValue<HashMap<String, CustomItem>>(file)
            customItems.forEach { (name, itemObj) ->
                customItemsMap[name] = itemObj.toItemstack()
            }
        }else {
            file.createNewFile()
            jacksonObjectMapper().writeValue(file, hashMapOf<String, CustomItem>())
        }
    }

    private fun CustomItem.toItemstack(): ItemStack {
        val itemStack = ItemStack(Material.valueOf(material))
        val itemMeta = itemStack.itemMeta
        itemMeta.displayName(Component.text(displayName))
        val componentArray = ArrayList<Component>()
        lore?.forEach {
            componentArray.add(Component.text(it))
        }
        itemMeta.lore(componentArray)
        if(cmd != null) itemMeta.setCustomModelData(cmd)
        data?.forEach {
            itemMeta.persistentDataContainer.set(NamespacedKey(INSTANCE, it.key), PersistentDataType.STRING, it.value)
        }
        itemStack.itemMeta = itemMeta
        enchantments?.forEach {
            itemStack.addUnsafeEnchantment(Enchantment.getByKey(NamespacedKey.fromString(it.key))!!, it.value)
        }
        return itemStack
    }

    fun defaultCustomItem(m: Material, displayName: String, lore: MutableList<String>, cmd: Int=0, data: HashMap<String, String>?=null): ItemStack{
        val itemStack = ItemStack(m)
        val itemMeta = itemStack.itemMeta
        itemMeta.displayName(Component.text(displayName))
        val componentArray = ArrayList<Component>()
        lore.forEach {
            componentArray.add(Component.text(it))
        }
        itemMeta.lore(componentArray)
        itemMeta.setCustomModelData(cmd)
        data?.forEach {
            itemMeta.persistentDataContainer.set(NamespacedKey(INSTANCE, it.key), PersistentDataType.STRING, it.value)
        }
        itemStack.itemMeta = itemMeta
        return itemStack
    }
}

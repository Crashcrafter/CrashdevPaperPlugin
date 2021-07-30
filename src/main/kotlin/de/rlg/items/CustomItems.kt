package de.rlg.items

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.rlg.INSTANCE
import de.rlg.beginnerbook
import de.rlg.customItemsMap
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.persistence.PersistentDataType
import java.io.File

object CustomItems {

    data class CustomItem(val material: String, val displayName: String, val lore: List<String>? = null, val cmd: Int? = null, val data: HashMap<String, String>? = null)

    fun loadItems(){
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

    fun CustomItem.toItemstack(): ItemStack {
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

    fun bookCustomItem(title: String, author: String, pages: Array<String>, cmd: Int=0): ItemStack {
        val itemStack = ItemStack(Material.WRITTEN_BOOK)
        val bm = itemStack.itemMeta as BookMeta
        bm.displayName(Component.text(title))
        bm.setCustomModelData(cmd)
        bm.author(Component.text(author))
        bm.title(Component.text(title))
        val componentArray = ArrayList<Component>()
        pages.forEach {
            componentArray.add(Component.text(it))
        }
        bm.pages(componentArray)
        itemStack.itemMeta = bm
        return itemStack
    }

    fun beginnerBook(): ItemStack = bookCustomItem("Beginner-Guide", "Server Team", beginnerbook, 2)
}

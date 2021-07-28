package de.rlg.items

import de.rlg.customItemsMap
import de.rlg.keysData
import net.kyori.adventure.text.TextComponent
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.lang.NullPointerException

fun ciName(type: Material, cmd: Int): String? {
    return try {
        (getByTypeCmd(type, cmd)!!.itemMeta.displayName() as TextComponent).content()
    }catch (ex: NullPointerException){
        null
    }
}

fun getByTypeCmd(type: Material?, cmd: Int): ItemStack? {
    customItemsMap.forEach {
        if(it.value.type == type && it.value.itemMeta.customModelData == cmd){
            return it.value
        }
    }
    if(type == Material.NAME_TAG){
        return CustomItems.defaultCustomItem(Material.NAME_TAG, keysData[cmd]!!.displayName, arrayListOf(), cmd)
    }
    return null
}
package de.rlg.items

import de.rlg.customItemsMap
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.lang.NullPointerException
import java.util.*

fun ciName(type: Material, cmd: Int): String? {
    return try {
        getByTypeCmd(type, cmd)!!.displayName().toString()
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
    return null
}

fun randomElement(): ItemStack {
    val random = Random()
    val randomId = random.nextInt(10)
    return when {
        randomId == 0 -> CustomItems.chaosElement()
        randomId == 1 -> CustomItems.fireElement()
        randomId <= 3 -> CustomItems.weatherElement()
        randomId <= 5 -> CustomItems.waterElement()
        else -> CustomItems.natureElement()
    }
}
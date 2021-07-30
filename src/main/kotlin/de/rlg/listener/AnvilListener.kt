package de.rlg.listener

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.rlg.INSTANCE
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import java.io.File

class AnvilListener : Listener {

    @EventHandler
    fun onAnvil(e: PrepareAnvilEvent) {
        try {
            val is1 = e.inventory.firstItem!!
            val is2 = e.inventory.secondItem!!
            val result = e.result!!
            is1.getAllEnchantments().forEach {
                result.applyEnchantment(it.key, is1, is2)
            }
            is2.getAllEnchantments().forEach {
                result.applyEnchantment(it.key, is1, is2)
            }
            e.result = result
        } catch (ignored: NullPointerException) {}
    }
}

val maxEnchLevel = HashMap<Enchantment, Int>()

data class EnchantmentSaveObj(val key: String, val level: Int)
fun loadMaxEnchantmentLevel(){
    val file = File(INSTANCE.dataFolder.path + "/enchantments.json")
    if(file.exists()){
        val enchantments = jacksonObjectMapper().readValue<List<EnchantmentSaveObj>>(File(INSTANCE.dataFolder.path + "/enchantments.json"))
        enchantments.forEach {
            try {
                maxEnchLevel[Enchantment.getByKey(NamespacedKey.fromString(it.key))!!] = it.level
            }catch (ex: NullPointerException){}
        }
    }else {
        file.createNewFile()
        val default = mutableListOf<EnchantmentSaveObj>()
        Enchantment.values().forEach {
            default.add(EnchantmentSaveObj(it.key.asString(), it.maxLevel))
        }
        jacksonObjectMapper().writeValue(file, default.toList())
    }
}

fun ItemStack.getEnchLevel(enchantment: Enchantment): Int {
    return if(this.type == Material.ENCHANTED_BOOK){
        (this.itemMeta as EnchantmentStorageMeta).getStoredEnchantLevel(enchantment)
    }else {
        this.getEnchantmentLevel(enchantment)
    }
}

fun ItemStack.getAllEnchantments(): MutableMap<Enchantment, Int> {
    return if(this.type == Material.ENCHANTED_BOOK){
        (this.itemMeta as EnchantmentStorageMeta).storedEnchants
    }else {
        this.enchantments
    }
}

fun ItemStack.applyEnchantment(enchantment: Enchantment, is1: ItemStack, is2: ItemStack){
    val ench1 = is1.getEnchLevel(enchantment)
    val ench2 = is2.getEnchLevel(enchantment)
    val resultLevel = when {
        ench1 == ench2 -> {
            if(ench1 < (maxEnchLevel[enchantment] ?: enchantment.maxLevel)) {
                ench1+1
            }else {
                ench1
            }
        }
        ench1 < ench2 -> ench2
        ench1 > ench2 -> ench1
        else -> {
            ench1
        }
    }
    this.addUnsafeEnchantment(enchantment, resultLevel)
}
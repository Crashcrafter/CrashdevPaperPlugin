package de.rlg.listener

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.EnchantmentStorageMeta

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

fun initEnchLevel(){
    maxEnchLevel[Enchantment.SWEEPING_EDGE] = 5
    maxEnchLevel[Enchantment.DAMAGE_ALL] = 7
    maxEnchLevel[Enchantment.DAMAGE_UNDEAD] = 7
    maxEnchLevel[Enchantment.DAMAGE_ARTHROPODS] = 7
    maxEnchLevel[Enchantment.PROTECTION_FALL] = 5
    maxEnchLevel[Enchantment.PROTECTION_EXPLOSIONS] = 5
    maxEnchLevel[Enchantment.PROTECTION_FIRE] = 5
    maxEnchLevel[Enchantment.PROTECTION_PROJECTILE] = 5
    maxEnchLevel[Enchantment.DIG_SPEED] = 6
    maxEnchLevel[Enchantment.THORNS] = 6
    maxEnchLevel[Enchantment.FIRE_ASPECT] = 3
    maxEnchLevel[Enchantment.ARROW_DAMAGE] = 6
    maxEnchLevel[Enchantment.ARROW_KNOCKBACK] = 3
    maxEnchLevel[Enchantment.KNOCKBACK] = 3
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
            if(ench1 < maxEnchLevel[enchantment]?:enchantment.maxLevel) {
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
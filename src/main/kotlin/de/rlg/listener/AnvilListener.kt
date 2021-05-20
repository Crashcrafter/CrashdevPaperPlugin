package de.rlg.listener

import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareAnvilEvent

class AnvilListener : Listener {

    @EventHandler
    fun onAnvil(e: PrepareAnvilEvent) {
        try {
            val is1 = e.inventory.firstItem
            val is2 = e.inventory.secondItem
            val ench1 = is1!!.getEnchantmentLevel(Enchantment.SWEEPING_EDGE)
            val ench2 = is2!!.getEnchantmentLevel(Enchantment.SWEEPING_EDGE)
            if (ench1 == 3 && ench2 == 4 || ench1 == 4 && ench2 == 3 || ench1 == 4 && ench2 == 4) {
                val result = e.result
                result!!.addUnsafeEnchantment(Enchantment.SWEEPING_EDGE, 4)
                e.inventory.result = result
            }
        } catch (ignored: NullPointerException) {
        }
    }
}
package de.rlg.listener

import de.rlg.redeemKey
import de.rlg.toStringList
import de.rlg.tokenExists
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.ItemDespawnEvent

class DeSpawnListener : Listener {

    @EventHandler
    fun onItemDeSpawn(e: ItemDespawnEvent) {
        val itemStack = e.entity.itemStack
        if (itemStack.hasItemMeta()) {
            val im = itemStack.itemMeta
            if (im.hasLore()) {
                val token = im.lore()!!.toStringList()[1].split(" ").toTypedArray()[1]
                if (tokenExists(token)) {
                    redeemKey(token)
                }
            }
        }
    }
}
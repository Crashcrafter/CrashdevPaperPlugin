package de.rlg.listener

import de.rlg.permission.rankData
import de.rlg.player.rlgPlayer
import de.rlg.toComponentList
import de.rlg.toStringList
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCreativeEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent

class CheatListener : Listener {
    
    @EventHandler
    fun onCreativeInv(e: InventoryCreativeEvent) {
        try {
            val itemStack = e.cursor
            if (itemStack.itemMeta.hasLore()) {
                val list = itemStack.lore()!!.toStringList()
                if (!list.contains("Aus Creative-Inventar")) {
                    list.add("Aus Creative-Inventar")
                    list.add("Von " + e.whoClicked.name)
                    itemStack.lore(list.toComponentList())
                }
            } else {
                val list: MutableList<String> = ArrayList()
                list.add("Aus Creative-Inventar")
                itemStack.lore(list.toComponentList())
            }
        } catch (ignored: NullPointerException) {
        }
    }

    @EventHandler
    fun onGameMode(e: PlayerGameModeChangeEvent) {
        val player = e.player
        if (!player.world.name.contentEquals("event")) {
            if (player.rlgPlayer().rank < rankData.size - 2) {
                if (e.newGameMode == GameMode.SPECTATOR || e.newGameMode == GameMode.CREATIVE) {
                    e.isCancelled = true
                }
            }
        }
    }
}
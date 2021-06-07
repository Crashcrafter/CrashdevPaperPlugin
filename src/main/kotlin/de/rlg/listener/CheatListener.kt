package de.rlg.listener

import de.rlg.INSTANCE
import de.rlg.player.rlgPlayer
import de.rlg.toComponentList
import de.rlg.toStringList
import org.bukkit.GameMode
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCreativeEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.persistence.PersistentDataType

class CheatListener : Listener {
    
    @EventHandler
    fun onCreativeInv(e: InventoryCreativeEvent) {
        if(e.whoClicked.isOp){
            return
        }
        try {
            val itemStack = e.cursor
            val im = itemStack.itemMeta
            im.persistentDataContainer.set(NamespacedKey(INSTANCE, "cheated"), PersistentDataType.STRING, e.whoClicked.name)
            itemStack.itemMeta = im
            if (im.hasLore()) {
                val list = itemStack.lore()!!.toStringList()
                if (!list.contains("Aus Creative-Inventar")) {
                    list.add("Aus Creative-Inventar")
                    list.add("Von " + e.whoClicked.name)
                    itemStack.lore(list.toComponentList())
                }
            } else {
                itemStack.lore(arrayListOf("Aus Creative-Inventar", "Von " + e.whoClicked.name).toComponentList())
            }
        } catch (ignored: NullPointerException) { }
    }

    @EventHandler
    fun onGameMode(e: PlayerGameModeChangeEvent) {
        val player = e.player
        if (!player.rlgPlayer().isMod) {
            if (e.newGameMode == GameMode.SPECTATOR || e.newGameMode == GameMode.CREATIVE) {
                e.isCancelled = true
            }
        }
    }
}
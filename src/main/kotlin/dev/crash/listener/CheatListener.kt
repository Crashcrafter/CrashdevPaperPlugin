package dev.crash.listener

import dev.crash.INSTANCE
import dev.crash.player.crashPlayer
import dev.crash.toComponentList
import dev.crash.toStringList
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
            im.persistentDataContainer.set(NamespacedKey(INSTANCE, "crashCheated"), PersistentDataType.STRING, e.whoClicked.name)
            itemStack.itemMeta = im
            if (im.hasLore()) {
                val list = itemStack.lore()!!.toStringList()
                if (!list.contains("From creative inventory")) {
                    list.add("From creative inventory")
                    list.add("Made by " + e.whoClicked.name)
                    itemStack.lore(list.toComponentList())
                }
            } else {
                itemStack.lore(arrayListOf("From creative inventory", "Made by " + e.whoClicked.name).toComponentList())
            }
        } catch (ignored: NullPointerException) { }
    }

    @EventHandler
    fun onGameMode(e: PlayerGameModeChangeEvent) {
        val player = e.player
        if (!player.crashPlayer().isMod) {
            if (e.newGameMode == GameMode.SPECTATOR || e.newGameMode == GameMode.CREATIVE) {
                e.isCancelled = true
            }
        }
    }
}
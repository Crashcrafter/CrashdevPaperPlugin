package de.rlg.listener

import de.rlg.INSTANCE
import de.rlg.redeemKey
import de.rlg.tokenExists
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.ItemDespawnEvent
import org.bukkit.persistence.PersistentDataType

class DeSpawnListener : Listener {

    @EventHandler
    fun onItemDeSpawn(e: ItemDespawnEvent) {
        val itemStack = e.entity.itemStack
        if (itemStack.type == Material.NAME_TAG && itemStack.itemMeta.hasCustomModelData()) {
            try {
                val token = itemStack.itemMeta.persistentDataContainer.get(NamespacedKey(INSTANCE, "rlgKeyToken"), PersistentDataType.STRING)!!
                if (tokenExists(token)) {
                    redeemKey(token)
                }
            }catch (ex: NullPointerException) {}
        }
    }
}
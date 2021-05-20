package de.rlg.listener

import de.rlg.spawn
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack

class RespawnListener : Listener {

    @EventHandler
    fun onRespawn(respawnEvent: PlayerRespawnEvent){
        respawnEvent.respawnLocation = spawn
    }
}
package dev.crash.listener

import dev.crash.CONFIG
import dev.crash.warps
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerRespawnEvent

class RespawnListener : Listener {

    @EventHandler
    fun onRespawn(respawnEvent: PlayerRespawnEvent){
        respawnEvent.respawnLocation = warps[CONFIG.defaultWarpName]!!
    }
}
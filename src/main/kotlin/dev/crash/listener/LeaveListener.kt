package dev.crash.listener

import dev.crash.*
import dev.crash.player.unload
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class LeaveListener : Listener {

    @EventHandler
    fun onQuit(leaveEvent: PlayerQuitEvent){
        val player: Player = leaveEvent.player
        leaveEvent.quitMessage(Component.text("§c${player.name} left us!"))
        moderator.remove(player)
        updateTabOfPlayers(true)
        player.unload()
    }
}
package de.rlg.listener

import de.rlg.*
import de.rlg.player.unload
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class LeaveListener : Listener {

    @EventHandler
    fun onQuit(leaveEvent: PlayerQuitEvent){
        val player: Player = leaveEvent.player
        amount_Sleeping.remove(player)
        var time = player.world.time
        while (time > 24000) {
            time -= 24000
        }
        leaveEvent.quitMessage(Component.text("§c${player.name} hat uns verlassen!"))
        moderator.remove(player)
        updateTabOfPlayers(true)
        if (amount_Sleeping.size > 0 && time > 12541) {
            calcSleepMessage()
        }
        player.unload()
    }
}
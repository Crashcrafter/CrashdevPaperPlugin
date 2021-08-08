package dev.crash.listener

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent
import dev.crash.dropRange
import dev.crash.player.crashPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerRiptideEvent

class ElytraListener : Listener {

    @EventHandler
    fun onElytra(e: PlayerElytraBoostEvent) {
        val player = e.player
        val crashPlayer = player.crashPlayer()
        if (crashPlayer.elytraCoolDown <= System.currentTimeMillis() || player.isOp) {
            crashPlayer.elytraCoolDown = System.currentTimeMillis() + 1000 * 30
            val location = player.location
            val range: Int = dropRange
            if (location.z < -range || location.z > range || location.x < -range || location.x > range) {
                e.isCancelled = true
            }
        } else {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onRiptide(e: PlayerRiptideEvent) {
        val player = e.player
        val crashPlayer = player.crashPlayer()
        if (crashPlayer.elytraCoolDown <= System.currentTimeMillis() || player.isOp) {
            crashPlayer.elytraCoolDown = System.currentTimeMillis() + 1000 * 30
            val location = player.location
            val range: Int = dropRange
            if (location.z < -range || location.z > range || location.x < -range || location.x > range) {
                player.crashPlayer().disabledMovement = true
            }
        } else {
            player.crashPlayer().disabledMovement = true
        }
    }

    @EventHandler
    fun onPlayerMove(e: PlayerMoveEvent) {
        if(e.player.crashPlayer().disabledMovement) {
            e.isCancelled = true
            e.player.crashPlayer().disabledMovement = false
        }
    }
}
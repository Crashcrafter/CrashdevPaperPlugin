package dev.crash.listener

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent
import dev.crash.dropRange
import dev.crash.player.rlgPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerRiptideEvent

class ElytraListener : Listener {

    @EventHandler
    fun onElytra(e: PlayerElytraBoostEvent) {
        val player = e.player
        val rlgPlayer = player.rlgPlayer()
        if (rlgPlayer.elytraCoolDown <= System.currentTimeMillis() || player.isOp) {
            rlgPlayer.elytraCoolDown = System.currentTimeMillis() + 1000 * 30
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
        val rlgPlayer = player.rlgPlayer()
        if (rlgPlayer.elytraCoolDown <= System.currentTimeMillis() || player.isOp) {
            rlgPlayer.elytraCoolDown = System.currentTimeMillis() + 1000 * 30
            val location = player.location
            val range: Int = dropRange
            if (location.z < -range || location.z > range || location.x < -range || location.x > range) {
                player.rlgPlayer().disabledMovement = true
            }
        } else {
            player.rlgPlayer().disabledMovement = true
        }
    }

    @EventHandler
    fun onPlayerMove(e: PlayerMoveEvent) {
        if(e.player.rlgPlayer().disabledMovement) {
            e.isCancelled = true
            e.player.rlgPlayer().disabledMovement = false
        }
    }
}
package dev.crash.listener

import dev.crash.INSTANCE
import dev.crash.portals
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.scheduler.BukkitRunnable

class PortalListener : Listener {

    @EventHandler
    fun onPlayerPortal(e: PlayerPortalEvent) {
        val block = e.player.location.block
        if (portals.containsKey(block)) {
            val player = e.player
            val location: Location =
                Bukkit.getWorld(portals[block]!!)!!.spawnLocation.add(0.5, 0.0, 0.5)
            object : BukkitRunnable() {
                override fun run() {
                    player.teleport(location)
                }
            }.runTaskLater(INSTANCE, 5L)
            object : BukkitRunnable() {
                override fun run() {
                    if (player.location.world.name !== portals[block]) {
                        player.teleport(location)
                    }
                }
            }.runTaskLater(INSTANCE, 10L)
        }
    }
}
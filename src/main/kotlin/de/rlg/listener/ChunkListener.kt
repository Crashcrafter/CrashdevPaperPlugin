package de.rlg.listener

import de.rlg.*
import de.rlg.player.rlgPlayer
import io.papermc.paper.event.packet.PlayerChunkLoadEvent
import io.papermc.paper.event.packet.PlayerChunkUnloadEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.*

class ChunkListener : Listener {
    @EventHandler
    fun onChunkLoad(e: PlayerChunkLoadEvent) {
        val player = e.player
        if (player.canGenDrops()) {
            val possibility = Random().nextInt(250)
            if (possibility == 100) {
                val chunk = e.chunk
                val block = chunk.getBlock(0, 0, 0)
                if (block.x > -dropRange && block.x < dropRange && block.z > -dropRange && block.z < dropRange) {
                    if(setDrop(chunk)){
                        player.rlgPlayer().dropCoolDown = System.currentTimeMillis() + 1000 * 60 * (10+Random().nextInt(10))
                    }
                }
            }
        }
    }

    @EventHandler
    fun onChunkUnload(e: PlayerChunkUnloadEvent) {
        if (drops.containsKey(e.chunk)) {
            if (!drops[e.chunk]!!.started) {
                unsetDrop(e.chunk, true)
            }
        }
    }
}
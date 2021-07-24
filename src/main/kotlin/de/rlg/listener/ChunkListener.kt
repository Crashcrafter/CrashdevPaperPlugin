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
            val random = Random()
            player.rlgPlayer().dropCoolDown = System.currentTimeMillis() + 1000 * 60 * (10+Random().nextInt(10))
            val possibility = random.nextInt(2500)
            if (possibility == 100) {
                val block = e.chunk.getBlock(0, 0, 0)
                val range: Int = DropRange
                if (block.x > -range && block.x < range && block.z > -range && block.z < range) {
                    val type: Int = getDropType()
                    setDrop(e.chunk, type)
                    println("Set Drop for " + player.name + ", Type=" + type + ",Chunk:" + block.chunk.x + "/" + block.chunk.z)
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
package dev.crash.listener

import dev.crash.checkMessage
import dev.crash.toStringList
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent

class SignListener : Listener {

    @EventHandler
    fun onSign(e: SignChangeEvent) {
        val lines = e.lines().toStringList()
        for (line in lines) {
            if (checkMessage(line, e.player)) {
                e.isCancelled = true
            }
        }
    }
}
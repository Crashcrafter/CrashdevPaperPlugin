package dev.crash

import dev.crash.player.load
import dev.crash.player.unload
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.messaging.PluginMessageListener

lateinit var INSTANCE : Main
class Main : JavaPlugin(), PluginMessageListener {

    override fun onLoad(){
        INSTANCE = this
        println("[INFO] Plugin wird geladen...")
    }

    override fun onEnable() {
        INSTANCE = this
        initServer()
        Bukkit.getOnlinePlayers().forEach {
            it.unload()
            it.load()
        }
        println("[INFO] Plugin wurde geladen...")
    }

    override fun onDisable() {
        Bukkit.getScheduler().pendingTasks.forEach {
            it.cancel()
        }
        allJobs.forEach {
            it.cancel()
        }
        Bukkit.getOnlinePlayers().forEach {
            it.unload()
        }
        Bukkit.getScheduler().cancelTasks(this)
        println("[INFO] Plugin wird deaktiviert...")
    }

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        println("Plugin detected: $channel")
    }
}
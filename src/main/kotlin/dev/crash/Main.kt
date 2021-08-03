package dev.crash

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.crash.player.load
import dev.crash.player.rlgPlayer
import dev.crash.player.unload
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.messaging.PluginMessageListener
import org.bukkit.scheduler.BukkitRunnable
import java.io.File

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
        object : BukkitRunnable(){
            override fun run() {
                Bukkit.getOnlinePlayers().forEach {
                    it.rlgPlayer().save()
                }
                val backupDirectory = File(INSTANCE.dataFolder.path + "/playerBackup/")
                backupDirectory.delete()
                copyDirectory(File(INSTANCE.dataFolder.path + "/player/"), backupDirectory)
            }
        }.runTaskTimerAsynchronously(INSTANCE, 0, 20*60)
        println("[INFO] Plugin wurde geladen...")
    }

    override fun onDisable() {
        allJobs.forEach {
            it.cancel()
        }
        Bukkit.getOnlinePlayers().forEach {
            it.unload()
        }
        jacksonObjectMapper().writeValue(File(INSTANCE.dataFolder.path + "/keychests.json"), keyChests.toStringKeyMap())
        jacksonObjectMapper().writeValue(File(INSTANCE.dataFolder.path + "/portals.json"), portals.toStringKeyMap())
        Bukkit.getScheduler().pendingTasks.forEach {
            it.cancel()
        }
        Bukkit.getScheduler().cancelTasks(this)
        println("[INFO] Plugin wird deaktiviert...")
    }

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        println("Plugin detected: $channel")
    }
}
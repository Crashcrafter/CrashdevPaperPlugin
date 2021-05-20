package de.rlg

import de.rlg.player.load
import de.rlg.player.unload
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.transactions.TransactionManager

lateinit var INSTANCE : Main
class Main : JavaPlugin() {

    override fun onLoad(){
        INSTANCE = this
        println("[INFO] Plugin wird geladen...")
    }

    override fun onEnable() {
        INSTANCE = this
        initServer()
        Bukkit.getOnlinePlayers().forEach {
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
}
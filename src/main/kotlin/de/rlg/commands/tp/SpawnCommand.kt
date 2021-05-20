package de.rlg.commands.tp

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.rlg.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.io.File

class SpawnCommand : CommandExecutor,TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if (player.isOp) {
            if(args.isNotEmpty() && args[0].contentEquals("set")){
                setSpawn(player.location.block.location)
                player.sendMessage("§2Set new Spawn!")
            }else {
                player.teleport(spawn)
                player.sendMessage("§2Du wurdest zum Spawn teleportiert!")
            }
        }else{
            delayedTeleport(player, spawn)
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String>? {
        val player = sender.asPlayer()
        if(player.isOp) return mutableListOf("set")
        return null
    }

}

fun loadSpawn(){
    val spawnFile = File("spawn.json")
    if(spawnFile.exists()){
        val jsonObj = jacksonObjectMapper().readValue<SpawnObj>(spawnFile.readText())
        spawn = Location(Bukkit.getWorld(jsonObj.world), jsonObj.x, jsonObj.y, jsonObj.z, -90.0F, 0.0F)
    }else {
        spawnFile.createNewFile()
        spawn = Location(Bukkit.getWorlds()[0], 0.0, 0.0, 0.0, -90.0F, 0.0F)
        val jsonObj = SpawnObj(spawn.x, spawn.y, spawn.z, spawn.world.name)
        jacksonObjectMapper().writeValue(spawnFile, jsonObj)
    }
}

fun setSpawn(location: Location){
    spawn = location.add(0.5, 0.0, 0.5)
    val jsonObj = SpawnObj(spawn.x, location.y, spawn.z, location.world.name)
    jacksonObjectMapper().writeValue(File("spawn.json"), jsonObj)
}

fun delayedTeleport(player: Player, location: Location){
    allJobs.add(GlobalScope.launch {
        var lastpos = player.location
        var i = 0
        player.sendMessage("§6Teleport in")
        while (true) {
            player.sendMessage("§6" + (3 - i) + "...")
            delay(1000L)
            if (lastpos.z != player.location.z || lastpos.y != player.location
                    .y || lastpos.x != player.location.x
            ) {
                player.sendMessage("§4Du hast dich bewegt! Stehe 3 Sekunden still, um dich zu teleportieren.")
                break
            }
            lastpos = player.location
            i++
            if (i == 3) {
                Bukkit.getScheduler().runTask(INSTANCE, Runnable {
                    player.teleport(location)
                    player.sendMessage("§2Du wurdest teleportiert!")
                })
                break
            }
        }
    })
}
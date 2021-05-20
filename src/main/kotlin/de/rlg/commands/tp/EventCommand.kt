package de.rlg.commands.tp

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.rlg.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.io.File

class EventCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if (player.isOp) {
            if(args.isNotEmpty() && args[0].contentEquals("set")){
                setEvent(player.location.block.location)
                player.sendMessage("§2Set new Event!")
            }else {
                player.teleport(event)
                player.sendMessage("§2Du wurdest zum Event teleportiert!")
            }
        }else{
            delayedTeleport(player, event)
        }
        return true
    }
}

fun loadEvent(){
    val eventFile = File("event.json")
    if(eventFile.exists()){
        val jsonObj = jacksonObjectMapper().readValue<SpawnObj>(eventFile.readText())
        event = Location(Bukkit.getWorld(jsonObj.world), jsonObj.x, jsonObj.y, jsonObj.z)
    }else {
        eventFile.createNewFile()
        event = Location(Bukkit.getWorlds()[0], 0.0, 0.0, 0.0)
        val jsonObj = SpawnObj(event.x, event.y, event.z, event.world.name)
        jacksonObjectMapper().writeValue(eventFile, jsonObj)
    }
}

fun setEvent(location: Location){
    event = location.add(0.5, 0.0, 0.5)
    val jsonObj = SpawnObj(event.x, location.y, event.z, location.world.name)
    jacksonObjectMapper().writeValue(File("event.json"), jsonObj)
}
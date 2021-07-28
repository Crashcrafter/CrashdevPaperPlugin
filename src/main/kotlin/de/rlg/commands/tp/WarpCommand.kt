package de.rlg.commands.tp

import de.rlg.*
import de.rlg.player.rlgPlayer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.MultipleFacing
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class WarpCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if(args.isEmpty()){
            player.sendMessage("§4Du musst einen Namen eingeben!")
            return true
        }
        if (player.isOp && args[0].contentEquals("set")) {
            val newPoint = player.location.block.location.add(0.5, 0.0, 0.5)
            newPoint.yaw = player.location.yaw
            newPoint.pitch = player.location.pitch
            val key = args[1]
            setWarp(key, newPoint)
            player.sendMessage("§2Neuer Warp wurde mit dem Namen $key gesetzt!")
            return true
        }
        else if (player.isOp && args[0].contentEquals("delete")){
            val key = args[1]
            player.sendMessage("§2Warp $key wurde gelöscht!")
            deleteWarp(key)
            return true
        }
        else{
            teleportToWarppoint(player, args[0])
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
        if(args.size == 1){
            val result = warps.keys.toMutableList()
            if(player.isOp){
                result.add("set")
                result.add("delete")
            }
            return result
        }else if(args[0] == "delete"){
            return warps.keys.toMutableList()
        }
        return null
    }
}

fun teleportToWarppoint(player: Player, key: String){
    val location = warps[key]
    if(location == null){
        player.sendMessage("§4Es gibt keinen Warppoint mit diesem Namen!")
        return
    }
    delayedTeleport(player, warps[key]!!)
}

fun setWarp(key: String, location: Location){
    warps[key] = location
    INSTANCE.config.set("warps.$key", location)
    INSTANCE.saveConfig()
}

fun deleteWarp(key: String){
    warps.remove(key)
    INSTANCE.config.set("warps.$key", null)
}

fun loadWarps(){
    val section = INSTANCE.config.getConfigurationSection("warps") ?: return
    section.getKeys(false).forEach {
        warps[it] = INSTANCE.config.getLocation("warps.$it")!!
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun delayedTeleport(player: Player, location: Location){
    if(player.isOp){
        player.teleport(location)
        player.sendMessage("§2Du wurdest teleportiert!")
        return
    }
    val rlgPlayer = player.rlgPlayer()
    if(rlgPlayer.dropCoolDown  <= System.currentTimeMillis() + 1000 * 30){
        rlgPlayer.dropCoolDown = System.currentTimeMillis() + 1000 * 30
    }
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
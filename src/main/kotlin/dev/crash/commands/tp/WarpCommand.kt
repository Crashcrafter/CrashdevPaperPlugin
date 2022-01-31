package dev.crash.commands.tp

import dev.crash.*
import dev.crash.player.crashPlayer
import kotlinx.coroutines.DelicateCoroutinesApi
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

class WarpCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if(!player.hasPermission("crash.warp")) return true
        if(args.isEmpty()){
            player.sendMessage("§4You must enter a valid warp name!")
            return true
        }else{
            if(player.hasPermission("crash.setwarp")){
                if(args[0] == "set"){
                    val newPoint = player.location.block.location.add(0.5, 0.0, 0.5)
                    newPoint.yaw = player.location.yaw
                    newPoint.pitch = player.location.pitch
                    val key = args[1]
                    setWarp(key, newPoint)
                    player.sendMessage("§2New warp with name $key was set!")
                    return true
                }else if(args[0] == "delete"){
                    val key = args[1]
                    player.sendMessage("§2Warp $key was deleted!")
                    deleteWarp(key)
                    return true
                }else {
                    teleportToWarp(player, args[0])
                }
            }else {
                teleportToWarp(player, args[0])
            }
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

fun teleportToWarp(player: Player, key: String){
    val location = warps[key]
    if(location == null){
        player.sendMessage("§4Invalid warp name!")
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
    INSTANCE.saveConfig()
}

internal fun loadWarps(){
    val section = INSTANCE.config.getConfigurationSection("warps") ?: return
    section.getKeys(false).forEach {
        warps[it] = INSTANCE.config.getLocation("warps.$it")!!
    }
}

@Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
@OptIn(DelicateCoroutinesApi::class)
fun delayedTeleport(player: Player, location: Location, after: (()->Unit)? = null){
    if(player.isOp){
        player.teleport(location)
        player.sendMessage("§2You have been teleported!")
        after?.invoke()
        return
    }
    val crashPlayer = player.crashPlayer()
    if(crashPlayer.dropCoolDown  <= System.currentTimeMillis() + 1000 * 30){
        crashPlayer.dropCoolDown = System.currentTimeMillis() + 1000 * 30
    }
    allJobs.add(GlobalScope.launch {
        var lastPos = player.location
        var i = 0
        player.sendMessage("§6Teleport in")
        while (true) {
            player.sendMessage("§6" + (3 - i) + "...")
            delay(1000L)
            if (lastPos.z != player.location.z || lastPos.y != player.location
                    .y || lastPos.x != player.location.x
            ) {
                player.sendMessage("§4Teleport cancelled! You must stand still for 3 seconds.")
                break
            }
            lastPos = player.location
            i++
            if (i == 3) {
                Bukkit.getScheduler().runTask(INSTANCE, Runnable {
                    player.teleport(location)
                    player.sendMessage("§2You've been teleported!")
                    after?.invoke()
                })
                break
            }
        }
    })
}
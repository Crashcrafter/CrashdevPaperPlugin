package dev.crash.commands.admin

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.crash.*
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import java.io.File

class KeyCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if (player.hasPermission("crash.key")) {
            if (args[0] == "chest") {
                val block = player.rayTraceBlocks(4.0)!!.hitBlock
                if(block == null) {
                    player.sendMessage("§4Du musst eine Kiste anschauen!")
                    return true
                }
                if (args[1] == "add") {
                    if(args.size < 3) return true
                    val key = Key.byName(args[2]) ?: return true
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "summon minecraft:armor_stand ${block.x+0.5} ${block.y} ${block.z+0.5} {CustomName:'{\"text\":\"${key.crateName}\"}',CustomNameVisible:1,NoGravity:1b,Invulnerable:1,Invisible:1,Small:1,Tags:[\"crates\"]}")
                    addKeyChest(block, key.id)
                } else if (args[1] == "remove") {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute positioned ${block.x+0.5} ${block.y} ${block.z+0.5} run kill @e[tag=crates,distance=..1,limit=1]")
                    removeKeyChest(block)
                } else {
                    player.sendMessage("&4Enter valid argument")
                }
            } else if (args[0] == "create") {
                if(args.size < 3){
                    player.sendMessage("§4Not enough arguments! Please use full command.")
                }
                val amount: Int = args[2].toInt()
                for(i in 0 until amount){
                    val target = if(args.size == 4) Bukkit.getPlayer(args[3]) ?: player else player
                    val key = Key.byName(args[1]) ?: return true
                    target.inventory.addItem(genKey(key.id))
                }
            } else {
                player.sendMessage("&4Enter valid argument")
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
        if (player.hasPermission("crash.key")) {
            if (args.size == 1) {
                return mutableListOf("chest", "create")
            } else if (args.size == 2) {
                if (args[0] == "create") {
                    return Key.getNames().toMutableList()
                } else if (args[0] == "chest") {
                    return mutableListOf("add", "remove")
                }
            } else if (args.size == 3) {
                if (args[0] == "chest") {
                    return Key.getNames().toMutableList()
                } else if (args[0] == "create") {
                    return mutableListOf("1", "2", "3", "5")
                }
            }
        }
        return null
    }
}

fun addKeyChest(block: Block, type: Int) {
    keyChests[block] = type
    saveKeyChests()
}

fun removeKeyChest(block: Block) {
    keyChests.remove(block)
    saveKeyChests()
}

private fun saveKeyChests(){
    val resultKeyChests = hashMapOf<String, Int>()
    keyChests.forEach {
        resultKeyChests[it.key.toPositionString()] = it.value
    }
    jacksonObjectMapper().writeValue(File(INSTANCE.dataFolder.path + "/keychests.json"), resultKeyChests)
}
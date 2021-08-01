package de.rlg.commands.admin

import de.rlg.*
import de.rlg.player.rlgPlayer
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class KeyCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if (player.hasPermission("rlg.key")) {
            if (args[0].equals("chest", ignoreCase = true)) {
                val block = player.rayTraceBlocks(4.0)!!.hitBlock
                if(block == null) {
                    player.sendMessage("§4Du musst eine Kiste anschauen!")
                    return true
                }
                if (args[1].equals("add", ignoreCase = true)) {
                    val key = Key.byName(args[2]) ?: return true
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "summon minecraft:armor_stand ${block.x+0.5} ${block.y} ${block.z+0.5} {CustomName:'{\"text\":\"${key.crateName}\"}',CustomNameVisible:1,NoGravity:1b,Invulnerable:1,Invisible:1,Small:1,Tags:[\"crates\"]}")
                    addKeyChest(block, key.id)
                } else if (args[1].equals("remove", ignoreCase = true)) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute positioned ${block.x+0.5} ${block.y} ${block.z+0.5} run kill @e[tag=crates,distance=..1,limit=1]")
                    removeKeyChest(block)
                } else {
                    player.sendMessage("&4Enter valid argument")
                }
            } else if (args[0].equals("create", ignoreCase = true)) {
                val amount: Int = try {
                    args[2].toInt()
                } catch (exception: IndexOutOfBoundsException) {
                    1
                }
                for(i in 0 until amount){
                    try {
                        val target = if(args.size == 4) Bukkit.getPlayer(args[3]) ?: player else player
                        val key = Key.byName(args[1]) ?: return true
                        target.inventory.addItem(genKey(key.id))
                    }catch (ex: IndexOutOfBoundsException) {
                        player.sendMessage("§4Bitte gib einen Typ an!")
                    }
                }
            } else {
                player.sendMessage("&4Enter valid argument")
            }
        } else {
            player.sendMessage("&4You dont have permissions to do that")
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
        if (player.rlgPlayer().isMod) {
            if (args.size == 1) {
                return mutableListOf("chest", "create")
            } else if (args.size == 2) {
                if (args[0].equals("create", ignoreCase = true)) {
                    return Key.getNames().toMutableList()
                } else if (args[0].equals("chest", ignoreCase = true)) {
                    return mutableListOf("add", "remove")
                }
            } else if (args.size == 3) {
                if (args[0].contentEquals("chest")) {
                    return Key.getNames().toMutableList()
                } else if (args[0].contentEquals("create")) {
                    return mutableListOf("1", "2", "3", "5")
                }
            }
        }
        return null
    }
}

fun addKeyChest(block: Block, type: Int) {
    val blockString = block.toPositionString()
    transaction {
        if(KeyChestTable.select(where = {KeyChestTable.chestPos eq blockString}).empty()){
            KeyChestTable.insert {
                it[chestPos] = blockString
                it[KeyChestTable.type] = type
            }
        }
    }
    keyChests[block] = type
}

fun removeKeyChest(block: Block) {
    transaction {
        KeyChestTable.deleteWhere {
            KeyChestTable.chestPos eq block.toPositionString()
        }
    }
    keyChests.remove(block)
}
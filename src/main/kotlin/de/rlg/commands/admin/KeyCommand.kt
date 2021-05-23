package de.rlg.commands.admin

import de.rlg.*
import de.rlg.player.rlgPlayer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.inventory.ItemStack
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
                    val type = when (args[2]) {
                        "common" -> {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "summon minecraft:armor_stand ${block.x+0.5} ${block.y} ${block.z+0.5} {CustomName:'{\"text\":\"Common Crate\",\"color\":\"green\"}',CustomNameVisible:1,NoGravity:1b,Invulnerable:1,Invisible:1,Small:1,Tags:[\"crates\"]}")
                            1
                        }
                        "epic" -> {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "summon minecraft:armor_stand ${block.x+0.5} ${block.y} ${block.z+0.5} {CustomName:'{\"text\":\"Epic Crate\",\"color\":\"light_purple\"}',CustomNameVisible:1,NoGravity:1b,Invulnerable:1,Small:1,Invisible:1,Tags:[\"crates\"]}")
                            2
                        }
                        "supreme" -> {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "summon minecraft:armor_stand ${block.x+0.5} ${block.y} ${block.z+0.5} {CustomName:'{\"text\":\"Supreme Crate\",\"color\":\"yellow\"}',CustomNameVisible:1,NoGravity:1b,Invulnerable:1,Small:1,Invisible:1,Tags:[\"crates\"]}")
                            3
                        }
                        "vote" -> {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "summon minecraft:armor_stand ${block.x+0.5} ${block.y} ${block.z+0.5} {CustomName:'{\"text\":\"Vote Crate\",\"color\":\"red\"}',CustomNameVisible:1,NoGravity:1b,Invulnerable:1,Small:1,Invisible:1,Tags:[\"crates\"]}")
                            4
                        }
                        "level" -> {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "summon minecraft:armor_stand ${block.x+0.5} ${block.y} ${block.z+0.5} {CustomName:'{\"text\":\"Level Crate\",\"color\":\"aqua\"}',CustomNameVisible:1,NoGravity:1b,Invulnerable:1,Small:1,Invisible:1,Tags:[\"crates\"]}")
                            5
                        }
                        else -> {
                            player.sendMessage("Bitte einen zulässigen Typ angeben")
                            0
                        }
                    }
                    if (type != 0) {
                        addKeyChest(block, type)
                    }
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
                        player.inventory.addItem(when(args[1]){
                            "common" -> genKey(1)
                            "epic" -> genKey(2)
                            "supreme" -> genKey(3)
                            "vote" -> genKey(4)
                            "level" -> genKey(5)
                            else -> ItemStack(Material.STRUCTURE_VOID)
                        })
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
                    return mutableListOf("common", "epic", "supreme", "vote", "level")
                } else if (args[0].equals("chest", ignoreCase = true)) {
                    return mutableListOf("add", "remove")
                }
            } else if (args.size == 3) {
                if (args[0].contentEquals("chest")) {
                    return mutableListOf("common", "epic", "supreme", "vote", "level")
                } else if (args[0].contentEquals("create")) {
                    return mutableListOf("1", "2", "3", "5")
                }
            }
        }
        return null
    }
}

fun addKeyChest(block: Block, type: Int) {
    val blockString = block.toSQLString()
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
            KeyChestTable.chestPos eq block.toSQLString()
        }
    }
    keyChests.remove(block)
}
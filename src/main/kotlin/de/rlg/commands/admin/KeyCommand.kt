package de.rlg.commands.admin

import de.rlg.*
import de.rlg.player.rlgPlayer
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
                    var type = 0
                    when (args[2]) {
                        "common" -> type = 1
                        "epic" -> type = 2
                        "supreme" -> type = 3
                        "vote" -> type = 4
                        "level" -> type = 5
                        else -> player.sendMessage("Bitte einen zulässigen Typ angeben")
                    }
                    if (type != 0) {
                        addKeyChest(block, type)
                    }
                } else if (args[1].equals("remove", ignoreCase = true)) {
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
                    player.inventory.addItem(when(args[1]){
                        "common" -> genKey(1)
                        "epic" -> genKey(2)
                        "supreme" -> genKey(3)
                        "vote" -> genKey(4)
                        "level" -> genKey(5)
                        else -> ItemStack(Material.STRUCTURE_VOID)
                    })
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
package dev.crash.commands.admin

import dev.crash.asPlayer
import dev.crash.customItemsMap
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.inventory.ItemStack

class CICommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if(!player.hasPermission("rlg.ci")) return false
        if(args.isEmpty()) {
            player.sendMessage("§4Please specify an item!")
            return true
        }
        val itemString = args[0]
        var amount = 1
        if(args.size >= 2) amount = try { args[1].toInt() } catch (ex: Exception) {1}
        if(!customItemsMap.containsKey(itemString)){
            player.sendMessage("§4Item not found")
            return true
        }
        val itemStack: ItemStack = customItemsMap[itemString]!!
        itemStack.amount = amount
        player.inventory.addItem(itemStack)
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String>? {
        if (args.size == 1) {
            val current: String = args[0]
            val list: MutableList<String> = ArrayList()
            for (key in customItemsMap.keys) {
                if (key.startsWith(current)) {
                    list.add(key)
                }
            }
            return list
        } else if (args.size == 2) return arrayListOf("1", "8", "16", "32", "64")
        return null
    }
}
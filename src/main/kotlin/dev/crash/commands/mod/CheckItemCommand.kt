package dev.crash.commands.mod

import dev.crash.INSTANCE
import dev.crash.asPlayer
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.persistence.PersistentDataType

class CheckItemCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if(!player.hasPermission("crash.checkitem")) return true
        val item = player.inventory.itemInMainHand
        if(item.type == Material.AIR) {
            player.sendMessage("§4You have no item in your main hand!")
            return true
        }
        if(item.itemMeta.persistentDataContainer.has(NamespacedKey(INSTANCE, "crashCheated"), PersistentDataType.STRING)){
            val cheaterName = item.itemMeta.persistentDataContainer.get(NamespacedKey(INSTANCE, "crashCheated"), PersistentDataType.STRING)
            player.sendMessage("§6The item was made by $cheaterName in creative mode!")
        }else if(item.itemMeta.persistentDataContainer.has(NamespacedKey(INSTANCE, "craftedBy"), PersistentDataType.STRING)){
            val crafterName = item.itemMeta.persistentDataContainer.get(NamespacedKey(INSTANCE, "craftedBy"), PersistentDataType.STRING)
            player.sendMessage("§6This item was crafted by $crafterName!")
        }else player.sendMessage("§4Item has no data!")
        return true
    }
}
package de.rlg.commands.mod

import de.rlg.INSTANCE
import de.rlg.asPlayer
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.persistence.PersistentDataType

class CheckItemCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        val item = player.inventory.itemInMainHand
        if(!item.hasItemMeta()){
            player.sendMessage("§4Item hat keine Daten!")
            return true
        }
        if(item.itemMeta.persistentDataContainer.has(NamespacedKey(INSTANCE, "rlgCheated"), PersistentDataType.STRING)){
            val cheaterName = item.itemMeta.persistentDataContainer.get(NamespacedKey(INSTANCE, "rlgCheated"), PersistentDataType.STRING)
            player.sendMessage("§6Das Item wurde von $cheaterName im Creative benutzt/erzeugt!")
        }else if(item.itemMeta.persistentDataContainer.has(NamespacedKey(INSTANCE, "craftedBy"), PersistentDataType.STRING)){
            val crafterName = item.itemMeta.persistentDataContainer.get(NamespacedKey(INSTANCE, "craftedBy"), PersistentDataType.STRING)
            player.sendMessage("§6Das Item wurde von $crafterName gecraftet!")
        }else player.sendMessage("§4Item hat keine Daten!")
        return true
    }
}
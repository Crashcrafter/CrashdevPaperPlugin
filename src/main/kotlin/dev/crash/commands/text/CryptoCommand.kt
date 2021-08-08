package dev.crash.commands.text

import dev.crash.*
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.text.SimpleDateFormat

class CryptoCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val time = SimpleDateFormat("HH:mm:ss").format(lastUpdate)
        sender.sendMessage("§a§l§nCurrent prices:§r\n§7Last refresh: $time§r\n§eBitcoin: §2${prices[Material.STICK]!![1]!!.withPoints()} Credits§r\n§7Ethereum: §2${prices[Material.STICK]!![2]!!.withPoints()} Credits§r\n§fLitecoin: §2${prices[Material.STICK]!![3]!!.withPoints()} Credits§r\n" +
                "§bNano: §2${prices[Material.STICK]!![5]!!.withPoints()} Credits§r\n§6Dogecoin: §2${prices[Material.STICK]!![4]!!.withPoints()} Credits§r\n§l§n§cPrices are refreshing every 5 minutes!")
        return true
    }
}
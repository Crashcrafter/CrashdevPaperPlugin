package de.rlg.commands.text

import de.rlg.*
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.text.SimpleDateFormat

class CryptoCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val time = SimpleDateFormat("HH:mm:ss").format(lastUpdate)
        sender.sendMessage("§a§l§nAktuelle Kurse:§r\n§7Letztes Update: $time Uhr§r\n§eBitcoin: §2${btcPrice!!.withPoints()} Credits§r\n§7Ethereum: §2${ethPrice!!.withPoints()} Credits§r\n§fLitecoin: §2${ltcPrice!!.withPoints()} Credits§r\n" +
                "§bNano: §2${nanoPrice!!.withPoints()} Credits§r\n§6Dogecoin: §2${dogePrice!!.withPoints()} Credits§r\n§l§n§cPreise werden alle 5 Minuten aktualisiert!")
        return true
    }
}
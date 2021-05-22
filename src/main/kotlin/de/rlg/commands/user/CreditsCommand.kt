package de.rlg.commands.user

import de.rlg.*
import de.rlg.player.rlgPlayer
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class CreditsCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if (args.isEmpty()) {
            player.sendMessage("§6Dein aktueller Kontostand: " + player.rlgPlayer().balance.withPoints() + " Credits")
        } else if (args[0].contentEquals("transfer")) {
            try {
                val target: Player
                try {
                    target = Bukkit.getPlayer(args[1])!!
                } catch (ignored: ArrayIndexOutOfBoundsException) {
                    player.sendMessage("Bitte gib einen Spieler an!")
                    return true
                }
                val amount: Long = try {
                    args[2].toLong()
                } catch (ignored: ArrayIndexOutOfBoundsException) {
                    player.sendMessage("Bitte gib eine Anzahl an!")
                    return true
                }
                if (transferBalance(player, target, amount)) {
                    player.sendMessage("§aTransaktion war erfolgreich")
                }
            } catch (e: NullPointerException) {
                player.sendMessage("§4Bitte gib einen gültigen Spieler/gültige Menge an Credits an")
            } catch (e: NumberFormatException) {
                player.sendMessage("§4Bitte gib einen gültigen Spieler/gültige Menge an Credits an")
            }
        } else if (args[0].contentEquals("add")) {
            if (player.isOp) {
                try {
                    val target: Player = Bukkit.getPlayer(args[1])!!
                    val amount: Long = args[2].toLong()
                    giveBalance(target, amount, player.name)
                    player.sendMessage("§2Dem Spieler " + target.name + " wurden " + amount + " Credits gegeben")
                } catch (e: NullPointerException) {
                    player.sendMessage("§4Bitte gib einen gültigen Spieler/gültige Menge an Credits an")
                } catch (e: NumberFormatException) {
                    player.sendMessage("§4Bitte gib einen gültigen Spieler/gültige Menge an Credits an")
                }
            }
        } else if (args[0].contentEquals("remove")) {
            if (player.isOp) {
                try {
                    val target: Player = Bukkit.getPlayer(args[1])!!
                    val amount: Long = args[2].toLong()
                    giveBalance(target, -amount, player.name)
                    player.sendMessage("§2Dem Spieler " + target.name + " wurden " + amount + " Credits entfernt")
                } catch (e: NullPointerException) {
                    player.sendMessage("§4Bitte gib einen gültigen Spieler/gültige Menge an Credits an")
                } catch (e: NumberFormatException) {
                    player.sendMessage("§4Bitte gib einen gültigen Spieler/gültige Menge an Credits an")
                }
            }
        } else if (args[0].contentEquals("info")) {
            if (player.isOp) {
                try {
                    val target: Player = Bukkit.getPlayer(args[1])!!
                    player.sendMessage("§6" + target.name + " hat " + target.rlgPlayer().balance + " Credits")
                } catch (e: NullPointerException) {
                    player.sendMessage("§4Bitte gib einen gültigen Spieler/gültige Menge an Credits an")
                } catch (e: NumberFormatException) {
                    player.sendMessage("§4Bitte gib einen gültigen Spieler/gültige Menge an Credits an")
                }
            }
        } else if (args[0].contentEquals("shop")) {
            if (player.isOp) {
                shopVillager(player.location)
            }
        } else if (args[0].contentEquals("blackmarket")) {
            if (player.isOp) {
                blackMarketVillager(player.location)
            }
        } else if (args[0].contentEquals("score")) {
            player.sendMessage(creditsScoreBoard)
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String>? {
        if (args.size == 1) {
            val list: MutableList<String> = mutableListOf("transfer", "score")
            if (sender.isOp) list.addAll(mutableListOf("add", "remove", "info", "shop", "blackmarket"))
            return list
        } else if (args.size == 3) {
            if (args[0].contentEquals("transfer") || args[0].contentEquals("add") || args[0].contentEquals("remove")) {
                return mutableListOf("10", "100", "1000")
            }
        }
        return null
    }
}
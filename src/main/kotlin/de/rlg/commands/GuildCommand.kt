package de.rlg.commands

import de.rlg.*
import de.rlg.player.rlgPlayer
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import java.util.*
import kotlin.collections.HashMap

class GuildCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        val rlgPlayer = player.rlgPlayer()
        when(args.size){
            1 -> {
                when(args[0]){
                    "setup" -> guildSetup(player, "")
                    "leave" -> rlgPlayer.removeFromGuild()
                    "delete" -> rlgPlayer.deleteGuild()
                    "accept" -> {
                        if(inviteTargetMap.containsKey(player.uniqueId)){
                            val inviter = Bukkit.getPlayer(inviteTargetMap[player.uniqueId]!!)
                            when {
                                inviter == null -> {
                                    player.sendMessage("§4Der andere Spieler ist offline gegangen!")
                                    inviteTargetMap.remove(player.uniqueId)
                                    return true
                                }
                                inviter.rlgPlayer().guildId == 0 -> {
                                    player.sendMessage("§4Der andere Spieler ist in keiner Guild!")
                                    inviteTargetMap.remove(player.uniqueId)
                                    return true
                                }
                                rlgPlayer.guildId != 0 -> {
                                    player.sendMessage("§4Du bist bereits in einer Guild!")
                                    inviteTargetMap.remove(player.uniqueId)
                                    return true
                                }
                                else -> rlgPlayer.joinGuild(inviter.rlgPlayer().guildId)
                            }
                        }else {
                            player.sendMessage("§4Du hast keine offenen Einladungen!")
                        }
                    }
                    "decline" -> {
                        if(inviteTargetMap.containsKey(player.uniqueId)){
                            Bukkit.getPlayer(inviteTargetMap[player.uniqueId]!!)?.sendMessage("§cEinladung an ${player.name} wurde abgelehnt.")
                            player.sendMessage("§aEinladung erfolgreich abgelehnt")
                            inviteTargetMap.remove(player.uniqueId)
                        }else {
                            player.sendMessage("§4Du hast keine offenen Einladungen!")
                        }
                    }
                }
            }
            2 -> {
                when(args[0]){
                    "setup" -> {
                        if(args[1] == "cancel"){
                            guildSetupProgress.remove(player)
                            player.sendMessage("§aGuild Setup wurde abgebrochen!")
                        }
                    }
                    "invite" -> {
                        if(rlgPlayer.guildId == 0) {
                            player.sendMessage("§4Du bist in keiner Guild!")
                            return true
                        }
                        val target = Bukkit.getPlayer(args[1])
                        if(target == null){
                            player.sendMessage("§4Du musst einen gültigen Spieler angeben!")
                            return true
                        }
                        val targetRLGPlayer = target.rlgPlayer()
                        if(targetRLGPlayer.guildId != 0){
                            player.sendMessage("§4Der Spieler ist bereits in einer Guild!")
                            return true
                        }
                        else if(inviteTargetMap.containsKey(target.uniqueId)){
                            player.sendMessage("§4Der Spieler hat bereits eine Einladung in eine andere Guild!")
                            return true
                        }
                        inviteTargetMap[target.uniqueId] = player.uniqueId
                        target.sendMessage("§bDu wurdest von ${player.name} in die Guild ${rlgPlayer.guild()!!.name} eingeladen!\n\nDu kannst diese Einladung mit /guild accept annehmen oder mit /guild decline ablehnen.")
                    }
                }
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
        return when(args.size){
            1 -> {
                val result = arrayListOf<String>()
                try { if(player.rlgPlayer().guild()!!.owner_uuid == player.uniqueId.toString()) result.add("delete") else result.add("leave") }catch (ex: NullPointerException) {
                    result.addAll(arrayListOf("accept", "setup"))}
                result
            }
            2 -> {
                when(args[0]){
                    "setup" -> arrayListOf("cancel")
                    else -> null
                }
            }
            else -> null
        }
    }
}

val inviteTargetMap = HashMap<UUID, UUID>()
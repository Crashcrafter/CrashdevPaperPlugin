package dev.crash.commands

import dev.crash.*
import dev.crash.player.rlgPlayer
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
            0 -> {
                return true
            }
            1 -> {
                when(args[0]){
                    "setup" -> guildSetup(player, "")
                    "leave" -> rlgPlayer.removeFromGuild("${player.name} hat die Guild verlassen!")
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
                                else -> {
                                    inviteTargetMap.remove(player.uniqueId)
                                    rlgPlayer.joinGuild(inviter.rlgPlayer().guildId)
                                }
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
                    else -> {
                        if(rlgPlayer.guildId == 0) return true
                        rlgPlayer.guild()?.sendMessage(args.drop(0).joinToString(" "), player)
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
                        player.sendMessage("§a${target.name} wurde zur Guild eingeladen!")
                    }
                    "kick" -> {
                        val guild = rlgPlayer.guild() ?: return true
                        if(guild.owner_uuid != player.uniqueId.toString()) return true
                        if(!guild.member_names.contains(args[1])) return true
                        val target = Bukkit.getPlayer(args[1])
                        rlgPlayer.removeFromGuild("${args[1]} wurde aus der Guild gekickt!")
                        target?.sendMessage("§cDu wurdest aus deiner Guild entfernt!")
                    }
                    else -> {
                        if(rlgPlayer.guildId == 0) return true
                        rlgPlayer.guild()?.sendMessage(args.drop(0).joinToString(" "), player)
                    }
                }
            }
            else -> {
                if(rlgPlayer.guildId == 0) return true
                rlgPlayer.guild()?.sendMessage(args.drop(0).joinToString(" "), player)
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
                try { if(player.rlgPlayer().guild()!!.owner_uuid == player.uniqueId.toString()) result.addAll(arrayListOf("delete", "invite", "kick")) else result.add("leave") }catch (ex: NullPointerException) {
                    result.addAll(arrayListOf("accept", "setup", "decline"))}
                result
            }
            2 -> {
                when(args[0]){
                    "setup" -> arrayListOf("cancel")
                    "kick" -> player.rlgPlayer().guild()?.member_names
                    else -> null
                }
            }
            else -> null
        }
    }
}

val inviteTargetMap = HashMap<UUID, UUID>()
package dev.crash.commands

import dev.crash.*
import dev.crash.player.crashPlayer
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
        val crashPlayer = player.crashPlayer()
        when(args.size){
            0 -> {
                return true
            }
            1 -> {
                when(args[0]){
                    "setup" -> guildSetup(player, "")
                    "leave" -> crashPlayer.removeFromGuild("${player.name} has left the Guild!")
                    "delete" -> crashPlayer.deleteGuild()
                    "accept" -> {
                        if(inviteTargetMap.containsKey(player.uniqueId)){
                            val inviter = Bukkit.getPlayer(inviteTargetMap[player.uniqueId]!!)
                            when {
                                inviter == null -> {
                                    player.sendMessage("§4The other player gone offline!")
                                    inviteTargetMap.remove(player.uniqueId)
                                    return true
                                }
                                inviter.crashPlayer().guildId == 0 -> {
                                    player.sendMessage("§4The other player is in no guild!")
                                    inviteTargetMap.remove(player.uniqueId)
                                    return true
                                }
                                crashPlayer.guildId != 0 -> {
                                    player.sendMessage("§4You are already in a guild!")
                                    inviteTargetMap.remove(player.uniqueId)
                                    return true
                                }
                                else -> {
                                    inviteTargetMap.remove(player.uniqueId)
                                    crashPlayer.joinGuild(inviter.crashPlayer().guildId)
                                }
                            }
                        }else {
                            player.sendMessage("§4You have no pending invites!")
                        }
                    }
                    "decline" -> {
                        if(inviteTargetMap.containsKey(player.uniqueId)){
                            Bukkit.getPlayer(inviteTargetMap[player.uniqueId]!!)?.sendMessage("§cInvite to ${player.name} was declined.")
                            player.sendMessage("§aInvite was successfully declined!")
                            inviteTargetMap.remove(player.uniqueId)
                        }else {
                            player.sendMessage("§4You have no pending invites!")
                        }
                    }
                    else -> {
                        if(crashPlayer.guildId == 0) return true
                        crashPlayer.guild()?.sendMessage(args.drop(0).joinToString(" "), player)
                    }
                }
            }
            2 -> {
                when(args[0]){
                    "setup" -> {
                        if(args[1] == "cancel"){
                            guildSetupProgress.remove(player)
                            player.sendMessage("§aGuild-Setup is cancelled!")
                        }
                    }
                    "invite" -> {
                        if(crashPlayer.guildId == 0) {
                            player.sendMessage("§4You are not in a guild!")
                            return true
                        }
                        val target = Bukkit.getPlayer(args[1])
                        if(target == null){
                            player.sendMessage("§4You must enter a valid player!")
                            return true
                        }
                        val targetCrashPlayer = target.crashPlayer()
                        if(targetCrashPlayer.guildId != 0){
                            player.sendMessage("§4The player ${target.name} is already in a guild!")
                            return true
                        }
                        else if(inviteTargetMap.containsKey(target.uniqueId)){
                            player.sendMessage("§4The player ${target.name} has already been invited to another guild!")
                            return true
                        }
                        inviteTargetMap[target.uniqueId] = player.uniqueId
                        target.sendMessage("§bYou have been invited by ${player.name} to the guild ${crashPlayer.guild()!!.name}!\n/guild accept OR /guild decline")
                        player.sendMessage("§a${target.name} has been invited to the guild!")
                    }
                    "kick" -> {
                        val guild = crashPlayer.guild() ?: return true
                        if(guild.owner_uuid != player.uniqueId.toString()) return true
                        if(!guild.member_names.contains(args[1])) return true
                        val target = Bukkit.getPlayer(args[1])
                        crashPlayer.removeFromGuild("${args[1]} has been removed from the guild!")
                        target?.sendMessage("§cYou have been removed from the guild!")
                    }
                    else -> {
                        if(crashPlayer.guildId == 0) return true
                        crashPlayer.guild()?.sendMessage(args.drop(0).joinToString(" "), player)
                    }
                }
            }
            else -> {
                if(crashPlayer.guildId == 0) return true
                crashPlayer.guild()?.sendMessage(args.drop(0).joinToString(" "), player)
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
                try { if(player.crashPlayer().guild()!!.owner_uuid == player.uniqueId.toString()) result.addAll(arrayListOf("delete", "invite", "kick")) else result.add("leave") }catch (ex: NullPointerException) {
                    result.addAll(arrayListOf("accept", "setup", "decline"))}
                result
            }
            2 -> {
                when(args[0]){
                    "setup" -> arrayListOf("cancel")
                    "kick" -> player.crashPlayer().guild()?.member_names
                    else -> null
                }
            }
            else -> null
        }
    }
}

val inviteTargetMap = HashMap<UUID, UUID>()
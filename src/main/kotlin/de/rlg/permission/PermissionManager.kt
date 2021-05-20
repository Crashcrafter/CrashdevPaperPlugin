package de.rlg.permission

import de.rlg.INSTANCE
import de.rlg.player.rlgPlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.permissions.PermissionAttachment

var invSeeInventories = HashMap<Inventory, Player>()
var invSeeECs = HashMap<Inventory, Player>()

fun Player.givePerms(){
    val attachment = this.addAttachment(INSTANCE)
    val stringBooleanMap = attachment.permissions
    for (string in stringBooleanMap.keys) {
        attachment.unsetPermission(string!!)
    }
    val rlgPlayer = this.rlgPlayer()
    val rank = rlgPlayer.rank
    if(rank != rankData.size - 1) attachment.memberPerms()
    if(rank > 1) attachment.ecPerms()
    if(rankData[rank]!!.isMod){
        if(rank == rankData.size - 1){
            this.isOp = true
        }else{
            attachment.builderPerms()
            if(rank == rankData.size - 2){
                attachment.modPerms()
            }
        }
    }else this.isOp = false
}

fun PermissionAttachment.memberPerms() {
    this.setPermission("minecraft.command.msg", false)
    this.setPermission("minecraft.command.me", false)
    this.setPermission("minecraft.command.help", false)
    this.setPermission("bukkit.command.version", false)
    this.setPermission("bukkit.command.plugins", false)
    this.setPermission("bukkit.command.about", false)
    this.setPermission("minecraft.command.say", false)
    this.setPermission("minecraft.command.trigger", false)
    this.setPermission("minecraft.command.say", false)
    this.setPermission("worldedit.*", false)
}

fun PermissionAttachment.ecPerms() = this.setPermission("rlg.ec", true)

fun PermissionAttachment.builderPerms() {
    this.setPermission("worldedit.*", true)
    this.setPermission("minecraft.command.gamemode", true)
    this.setPermission("rlg.modchat", true)
}

fun PermissionAttachment.modPerms() {
    this.setPermission("minecraft.command.teleport", true)
    this.setPermission("minecraft.command.clear", true)
    this.setPermission("minecraft.command.enchant", true)
    this.setPermission("rlg.invsee", true)
    this.setPermission("rlg.invec", true)
    this.setPermission("rlg.warn", true)
    this.setPermission("rlg.mute", true)
    this.setPermission("rlg.unmute", true)
    this.setPermission("sv.use", true)
    this.setPermission("sv.keepfly", true)
    this.setPermission("sv.logout", true)
    this.setPermission("sv.login", true)
    this.setPermission("anticheat.system.alert", true)
}
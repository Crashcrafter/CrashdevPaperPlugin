package dev.crash.permission

import dev.crash.INSTANCE
import dev.crash.moderator
import dev.crash.player.crashPlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

var invSeeInventories = HashMap<Inventory, Player>()
var invSeeECs = HashMap<Inventory, Player>()

fun Player.givePerms(){
    val attachment = addAttachment(INSTANCE)
    for (string in attachment.permissions.keys) {
        attachment.unsetPermission(string!!)
    }
    val crashPlayer = crashPlayer()
    val rankData = crashPlayer.rankData()
    rankData.perms.forEach {
        attachment.setPermission(it.key, it.value)
    }
    if(hasPermission("crash.modchat")) moderator.add(this)
    isOp = rankData.isAdmin
}
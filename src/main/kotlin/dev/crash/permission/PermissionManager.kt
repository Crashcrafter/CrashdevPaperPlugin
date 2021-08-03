package dev.crash.permission

import dev.crash.INSTANCE
import dev.crash.player.rlgPlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

var invSeeInventories = HashMap<Inventory, Player>()
var invSeeECs = HashMap<Inventory, Player>()

fun Player.givePerms(){
    val attachment = addAttachment(INSTANCE)
    for (string in attachment.permissions.keys) {
        attachment.unsetPermission(string!!)
    }
    val rlgPlayer = rlgPlayer()
    val rankData = rlgPlayer.rankData()
    rankData.perms.forEach {
        attachment.setPermission(it.key, it.value)
    }
    isOp = rankData.isAdmin
}
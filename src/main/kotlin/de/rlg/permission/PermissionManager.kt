package de.rlg.permission

import de.rlg.INSTANCE
import de.rlg.player.rlgPlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

var invSeeInventories = HashMap<Inventory, Player>()
var invSeeECs = HashMap<Inventory, Player>()

fun Player.givePerms(){
    val attachment = this.addAttachment(INSTANCE)
    for (string in attachment.permissions.keys) {
        attachment.unsetPermission(string!!)
    }
    val rlgPlayer = this.rlgPlayer()
    val rankData = rlgPlayer.rankData()
    rankData.perms.forEach {
        attachment.setPermission(it.key, it.value)
    }
    this.isOp = rankData.isAdmin
}
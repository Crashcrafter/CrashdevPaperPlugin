package dev.crash.listener

import dev.crash.questCount
import org.bukkit.Material
import org.bukkit.Raid
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.raid.RaidFinishEvent

class QuestListener : Listener {
    @EventHandler
    fun onEnchant(e: EnchantItemEvent) {
        questCount(e.enchanter, 13, 1, true)
    }

    @EventHandler
    fun onRaidComplete(e: RaidFinishEvent) {
        if (e.raid.status == Raid.RaidStatus.VICTORY) {
            for (player in e.winners) {
                questCount(player, 7, 1, false)
            }
        }
    }

    @EventHandler
    fun onFishing(e: PlayerFishEvent) {
        if (e.state == PlayerFishEvent.State.CAUGHT_FISH) {
            if (e.caught is Item) {
                val itemStack = (e.caught as Item?)!!.itemStack
                if (itemStack.type == Material.NAME_TAG) {
                    questCount(e.player, 11, 1, true)
                } else if (itemStack.type == Material.COD) {
                    questCount(e.player, 5, 1, true)
                }
            }
        }
    }
}
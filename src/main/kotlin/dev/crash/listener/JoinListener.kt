package dev.crash.listener

import dev.crash.*
import dev.crash.player.crashPlayer
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.*

class JoinListener : Listener{

    @EventHandler
    fun onJoin(joinEvent: PlayerJoinEvent){
        val player = joinEvent.player
        val hostString = player.address.hostString
        var count = 0
        Bukkit.recipeIterator().iterator().forEach {
            val namespacedKey = when(it){
                is ShapedRecipe -> it.key
                is FurnaceRecipe -> it.key
                is BlastingRecipe -> it.key
                is CampfireRecipe -> it.key
                is ShapelessRecipe -> it.key
                is SmokingRecipe -> it.key
                is StonecuttingRecipe -> it.key
                else -> null
            }
            if(namespacedKey != null && !player.hasDiscoveredRecipe(namespacedKey)){
                player.discoverRecipe(namespacedKey)
            }
        }
        Bukkit.getOnlinePlayers().forEach {
            if(it.address.hostString == hostString) {
                count++
                if(count >= 2){
                    player.kick(Component.text("§4Multiple Accounts from the same PC are not allowed!"))
                    sendModchatMessage("Account was blocked: ${player.name} via $hostString")
                    return
                }
            }
        }
        player.crashPlayer()
        player.setResourcePack(CONFIG.texturePackURL, CONFIG.texturePackHash)
        joinEvent.joinMessage(Component.text("§a${player.name} joined!"))
        updateTabOfPlayers()
        player.isCustomNameVisible = true
    }
}
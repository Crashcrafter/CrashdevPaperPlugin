package dev.crash.listener

import com.vexsoftware.votifier.model.Vote
import dev.crash.*
import dev.crash.player.crashPlayer
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.*
import kotlin.collections.ArrayList

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
        val remove: MutableList<Vote> = ArrayList()
        for (vote in cachedVoteRewards) {
            if (vote.username!!.contentEquals(player.name)) {
                val votes: Int = INSTANCE.config.getInt("Votes." + player.uniqueId.toString() + "." + player.name)
                INSTANCE.config.set("Votes." + player.uniqueId.toString() + "." + player.name, votes + 1)
                INSTANCE.saveConfig()
                remove.add(vote)
                player.inventory.addItem(genKey(4))
                player.sendMessage("§2Thank you for voting!\nYou received a vote key!")
                Bukkit.getScheduler().runTask(INSTANCE,
                    Runnable { player.world.spawnEntity(player.location, EntityType.FIREWORK) })
                questCount(player, 8, 1, true)
                questCount(player, 3, 1, false)
            }
        }
        for (vote in remove) {
            cachedVoteRewards.remove(vote)
        }
    }
}
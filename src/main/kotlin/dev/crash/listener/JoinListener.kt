package dev.crash.listener

import com.vexsoftware.votifier.model.Vote
import dev.crash.*
import dev.crash.player.load
import dev.crash.player.rlgPlayer
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
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
                    player.kick(Component.text("§4Mehrere Accounts vom selben PC sind nicht erlaubt!"))
                    sendModchatMessage("Doppelter Account wurde geblockt: ${player.name} von $hostString")
                    return
                }
            }
        }
        player.sendMessage("Willkommen, ${player.name}!\nJoin unserem Discord Server, um Mitspieler zu finden und den Support zu kontaktieren!\n§o§nhttps://discord.gg/qQtaYsDN6w\n")
        player.rlgPlayer()
        player.setResourcePack(CONFIG.texturePackURL, CONFIG.texturePackHash)
        if(player.hasResourcePack()){
            player.sendMessage("§4Die Texturen des Server-Texturepack sind auf das Standard-Texturen ausgelegt!")
        }
        joinEvent.joinMessage(Component.text("§a${player.name} ist erschienen!"))
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
                player.sendMessage("§2Vielen Dank für das Voten!\nDu hast einen Vote Key erhalten!")
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
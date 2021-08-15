package dev.crash.listener

import com.vexsoftware.votifier.model.Vote
import com.vexsoftware.votifier.model.VotifierEvent
import dev.crash.INSTANCE
import dev.crash.cachedVoteRewards
import dev.crash.genKey
import dev.crash.questCount
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class VoteListener : Listener {

    @EventHandler
    fun onVote(e: VotifierEvent) {
        val vote: Vote = e.vote
        println("Vote by " + vote.username)
        val player = Bukkit.getPlayer(vote.username)
        if(player == null){
            cachedVoteRewards.add(vote)
            return
        }
        player.inventory.addItem(genKey(4))
        val votes: Int = INSTANCE.config.getInt("Votes." + player.uniqueId.toString() + "." + player.name)
        INSTANCE.config.set("Votes." + player.uniqueId.toString() + "." + player.name, votes + 1)
        INSTANCE.saveConfig()
        player.sendMessage("§2Thank you for voting!\nYou received a vote key!")
        Bukkit.getScheduler().runTask(INSTANCE, Runnable { player.world.spawnEntity(player.location, EntityType.FIREWORK) })
        questCount(player, 8, 1, true)
        questCount(player, 3, 1, false)
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player
        val remove: MutableList<Vote> = arrayListOf()
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
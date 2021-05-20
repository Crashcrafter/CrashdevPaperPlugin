package de.rlg.listener

import com.vexsoftware.votifier.model.Vote
import com.vexsoftware.votifier.model.VotifierEvent
import de.rlg.INSTANCE
import de.rlg.cachedVoteRewards
import de.rlg.genKey
import de.rlg.questCount
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class VoteListener : Listener {

    @EventHandler
    fun onVote(e: VotifierEvent) {
        val vote: Vote = e.vote
        println("Vote by " + vote.username)
        try {
            val player = Bukkit.getPlayer(vote.username)
            player!!.inventory.addItem(genKey(4))
            val votes: Int = INSTANCE.config.getInt("Votes." + player.uniqueId.toString() + "." + player.name)
            INSTANCE.config.set("Votes." + player.uniqueId.toString() + "." + player.name, votes + 1)
            INSTANCE.saveConfig()
            player.sendMessage("§2Vielen Dank für das Voten!\nDu hast einen Vote Key erhalten!")
            Bukkit.getScheduler().runTask(INSTANCE,
                Runnable { player.world.spawnEntity(player.location, EntityType.FIREWORK) })
            questCount(player, 8, 1, true)
            questCount(player, 3, 1, false)
        } catch (ex: NullPointerException) {
            cachedVoteRewards.add(vote)
        }
    }
}
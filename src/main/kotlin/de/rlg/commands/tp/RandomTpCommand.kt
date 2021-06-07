package de.rlg.commands.tp

import de.rlg.asPlayer
import de.rlg.permission.isClaimed
import de.rlg.player.rlgPlayer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.util.*

class RandomTpCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        val rlgPlayer = player.rlgPlayer()
        if(rlgPlayer.randomTpCoolDown <= System.currentTimeMillis()){
            rlgPlayer.randomTpCoolDown = System.currentTimeMillis() + 1000*300
            player.sendMessage("§6Suche Ort...")
            var x = Random().nextInt(20000) - 10000
            var z = Random().nextInt(20000) - 10000
            var randomBlock = Bukkit.getWorlds()[0].getHighestBlockAt(x, z)
            while (randomBlock.type == Material.WATER || randomBlock.type == Material.LAVA || randomBlock.chunk.isClaimed()){
                x = Random().nextInt(20000) - 10000
                z = Random().nextInt(20000) - 10000
                randomBlock = Bukkit.getWorlds()[0].getHighestBlockAt(x, z)
            }
            delayedTeleport(player, randomBlock.location.add(0.5, 1.0, 0.5))
        }
        return true
    }
}
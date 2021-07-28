package de.rlg.listener

import de.rlg.*
import de.rlg.permission.rankData
import de.rlg.player.rlgPlayer
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ChatListener : Listener {

    @EventHandler
    fun onChat(chatEvent: AsyncChatEvent){
        val player: Player = chatEvent.player
        val rlgPlayer = player.rlgPlayer()
        val message: String = (chatEvent.message() as TextComponent).content().replace("$", "§")
        when {
            checkMessage(message, player) || rlgPlayer.mutedUntil > System.currentTimeMillis() -> {
                chatEvent.isCancelled = true
                return
            }
            playerMessageMap.containsKey(player) -> {
                if(playerMessageMap[player]!!.invoke(chatEvent, message)){
                    player.removeMessageListener()
                }
                return
            }
        }
        val rankData = rlgPlayer.rankData()
        if(rlgPlayer.guildId == 0){
            chatEvent.renderer { _, _, _, _ -> Component.text("${rankData.prefix} ${player.name}> $message")}
        }else {
            chatEvent.renderer { _, _, _, _ -> Component.text("${rankData.prefix} §8[§6${rlgPlayer.guild()!!.suffix}§8]§r ${player.name}> $message")}
        }
    }
}

val playerMessageMap = hashMapOf<Player, (AsyncChatEvent, String) -> Boolean>()

fun Player.addMessageListener(f: (AsyncChatEvent, String) -> Boolean){
    playerMessageMap[this] = f
}

fun Player.removeMessageListener() = playerMessageMap.remove(this)
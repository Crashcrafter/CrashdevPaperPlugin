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
        val message: String = (chatEvent.message() as TextComponent).content()
        if (checkMessage(message, player) || rlgPlayer.mutedUntil > System.currentTimeMillis()) return
        if (setup1.containsKey(player)) {
            setupShop2(player, message)
            chatEvent.isCancelled = true
            return
        } else if (setup2.containsKey(player)) {
            setupShop3(player, message)
            chatEvent.isCancelled = true
            return
        }
        chatEvent.composer { _, _, _ -> Component.text("${rankData[rlgPlayer.rank]!!.prefix} ${player.name}> $message")}
    }
}
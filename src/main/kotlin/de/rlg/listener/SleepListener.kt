package de.rlg.listener

import de.rlg.INSTANCE
import de.rlg.allJobs
import de.rlg.amount_Sleeping
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerBedLeaveEvent

class SleepListener : Listener {
    @EventHandler
    fun onPlayerBedEnter(e: PlayerBedEnterEvent) {
        if (!e.isCancelled) {
            val player = e.player
            var time = player.world.time
            while (time > 24000) {
                time -= 24000
            }
            if (time >= 12541) {
                if (!amount_Sleeping.contains(player)) {
                    amount_Sleeping.add(player)
                    calcSleepMessage()
                    if (percentSleeping() >= 0.25f) {
                        allJobs.add(GlobalScope.launch{
                            var i = 0
                            var percent2 = 0f
                            while (true) {
                                try {
                                    delay(505L)
                                    percent2 = percentSleeping()
                                    if (percent2 < 0.25f) {
                                        break
                                    }
                                    i++
                                } catch (interruptedException: InterruptedException) {
                                    interruptedException.printStackTrace()
                                }
                                if (i == 10) {
                                    break
                                }
                            }
                            if (percent2 >= 0.25f) {
                                Bukkit.getScheduler().runTask(INSTANCE, Runnable {
                                    Bukkit.getServer().sendMessage(Component.text("§eNacht wurde übersprungen!"))
                                })
                            }
                        })
                    }
                }
            }
        }
    }

    @EventHandler
    fun onPlayerBedLeave(e: PlayerBedLeaveEvent) {
        val player = e.player
        if (amount_Sleeping.contains(player)) {
            amount_Sleeping.remove(player)
            if (player.world.time >= 13000) {
                calcSleepMessage()
            } else {
                amount_Sleeping.clear()
            }
        }
    }
}

fun percentSleeping(): Float {
    return if (amount_Sleeping.isEmpty()) 0f else amount_Sleeping.size.toFloat() / Bukkit.getOnlinePlayers().size
}

fun calcSleepMessage() {
    val amountPlayers = Bukkit.getOnlinePlayers().size
    Bukkit.getServer().sendMessage(
        Component.text("§e" + amount_Sleeping.size
            .toString() + "/" + amountPlayers.toString() + " Spieler schlafen (" + (percentSleeping() * 100f).toString() + "%)")
    )
}
package dev.crash.items.staffs

import dev.crash.INSTANCE
import dev.crash.allJobs
import dev.crash.permission.isClaimed
import dev.crash.player.crashPlayer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.entity.SmallFireball
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

object FireStaff1 {
    private var spell1Cooldown = HashMap<Player, Long>()
    private var spell2Cooldown = HashMap<Player, Long>()

    fun handleClick(e: PlayerInteractEvent) {
        val player = e.player
        val crashPlayer = player.crashPlayer()
        val action = e.action
        val mana: Int = crashPlayer.mana
        when (action) {
            Action.RIGHT_CLICK_AIR -> try {
                if (mana >= 30) {
                    if (spell1Cooldown.containsKey(player) && spell1Cooldown[player]!! <= System.currentTimeMillis() || !spell1Cooldown.containsKey(
                            player
                        )
                    ) {
                        spell1Cooldown.remove(player)
                        player.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 80, 10))
                        player.addPotionEffect(PotionEffect(PotionEffectType.SLOW_DIGGING, 80, 10))
                        if (!player.chunk.isClaimed() || player.isOp) {
                            allJobs.add(GlobalScope.launch{
                                delay(1000)
                                var count = 0
                                while (count < 15) {
                                    Bukkit.getScheduler().runTask(INSTANCE, Runnable {
                                        crashPlayer.changeMana(2)
                                        val fireball =
                                            player.launchProjectile(SmallFireball::class.java)
                                        fireball.customName = "firestaff1"
                                        fireball.isCustomNameVisible = false
                                    })
                                    count++
                                    delay(150)
                                }
                            })
                            spell1Cooldown[player] = System.currentTimeMillis() + 1000 * 10
                        } else {
                            player.sendMessage("§4Du kannst in geclaimten Chunks nicht Zaubern!")
                        }
                    }
                } else {
                    player.sendMessage("§4Du hast nicht genügend Mana, um diesen Zauber zu wirken!")
                }
            } catch (ignored: NullPointerException) {
                ignored.printStackTrace()
            }
            Action.RIGHT_CLICK_BLOCK -> try {
                if (mana >= 5) {
                    if (spell2Cooldown.containsKey(player) && spell2Cooldown[player]!! <= System.currentTimeMillis() || !spell2Cooldown.containsKey(
                            player
                        )
                    ) {
                        spell2Cooldown.remove(player)
                        if (!player.chunk.isClaimed() || player.isOp) {
                            val fireball = player.launchProjectile(SmallFireball::class.java)
                            fireball.customName = "firestaff1"
                            fireball.isCustomNameVisible = false
                            crashPlayer.changeMana(5)
                            spell2Cooldown[player] = System.currentTimeMillis() + 500
                        } else {
                            player.sendMessage("§4Du kannst in geclaimten Chunks nicht Zaubern!")
                        }
                    }
                } else {
                    player.sendMessage("§4Du hast nicht genügend Mana, um diesen Zauber zu wirken!")
                }
            } catch (ignored: NullPointerException) {
                ignored.printStackTrace()
            }
            else -> {
            }
        }
    }
}
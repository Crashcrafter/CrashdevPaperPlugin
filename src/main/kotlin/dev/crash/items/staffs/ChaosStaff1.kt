package dev.crash.items.staffs

import dev.crash.allJobs
import dev.crash.permission.eventCancel
import dev.crash.player.crashPlayer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

object ChaosStaff1 {
    private var spell1Cooldown = HashMap<Player, Long>()
    private var spell2Cooldown = HashMap<Player, Long>()

    @OptIn(DelicateCoroutinesApi::class)
    fun handleClick(e: PlayerInteractEvent) {
        val player = e.player
        val crashPlayer = player.crashPlayer()
        val action = e.action
        val mana: Int = crashPlayer.mana
        when (action) {
            Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> try {
                if (mana >= 30) {
                    if (spell1Cooldown.containsKey(player) && spell1Cooldown[player]!! <= System.currentTimeMillis() || !spell1Cooldown.containsKey(player)) {
                        spell1Cooldown.remove(player)
                        crashPlayer.changeMana(30)
                        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 405, 3))
                        player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 405, 0))
                        player.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, 405, 0))
                        spell1Cooldown[player] = System.currentTimeMillis() + 20000
                    }
                } else {
                    player.sendMessage("§4Du hast nicht genügend Mana, um diesen Zauber zu wirken!")
                }
            } catch (ignored: NullPointerException) {
            }
            Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> try {
                if (mana >= 75) {
                    if (spell2Cooldown.containsKey(player) && spell2Cooldown[player]!! <= System.currentTimeMillis() || !spell2Cooldown.containsKey(player)) {
                        spell2Cooldown.remove(player)
                        val block = Objects.requireNonNull(player.rayTraceBlocks(25.0))!!
                            .hitBlock!!
                        if (!eventCancel(block.chunk, player)) {
                            val world = block.world
                            val location = block.location
                            val entities = world.getNearbyLivingEntities(location, 5.0)
                            for (entity in entities) {
                                entity.addPotionEffect(PotionEffect(PotionEffectType.WITHER, 300, 0))
                                entity.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 300, 3))
                                entity.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 300, 3))
                                entity.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, 300, 3))
                            }
                            crashPlayer.changeMana(75)
                            allJobs.add(GlobalScope.launch {
                                var count = 0
                                while (count < 10) {
                                    world.playSound(location, Sound.ENTITY_GHAST_HURT, 1f, 1f)
                                    count++
                                    try {
                                        delay(500)
                                    } catch (interruptedException: InterruptedException) {
                                        interruptedException.printStackTrace()
                                    }
                                }
                            })
                            spell2Cooldown[player] = System.currentTimeMillis() + 1000 * 60
                        } else {
                            player.sendMessage("§4Du kannst in geclaimten Chunks nicht Zaubern!")
                        }
                    }
                } else {
                    player.sendMessage("§4Du hast nicht genügend Mana, um diesen Zauber zu wirken!")
                }
            } catch (ignored: NullPointerException) {
            }
            else -> return
        }
    }
}
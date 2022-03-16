package dev.crash.commands.user

import dev.crash.*
import dev.crash.player.crashPlayer
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.persistence.PersistentDataType

class QuestsCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if (args.isEmpty()) {
            showQuests(player)
        } else {
            if (player.isOp) {
                if (args[0].contentEquals("quester")) {
                    questVillager(player.location)
                    player.sendMessage("§2Quester spawned!")
                } else if (args[0].contentEquals("counter")) {
                    try {
                        val target: Player = Bukkit.getPlayer(args[1])!!
                        val qid: Int = args[2].toInt()
                        val amount: Int = args[3].toInt()
                        val isDaily = java.lang.Boolean.parseBoolean(args[4])
                        questCount(target, qid, amount, isDaily)
                        player.sendMessage("§2Quest counter changed")
                    } catch (ignored: ArrayIndexOutOfBoundsException) {
                        player.sendMessage("§4Invalid arguments!")
                    }
                } else if (args[0].contentEquals("reset")) {
                    if (args.size >= 2) {
                        val target: Player = Bukkit.getPlayer(args[1])!!
                        target.crashPlayer().weeklyQuestCreation()
                        player.sendMessage("§2Quests of ${target.name} were resetted!")
                    } else {
                        player.sendMessage("§4Invalid arguments!")
                    }
                }
            } else {
                showQuests(player)
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String>? {
        val player = sender.asPlayer()
        if (player.isOp) {
            val list: MutableList<String> = ArrayList()
            if (args.size == 1) {
                list.add("quester")
                list.add("counter")
                list.add("reset")
            } else if (args.size == 2) {
                if (args[0].contentEquals("reset") || args[0].contentEquals("counter")) {
                    val currentString: String = args[1]
                    for (player1 in Bukkit.getOnlinePlayers()) {
                        if (player1.name.startsWith(currentString)) {
                            list.add(player1.name)
                        }
                    }
                }
            } else if (args.size == 3) {
                if (args[0].contentEquals("counter")) {
                    val target: Player = Bukkit.getPlayer(args[1])!!
                    var hasValidQuest = false
                    for (quest in target.crashPlayer().quests) {
                        if (quest.status == 1) {
                            list.add(java.lang.String.valueOf(quest.qid))
                            hasValidQuest = true
                        }
                    }
                    if (!hasValidQuest) {
                        list.add("Player has no open quests!")
                    }
                }
            } else if (args.size == 4) {
                if (args[0].contentEquals("counter")) {
                    list.add("1")
                    list.add("2")
                    list.add("3")
                    list.add("5")
                }
            } else if (args.size == 5) {
                list.add("true")
                list.add("false")
            }
            return list
        }
        return null
    }
}

fun questVillager(location: Location) {
    val shop: Villager = location.world.spawnEntity(location, EntityType.VILLAGER) as Villager
    shop.setAI(false)
    shop.isInvulnerable = true
    shop.villagerLevel = 3
    shop.profession = Villager.Profession.CARTOGRAPHER
    shop.customName(Component.text("§a§l§nQuests"))
    shop.isCustomNameVisible = true
    shop.isSilent = true
    shop.removeWhenFarAway = false
    shop.persistentDataContainer.set(NamespacedKey(INSTANCE, "entityData"), PersistentDataType.STRING, "quester")
}
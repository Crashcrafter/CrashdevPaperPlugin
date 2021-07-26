package de.rlg

import de.rlg.commands.*
import de.rlg.commands.admin.*
import de.rlg.commands.home.*
import de.rlg.commands.mod.*
import de.rlg.commands.tp.*
import de.rlg.commands.text.*
import de.rlg.commands.user.*
import de.rlg.items.CraftingRecipes
import de.rlg.items.CustomItems
import de.rlg.listener.*
import de.rlg.permission.*
import de.rlg.player.clearPlayerData
import de.rlg.player.load
import de.rlg.player.rlgPlayer
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.WorldCreator
import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.io.File
import java.nio.file.Files

fun initServer(){
    initDatabase()
    loadSpawn()
    loadEvent()

    initQuests()
    initGuilds()
    loadFromDb()
    fixDb()

    registerCustomItems()
    initLootTables()
    initTradingInventories()
    loadWorlds()
    initDrops()
    updateCreditScore()
    CraftingRecipes.initRecipes()
    initEnchLevel()

    registerCommands()
    registerEvents()
}

fun registerEvents(){
    val listener = listOf(AnvilListener(), ChatListener(), CheatListener(), ChunkListener(), ClaimListener(), CraftingListener(), DamageListener(),
    DeathListener(), DeSpawnListener(), ElytraListener(), InteractListener(), InventoryListener(), JoinListener(), LeaveListener(), MobSpawnListener(),
    PortalListener(), QuestListener(), RespawnListener(), SignListener(), SleepListener(), VoteListener(), ProjectileListener())

    val pluginManager = Bukkit.getPluginManager()
    listener.forEach {
        pluginManager.registerEvents(it, INSTANCE)
    }
}

fun registerCommands(){
    INSTANCE.getCommand("spawn")!!.setExecutor(SpawnCommand())
    INSTANCE.getCommand("spawn")!!.tabCompleter = SpawnCommand()
    INSTANCE.getCommand("event")!!.setExecutor(EventCommand())
    INSTANCE.getCommand("event")!!.tabCompleter = SpawnCommand()
    INSTANCE.getCommand("ci")!!.setExecutor(CICommand())
    INSTANCE.getCommand("ci")!!.tabCompleter = CICommand()
    INSTANCE.getCommand("discord")!!.setExecutor(DiscordCommand())
    INSTANCE.getCommand("vote")!!.setExecutor(VoteCommand())
    INSTANCE.getCommand("multiv")!!.setExecutor(MultivCommand())
    INSTANCE.getCommand("multiv")!!.tabCompleter = MultivCommand()
    INSTANCE.getCommand("ec")!!.setExecutor(EcCommand())
    INSTANCE.getCommand("back")!!.setExecutor(BackCommand())
    INSTANCE.getCommand("claim")!!.setExecutor(ClaimCommand())
    INSTANCE.getCommand("claim")!!.tabCompleter = ClaimCommand()
    INSTANCE.getCommand("drop")!!.setExecutor(DropCommand())
    INSTANCE.getCommand("drop")!!.tabCompleter = DropCommand()
    INSTANCE.getCommand("exp")!!.setExecutor(ExpCommand())
    INSTANCE.getCommand("exp")!!.tabCompleter = ExpCommand()
    INSTANCE.getCommand("home")!!.setExecutor(HomeCommand())
    INSTANCE.getCommand("home")!!.tabCompleter = HomeCommand()
    INSTANCE.getCommand("delhome")!!.setExecutor(DelhomeCommand())
    INSTANCE.getCommand("delhome")!!.tabCompleter = HomeCommand()
    INSTANCE.getCommand("sethome")!!.setExecutor(SetHomeCommand())
    INSTANCE.getCommand("homes")!!.setExecutor(HomesCommand())
    INSTANCE.getCommand("invec")!!.setExecutor(InvEcCommand())
    INSTANCE.getCommand("invsee")!!.setExecutor(InvSeeCommand())
    INSTANCE.getCommand("m")!!.setExecutor(MessageCommand())
    INSTANCE.getCommand("modchat")!!.setExecutor(ModChatCommand())
    INSTANCE.getCommand("profile")!!.setExecutor(ProfileCommand())
    INSTANCE.getCommand("quests")!!.setExecutor(QuestsCommand())
    INSTANCE.getCommand("quests")!!.tabCompleter = QuestsCommand()
    INSTANCE.getCommand("rank")!!.setExecutor(RankCommand())
    INSTANCE.getCommand("rank")!!.tabCompleter = RankCommand()
    INSTANCE.getCommand("reloaddb")!!.setExecutor(ReloadCommand())
    INSTANCE.getCommand("reloaduser")!!.setExecutor(ReloadUserCommand())
    INSTANCE.getCommand("tempban")!!.setExecutor(TempbanCommand())
    INSTANCE.getCommand("tempban")!!.tabCompleter = TempbanCommand()
    INSTANCE.getCommand("warn")!!.setExecutor(WarnCommand())
    INSTANCE.getCommand("warn")!!.tabCompleter = WarnCommand()
    INSTANCE.getCommand("tpa")!!.setExecutor(TpaCommand())
    INSTANCE.getCommand("tpaccept")!!.setExecutor(TpAcceptCommand())
    INSTANCE.getCommand("tpdeny")!!.setExecutor(TpDenyCommand())
    INSTANCE.getCommand("credits")!!.setExecutor(CreditsCommand())
    INSTANCE.getCommand("credits")!!.tabCompleter = CreditsCommand()
    INSTANCE.getCommand("key")!!.setExecutor(KeyCommand())
    INSTANCE.getCommand("key")!!.tabCompleter = KeyCommand()
    INSTANCE.getCommand("weekly")!!.setExecutor(WeeklyCommand())
    INSTANCE.getCommand("mute")!!.setExecutor(MuteCommand())
    INSTANCE.getCommand("mute")!!.tabCompleter = MuteCommand()
    INSTANCE.getCommand("unmute")!!.setExecutor(UnmuteCommand())
    INSTANCE.getCommand("crypto")!!.setExecutor(CryptoCommand())
    INSTANCE.getCommand("checkitem")!!.setExecutor(CheckItemCommand())
    INSTANCE.getCommand("guild")!!.setExecutor(GuildCommand())
    INSTANCE.getCommand("guild")!!.tabCompleter = GuildCommand()
}

fun registerCustomItems(){
    customItemsMap["KatzeRose"] = CustomItems.katzeRose()
    customItemsMap["ManaShard"] = CustomItems.manaShard()
    customItemsMap["ManaDust"] = CustomItems.manaDust()
    customItemsMap["ManaCrystal"] = CustomItems.manaCrystal()
    customItemsMap["WeatherElement"] = CustomItems.weatherElement()
    customItemsMap["ChaosElement"] = CustomItems.chaosElement()
    customItemsMap["WaterElement"] = CustomItems.waterElement()
    customItemsMap["NatureElement"] = CustomItems.natureElement()
    customItemsMap["FireElement"] = CustomItems.fireElement()
    customItemsMap["NatureStaff1"] = CustomItems.natureStaff1()
    customItemsMap["FireStaff1"] = CustomItems.fireStaff1()
    customItemsMap["WeatherStaff1"] = CustomItems.weatherStaff1()
    customItemsMap["ChaosStaff1"] = CustomItems.chaosStaff1()
    customItemsMap["WaterStaff1"] = CustomItems.waterStaff1()
    customItemsMap["Excalibur"] = CustomItems.excalibur()
    customItemsMap["KnockbackStick"] = CustomItems.knockBackStick()
    customItemsMap["IronKatana"] = CustomItems.ironKatana()
    customItemsMap["DiaKatana"] = CustomItems.diaKatana()
    customItemsMap["NetherKatana"] = CustomItems.netherKatana()
    customItemsMap["Bitcoin"] = CustomItems.bitcoin()
    customItemsMap["Ethereum"] = CustomItems.ethereum()
    customItemsMap["Litecoin"] = CustomItems.litecoin()
    customItemsMap["Dogecoin"] = CustomItems.dogecoin()
    customItemsMap["Nano"] = CustomItems.nano()
    customItemsMap["MagicBook"] = CustomItems.magicBook()
    customItemsMap["BeginnerBook"] = CustomItems.beginnerBook()
    customItemsMap["SmallFireball"] = CustomItems.throwableSmallFireBall()
    customItemsMap["MediumFireball"] = CustomItems.throwableMediumFireBall()
    customItemsMap["BigFireball"] = CustomItems.throwableBigFireBall()
    customItemsMap["MudBall"] = CustomItems.mudBall()
    customItemsMap["AddClaim"] = CustomItems.additionalClaim()
    customItemsMap["DragonScale"] = CustomItems.dragonScale()
}

fun loadWorlds(){
    val processedFile = File("worlds.txt")
    if (!processedFile.exists()) {
        processedFile.createNewFile()
    }
    val txtString = Files.readString(processedFile.toPath())
    val worlds = txtString.split(" ")
    for (world in worlds) {
        val w = world.replace(" ", "")
        if (!w.contentEquals("")) {
            WorldCreator(w).createWorld()
        }
    }
}

fun loadFromDb(){
    transaction {
        rankData.clear()
        RankTable.selectAll().forEach {
            rankData[it[RankTable.id]] = RankObj(it[RankTable.prefix], it[RankTable.name], it[RankTable.claims], it[RankTable.isMod], it[RankTable.homes],
                it[RankTable.shopMultiplier])
        }
        chunks.clear()
        chunkList.clear()
        ChunkTable.selectAll().forEach {
            val chunk = Bukkit.getWorld(it[ChunkTable.world])!!.getChunkAt(it[ChunkTable.x], it[ChunkTable.z])
            val chunkClass = ChunkClass(it[ChunkTable.x], it[ChunkTable.z], it[ChunkTable.world], it[ChunkTable.uuid], it[ChunkTable.name],
                it[ChunkTable.shared].split(" ").toMutableList())
            chunks[chunk] = chunkClass
            chunkList.add(chunkClass)
        }
        shops.clear()
        ShopTable.selectAll().forEach {
            val sign = getBlockBySQLString(it[ShopTable.signPos])
            val chest = getBlockBySQLString(it[ShopTable.chestPos])
            val shop = Shop(chest.state as Chest, sign.state as Sign,
                it[ShopTable.ownerUUID], it[ShopTable.playername], it[ShopTable.sellPrice], it[ShopTable.buyPrice], Material.valueOf(it[ShopTable.material]),
                it[ShopTable.cmd])
            if(sign.state !is Sign || chest.state !is Chest){
                removeShop(shop)
            }else {
                shops.add(shop)
                signs[sign] = shop
            }
        }
        keyChests.clear()
        KeyChestTable.selectAll().forEach {
            keyChests[getBlockBySQLString(it[KeyChestTable.chestPos])] = it[KeyChestTable.type]
        }
        prices.clear()
        PricesTable.selectAll().forEach {
            val material = Material.valueOf(it[PricesTable.itemId])
            if(!prices.containsKey(material)){
                prices[material] = hashMapOf(it[PricesTable.cmd] to it[PricesTable.credits])
            }else {
                prices[material]!![it[PricesTable.cmd]] = it[PricesTable.credits]
            }
        }
        portals.clear()
        PortalTable.selectAll().forEach {
            portals[getBlockBySQLString(it[PortalTable.portalPos])] = it[PortalTable.targetWorld]
        }
    }
    updateTabOfPlayers()
    clearPlayerData()
    Bukkit.getOnlinePlayers().forEach {
        player -> run {
            player.setResourcePack(texturePackUrl, texturePackHash)
            player.load()
            player.updateScoreboard()
            val playerTextComponent = Component.text("${rankData[player.rlgPlayer().rank]!!.prefix} ${player.name}")
            player.playerListName(playerTextComponent)
            player.displayName(playerTextComponent)
            player.customName(playerTextComponent)
            player.isCustomNameVisible = true
        }
    }
}

fun fixDb(){
    transaction {
        PlayersTable.selectAll().forEach {
            val uuid = it[PlayersTable.uuid]
            val chunkAmount = ChunkTable.select(where = {ChunkTable.uuid eq uuid}).toList().size
            val homesAmount = HomepointTable.select(where = {HomepointTable.uuid eq uuid}).toList().size
            val rank = it[PlayersTable.rank]
            if(chunkAmount != (rankData[rank]!!.claims + it[PlayersTable.addedClaims]) - it[PlayersTable.remainingClaims]){
                PlayersTable.update(where = {PlayersTable.uuid eq uuid}){ it2 ->
                    it2[remainingClaims] = rankData[rank]!!.claims + it[addedClaims] - chunkAmount
                }
            }
            if(homesAmount != rankData[rank]!!.homes - it[PlayersTable.remainingHomes]){
                PlayersTable.update(where = {PlayersTable.uuid eq uuid}){ it2 ->
                    it2[remainingHomes] = rankData[rank]!!.homes - homesAmount
                }
            }
        }
    }
}
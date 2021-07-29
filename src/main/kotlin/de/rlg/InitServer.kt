package de.rlg

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
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
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.WorldCreator
import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.io.File

fun initServer(){
    initDatabase()
    loadWarps()

    initQuests()
    initGuilds()
    loadRanks()
    loadFromDb()
    fixDb()

    registerCustomItems()
    loadLootTables()
    initTradingInventories()
    loadWorlds()
    loadDropTables()
    updateCreditScore()
    CraftingRecipes.initRecipes()
    loadMaxEnchantmentLevel()
    loadPrices()

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
    INSTANCE.getCommand("warp")!!.setExecutor(WarpCommand())
    INSTANCE.getCommand("warp")!!.tabCompleter = WarpCommand()
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
    val worlds = INSTANCE.config.getStringList("worlds")
    if(worlds.isEmpty()){
        worlds.add("event")
        INSTANCE.config.set("worlds", worlds)
        INSTANCE.saveConfig()
    }
    worlds.forEach {
        WorldCreator(it).createWorld()
    }
}

fun loadFromDb(){
    transaction {
        chunks.clear()
        ChunkTable.selectAll().forEach {
            val chunkKey = Chunk.getChunkKey(it[ChunkTable.x], it[ChunkTable.z])
            val chunkClass = ChunkClass(it[ChunkTable.x], it[ChunkTable.z], it[ChunkTable.world], it[ChunkTable.uuid], it[ChunkTable.name],
                it[ChunkTable.shared].split(" ").toMutableList())
            if(chunks.containsKey(chunkKey)){
                chunks[chunkKey]!![chunkClass.world] = chunkClass
            }else {
                chunks[chunkKey] = hashMapOf(chunkClass.world to chunkClass)
            }
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
        portals.clear()
        PortalTable.selectAll().forEach {
            portals[getBlockBySQLString(it[PortalTable.portalPos])] = it[PortalTable.targetWorld]
        }
    }
    updateTabOfPlayers()
    clearPlayerData()
    Bukkit.getOnlinePlayers().forEach {
        it.setResourcePack(texturePackUrl, texturePackHash)
        it.load()
        it.updateScoreboard()
        val playerTextComponent = Component.text("${it.rlgPlayer().rankData().prefix} ${it.name}")
        it.playerListName(playerTextComponent)
        it.displayName(playerTextComponent)
        it.customName(playerTextComponent)
        it.isCustomNameVisible = true
    }
}

data class PricesSaveObj(val material: String, val cmd: Int, val price: Long)
fun loadPrices(){
    prices.clear()
    val file = File(INSTANCE.dataFolder.path + "/prices.json")
    if(file.exists()){
        val pricesObjects = jacksonObjectMapper().readValue<List<PricesSaveObj>>(file)
        pricesObjects.forEach {
            val m = Material.valueOf(it.material)
            if(!prices.containsKey(m)){
                prices[m] = hashMapOf()
            }
            prices[m]!![it.cmd] = it.price
        }
    }else {
        file.createNewFile()
        jacksonObjectMapper().writeValue(file, listOf<PricesSaveObj>())
    }
}

fun fixDb(){
    transaction {
        PlayersTable.selectAll().forEach {
            val uuid = it[PlayersTable.uuid]
            val chunkAmount = ChunkTable.select(where = {ChunkTable.uuid eq uuid}).toList().size
            val homesAmount = HomepointTable.select(where = {HomepointTable.uuid eq uuid}).toList().size
            val rankData = ranks[it[PlayersTable.rank]]!!
            if(chunkAmount != (rankData.claims + it[PlayersTable.addedClaims]) - it[PlayersTable.remainingClaims]){
                PlayersTable.update(where = {PlayersTable.uuid eq uuid}){ it2 ->
                    it2[remainingClaims] = rankData.claims + it[addedClaims] - chunkAmount
                }
            }
            if(homesAmount != rankData.homes - it[PlayersTable.remainingHomes]){
                PlayersTable.update(where = {PlayersTable.uuid eq uuid}){ it2 ->
                    it2[remainingHomes] = rankData.homes - homesAmount
                }
            }
        }
    }
}
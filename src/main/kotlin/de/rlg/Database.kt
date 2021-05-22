package de.rlg

import de.rlg.permission.rankData
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.`java-time`.timestamp
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.time.LocalDate

fun initDatabase(){
    TransactionManager.defaultDatabase = Database.connect("jdbc:mysql://${LoginData.ip}/mcplugin", user = LoginData.user, password = LoginData.pw)
}

object ChunkTable : Table("chunks"){
    val x = integer("x")
    val z = integer("z")
    val world = varchar("world", 20)
    val uuid = varchar("uuid", 36)
    val name = varchar("name", 100)
    val shared = text("shared")
}

object HomepointTable : Table("homepoints"){
    val uuid = varchar("uuid", 36)
    val keyword = varchar("keyword", 40)
    val homePos = text("homePos")
}

object KeyChestTable : Table("keychests"){
    val chestPos = text("chestPos")
    val type = integer("type")
}

object KeyIndexTable : Table("keyindex"){
    val token = varchar("token", 20)
    val type = integer("type")
    override val primaryKey = PrimaryKey(token)
}

object PlayersTable : Table("players"){
    val uuid = varchar("uuid", 36)
    val rank = integer("rank").default(0)
    val remainingClaims = integer("remainingclaims").default(rankData[0]!!.claims)
    val remainingHomes = integer("remaininghomes").default(rankData[0]!!.homes)
    val addedClaims = integer("addedClaims").default(0)
    val balance = long("balance").default(0)
    val plot = integer("plot").default(1)
    val questStatus = varchar("queststatus", 50).default("0 0 0 0 0 0 0 0")
    val quests = varchar("quests", 50).default("1 2 3 1 2 3")
    val questProgress = varchar("questprogress", 50).default("0 0 0 0 0 0")
    val lastDailyQuest = date("lastdailyquest").default(LocalDate.now())
    val lastWeeklyQuest = date("lastweeklyquest").default(LocalDate.now())
    val xpLevel = integer("xpLevel").default(0)
    val xp = long("xp").default(0)
    val vxpLevel = integer("vxpLevel").default(0)
    override val primaryKey = PrimaryKey(uuid)
}

object PlotTable : Table("plots"){
    val uuid = varchar("uuid", 36)
    val chunks = text("chunks")
}

object PortalTable : Table("portals"){
    val portalPos = text("portalPos")
    val targetWorld = varchar("targetworld", 20)
}

object PricesTable : Table("prices"){
    val itemId = varchar("itemid", 100)
    val credits = long("credits")
    override val primaryKey = PrimaryKey(itemId)
}

object ProcessedTable : Table("processed"){
    val uuid = varchar("uuid", 36)
    val lastTime = date("lasttime")
    val leftKeys = varchar("leftkeys", 20)
    override val primaryKey = PrimaryKey(uuid)
}

object RankTable : Table("ranks"){
    val id = integer("id")
    val prefix = varchar("prefix", 50)
    val name = varchar("name", 50)
    val claims = integer("claims")
    val isMod = bool("isMod")
    val homes = integer("homes")
    val shopMultiplier = double("shopMultiplier")
    override val primaryKey = PrimaryKey(id)
}

object ShopTable : Table("shops"){
    val signPos = text("signcords")
    val chestPos = text("chestcords")
    val ownerUUID = varchar("owner_uuid", 36)
    val playername = varchar("playername", 100)
    val sellPrice = integer("sellprice")
    val buyPrice = integer("buyprice")
    val material = varchar("material", 50)
    val cmd = integer("cmd")
}

object WarnTable : Table("warns"){
    val uuid = varchar("uuid", 36)
    val name = varchar("name", 100)
    val reason = text("reason")
    val modName = varchar("modname", 100)
    val time = timestamp("time")
}
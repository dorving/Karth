package karth.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Users : IntIdTable() {
    val userId = integer("userId")
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var userId by Users.userId
}

object Rooms : IntIdTable() {
    val roomId = integer("roomId")
    val owner = reference("owner", Users)
}

class Room(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Room>(Rooms)

    var roomId by Rooms.roomId
    var owner by User referencedOn Rooms.owner
}

fun main(args: Array<String>) {
    //an example connection to H2 DB
    Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")

    transaction {

        // print sql to std-out
        addLogger(StdOutSqlLogger)

        SchemaUtils.create(Users)
        SchemaUtils.create(Rooms)

        val user = User.new {
            userId = 69
        }
        val room = Room.new {
            roomId = 420
            owner = user
        }

        println("Users: ${Users.selectAll().toList()}")
        println("Rooms: ${Rooms.selectAll().toList()}")
    }
}

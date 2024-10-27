package org.example.plugins

import org.jetbrains.exposed.sql.Database

object DatabaseInit {
    fun init(): Database {
        return Database.connect(
            url = "jdbc:postgresql://localhost:5432/mydb",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "mysecretpassword"
        )
    }
}
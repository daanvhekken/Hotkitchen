package routes

import io.ktor.client.request.*
import io.ktor.server.testing.*
import org.example.module
import kotlin.test.Test

class AuthRoutesKtTest {

    @Test
    fun testPostSignup() = testApplication {
        application {
            module(testing = true)
        }
        client.post("/signup").apply {
            print("Y")
        }
    }
}
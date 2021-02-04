/*
 * Developed as part of the Cramolith project.
 * This file was last modified at 2/4/21, 11:17 PM.
 * Copyright 2021, see git repository at git.angm.xyz for authors and other info.
 * This file is under the GPL3 license. See LICENSE in the root directory of this repository for details.
 */

package xyz.angm.cramolith.web

import freemarker.cache.ClassTemplateLoader
import freemarker.core.HTMLOutputFormat
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import xyz.angm.cramolith.server.database.DB
import xyz.angm.cramolith.server.database.Player
import xyz.angm.cramolith.server.database.Players

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
        outputFormat = HTMLOutputFormat.INSTANCE
    }

    routing {
        static("/static") {
            resources("static")
        }

        get("/") {
            call.respond(FreeMarkerContent("index.ftl", mapOf("message" to "")))
        }

        get("/register") {
            call.respond(FreeMarkerContent("register.ftl", mapOf("error" to "")))
        }

        post("/register/submit") {
            val input = call.receiveParameters()
            val username = input["username"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val pw = input["pw"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val pwConfirm = input["pw-confirm"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val playerExists = DB.transaction { !Player.find { Players.name eq username }.empty() }

            val error = when {
                pw != pwConfirm -> "Passwords are not the same"
                playerExists -> "Username already exists"
                else -> null
            }

            return@post if (error == null) {
                DB.transaction {
                    Player.new {
                        name = username
                        password = pw
                    }
                }
                call.respond(FreeMarkerContent("index.ftl", mapOf("message" to "Successfully registered. Welcome, $username!")))
            } else {
                call.respond(FreeMarkerContent("register.ftl", mapOf("error" to error)))
            }
        }
    }
}
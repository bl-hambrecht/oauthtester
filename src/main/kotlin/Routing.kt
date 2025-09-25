package de.bikeleasing

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

fun Application.configureRouting() {
    val json = Json { prettyPrint = true }
    routing {
        get("/logout") {
            call.sessions.clear<UserSession>()
            call.respondRedirect("/")
        }

        get("/") {
            val session = call.sessions.get<UserSession>()

            call.respondHtml {
                head {
                    style {
                        unsafe {
                            +"""
                            body {
                                max-width: 800px;
                                margin: 0 auto;
                                padding: 20px;
                            }
                            .data-table {
                                width: 100%;
                                border-collapse: collapse;
                                margin-bottom: 20px;
                            }
                            .data-table th, .data-table td {
                                border: 1px solid #ddd;
                                padding: 8px;
                                text-align: left;
                            }
                            .data-table th {
                                background-color: #f2f2f2;
                            }
                            """
                        }
                    }
                }
                body {
                    h1 { +"Welcome to AuthTester" }

                    if (session != null) {
                        p {
                            a("/logout") { +"Logout" }
                        }

                        h2 { +"Session Data:" }

                        try {
                            // Decode the JWT token (which is in format header.payload.signature)
                            val parts = session.accessToken.split(".")
                            if (parts.size >= 2) {
                                val payload = parts[1]
                                // Add padding if needed
                                val padding = when (payload.length % 4) {
                                    0 -> ""
                                    1 -> "==="
                                    2 -> "=="
                                    else -> "="
                                }
                                val decodedBytes = Base64.getUrlDecoder().decode(payload + padding)
                                val decodedJson = String(decodedBytes)

                                // Pretty print the JSON
                                val prettyJson = try {
                                    json.encodeToString(json.parseToJsonElement(decodedJson))
                                } catch (e: Exception) {
                                    log.error("Failed to pretty print JSON", e)
                                    decodedJson
                                }

                                // Extract and format the timestamp claims
                                try {
                                    val jsonElement = json.parseToJsonElement(decodedJson)
                                    val jsonObj = jsonElement.jsonObject

                                    // Define a formatter for the date and time
                                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

                                    // Function to convert Unix timestamp to formatted date string
                                    fun formatTimestamp(timestamp: Long): String {
                                        val instant = Instant.ofEpochSecond(timestamp)
                                        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                                        return formatter.format(localDateTime)
                                    }

                                    h3 { +"Common Claims:" }
                                    table(classes = "data-table") {
                                        tr {
                                            th { +"Claim" }
                                            th { +"Value" }
                                        }

                                        // Extract and display sub (Subject)
                                        jsonObj["sub"]?.jsonPrimitive?.content?.let { sub ->
                                            tr {
                                                td { +"sub (Subject)" }
                                                td { +sub }
                                            }
                                        }

                                        // Extract and display iss (Issuer)
                                        jsonObj["iss"]?.jsonPrimitive?.content?.let { iss ->
                                            tr {
                                                td { +"iss (Issuer)" }
                                                td { +iss }
                                            }
                                        }

                                        // Extract and display aud (Audience)
                                        jsonObj["aud"]?.jsonPrimitive?.content?.let { aud ->
                                            tr {
                                                td { +"aud (Audience)" }
                                                td { +aud }
                                            }
                                        }

                                        // Extract and display azp (Authorized Party)
                                        jsonObj["azp"]?.jsonPrimitive?.content?.let { azp ->
                                            tr {
                                                td { +"azp (Authorized Party)" }
                                                td { +azp }
                                            }
                                        }

                                        // Extract and display jti (JWT ID)
                                        jsonObj["jti"]?.jsonPrimitive?.content?.let { jti ->
                                            tr {
                                                td { +"jti (JWT ID)" }
                                                td { +jti }
                                            }
                                        }

                                        // Extract and display scope
                                        jsonObj["scope"]?.jsonPrimitive?.content?.let { scope ->
                                            tr {
                                                td { +"scope (Scope)" }
                                                td { +scope }
                                            }
                                        }

                                        // Extract and display email
                                        jsonObj["email"]?.jsonPrimitive?.content?.let { email ->
                                            tr {
                                                td { +"email (Email Address)" }
                                                td { +email }
                                            }
                                        }

                                        // Extract and display name
                                        jsonObj["name"]?.jsonPrimitive?.content?.let { name ->
                                            tr {
                                                td { +"name (Full Name)" }
                                                td { +name }
                                            }
                                        }

                                        // Extract and display preferred_username
                                        jsonObj["preferred_username"]?.jsonPrimitive?.content?.let { username ->
                                            tr {
                                                td { +"preferred_username (Preferred Username)" }
                                                td { +username }
                                            }
                                        }
                                    }

                                    h3 { +"Token Claims:" }
                                    table(classes = "data-table") {
                                        tr {
                                            th { +"Claim" }
                                            th { +"Unix Timestamp" }
                                            th { +"Local Date and Time" }
                                        }

                                        // Extract and display iat (Issued At)
                                        jsonObj["iat"]?.jsonPrimitive?.content?.toLongOrNull()?.let { iat ->
                                            tr {
                                                td { +"iat (Issued At)" }
                                                td { +iat.toString() }
                                                td { +formatTimestamp(iat) }
                                            }
                                        }

                                        // Extract and display exp (Expiration Time)
                                        jsonObj["exp"]?.jsonPrimitive?.content?.toLongOrNull()?.let { exp ->
                                            tr {
                                                td { +"exp (Expiration Time)" }
                                                td { +exp.toString() }
                                                td { +formatTimestamp(exp) }
                                            }
                                        }

                                        // Extract and display auth_time (Authentication Time)
                                        jsonObj["auth_time"]?.jsonPrimitive?.content?.toLongOrNull()?.let { authTime ->
                                            tr {
                                                td { +"auth_time (Authentication Time)" }
                                                td { +authTime.toString() }
                                                td { +formatTimestamp(authTime) }
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    p { +"Error extracting claims: ${e.message}" }
                                }

                                h3 { +"Decoded Token:" }
                                pre {
                                    style = "white-space: pre-wrap; word-break: break-all;"
                                    +prettyJson
                                }

                                h3 { +"Access Token:" }
                                pre {
                                    style = "white-space: pre-wrap; word-break: break-all;"
                                    +session.accessToken
                                }
                            }
                        } catch (e: Exception) {
                            p { +"Error decoding token: ${e.message}" }
                        }
                    } else {
                        a("/login") { +"Login" }
                    }
                }
            }
        }
    }
}

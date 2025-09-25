package de.bikeleasing

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable

fun Application.configureSecurity() {
    // Get configuration
    val config = environment.config

    // Read OAuth configuration values
    val authorizeUrl = config.property("security.oauth.authorizeUrl").getString()
    val accessTokenUrl = config.property("security.oauth.accessTokenUrl").getString()
    val clientId = config.property("security.oauth.clientId").getString()
    val clientSecret = config.property("security.oauth.clientSecret").getString()
    val defaultScopes = config.property("security.oauth.defaultScopes").getList()
    val requestMethod = HttpMethod.parse(config.property("security.oauth.requestMethod").getString())

    install(Sessions) {
        cookie<UserSession>("user_session")
    }

    authentication {
        oauth("auth-oauth") {
            urlProvider = { "http://localhost:8080/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "oauth-test",
                    authorizeUrl = authorizeUrl,
                    accessTokenUrl = accessTokenUrl,
                    requestMethod = requestMethod,
                    clientId = clientId,
                    clientSecret = clientSecret,
                    defaultScopes = defaultScopes
                )
            }
            client = HttpClient(Apache)
        }
    }
    routing {
        authenticate("auth-oauth") {
            get("login") {
                call.respondRedirect("/callback")
            }

            get("/callback") {
                val principal: OAuthAccessTokenResponse.OAuth2? = call.authentication.principal()
                call.sessions.set(UserSession(principal?.accessToken.toString()))
                call.respondRedirect("/")
            }
        }
    }
}

@Serializable
data class UserSession(val accessToken: String)

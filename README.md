# AuthTester

AuthTester is a simple Kotlin application built with Ktor that helps you test OAuth2 authentication against your OAuth server. It provides a user-friendly interface to visualize token information and claims.

## Features

- OAuth2 authentication flow
- JWT token decoding and visualization
- Display of common claims and token information
- Configurable OAuth2 settings

## Setup

### Prerequisites

- JDK 11 or higher
- Gradle

### Configuration

The application uses YAML configuration for OAuth settings. Edit the `src/main/resources/application.yaml` file to configure your OAuth server:

```yaml
security:
  oauth:
    authorizeUrl: "https://your-oauth-server.com/auth"
    accessTokenUrl: "https://your-oauth-server.com/token"
    clientId: "your-client-id"
    clientSecret: "your-client-secret"
    defaultScopes: ["your scopes"]
    requestMethod: "POST"  # or "GET" depending on your OAuth server
```

#### Configuration Parameters

- `authorizeUrl`: The authorization endpoint URL of your OAuth server
- `accessTokenUrl`: The token endpoint URL of your OAuth server
- `clientId`: Your OAuth client ID
- `clientSecret`: Your OAuth client secret
- `defaultScopes`: List of scopes to request during authentication
- `requestMethod`: HTTP method to use for token requests (usually "POST")

### Running the Application

1. Clone the repository
2. Configure your OAuth settings in `application.yaml`
3. Run the application:

```bash
./gradlew run
```

The application will start on http://localhost:8080

## Usage

1. Open http://localhost:8080 in your browser
2. Click "Login" to start the OAuth flow
3. After successful authentication, you'll be redirected back to the application
4. The application will display your token information, including:
   - Common claims (subject, issuer, audience, etc.)
   - Token claims (issued at, expiration time, etc.)
   - The full decoded JWT token
   - The raw access token

## Testing Against Different OAuth Servers

To test against different OAuth servers:

1. Update the configuration in `application.yaml` with the new server details
2. Restart the application
3. Test the authentication flow

## Troubleshooting

- If you encounter CORS issues, make sure your OAuth server allows redirects to http://localhost:8080/callback
- Verify that your client ID and secret are correct
- Ensure the requested scopes are allowed for your client
- Check that the redirect URI (http://localhost:8080/callback) is registered with your OAuth server
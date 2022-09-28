# SkinFixer
Minecraft Spigot

SkinFixer aims to make skins possible for Offline-mode servers, and allow for users to change their skin in-game.
SkinFixer also works with Discord. You can upload your skin to a designated Discord channel, and get a code.

## Commands
- `/skin get <Nickname OR URL>` Get a token to set your skin. You can pass in either a URL which points directly at the image, or the username of a Minecraft player
- `/skin set <code>` Set the skin associated with the token
- `/skin help` View SkinFixer's Help page
- `/skin reload` Reload SkinFixer's configuration
- `/skin reset` Reset your skin back to defaults. This will remove it from the local storage. Relog to see the effects
- `/skin version` Perform an update check
- `/skin direct <url>` Fetches and immediately applies skin from URL. Useful for automated server-side skin management when reading a code from chat isn't feasible. 
## Permissions
- `skinfixer.get` Allows the use of the `get` subcommand. Default: ALL
- `skinfixer.reload` Allows the use of the `reload` subcommand. Default: OP
- `skinfixer.reset` Allows the use of the `reset` subcommand. Default: ALL
- `skinfixer.set` Allows the use of the `set` subcommand. Default: ALL
- `skinfixer.version` Allows the use of the `version` subcommand. Default: OP

## Features
- Change your Skin without restarting your game
- Allow skins to be used on offline-mode servers
    - This is automatic for those with a premium Minecraft account
- Upload an image to Discord and change your skin to that
- Paste a link in chat and that will become your skin
- Change your skin to another player's by their username

## Config
The default configuration is as follows:
```yaml
# DO NOT CHANGE THIS
configVersion: 2

# The type of database to use as storage backend
# Possible values are:
# - MYSQL
# - POSTGRES
# - BIN (default)
# - SQLITE
# Some of these options require further configuration, refer to sqlSettings
databaseType: BIN

# This setting is required for MySQL and PostgreSQL
sqlSettings:
    host: 'mysql.example.com'
    database: 'skinfixer_database'
    username: 'skinfixer_user'
    password: 'super secure password herer'

# Should SkinFixer itegrate with Discord, if this is set to true you will need to configure discordSettigns
useDiscord: false

# This is required if useDiscord is set to true
discordSettings:
    token: 'your secret bot token here'
    channelId: The ID of the channel to listen on here

# The language to use. Default: en
# If you want to use your own language, place your YAML file in the langs/ directory
# e.g if you have nl.yml, you would set this option to 'nl'
language: en

# Should SkinFixer perform an update check.
# It is recommended that you keep this enabled
updateCheck: true

# If this is set to true, SkinFixer will not inform players that it is changing their skin when they log in
# This however does not suppress any error message that might be send to the player
disableSkinApplyOnLoginMessage: false

# Should anonymous metrics be send to Dutchy76 (The author of SkinFixer)
# These metrics are used to get insights on what kind of systems SkinFixer is running
sendMetrics: true
```

## APIs
SkinFixer relies only on the [SkinFixer API](https://github.com/TobiasDeBruijn/SkinFixer-API). This API relies on two other APIs under the hood
- Mojang API
- [MineSkin API](https://github.com/MineSkin/api.mineskin.org)

The SkinFixer API was introduced as of v1.5.1 due to changes in the MineSkin API requiring authentication with an API key.  
The Mojang API was moved from being on the plugin side to being on the API side as of v1.7.0 to allow for easy cross-server caching

## Contributing
Please try to follow my codestyle, I don't yet follow any guideline myself.

## Issues
Issues can be reported [here](https://github.com/TobiasDeBruijn/SkinFixer/issues)

## Building
Refer to [this document](BUILDING.md)

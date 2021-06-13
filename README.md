# SkinFixer
Minecraft Spigot

SkinFixer aims to make skins possible for Offline-mode servers, and allow for users to change their skin in-game.

SkinFixer also works with Discord. You can upload your skin to a designated Discord channel, and get a code.

## Commands
- `/setskin <code> [slim true/false]` Set your skin from a provided code.
- `/getskin <url>` Get a code for your skin
- `/skinfixer help` Show the SkinFixer help page
- `/skinfixer version` Get the version of SkinFixer you are using

## Permissions
- `skinfixer.*` Grants all SkinFixer permissions
- `skinfixer.setskin` Allows the use of /setskin
- `skinfixer.getskin` Allows the use of /getskin
- `skinfixer.help` Allows the use of /skinfixer help
- `skinfixer.version` Allows the use of /skinfixer version

## Features
- Allow players to change their skin without logging off
- Allow players on Offline-mode servers to have a skin
- Premium-minecraft players will get their skin applied on offline-mode servers
- Allow players to upload their skin to Discord, and set that skin in-game
- Multi-language support

## APIs
SkinFixer relies on two APIs directly:
- Mojang API
- [SkinFixer API](https://github.com/TheDutchMC/SkinFixer-API)
    - [MineSkin API](https://github.com/MineSkin/api.mineskin.org)

The SkinFixer API was introduced as of v2.5.1 due to changes in the MineSkin API requiring authentication with an API key. The SkinFixer API relies on the MineSkin API under the hood.

## Contributing
Please try to follow my codestyle, I don't yet follow any guideline myself.

## Issues
Issues can be reported [here](https://github.com/TheDutchMC/SkinFixer/issues)

## Building
On Windows:  
`gradlew releasejar`  
On Linux:  
`./gradlew releasejar`

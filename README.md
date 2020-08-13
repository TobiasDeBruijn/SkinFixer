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

## Contributing
Please try to follow my codestyle, I don't yet follow any guideline myself.

## Issues
Issues can be reported [here](https://github.com/TheDutchMC/SkinFixer/issues)

## Adding new Spigot versions
1. Add a new folder in root called `Spigot_1_xx_Ry`, where `x` is the Minecraft version, and `y` is the specific release.
2. Add a new include to settings.gradle
3. Add a new depenceny to build.gradle for your version
4. Write your class
3. Add a new case to the switch in SkinChangeOrchestrator.java
4. Test and open a PR :)

## Building
On Windows:  
`gradlew shadowJar`  
On Linux:  
`./gradlew shadowJar`

# TreeLauncher by TreSet

A modern, component based Minecraft Launcher.

### Visit [the official Website](https://treelauncher.net) for more information.

## Features
- Component System
  - Instances are made up of components that can be reused across different instances
  - Components are:
    - Saves
    - Resourcepacks
    - Options
    - Mods
- Instance creation and management
  - Create, delete, and edit instances
  - Support for Vanilla, Fabric, Forge and Quilt
  - Smart version management so that the same version isn't installed multiple times
- Support for fabric, forge and quilt Mods
  - Searching mods from [Modrinth](https://modrinth.com/) and [CurseForge](https://www.curseforge.com/)
  - Automatic installation and updates
  - Automatic dependency resolution
- Automatic launcher updates

## WTF is a component based launcher?
As opposed to other launchers, TreeLauncher doesn't have instances that are hard capsuled from one another. Instead, each instance is made up of components that contain worlds, resourcepacks, mods etc. These components can be created and managed independently form any instance. 

Components can be used across different instances at the same time to synchronize data between them. So no more copying settings and worlds between instances or different game versions and having to try to keep them up to date! 

When creating a component, you can also easily inherit the configuration from another component, so you can easily update your mods or resourcepacks to a new game version without having to copy any files manually.

## Setup
- Download the latest `TreeLauncher-xxx.msi` file from [releases](https://github.com/Tre5et/treelauncher/releases) and install it
  - Windows might warn you about the installer because I'm not smart enough to sign it properly. Decide for your self whether you want to click `More info` and `Run anyway` ;)
- Alternatively, download the latest `TreeLauncher-xxx.zip` file from [releases](https://github.com/Tre5et/treelauncher/releases), extract it and run the `.exe` file.
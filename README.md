# World Preset Pack

A small collection of **custom world presets** for Minecraft **26.2**. Vanilla World Type and Customize stay as they are. The mod adds **World Presets…** on the Create World screen, plus **Configure** when the selected preset has options.

Currently ships **Skyblock** and **OneBlock**.

## Requirements

- Minecraft 26.2
- Java 25
- **Fabric** (with [Fabric API](https://modrinth.com/mod/fabric-api)) **or** **NeoForge**

Use the JAR that matches your loader. Do not install both. Client and server both need the mod if you play on a dedicated server.

## How to use

1. Create a new world.
2. Open the **World** tab.
3. Click **World Presets…** and pick Skyblock or OneBlock.
4. Click **Configure** if you want to change options.
5. Create the world.

Vanilla Default / Superflat / Large Biomes are unchanged. These presets are not mixed into the vanilla World Type list.

Designed for singleplayer Create World. A dedicated server can run a Skyblock or OneBlock world if that world was created with this mod; every player still needs the JAR.

## Skyblock

Void Overworld and Nether. You start on a small island with a chest, lava, water, ice, and obsidian. The End is vanilla (dragon island).

Configure (defaults: Classic, structures **OFF**, biome from seed):

- **Difficulty** — Easy (larger island, extra starter loot), Classic, Hard (tiny island, sparse chest). Same oak tree and center bedrock on every setting. No free iron or cobblestone.
- **Structures (Overworld & Nether)** — OFF by default. When ON, structures spawn farther apart than vanilla. The End is unchanged.
- **Spawn biome** — from the world seed, The Void, or a chosen biome around spawn. The Void cannot generate structures.

Skyblock-only advancements stay off in normal and OneBlock worlds.

## OneBlock

One grass block at spawn. Break it — another appears. Void Overworld and Nether, vanilla End. No Skyblock island and no Skyblock advancements.

- Chest every 25 blocks, mob every 50.
- Fall through the hole and you snap back if you stay next to the column.
- Phases: Plains → Underground → Winter → Ocean → Jungle → Nether → End (then stays End).

Configure: Slow / Normal / Fast phase speed.

## Build

```bat
.\gradlew.bat assemble
```

JARs:

- `fabric/build/libs/worldpresetpack-fabric-26.2-1.0.0.jar`
- `neoforge/build/libs/worldpresetpack-neoforge-26.2-1.0.0.jar`

Copy one of them with:

```bat
.\gradlew.bat :fabric:copyToMinecraftMods
.\gradlew.bat :neoforge:copyToMinecraftMods
```

Do not run `assemble` while a NeoForge `runClient` / `runServer` is still open (Windows file lock).

## License

[MIT](LICENSE.txt)

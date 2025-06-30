# Tree Command Mod

A simple Minecraft Forge mod that adds helpful tree-related commands.  
Use `/tree` to manage trees easily with a few subcommands.

## Commands

### `/tree clear <radius>`
Clears all trees within the given radius around the player.  
**Note:** This deletes logs and leaves in the area.

### `/tree place <radius> <density%>`
Gives you a **placement stick**.  
Right-click the ground with it to place the sapling in your offhand in the selected radius and density.

- **radius** – How far out from the click location to place saplings.
- **density** – A number from `0` to `100`, representing the percentage of area that will be filled with saplings.

### `/tree grow`
Grows all saplings placed using the placement stick.  
This works only for saplings placed with `/tree place`.

## Requirements

- Minecraft 1.20.1 (or your version here)
- Forge (same version as your mod setup)

## Notes

- Only standard Minecraft saplings are supported (oak, birch, etc.)
- You must hold a valid sapling in your **offhand** when using the placement stick.

## Example Usage

```
/tree clear 20
/tree place 10 30
/tree grow
```


This clears trees in a 20 block radius, lets you place saplings in a 10 block radius with 30% density, and then grows them.

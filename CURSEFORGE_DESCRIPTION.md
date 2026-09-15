# Enhanced Training Dummy

Turn Hytale's target dummy into a practical combat laboratory for testing weapons, abilities, character builds, and modded damage systems.

## Features

### Final Damage Tracking

Measures the final damage value after Hytale's regular damage processing. Compatible modifiers from other mods are included automatically, such as RPG level bonuses, critical-hit multipliers, backstab bonuses, weapon modifiers, buffs, and similar effects.

### Timed Damage Tests

Run focused 10-second tests and receive your total damage, average DPS, hit count, critical-hit count, and test duration.

### Live Results

Every successful hit and the running damage total are displayed directly in chat.

### Zone Simulation

Switch between five enemy-zone levels to compare a build across different progression tiers.

### Multiplayer Ready

Every player receives an independent test session. Results from multiple players never become mixed together.

### Stable Custom Dummy

The mod uses its own persistent training-dummy entity. It remains in place while being attacked and cannot be accidentally destroyed during a damage test.

### Localized

Interaction hints and chat results follow the player's selected Hytale language. English and German are included.

### Survival-Friendly

Craft the Enhanced Training Dummy at the Workbench in the Tinkering category. Place it with right-click and pick it back up whenever needed.

## How to Use

1. Craft an Enhanced Training Dummy at the Workbench.
2. Place it with right-click.
3. Look at the dummy and press **F** to start a 10-second test.
4. Attack it with the weapon or ability you want to measure.
5. Read individual hits and the running total in chat.
6. After 10 seconds, the total damage, DPS, hits, critical hits, and duration are shown automatically.
7. Press **F** again to restart the test and select the next enemy zone.
8. Hold crouch and press **F** to pick up the dummy.

## Mod Compatibility

Enhanced Training Dummy runs after Hytale's normal gather and filter damage stages and immediately before health damage is applied. Mods that use Hytale's regular damage pipeline are therefore measured automatically, including compatible RPG progression, critical-hit, backstab, equipment, skill, and buff systems.

Damage applied by another mod through a completely separate system that directly changes health without producing a Hytale damage event cannot be detected.

## Installation

Place the mod JAR in your Hytale server's mods directory and restart the server completely.

## Feedback

Enhanced Training Dummy is under active development. If you encounter an incompatible damage modifier or have an idea for another training feature, please leave a comment and include the name of the affected mod.

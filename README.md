# Enhanced Training Dummy

A Hytale server mod for repeatable damage and DPS tests with a dedicated, craftable training dummy.

## Current Features

- Custom item and NPC role using the ID `Enhanced_Training_Dummy`
- Craftable at the Workbench in the Tinkering category
- Placement with the secondary-use action (right-click by default)
- 10-second damage tests started with **F/Use**
- Five zone simulations with damage multipliers from 100% to 60%
- Live damage and running totals in chat
- Final summary with total damage, DPS, hit count, critical-hit count, and duration
- Independent sessions for every player and dummy
- No knockback and no applied health damage
- Pick-up with **Crouch + F**; the item is returned to the inventory
- English and German translations

## How to Use

1. Craft the **Enhanced Training Dummy** at a Workbench in the Tinkering category.
2. Place the dummy with right-click.
3. Look at the dummy and press **F** to start a 10-second test.
4. Attack the dummy. Each hit and the running total appear in chat.
5. Press **F** again to restart the test and switch to the next zone.
6. Hold crouch and press **F** to pick up the dummy.

Zone multipliers currently use these fixed presets:

- Zone 1: 100%
- Zone 2: 90%
- Zone 3: 80%
- Zone 4: 70%
- Zone 5: 60%

## Damage Integration

The plugin registers `PlayerInteractEvent` for **F/Use** and a `DamageEventSystem` through `getEntityStoreRegistry()`.

Damage is captured after Hytale's gather and filter stages and before health damage is applied. The dummy therefore records the final damage value produced by Hytale's regular damage pipeline, including compatible modifiers from other mods. The damage event is then cancelled, which prevents the dummy from losing health.

Damage that bypasses Hytale's regular damage events and changes health directly cannot be recorded.

Critical-hit detection is not connected yet, so the critical-hit count currently remains at zero.

## Building

Requirements:

- JDK 25
- Gradle 9
- `HytaleServer.jar` from your Hytale installation

Set the server JAR through an environment variable:

```bash
export HYTALE_SERVER_JAR=/absolute/path/to/HytaleServer.jar
gradle clean test jar
```

Alternatively, pass it as a Gradle property:

```bash
gradle clean test jar -PhytaleServerJar=/absolute/path/to/HytaleServer.jar
```

Without the server JAR, Gradle excludes the Hytale integration and builds only the testable core:

```bash
gradle clean test
```

The complete mod JAR is generated at:

```text
build/libs/TrainingDummy-0.4.0.jar
```

## Installation

Copy `TrainingDummy-0.4.0.jar` into the Hytale server's mods directory and restart the server completely.

The plugin manifest currently declares:

- Group: `de.trainingdummy`
- Name: `TrainingDummy`
- Version: `0.4.0`
- Main class: `de.trainingdummy.hytale.TrainingDummyPlugin`
- Asset pack: included

## Asset IDs

- Item ID: `Enhanced_Training_Dummy`
- NPC role ID: `Enhanced_Training_Dummy`
- Model asset: `NPC/TrainingDummy/TrainingDummy.blockymodel`
- Texture asset: `NPC/TrainingDummy/TrainingDummy_Default.png`

The old vanilla NPC ID `Tinkering_Target_Dummy` is no longer used by the plugin.

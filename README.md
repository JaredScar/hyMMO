# hyMMO

hyMMO is a Hytale skill plugin. Nine skills gain experience from the tool or action you use. Each level gives one point for that skill, and those points buy ranks in its tree. Active skills also have a timed ability.

Author: Axeex

## Install

Build `hyMMO.jar`, put it in the server `mods` folder, and restart the server. The plugin id is `jaredscarito:hyMMO`.

Open `/mmo` in game. `/hymmo`, `/mcmmo`, and `/mcstats` open the same page.

Saves are written under the server data directory in `mods/jaredscarito_hyMMO/`. `skills.json` holds profiles. `config.json` holds the tuning values and is created on first start.

## Build

Java 25 is required. The Gradle wrapper is included.

```shell
./gradlew build -PhytaleServerJar=/path/to/HytaleServer.jar
```

`HYTALE_SERVER_JAR` is accepted as well. The jar is written to `build/libs/hyMMO.jar`.

## Skills

Creative mode does not gain experience and does not start abilities. Adventure mode does.

Right-click the matching tool to start an ability once that skill is level 5. The click still performs the normal item use. `/mmo activate` starts the ability for the tool in your hand if you would rather type it. Acrobatics has no right-click ability.

| Skill | How you train | Ability |
| --- | --- | --- |
| Mining | Break stone and ore with a pickaxe. Higher levels can drop extra ore. | Super Breaker mines faster and guarantees a double drop. Right-click the pickaxe. |
| Woodcutting | Chop logs with an axe. Higher levels can drop extra wood. | Tree Feller breaks the connected tree, up to a cap. Right-click the axe. |
| Excavation | Dig soil, sand, and gravel with a shovel. | Giga Drill breaks a horizontal 3×3. Right-click the shovel. |
| Herbalism | Harvest plants with a hoe, or with empty hands on a plant. | Green Terra harvests a horizontal 3×3 and doubles the drop. Right-click the hoe. |
| Swords | Hit with a sword. Higher levels hit harder and can cause bleed. | Serrated Strikes hits harder. Right-click the sword. |
| Axes | Hit with an axe. Higher levels hit harder. | Skull Splitter hits harder. Right-click the axe. That same click also starts Tree Feller. |
| Unarmed | Hit with an empty hand. Higher levels hit harder. | Berserk hits harder. Right-click with nothing held. |
| Archery | Land hits with a bow. Higher levels hit harder. | Skill Shot hits harder. Right-click the bow. |
| Acrobatics | Falling and dodging. | Roll reduces fall damage. Dodge can cancel a hit. There is nothing to activate. |

An ability that is locked, already running, or cooling down tells you once, then stays quiet for a few seconds. A successful start always tells you.

## Skill tree

`/mmo` lists every skill. Open one to see its level, experience, what the ability does, and two paths of ranks.

Click a gold rank to spend a point. Green is learned. Gray is locked. The first rank of a path needs level 1, the second needs level 8, and the third needs level 20. Each rank also requires the rank before it. Learned ranks stay if you keep the level. Respec refunds the ranks you bought on that skill and gives the points back. Level and experience stay.

## Commands

| Command | What it does |
| --- | --- |
| `/mmo` | Open the skill page. |
| `/mmo <skill>` | Open one skill. Aliases include `mine`, `wood`, `wc`, `dig`, `herb`, `sword`, `axe`, `fist`, `bow`, and `acro`. |
| `/mmo activate` | Start the ability for the tool in your hand. `ability` and `ready` are aliases. |
| `/mmo top <skill>` | Show the leaders for a skill. `rank` and `mctop` are aliases. |

## Staff

Grant `hymmo.admin`.

| Command | What it does |
| --- | --- |
| `/mmo admin set <player> <skill> <level>` | Set a player's skill level. |
| `/mmo admin addxp <player> <skill> <amount>` | Give a player skill experience. |
| `/mmo admin reload` | Reload `config.json`. |

## Config

`config.json` lives beside `skills.json`. A few of the values:

| Key | Default | Meaning |
| --- | --- | --- |
| `maxLevel` | 1000 | Level cap. |
| `baseXp` | 120 | Experience required at the start of the curve. |
| `xpPerLevel` | 20 | Extra experience required for each later level. |
| `abilityUnlockLevel` | 5 | Level where the timed ability can be used. |
| `baseAbilitySeconds` | 2 | Shortest ability duration. |
| `levelsPerAbilitySecond` | 50 | Levels needed for one extra second. |
| `maxAbilitySeconds` | 16 | Longest ability duration. |
| `cooldownFactor` | 3 | Cooldown relative to the duration. |
| `treeCap` | 48 | Connected blocks Tree Feller will break before tree ranks. |
| `superBreakerSpeed` | 3 | Break-speed multiplier for Super Breaker. |

Experience amounts, double-drop chance, damage bonus, dodge, roll, and bleed are in the same file. Fishing, repair, taming, alchemy, and parties are not part of hyMMO.

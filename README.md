# Hytale Training Dummy

Ein wartbares Mod-Grundprojekt für einen echten Trainings-Dummy:

- Zeitmessung für 1–600 Sekunden
- Gesamtschaden, DPS, Treffer und Crits
- freies Training ohne Zeitlimit
- einzelne Schadenszahlen ein/aus
- fünf konfigurierbare Zonenstufen
- getrennte Sitzungen pro Spieler und Puppe
- automatisch unzerstörbar in der Hytale-Anbindung

## Wichtiger Stand

Die Kernlogik und die serverseitige Hytale-Anbindung sind implementiert. Sie wurden anhand der tatsächlich gelieferten
`HytaleServer.jar`-Signaturen und der NPC-ID `Tinkering_Target_Dummy` erstellt. **F** auf der Puppe startet einen
10-Sekunden-Test und schaltet bei jedem erneuten Drücken zur nächsten Zone. Trefferwerte und das Endergebnis erscheinen
im Chat; die Puppe ist unzerstörbar.

Ein grafisches Custom-UI und schwebende World-Space-Zahlen sind noch nicht enthalten. Dafür werden zusätzlich passende
`.ui`-/HUD-Assets benötigt; die aktuelle Version verwendet bewusst den zuverlässigeren Chat-Workflow.

## Bauen

Voraussetzungen: JDK 25, Gradle 9 und die `HytaleServer.jar` deiner Installation.

```bash
export HYTALE_SERVER_JAR=/absoluter/pfad/HytaleServer.jar
gradle clean test jar
```

Ohne `HYTALE_SERVER_JAR` wird nur der vollständig testbare Kern gebaut:

```bash
gradle clean test
```

Danach liegt die JAR unter `build/libs/TrainingDummy-0.1.0.jar`. Kopiere sie in den Mods-/Plugins-Ordner des Servers.

## Gewünschter Spielablauf

1. Spieler drückt **F** auf der Trainingspuppe.
2. Menü: Zone, 5/10/30/60 Sekunden oder freies Training, Schadenszahlen, Start/Reset.
3. Treffer werden serverseitig aus dem finalen `Damage#getAmount()` übernommen.
4. Bei aktivierten Schadenszahlen wird der effektive Wert über der Puppe angezeigt.
5. Nach Ablauf erscheint im Chat: Gesamtschaden, DPS, Treffer, Crits und Dauer.

## Anschluss an Hytale 0.6.6

Die Hauptklasse registriert `PlayerInteractEvent` für **F/Use** und ein `DamageEventSystem` über
`getEntityStoreRegistry()`. Zielprüfung, Angreiferauflösung, Messung und Unzerstörbarkeit sind bereits angeschlossen.

Die Zone-Multiplikatoren sind zunächst neutrale, editierbare Presets. Für exakte Vanilla-Gegnerwerte sollten sie
nach dem Auslesen der aktuellen Zone-/NPC-Assets deiner Installation ersetzt werden.

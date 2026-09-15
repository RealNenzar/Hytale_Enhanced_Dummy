package de.trainingdummy.core;

public record TestSummary(double totalDamage, double dps, int hits, int criticalHits, double elapsedSeconds) {
    public String chatLine() {
        return "Training Dummy | Gesamtschaden: %.1f | DPS: %.1f | Treffer: %d | Crits: %d | Zeit: %.2fs"
            .formatted(totalDamage, dps, hits, criticalHits, elapsedSeconds);
    }
}

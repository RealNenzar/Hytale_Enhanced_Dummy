package de.trainingdummy.core;

/** Enemy progression tiers. Multipliers model the damage that remains after zone defence. */
public enum Zone {
    ZONE_1("Zone 1", 1.00),
    ZONE_2("Zone 2", 0.90),
    ZONE_3("Zone 3", 0.80),
    ZONE_4("Zone 4", 0.70),
    ZONE_5("Zone 5", 0.60);

    private final String label;
    private final double damageMultiplier;

    Zone(String label, double damageMultiplier) {
        this.label = label;
        this.damageMultiplier = damageMultiplier;
    }

    public String label() { return label; }
    public double damageMultiplier() { return damageMultiplier; }
}

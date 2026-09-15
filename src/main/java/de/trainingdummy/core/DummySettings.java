package de.trainingdummy.core;

public record DummySettings(Zone zone, int durationSeconds, boolean damageNumbers, boolean freeTraining) {
    public DummySettings {
        if (zone == null) throw new IllegalArgumentException("zone");
        if (durationSeconds < 1 || durationSeconds > 600) {
            throw new IllegalArgumentException("durationSeconds must be 1..600");
        }
    }

    public static DummySettings defaults() {
        return new DummySettings(Zone.ZONE_1, 10, true, false);
    }
}

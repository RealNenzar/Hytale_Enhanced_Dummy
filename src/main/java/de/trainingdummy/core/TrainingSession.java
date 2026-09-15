package de.trainingdummy.core;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public final class TrainingSession {
    private final Instant startedAt;
    private final DummySettings settings;
    private double totalDamage;
    private int hits;
    private int criticalHits;
    private boolean finished;

    public TrainingSession(Instant startedAt, DummySettings settings) {
        this.startedAt = startedAt;
        this.settings = settings;
    }

    public HitResult hit(Instant now, double rawDamage, boolean critical) {
        if (rawDamage < 0 || !Double.isFinite(rawDamage)) throw new IllegalArgumentException("damage");
        boolean expired = !settings.freeTraining() && elapsed(now) >= settings.durationSeconds();
        if (finished || expired) {
            finished = true;
            return new HitResult(rawDamage, 0, hits, totalDamage, critical, true);
        }
        double effective = rawDamage * settings.zone().damageMultiplier();
        totalDamage += effective;
        hits++;
        if (critical) criticalHits++;
        return new HitResult(rawDamage, effective, hits, totalDamage, critical, false);
    }

    public Optional<TestSummary> finishIfDue(Instant now) {
        if (!settings.freeTraining() && elapsed(now) >= settings.durationSeconds()) {
            finished = true;
            return Optional.of(summary(now));
        }
        return Optional.empty();
    }

    public TestSummary stop(Instant now) {
        finished = true;
        return summary(now);
    }

    public TestSummary summary(Instant now) {
        double seconds = settings.freeTraining()
            ? Math.max(0.001, elapsed(now))
            : Math.min(settings.durationSeconds(), Math.max(0.001, elapsed(now)));
        return new TestSummary(totalDamage, totalDamage / seconds, hits, criticalHits, seconds);
    }

    private double elapsed(Instant now) {
        return Math.max(0, Duration.between(startedAt, now).toNanos() / 1_000_000_000d);
    }
}

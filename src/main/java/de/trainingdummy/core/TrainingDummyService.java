package de.trainingdummy.core;

import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Thread-safe state store. A session belongs to one player and one dummy. */
public final class TrainingDummyService {
    private record Key(UUID playerId, UUID dummyId) {}
    private final Clock clock;
    private final Map<UUID, DummySettings> settings = new ConcurrentHashMap<>();
    private final Map<Key, TrainingSession> sessions = new ConcurrentHashMap<>();

    public TrainingDummyService(Clock clock) { this.clock = clock; }

    public DummySettings settings(UUID dummyId) {
        return settings.getOrDefault(dummyId, DummySettings.defaults());
    }

    public void configure(UUID dummyId, DummySettings value) { settings.put(dummyId, value); }

    public void start(UUID playerId, UUID dummyId) {
        sessions.put(new Key(playerId, dummyId), new TrainingSession(clock.instant(), settings(dummyId)));
    }

    public Optional<HitResult> hit(UUID playerId, UUID dummyId, double damage, boolean critical) {
        var key = new Key(playerId, dummyId);
        var session = sessions.get(key);
        if (session == null) {
            start(playerId, dummyId);
            session = sessions.get(key);
        }
        return Optional.of(session.hit(clock.instant(), damage, critical));
    }

    public Optional<TestSummary> poll(UUID playerId, UUID dummyId) {
        var key = new Key(playerId, dummyId);
        var session = sessions.get(key);
        if (session == null) return Optional.empty();
        var result = session.finishIfDue(clock.instant());
        result.ifPresent(ignored -> sessions.remove(key));
        return result;
    }

    public Optional<TestSummary> stop(UUID playerId, UUID dummyId) {
        var session = sessions.remove(new Key(playerId, dummyId));
        return session == null ? Optional.empty() : Optional.of(session.stop(clock.instant()));
    }

    public void reset(UUID playerId, UUID dummyId) { sessions.remove(new Key(playerId, dummyId)); }

    public void removeDummy(UUID dummyId) {
        settings.remove(dummyId);
        sessions.keySet().removeIf(key -> key.dummyId().equals(dummyId));
    }
}

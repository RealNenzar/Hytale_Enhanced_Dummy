package de.trainingdummy.core;

import static org.junit.jupiter.api.Assertions.*;
import java.time.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TrainingDummyServiceTest {
    static final class MutableClock extends Clock {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        public ZoneId getZone() { return ZoneOffset.UTC; }
        public Clock withZone(ZoneId zone) { return this; }
        public Instant instant() { return now; }
        void addSeconds(long seconds) { now = now.plusSeconds(seconds); }
    }

    @Test void totalsAndDpsUseZoneMultiplier() {
        var clock = new MutableClock();
        var service = new TrainingDummyService(clock);
        var player = UUID.randomUUID(); var dummy = UUID.randomUUID();
        service.configure(dummy, new DummySettings(Zone.ZONE_3, 10, true, false));
        service.start(player, dummy);
        assertEquals(80, service.hit(player, dummy, 100, false).orElseThrow().effectiveDamage());
        clock.addSeconds(5);
        service.hit(player, dummy, 50, true);
        var summary = service.stop(player, dummy).orElseThrow();
        assertEquals(120, summary.totalDamage());
        assertEquals(24, summary.dps());
        assertEquals(2, summary.hits());
        assertEquals(1, summary.criticalHits());
    }

    @Test void timedSessionRejectsLateHitAndFinishes() {
        var clock = new MutableClock();
        var service = new TrainingDummyService(clock);
        var player = UUID.randomUUID(); var dummy = UUID.randomUUID();
        service.start(player, dummy);
        service.hit(player, dummy, 25, false);
        clock.addSeconds(10);
        assertTrue(service.hit(player, dummy, 99, false).orElseThrow().testFinished());
        var summary = service.poll(player, dummy).orElseThrow();
        assertEquals(25, summary.totalDamage());
        assertEquals(2.5, summary.dps());
    }
}

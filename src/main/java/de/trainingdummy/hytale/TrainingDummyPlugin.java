package de.trainingdummy.hytale;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.dependency.SystemGroupDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import de.trainingdummy.core.DummySettings;
import de.trainingdummy.core.TrainingDummyService;
import de.trainingdummy.core.Zone;
import java.time.Clock;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TrainingDummyPlugin extends JavaPlugin {
    public static final String DUMMY_TYPE = "Enhanced_Training_Dummy";
    private final TrainingDummyService training = new TrainingDummyService(Clock.systemUTC());
    private final Map<UUID, Zone> selectedZones = new ConcurrentHashMap<>();
    private final Set<UUID> removalPending = ConcurrentHashMap.newKeySet();

    public TrainingDummyPlugin(JavaPluginInit init) { super(init); }

    @Override
    protected void setup() {
        getEventRegistry().registerGlobal(PlayerInteractEvent.class, this::onUseDummy);
        getEntityStoreRegistry().registerSystem(new DummyInteractionSystem(this));
        getEntityStoreRegistry().registerSystem(new DummyDamageSystem(training));
        getLogger().atInfo().log("Enhanced Training Dummy enabled for NPC role %s", DUMMY_TYPE);
    }

    private void onUseDummy(PlayerInteractEvent event) {
        if (event.getActionType() != InteractionType.Use) return;
        var dummy = resolveTargetDummy(event);
        if (dummy == null || !DUMMY_TYPE.equals(dummy.getNPCTypeId())) return;

        event.setCancelled(true);
        var dummyRef = event.getTargetRef() != null ? event.getTargetRef() : dummy.getReference();
        handleDummyUse(event.getPlayerRef(), event.getPlayer().getPlayerRef(),
            dummyRef, dummy, null);
    }

    private void handleDummyUse(Ref<EntityStore> playerEntityRef, PlayerRef playerRef,
                                Ref<EntityStore> dummyRef, NPCEntity dummy,
                                CommandBuffer<EntityStore> commandBuffer) {

        var movement = playerEntityRef.getStore().getComponent(
            playerEntityRef, MovementStatesComponent.getComponentType()
        );
        if (movement != null && movement.getMovementStates() != null
                && movement.getMovementStates().crouching) {
            var dummyId = dummy.getUuid();
            if (dummyRef == null || !dummyRef.isValid() || !removalPending.add(dummyId)) return;

            var returned = Player.giveItem(
                new ItemStack(DUMMY_TYPE, 1), playerEntityRef, playerEntityRef.getStore()
            );
            if (!returned.succeeded()) {
                removalPending.remove(dummyId);
                playerRef.sendMessage(Message.translation(
                    "server.trainingDummy.inventoryFull"
                ).color("#FF6B6B"));
                return;
            }

            if (commandBuffer != null) {
                commandBuffer.removeEntity(dummyRef, RemoveReason.REMOVE);
            } else {
                dummyRef.getStore().removeEntity(dummyRef, RemoveReason.REMOVE);
            }
            training.removeDummy(dummyId);
            playerRef.sendMessage(Message.translation(
                "server.trainingDummy.pickedUp"
            ).color("#7CFF6B"));
            return;
        }

        var playerId = playerRef.getUuid();
        var dummyId = dummy.getUuid();
        var next = nextZone(selectedZones.getOrDefault(playerId, Zone.ZONE_5));
        selectedZones.put(playerId, next);
        training.configure(dummyId, new DummySettings(next, 10, true, false));
        training.reset(playerId, dummyId);
        training.start(playerId, dummyId);
        playerRef.sendMessage(Message.translation("server.trainingDummy.testStarted")
            .param("zone", next.ordinal() + 1)
            .param("seconds", 10)
            .color("#70E1F5"));
    }

    private static final class DummyInteractionSystem extends EntityTickingSystem<EntityStore> {
        private final TrainingDummyPlugin plugin;

        private DummyInteractionSystem(TrainingDummyPlugin plugin) { this.plugin = plugin; }

        @Override
        public Query<EntityStore> getQuery() {
            return Query.and(NPCEntity.getComponentType(), StateSupport.getComponentType());
        }

        @Override
        public void tick(float deltaTime, int index, ArchetypeChunk<EntityStore> chunk,
                         Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer) {
            var dummy = chunk.getComponent(index, NPCEntity.getComponentType());
            if (dummy == null || !DUMMY_TYPE.equals(dummy.getNPCTypeId())) return;
            var state = chunk.getComponent(index, StateSupport.getComponentType());
            if (state == null) return;
            var dummyRef = chunk.getReferenceTo(index);

            store.forEachChunk(PlayerRef.getComponentType(), (playerChunk, ignored) -> {
                for (int playerIndex = 0; playerIndex < playerChunk.size(); playerIndex++) {
                    var playerEntityRef = playerChunk.getReferenceTo(playerIndex);
                    if (!state.consumeInteraction(playerEntityRef)) continue;
                    var playerRef = playerChunk.getComponent(playerIndex, PlayerRef.getComponentType());
                    if (playerRef != null) {
                        plugin.handleDummyUse(playerEntityRef, playerRef, dummyRef, dummy, commandBuffer);
                    }
                }
            });
        }
    }

    private static NPCEntity resolveTargetDummy(PlayerInteractEvent event) {
        if (event.getTargetEntity() instanceof NPCEntity dummy) return dummy;
        var targetRef = event.getTargetRef();
        if (targetRef == null || !targetRef.isValid()) return null;
        return targetRef.getStore().getComponent(targetRef, NPCEntity.getComponentType());
    }

    private static Zone nextZone(Zone zone) {
        var values = Zone.values();
        return values[(zone.ordinal() + 1) % values.length];
    }

    private static final class DummyDamageSystem extends DamageEventSystem {
        private final TrainingDummyService training;
        private DummyDamageSystem(TrainingDummyService training) { this.training = training; }

        @Override
        public Query<EntityStore> getQuery() { return NPCEntity.getComponentType(); }

        @Override
        public Set<Dependency<EntityStore>> getDependencies() {
            return Set.of(
                new SystemGroupDependency<>(Order.AFTER, DamageModule.get().getGatherDamageGroup()),
                new SystemGroupDependency<>(Order.AFTER, DamageModule.get().getFilterDamageGroup()),
                new SystemDependency<>(Order.BEFORE, DamageSystems.ApplyDamage.class)
            );
        }

        @Override
        public void handle(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store,
                           CommandBuffer<EntityStore> commandBuffer, Damage damage) {
            var dummy = chunk.getComponent(index, NPCEntity.getComponentType());
            if (dummy == null || !DUMMY_TYPE.equals(dummy.getNPCTypeId())) return;

            damage.setCancelled(true);
            if (!(damage.getSource() instanceof Damage.EntitySource source)) return;
            var playerRef = commandBuffer.getComponent(source.getRef(), PlayerRef.getComponentType());
            if (playerRef == null) return;

            var playerId = playerRef.getUuid();
            var dummyId = dummy.getUuid();
            var hit = training.hit(playerId, dummyId, damage.getAmount(), false).orElseThrow();
            var settings = training.settings(dummyId);
            if (settings.damageNumbers() && !hit.testFinished()) {
                playerRef.sendMessage(Message.translation("server.trainingDummy.damage")
                    .param("damage", hit.effectiveDamage())
                    .param("total", hit.runningTotal())
                    .color("#FFD166"));
            }
            training.poll(playerId, dummyId)
                .ifPresent(summary -> playerRef.sendMessage(
                    Message.translation("server.trainingDummy.summary")
                        .param("total", summary.totalDamage())
                        .param("dps", summary.dps())
                        .param("hits", summary.hits())
                        .param("crits", summary.criticalHits())
                        .param("seconds", summary.elapsedSeconds())
                        .color("#7CFF6B")
                ));
        }
    }
}

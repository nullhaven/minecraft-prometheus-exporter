package de.sldk.mc.metrics;

import io.prometheus.client.Gauge;
import org.bukkit.World;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Get total count of Villagers.
 * <p>
 * Labelled by
 * <ul>
 *     <li> World ({@link World#getName()})
 *     <li> Profession, e.g. 'fisherman', 'farmer', or 'none' ({@link org.bukkit.entity.Villager.Profession})
 * </ul>
 */
public class Villagers extends WorldMetric {

    private static final Gauge VILLAGERS = Gauge.build()
            .name(prefix("villagers_total"))
            .help("Villagers total count, labelled by world, type, profession, and level")
            .labelNames("world", "profession")
            .create();

    public Villagers(Plugin plugin) {
        super(plugin, VILLAGERS);
    }

    @Override
    protected void clear() {
        VILLAGERS.clear();
    }

    @Override
    public void collect(World world) {
        Map<VillagerGrouping, Long> mapVillagerGroupingToCount = world
                .getEntitiesByClass(Villager.class).stream()
                .collect(Collectors.groupingBy(VillagerGrouping::new, Collectors.counting()));

        mapVillagerGroupingToCount.forEach((grouping, count) ->
                VILLAGERS
                        .labels(world.getName(), grouping.profession.name())
                        .set(count)
        );
    }

    /**
     * Class used to group villagers together before summation.
     */
    private static class VillagerGrouping {
        private final Villager.Profession profession;

        VillagerGrouping(Villager villager) {
            this.profession = villager.getProfession();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VillagerGrouping that = (VillagerGrouping) o;
            return profession == that.profession;
        }

        @Override
        public int hashCode() {
            return Objects.hash(profession);
        }
    }
}

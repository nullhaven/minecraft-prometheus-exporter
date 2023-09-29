package de.sldk.mc.metrics;

import de.sldk.mc.utils.PluginClassRegistry;
import io.prometheus.client.Gauge;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;

import java.lang.reflect.Field;
import java.util.List;

public class EventsTotal extends Metric {

    private static final Gauge EVENTS = Gauge.build()
            .name(prefix("events_total"))
            .help("Count of events handled (allowed/cancelled)")
            .labelNames("type", "cancellable", "cancelled", "asynchronous", "plugin")
            .create();

    private final EventHandler handler = new EventHandler();
    private final PluginClassRegistry classRegistry = new PluginClassRegistry(Bukkit.getPluginManager());

    public EventsTotal(Plugin plugin) {
        super(plugin, EVENTS);
        this.injectListener();
    }

    private void injectListener() {
        List<HandlerList> lists;

        try {
            Field allLists = HandlerList.class.getDeclaredField("allLists");
            allLists.setAccessible(true);
            @SuppressWarnings("unchecked") List<HandlerList> _lists =
                    (List<HandlerList>) allLists.get(null);
            lists = _lists;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        Plugin plugin = getPlugin();
        RegisteredListener listener = new RegisteredListener(handler, handler::execute,
                EventPriority.MONITOR, plugin, false);

        for (HandlerList list : lists) {
            list.register(listener);
        }
    }

    private class EventHandler implements Listener {
        public void execute(Listener listener, Event event) {
            String type = event.getEventName();
            boolean cancellable = event instanceof Cancellable;
            String cancelled = String.valueOf(cancellable && ((Cancellable) event).isCancelled());
            String async = String.valueOf(event.isAsynchronous());

            String ownPlugin = "null";
            Plugin plugin = classRegistry.getPluginByClass(event.getClass());
            if (plugin != null) ownPlugin = plugin.getName();

            EventsTotal.EVENTS.labels(type, String.valueOf(cancellable), cancelled, async, ownPlugin).inc();
        }
    }

    @Override
    public void doCollect() {

    }
}

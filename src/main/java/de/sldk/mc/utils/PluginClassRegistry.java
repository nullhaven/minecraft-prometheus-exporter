package de.sldk.mc.utils;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.lang.reflect.Field;
import java.util.*;

public final class PluginClassRegistry {
    private final Map<Class<?>, Plugin> classPluginMap = new HashMap<>();
    private final PluginManager pluginManager;

    public PluginClassRegistry(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
        this.reload();
    }

    public void reload() {
        for (Plugin plugin : pluginManager.getPlugins()) {
            for (Class<?> clazz : getClasses(plugin)) {
                classPluginMap.put(clazz, plugin);
            }
        }
    }

    private static Collection<Class<?>> getClasses(Plugin plugin) {
        JavaPluginLoader pluginLoader = (JavaPluginLoader) plugin.getPluginLoader();
        try {
            Field classes0 = JavaPluginLoader.class.getDeclaredField("classes0");
            classes0.setAccessible(true);
            @SuppressWarnings("unchecked") Map<String, Class<?>> classMap =
                    (Map<String, Class<?>>) classes0.get(pluginLoader);
            return classMap.values();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Plugin getPluginByClass(Class<?> clazz) {
        return classPluginMap.get(clazz);
    }
}

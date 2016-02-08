package me.yourselvs;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ListenerClass implements Listener {
	public ListenerClass(MainClass plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onEvent() {
		
	}
}

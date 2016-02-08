package me.yourselvs;

import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import resources.Suggestion;

public class MainClass extends JavaPlugin {
	
	String prefix = "[" + ChatColor.AQUA + ChatColor.BOLD + "SH" + ChatColor.RESET + "]";
	String[] info = {prefix + " SuggestionHelper plugin v1.0", prefix + " Created by yourselvs", prefix + " Type \"/sh help\" to view Simple Poll commands."};
	
	ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();

	@Override
	public void onEnable() {
		getLogger().info("SimplePoll successfully enabled.");
	}
	
	@Override
	public void onDisable() {
		getLogger().info("SimplePoll successfully disbled.");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(label.equalsIgnoreCase("suggest") && sender instanceof Player) {
			Player player = (Player) sender;
			String suggestion = "";
			for(String word : args)
				suggestion = suggestion + word;
			suggestions.add(new Suggestion(suggestion, player));
			return true;
		}
		
		else if((label.equalsIgnoreCase("sh") || label.equalsIgnoreCase("suggestionhelper")) && sender instanceof Player){
			Player player = (Player) sender;
			if(args.length == 0)
				player.sendMessage(info);
			else{
				String subcmd = args[0];
				if(subcmd.equals("list"))
					;
				else if(subcmd.equals("view"))
					;
				else if(subcmd.equals("save"))
					;
				else if(subcmd.equals("del"))
					;
				else
					processError(player);
			}
		}
		
		return false;	
		
	}
	
	private void processError(Player player) {
		sendMessage(player, "Unknown command. Type \"/poll help\" to see a command list.");
	}
	
	private void sendMessage(Player player, String line){
		player.sendMessage(prefix + " " + line);
	}
}
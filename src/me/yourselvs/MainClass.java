package me.yourselvs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class MainClass extends JavaPlugin {
	
	
	final String prefix = "[" + ChatColor.AQUA + ChatColor.BOLD + "SH" + ChatColor.RESET + "]";
	final String[] info = {prefix + " SuggestionHelper plugin v1.0", prefix + " Created by yourselvs", prefix + " Type \"/sh help\" to view Simple Poll commands."};
	
	final String allPath = "suggestions.json";
	
	final int pageSize = 6;
	
	Map<String, Suggestion> map = new HashMap<String, Suggestion>();
	
	File dataFolder;
	File suggestions;
	
	Gson gson;
	Writer writer;
	
	@Override
	public void onEnable() {
		map = new HashMap<String, Suggestion>();
		gson = new Gson();
		initFiles();
		readFiles();
		initWriter();
		writeFiles();
		
		getLogger().info("SuggestionHelper successfully enabled.");
	}
	
	@Override
	public void onDisable() {
		getLogger().info("SuggestionHelper successfully disbled.");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(label.equalsIgnoreCase("suggest") && sender instanceof Player) 
			processSuggest(sender, args);
		else if((label.equalsIgnoreCase("sh") || label.equalsIgnoreCase("suggestionhelper")) && sender instanceof Player)
			processSh(sender, args);
		else
			return false;
		return true;
	}

	private void initFiles() {
		
		dataFolder = getDataFolder();
		if(!dataFolder.exists())
			dataFolder.mkdir();
		
		suggestions = new File(getDataFolder(), allPath);
		if(!suggestions.exists())
			try {suggestions.createNewFile();} catch (IOException e) {getLogger().info(e.getMessage());}
	}
	
	private void initWriter() {		
		try {
			writer = new FileWriter(suggestions);
		} catch (IOException e) {getLogger().info(e.getMessage());}
	}
	
	@SuppressWarnings({ "unchecked" })
	private void readFiles() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(suggestions));
			map = gson.fromJson(br, Map.class);
			if(map == null){
				getLogger().info("Empty file read in. Creating new one.");
				map = new HashMap<String, Suggestion>();
			}
		} catch (JsonSyntaxException | JsonIOException | IOException e) {getLogger().info(e.getMessage());}
	}
	
	private void writeFiles(){
		initWriter();
		try {
			writer.write(gson.toJson(map));
		} catch (IOException e) {getLogger().info(e.getMessage());}
		closeFiles();
	}
	
	private void closeFiles() {
		try {
			writer.close();
		} catch (IOException e) {getLogger().info(e.getMessage());}
	}

	private void processSuggest(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		String suggestion = "";
		for(String word : args)
			suggestion = suggestion + word + " ";
		Integer size = map.size();
		map.put(size.toString(), new Suggestion(suggestion, player.getName(), false, false));
		if(args.length == 1)
			writeFiles();
		sendMessage(player, "Suggestion sent: " + suggestion);
	}

	private void processSh(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		
		if(args.length == 0)
			player.sendMessage(info);
		else{
			String subcmd = args[0];
			if(subcmd.equals("list")){}
			else if(subcmd.equals("view")){}
			else if(subcmd.equals("save")){}
			else if(subcmd.equals("help")){}
			else if(subcmd.equals("close")){}
			else if(subcmd.equals("open")){}
			else if(subcmd.equals("num")){
				sendMessage(player, "There are (" + map.size() + ") total suggestions.");
			}
			else if(subcmd.equals("delete")){processDelete(args, player);}
			else
				processError(player);
		}
	}

	private void processDelete(String[] args, Player player) {
		if(args.length == 2 && args[1].equals("CoNfIrM")){
			initWriter();
			suggestions.delete();
		}
		else
			sendMessage(player, "" + ChatColor.RED + ChatColor.BOLD + "WARNING: " + ChatColor.RESET + "Are you sure you want to delete all files? Type \"/sh delete CoNfIrM\" to confirm.");
	}
	
	private void processError(Player player) {
		sendMessage(player, "Unknown command. Type \"/sh help\" to see a command list.");
	}
	
	private void sendMessage(Player player, String line){
		player.sendMessage(prefix + " " + line);
	}
}
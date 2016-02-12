package me.yourselvs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class MainClass extends JavaPlugin {
	
	
	final String prefix = "[" + ChatColor.GOLD + ChatColor.BOLD + "SH" + ChatColor.RESET + "]";
	final String[] info = {prefix + " SuggestionHelper plugin v1.0", prefix + " Created by " + ChatColor.YELLOW + "yourselvs"};
	
	final String allPath = "suggestions.json";
	
	final int pageSize = 6;
	
	List<Suggestion> map = new ArrayList<Suggestion>();
	
	File dataFolder;
	File suggestions;
	
	Gson gson;
	Writer writer;
	
	@Override
	public void onEnable() {
		map = new ArrayList<Suggestion>();
		gson = new Gson();
		initFiles();
		readFiles();
		
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
	
	@SuppressWarnings({"unchecked" })
	private void readFiles() {
		try {
			Type type = new TypeToken<ArrayList<Suggestion>>(){}.getType();
			Reader isReader = new InputStreamReader(new FileInputStream((suggestions)));
			map = Collections.synchronizedList((ArrayList<Suggestion>)gson.fromJson(isReader, type));
			
			if(map == null){
				getLogger().info("Empty file read in. Creating new one.");
				map = new ArrayList<Suggestion>();
				
			}
			
			for(int i = 0; i < map.size(); i++)
				map.get(i).setID(i + "");
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
		map.add(new Suggestion(((Integer)map.size()).toString(), suggestion, player.getName(), false, false));
		if(args.length == 1)
			writeFiles();
		sendMessage(player, "Suggestion sent: " + suggestion);
	}

	private void processSh(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		
		if(!player.isOp()){
			if(args.length == 0){
				player.sendMessage(info);
				player.sendMessage(prefix + " Type " + ChatColor.YELLOW + "/sh help" + ChatColor.RESET + " to view SuggestionHelper commands.");
			}
			else{
				String subcmd = args[0];
				
				if(subcmd.equals("list"))
					processList(args, player);
				else if(subcmd.equals("listall"))
					processListAll(args, player);
				else if(subcmd.equals("view"))
					processView(args, player);
				else if(subcmd.equals("save"))
					processSave(args, player);
				else if(subcmd.equals("unsave"))
					processUnsave(args, player);
				else if(subcmd.equals("close"))
					processClose(args, player);
				else if(subcmd.equals("open"))
					processOpen(args, player);
				else if(subcmd.equals("help"))
					processHelp(player);
				else if(subcmd.equals("num"))
					processNum(player);
				else if(subcmd.equals("delete"))
					processDelete(args, player);
				else
					processError(player);
			}
		}
		else
			player.sendMessage(info);
	}
	
	private void processHelp(Player player) {
		sendMessage(player, ChatColor.YELLOW + "/sh" + ChatColor.RESET + " Views information about the SuggestionHelper plugin.");
		sendMessage(player, ChatColor.YELLOW + "/suggest <text>" + ChatColor.RESET + " Sends a suggestion to the server.");
		sendMessage(player, ChatColor.YELLOW + "/sh list <page>" + ChatColor.RESET + " Lists open suggestions at a specific page.");
		sendMessage(player, ChatColor.YELLOW + "/sh listall <page>" + ChatColor.RESET + " Lists all suggestions at a specific page.");
		sendMessage(player, ChatColor.YELLOW + "/sh view <ID>" + ChatColor.RESET + " Views a suggestion by ID.");
		sendMessage(player, ChatColor.YELLOW + "/sh save <ID>" + ChatColor.RESET + " Saves a suggestion by ID.");
		sendMessage(player, ChatColor.YELLOW + "/sh unsave <ID>" + ChatColor.RESET + " Unsaves a suggestion by ID.");
		sendMessage(player, ChatColor.YELLOW + "/sh close <ID>" + ChatColor.RESET + " Closes a suggestion by ID.");
		sendMessage(player, ChatColor.YELLOW + "/sh open <ID>" + ChatColor.RESET + " Open a suggestion by ID.");
		sendMessage(player, ChatColor.YELLOW + "/sh num" + ChatColor.RESET + " Gives the number of all open suggestions.");
		sendMessage(player, ChatColor.YELLOW + "/sh delete" + ChatColor.RESET + " Gives the option to erase all files.");
	}
	
	private void processSave(String[] args, Player player) {
		boolean proceed = true;
		int save = -1;
		if(args.length > 1){
			try {
				save = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {proceed = false; e.printStackTrace();}
		}
		if(proceed && save >= 0 && save < map.size()){
			if(map.get(save).isSaved())
				sendMessage(player, "This suggestion is already saved.");
			else{
				map.get(save).save();
				sendMessage(player, "Suggestion " + ChatColor.YELLOW + save + ChatColor.RESET + " saved.");
				writeFiles();
			}
		}
	}
	
	private void processUnsave(String[] args, Player player) {
		boolean proceed = true;
		int save = -1;
		if(args.length > 1){
			try {
				save = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {proceed = false; e.printStackTrace();}
		}
		if(proceed && save >= 0 && save < map.size()){
			if(!map.get(save).isSaved())
				sendMessage(player, "This suggestion already isn't saved.");
			else{
				map.get(save).unsave();
				sendMessage(player, "Suggestion " + ChatColor.YELLOW + save + ChatColor.RESET + " unsaved.");
				writeFiles();
			}
		}
	}
	
	private void processOpen(String[] args, Player player) {
		boolean proceed = true;
		int open = -1;
		if(args.length > 1){
			try {
				open = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {proceed = false; e.printStackTrace();}
		}
		if(proceed && open >= 0 && open < map.size()){
			if(!map.get(open).isClosed())
				sendMessage(player, "This suggestion is already open.");
			else{
				map.get(open).open();
				sendMessage(player, "Suggestion " + ChatColor.YELLOW + open + ChatColor.RESET + " opened.");
				writeFiles();
			}
		}	
	}
	
	private void processClose(String[] args, Player player) {
		boolean proceed = true;
		int close = -1;
		if(args.length > 1){
			try {
				close = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {proceed = false; e.printStackTrace();}
		}
		if(proceed && close >= 0 && close < map.size()){
			if(map.get(close).isClosed())
				sendMessage(player, "This suggestion is already saved.");
			else{
				map.get(close).close();
				sendMessage(player, "Suggestion " + ChatColor.YELLOW + close + ChatColor.RESET + " closed.");
				writeFiles();
			}
		}
	}
	
	private void processList(String[] args, Player player) {
		List<Suggestion> list = getOpen();
		int maxPage = (list.size() / pageSize) + 1;
		int pageNum = 1;
		boolean proceed = true;
		if(args.length > 1){
			try {
				pageNum = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {proceed = false; e.printStackTrace();}
		}
		if(proceed && pageNum > 0 && pageNum <= maxPage){
			sendMessage(player, "Page " + ChatColor.YELLOW + pageNum + ChatColor.RESET + " of " + ChatColor.YELLOW + maxPage);
			for(int i = (pageNum - 1) * pageSize; i < ((pageNum - 1) * pageSize) + 6; i++){
				if(i < list.size()){
					String status;
					
					if(list.get(i).isClosed())
						status = ChatColor.RED + "CLOSED";
					else if(list.get(i).isSaved())
						status = ChatColor.GREEN + "SAVED";
					else
						status = ChatColor.GOLD + "OPEN";
					sendMessage(player, "#" + list.get(i).getID() + " : " + ChatColor.YELLOW + ChatColor.ITALIC + list.get(i).getPlayer() + ChatColor.RESET + " : " + status);
				}
			}
			
			if(pageNum < maxPage)
				sendMessage(player, "Type " + ChatColor.YELLOW + ChatColor.RESET + "/sh list " + (pageNum + 1) + ChatColor.RESET + " to see the next page.");
			
		}
		else
			sendMessage(player, "Error: Invalid page.");
	}
	
	private void processListAll(String[] args, Player player) {
		int maxPage = (map.size() / pageSize) + 1;
		int pageNum = 1;
		boolean proceed = true;
		if(args.length > 1){
			try {
				pageNum = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {proceed = false; e.printStackTrace();}
		}
		if(proceed && pageNum > 0 && pageNum <= maxPage){
			sendMessage(player, "Page " + ChatColor.YELLOW + pageNum + ChatColor.RESET + " of " + ChatColor.YELLOW + maxPage);
			for(int i = (pageNum - 1) * pageSize; i < ((pageNum - 1) * pageSize) + 6; i++){
				if(i < map.size()){
					String status;
					
					if(map.get(i).isClosed())
						status = ChatColor.RED + "CLOSED";
					else if(map.get(i).isSaved())
						status = ChatColor.GREEN + "SAVED";
					else
						status = ChatColor.GOLD + "OPEN";
					sendMessage(player, "#" + map.get(i).getID() + " : " + ChatColor.YELLOW + ChatColor.ITALIC + map.get(i).getPlayer() + ChatColor.RESET + " : " + status);
				}
			}
			
			if(pageNum < maxPage)
				sendMessage(player, "Type " + ChatColor.YELLOW + ChatColor.RESET + "/sh list " + (pageNum + 1) + ChatColor.RESET + " to see the next page.");
			
		}
		else
			sendMessage(player, "Error: Invalid page.");
	}
	
	private void processView(String[] args, Player player) {
		int i = 0;
		boolean proceed = true;
		try {
			i = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {proceed = false; e.printStackTrace();}
		
		if(proceed && args.length > 1 && i >= 0 && i < map.size() && !(map.get(i) == null)){
			String status;
			
			if(map.get(i).isClosed())
				status = ChatColor.RED + "CLOSED";
			else if(map.get(i).isSaved())
				status = ChatColor.GREEN + "SAVED";
			else
				status = ChatColor.GOLD + "OPEN";
			sendMessage(player, "Suggestion #" + map.get(i).getID() + " : " + ChatColor.YELLOW + ChatColor.ITALIC + map.get(i).getPlayer() + ChatColor.RESET + " : " + status + ChatColor.RESET + " : " + map.get(i).getDescription());
		}
		else
			sendMessage(player, "Error: Invalid ID");
		
	}

	private void processNum(Player player) {
		sendMessage(player, "There are " + ChatColor.YELLOW + map.size() + ChatColor.RESET + " total suggestions.");
		sendMessage(player, "There are " + ChatColor.YELLOW + getOpen().size() + ChatColor.RESET + " open suggestions.");
		sendMessage(player, "There are " + ChatColor.YELLOW + getSaved().size() + ChatColor.RESET + " saved suggestions.");
		sendMessage(player, "There are " + ChatColor.YELLOW + getClosed().size() + ChatColor.RESET + " closed suggestions.");
	}

	private void processDelete(String[] args, Player player) {
		if(args.length == 2 && args[1].equals("CoNfIrM")){
			initWriter();
			suggestions.delete();
		}
		else
			sendMessage(player, "" + ChatColor.RED + ChatColor.BOLD + "WARNING: " + ChatColor.RESET + ChatColor.DARK_RED + "Are you sure you want to delete all files? Type \"/sh delete CoNfIrM\" to confirm.");
	}
	
	private List<Suggestion> getOpen(){
		List<Suggestion> open = new ArrayList<Suggestion>();
		for(Suggestion suggestion : map){
			if(!suggestion.isClosed())
				open.add(suggestion);
		}
		return open;
	}
	private List<Suggestion> getClosed(){
		List<Suggestion> closed = new ArrayList<Suggestion>();
		for(Suggestion suggestion : map){
			if(suggestion.isClosed())
				closed.add(suggestion);
		}
		return closed;
	}
	private List<Suggestion> getSaved(){
		List<Suggestion> saved = new ArrayList<Suggestion>();
		for(Suggestion suggestion : map){
			if(suggestion.isSaved())
				saved.add(suggestion);
		}
		return saved;
	}
	
	private void processError(Player player) {
		sendMessage(player, "Unknown command. Type " + ChatColor.YELLOW + "/sh help" + ChatColor.RESET + " to see a command list.");
	}
	
	private void sendMessage(Player player, String line){
		player.sendMessage(prefix + " " + line);
	}
}
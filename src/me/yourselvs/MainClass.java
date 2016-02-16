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
	File allSuggestions;
	
	Writer allWriter;
	
	Gson gson;
	
	
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
		else if((label.equalsIgnoreCase("sh") || label.equalsIgnoreCase("suggestionhelper") || label.equalsIgnoreCase("suggestion")) && sender instanceof Player)
			processSh(sender, args);
		else
			return false;
		return true;
	}

	private void initFiles() {
		dataFolder = getDataFolder();
		if(!dataFolder.exists())
			dataFolder.mkdir();
		
		allSuggestions = new File(getDataFolder(), allPath);
		if(!allSuggestions.exists())
			try {allSuggestions.createNewFile();} catch (IOException e) {getLogger().info(e.getMessage());}
	}
	
	private void initWriter() {		
		try {
			allWriter = new FileWriter(allSuggestions);
		} catch (IOException e) {getLogger().info(e.getMessage());}
	}
	
	@SuppressWarnings({"unchecked" })
	private void readFiles() {
		try {
			Type type = new TypeToken<ArrayList<Suggestion>>(){}.getType();
			Reader isReader = new InputStreamReader(new FileInputStream((allSuggestions)));
			getLogger().info("Reading file.");
			map = Collections.synchronizedList((ArrayList<Suggestion>)gson.fromJson(isReader, type));
			
			if(map == null){
				getLogger().info("Empty file read in. Creating new one.");
				map = new ArrayList<Suggestion>();
				writeFiles();
				readFiles();
			}
			
			for(int i = 0; i < map.size(); i++)
				map.get(i).setID(i + "");
		} catch (JsonSyntaxException | JsonIOException | IOException e) {getLogger().info(e.getMessage());}
	}
	
	private void writeFiles(){
		initWriter();
		try {
			allWriter.write(gson.toJson(map));
		} catch (IOException e) {getLogger().info(e.getMessage());}
		closeFiles();
	}
	
	private void closeFiles() {
		try {
			allWriter.close();
		} catch (IOException e) {getLogger().info(e.getMessage());}
	}

	private void processSuggest(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		if(args.length > 0){
			String suggestion = "";
			for(String word : args)
				suggestion = suggestion + word + " ";
			map.add(new Suggestion(map.size() + "", suggestion, player.getName(), false, false));
			map.get(map.size() - 1).setID(map.size() - 1 + "");;
			if(args.length == 1)
				writeFiles();
			sendMessage(player, "Suggestion sent: " + suggestion);
		}
		else
			sendMessage(player, "You must include a suggestion.");
	}

	private void processSh(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		
		if(player.isOp()){
			if(args.length == 0){
				player.sendMessage(info);
				player.sendMessage(prefix + " Type " + ChatColor.YELLOW + "/sh help" + ChatColor.RESET + " to view SuggestionHelper commands.");
			}
			else{
				String subcmd = args[0];
				
				if(subcmd.equalsIgnoreCase("list"))
					processList(args, player);
				else if(subcmd.equalsIgnoreCase("listall"))
					processListAll(args, player);
				else if(subcmd.equalsIgnoreCase("listsaved"))
					processListSaved(args, player);
				else if(subcmd.equalsIgnoreCase("listopen"))
					processListOpen(args, player);
				else if(subcmd.equalsIgnoreCase("listclosed"))
					processListClosed(args, player);
				else if(subcmd.equalsIgnoreCase("view"))
					processView(args, player);
				else if(subcmd.equalsIgnoreCase("save"))
					processSave(args, player);
				else if(subcmd.equalsIgnoreCase("unsave"))
					processUnsave(args, player);
				else if(subcmd.equalsIgnoreCase("close"))
					processClose(args, player);
				else if(subcmd.equalsIgnoreCase("open"))
					processOpen(args, player);
				else if(subcmd.equalsIgnoreCase("help"))
					processHelp(player);
				else if(subcmd.equalsIgnoreCase("num"))
					processNum(player);
				else if(subcmd.equalsIgnoreCase("delete"))
					processDelete(args, player);
				else
					processError(player);
			}
		}
		else
			player.sendMessage(info);
	}
	
	private void processHelp(Player player) {
		sendMessage(player, ChatColor.YELLOW + "/sh" + ChatColor.RESET + " Views information about the SuggestionHelper plugin");
		sendMessage(player, ChatColor.YELLOW + "/suggest <text>" + ChatColor.RESET + " Sends a suggestion to the server");
		sendMessage(player, ChatColor.YELLOW + "/sh list <page>" + ChatColor.RESET + " Lists unclosed suggestions");
		sendMessage(player, ChatColor.YELLOW + "/sh listall <page>" + ChatColor.RESET + " Lists all suggestion.");
		sendMessage(player, ChatColor.YELLOW + "/sh listsaved <page>" + ChatColor.RESET + " Lists saved suggestions");
		sendMessage(player, ChatColor.YELLOW + "/sh listclosed <page>" + ChatColor.RESET + " Lists closed suggestions");
		sendMessage(player, ChatColor.YELLOW + "/sh listopen <page>" + ChatColor.RESET + " Lists open and unsaved suggestions");
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
		if(list.size() == 0)
			sendMessage(player, "There are no " + ChatColor.GOLD + "OPEN" + ChatColor.RESET + " or " + ChatColor.GREEN + "SAVED" + ChatColor.RESET + " suggestions.");
		else{
			int maxPage = (list.size() / pageSize) + 1;
			int pageNum = 1;
			boolean proceed = true;
			if(args.length > 1){
				try {
					pageNum = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {proceed = false; e.printStackTrace();}
			}
			if(proceed && pageNum > 0 && pageNum <= maxPage){
				sendMessage(player, ChatColor.GOLD + "OPEN" + ChatColor.RESET + " and " + ChatColor.GREEN + "SAVED " + ChatColor.RESET + "suggestions. Page " + ChatColor.YELLOW + pageNum + ChatColor.RESET + " of " + ChatColor.YELLOW + maxPage);
				for(int i = (pageNum - 1) * pageSize; i < ((pageNum - 1) * pageSize) + 6; i++){
					if(i < list.size()){
						String status;
						
						if(list.get(i).isSaved())
							status = ChatColor.GREEN + "SAVED";
						else
							status = ChatColor.GOLD + "OPEN";
						sendMessage(player, "#" + list.get(i).getID() + " : " + ChatColor.YELLOW + ChatColor.ITALIC + list.get(i).getPlayer() + ChatColor.RESET + " : " + status);
					}
				}
				sendMessage(player, "Type " + ChatColor.YELLOW + ChatColor.RESET + "/sh list <page>" + ChatColor.RESET + " to see another page.");
				
			}
			else
				sendMessage(player, "Error: Invalid page.");
		}
	}
	
	private void processListSaved(String[] args, Player player) {
		List<Suggestion> list = getSaved();
		if(list.size() == 0)
			sendMessage(player, "There are no " + ChatColor.GREEN + "SAVED" + ChatColor.RESET + " suggestions.");
		else{
			int maxPage = (list.size() / pageSize) + 1;
			int pageNum = 1;
			boolean proceed = true;
			if(args.length > 1){
				try {
					pageNum = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {proceed = false; e.printStackTrace();}
			}
			if(proceed && pageNum > 0 && pageNum <= maxPage){
				sendMessage(player, ChatColor.GREEN + "SAVED " + ChatColor.RESET + "suggestions. Page " + ChatColor.YELLOW + pageNum + ChatColor.RESET + " of " + ChatColor.YELLOW + maxPage);
				for(int i = (pageNum - 1) * pageSize; i < ((pageNum - 1) * pageSize) + 6; i++)
					if(i < list.size())
						sendMessage(player, "#" + list.get(i).getID() + " : " + ChatColor.YELLOW + ChatColor.ITALIC + list.get(i).getPlayer());
				sendMessage(player, "Type " + ChatColor.YELLOW + "/sh list <page>" + ChatColor.RESET + " to see another page.");
			}
			else
				sendMessage(player, "Error: Invalid page.");
		}
	}
	
	private void processListAll(String[] args, Player player) {
		if(map.size() == 0)
			sendMessage(player, "There are no suggestions.");
		else {
			int maxPage = (map.size() / pageSize) + 1;
			int pageNum = 1;
			boolean proceed = true;
			if(args.length > 1){
				try {
					pageNum = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {proceed = false; e.printStackTrace();}
			}
			if(proceed && pageNum > 0 && pageNum <= maxPage){
				sendMessage(player, ChatColor.AQUA + "ALL " + ChatColor.RESET + "suggestions. Page " + ChatColor.YELLOW + pageNum + ChatColor.RESET + " of " + ChatColor.YELLOW + maxPage);
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
				sendMessage(player, "Type " + ChatColor.YELLOW + "/sh list <page>" + ChatColor.RESET + " to see another page.");
				
			}
			else
				sendMessage(player, "Error: Invalid page.");
		}
	}
	
	private void processListOpen(String[] args, Player player) {
		List<Suggestion> list = getOpenOnly();
		if(list.size() == 0)
			sendMessage(player, "There are no " + ChatColor.GOLD + "OPEN" + ChatColor.RESET + " suggestions.");
		else {
			int maxPage = (list.size() / pageSize) + 1;
			int pageNum = 1;
			boolean proceed = true;
			if(args.length > 1){
				try {
					pageNum = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {proceed = false; e.printStackTrace();}
			}
			if(proceed && pageNum > 0 && pageNum <= maxPage){
				sendMessage(player, ChatColor.GOLD + "OPEN " + ChatColor.RESET + "suggestions. Page " + ChatColor.YELLOW + pageNum + ChatColor.RESET + " of " + ChatColor.YELLOW + maxPage);
				for(int i = (pageNum - 1) * pageSize; i < ((pageNum - 1) * pageSize) + 6; i++)
					if(i < list.size())
						sendMessage(player, "#" + list.get(i).getID() + " : " + ChatColor.YELLOW + ChatColor.ITALIC + list.get(i).getPlayer());
				sendMessage(player, "Type " + ChatColor.YELLOW + "/sh list <page>" + ChatColor.RESET + " to see another page.");
			}
			else
				sendMessage(player, "Error: Invalid page.");
		}
	}
	
	private void processListClosed(String[] args, Player player) {
		List<Suggestion> list = getClosed();
		if(list.size() == 0)
			sendMessage(player, "There are no " + ChatColor.RED + "CLOSED" + ChatColor.RESET + " suggestions");
		else {
			int maxPage = (list.size() / pageSize) + 1;
			int pageNum = 1;
			boolean proceed = true;
			if(args.length > 1){
				try {
					pageNum = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {proceed = false; e.printStackTrace();}
			}
			if(proceed && pageNum > 0 && pageNum <= maxPage){
				sendMessage(player, ChatColor.RED + "CLOSED " + ChatColor.RESET + "suggestions. Page " + ChatColor.YELLOW + pageNum + ChatColor.RESET + " of " + ChatColor.YELLOW + maxPage);
				for(int i = (pageNum - 1) * pageSize; i < ((pageNum - 1) * pageSize) + 6; i++)
					if(i < list.size())
						sendMessage(player, "#" + list.get(i).getID() + " : " + ChatColor.YELLOW + ChatColor.ITALIC + list.get(i).getPlayer());
				sendMessage(player, "Type " + ChatColor.YELLOW + "/sh list <page>" + ChatColor.RESET + " to see another page.");
				
			}
			else
				sendMessage(player, "Error: Invalid page.");
		}
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
			sendMessage(player, "#" + map.get(i).getID() + " : " + ChatColor.YELLOW + ChatColor.ITALIC + map.get(i).getPlayer() + ChatColor.RESET + " : " + status + ChatColor.RESET + " : " + map.get(i).getDescription());
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
		if(args.length > 1 && args[1].equalsIgnoreCase("CoNfIrM")){
			initWriter();
			allSuggestions.delete();
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
	private List<Suggestion> getOpenOnly(){
		List<Suggestion> open = new ArrayList<Suggestion>();
		for(Suggestion suggestion : map){
			if(!suggestion.isClosed() && !suggestion.isSaved())
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
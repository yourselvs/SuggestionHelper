package me.yourselvs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MainClass extends JavaPlugin {
	
	
	final String prefix = "[" + ChatColor.GOLD + ChatColor.BOLD + "SH" + ChatColor.RESET + "]";
	final String[] info = {prefix + " SuggestionHelper plugin v1.0", prefix + " Created by " + ChatColor.YELLOW + "yourselvs"};
	final int pageSize = 6;
	
	final String textUri = "mongodb://<username>:<password>@ds056288.mongolab.com:56288/minecraft";

	MongoDBStorage mongoStorage;
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	final String suggestionType = "suggestion";
	final String savedStatus = "saved";
	final String openStatus = "open";
	final String closedStatus = "closed";
	
	public void setStatus(int id, String status) {
		// Sets the status of a suggestion based on id
		//mongoStorage.updateDocument("{type:\"suggestion\", id:\"" + id + "\"}", "{$set: {status:\"" + status + "\"} }");
		mongoStorage.updateDocument(new Document("_id",id), new Document("$set",new Document("status", status)));
	}

	public String getStatus(int id) {
		// Gets the status of a suggestion based on id
		return mongoStorage.findDocument(new Document("type", "suggestion").append("_id", id)).getString("status");
	}
	
	public int getHighestNum() {
		// Gets the highestNum count
		return mongoStorage.findDocument(new Document("type","counter")).getInteger("highCount");
	}
	
	public String getAuthor(int num) {
		// Gets the of a suggestion by id
		return mongoStorage.findDocument(new Document("type", "suggestion").append("_id", num)).getString("author");
	}
	
	public String getSuggestion(int num) {
		// Gets the description of a suggestion by id
		return mongoStorage.findDocument(new Document("type", "suggestion").append("_id", num)).getString("description");
	}
	
	public List<Document> getOpenAndSaved() {
		// Gets both open and saved suggestions in a list
		List<Document> list = mongoStorage.findDocuments(new Document("status", "open"));
		list.addAll(mongoStorage.findDocuments(new Document("status", "saved")));
		return list;
	}
	
	public List<Document> getOpen() {
		// Gets open suggestions in a list
		return mongoStorage.findDocuments(new Document("status", "open"));
	}
	public List<Document> getClosed(){
		// Gets closed suggestions in a list
		return mongoStorage.findDocuments(new Document("status", "closed"));
	}
	public List<Document> getSaved(){
		// Gets saved suggestions in a list
		return mongoStorage.findDocuments(new Document("status", "saved"));
	}
	
	public int updateSuggestions(){
		mongoStorage.updateDocument("{type:\"counter\"}", "{$inc: {highCount:1} }");
		
		Document counters = mongoStorage.findDocument(new Document("type","counter"));
		return counters.getInteger("highCount");
	}
	
	public long deleteSuggestions(){
		return mongoStorage.deleteDocuments(new Document("type","suggestion"));
	}
	
	public long deleteFiles(){
		return mongoStorage.deleteDocuments(new Document());
	}
	
	public void addSuggestion(String player, String description) {
		// Adds a suggestion based off of several variables
		Document suggestion = new Document("type", "suggestion")
				.append("description", description)
				.append("author", player)
				.append("status", "open")
				.append("time", sdf.format(new Date()))
				.append("_id", getHighestNum());
		
		mongoStorage.insertDocument(suggestion);
		
		updateSuggestions();
	}
	
	public void addSuggestion(Document suggestion) {
		mongoStorage.insertDocument(suggestion);
	}
	
	@Override
	public void onEnable() {
		initDB();
		
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

	public void initDB() {
		mongoStorage = new MongoDBStorage(textUri,"minecraft","suggestions");
	}

	public void processSuggest(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		if(args.length > 0){
			String suggestion = "";
			for(String word : args)
				suggestion = suggestion + word + " ";
			addSuggestion(player.getName(), suggestion);
			sendMessage(player, "Suggestion sent: " + suggestion);
		}
		else
			sendMessage(player, "You must include a suggestion. Try \"/suggest <Text>\"");
	}

	public void processSh(CommandSender sender, String[] args) {
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
				else if(subcmd.equalsIgnoreCase("close"))
					processClose(args, player);
				else if(subcmd.equalsIgnoreCase("open"))
					processOpen(args, player);
				else if(subcmd.equalsIgnoreCase("help"))
					processHelp(player);
				else if(subcmd.equalsIgnoreCase("num"))
					processNum(player);
				else
					processError(player);
			}
		}
		else
			player.sendMessage(info);
	}
	
	public void processHelp(Player player) {
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
	
	public void processSave(String[] args, Player player) {
		boolean proceed = true;
		int save = -1;
		if(args.length > 1){
			try {
				save = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {proceed = false; e.printStackTrace();}
		}
		if(proceed && save >= 0 && save < getHighestNum()){
			if(getStatus(save).equals(savedStatus))
				sendMessage(player, "This suggestion is already saved.");
			else{
				setStatus(save, savedStatus);
				sendMessage(player, "Suggestion " + ChatColor.YELLOW + save + ChatColor.RESET + " saved.");
			}
		}
	}
	
	
	
	public void processOpen(String[] args, Player player) {
		boolean proceed = true;
		int open = -1;
		if(args.length > 1){
			try {
				open = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {proceed = false; e.printStackTrace();}
		}
		if(proceed && open >= 0 && open < getHighestNum()){
			if(!getStatus(open).equals(openStatus))
				sendMessage(player, "This suggestion is already open.");
			else{
				setStatus(open, openStatus);
				sendMessage(player, "Suggestion " + ChatColor.YELLOW + open + ChatColor.RESET + " opened.");
			}
		}	
	}
	
	public void processClose(String[] args, Player player) {
		boolean proceed = true;
		int close = -1;
		if(args.length > 1){
			try {
				close = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {proceed = false; e.printStackTrace();}
		}
		if(proceed && close >= 0 && close < getHighestNum()){
			if(getStatus(close).equals(closedStatus))
				sendMessage(player, "This suggestion is already saved.");
			else{
				setStatus(close, closedStatus);
				sendMessage(player, "Suggestion " + ChatColor.YELLOW + close + ChatColor.RESET + " closed.");
			}
		}
	}
	
	public void processList(String[] args, Player player) {
		List<Document> list = getOpenAndSaved();
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
						
						if(list.get(i).getString("status").equals(savedStatus))
							status = ChatColor.GREEN + "SAVED";
						else
							status = ChatColor.GOLD + "OPEN";
						sendMessage(player, "#" + list.get(i).getInteger("_id") + " : " + ChatColor.YELLOW + ChatColor.ITALIC + list.get(i).getString("author") + ChatColor.RESET + " : " + status);
					}
				}
				sendMessage(player, "Type " + ChatColor.YELLOW + ChatColor.RESET + "/sh list <page>" + ChatColor.RESET + " to see another page.");
				
			}
			else
				sendMessage(player, "Error: Invalid page.");
		}
	}
	
	public void processListSaved(String[] args, Player player) {
		List<Document> list = getSaved();
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
						sendMessage(player, "#" + list.get(i).getInteger("_id") + " : " + ChatColor.YELLOW + ChatColor.ITALIC + list.get(i).getString("author"));
				sendMessage(player, "Type " + ChatColor.YELLOW + "/sh list <page>" + ChatColor.RESET + " to see another page.");
			}
			else
				sendMessage(player, "Error: Invalid page.");
		}
	}
	
	public void processListAll(String[] args, Player player) {
		if(getHighestNum() == 0)
			sendMessage(player, "There are no suggestions.");
		else {
			int maxPage = (getHighestNum() / pageSize) + 1;
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
					if(i < getHighestNum()){
						String status;
						
						if(getStatus(i).equals(closedStatus))
							status = ChatColor.RED + "CLOSED";
						else if(getStatus(i).equals(savedStatus))
							status = ChatColor.GREEN + "SAVED";
						else
							status = ChatColor.GOLD + "OPEN";
						sendMessage(player, "#" + i + " : " + ChatColor.YELLOW + ChatColor.ITALIC + getAuthor(i) + ChatColor.RESET + " : " + status);
					}
				}
				sendMessage(player, "Type " + ChatColor.YELLOW + "/sh list <page>" + ChatColor.RESET + " to see another page.");
				
			}
			else
				sendMessage(player, "Error: Invalid page.");
		}
	}
	
	public void processListOpen(String[] args, Player player) {
		List<Document> list = getOpen();
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
						sendMessage(player, "#" + list.get(i).getInteger("_id") + " : " + ChatColor.YELLOW + ChatColor.ITALIC + list.get(i).getString("author"));
				sendMessage(player, "Type " + ChatColor.YELLOW + "/sh list <page>" + ChatColor.RESET + " to see another page.");
			}
			else
				sendMessage(player, "Error: Invalid page.");
		}
	}
	
	public void processListClosed(String[] args, Player player) {
		List<Document> list = getClosed();
		
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
						sendMessage(player, "#" + list.get(i).getInteger("_id") + " : " + ChatColor.YELLOW + ChatColor.ITALIC + list.get(i).getString("author"));
				sendMessage(player, "Type " + ChatColor.YELLOW + "/sh list <page>" + ChatColor.RESET + " to see another page.");
				
			}
			else
				sendMessage(player, "Error: Invalid page.");
		}
	}
	
	public void processView(String[] args, Player player) {
		int i = 0;
		boolean proceed = true;
		try {
			i = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {proceed = false; e.printStackTrace();}
		
		if(proceed && args.length > 1 && i >= 0 && i < getHighestNum()){
			String status;
			
			if(getStatus(i).equals(closedStatus))
				status = ChatColor.RED + "CLOSED";
			else if(getStatus(i).equals(savedStatus))
				status = ChatColor.GREEN + "SAVED";
			else
				status = ChatColor.GOLD + "OPEN";
			sendMessage(player, "#" + i + " : " + ChatColor.YELLOW + ChatColor.ITALIC + getAuthor(i) + ChatColor.RESET + " : " + status + ChatColor.RESET + " : " + getSuggestion(i));
		}
		else
			sendMessage(player, "Error: Invalid ID");
		
	}

	public void processNum(Player player) {
		sendMessage(player, "There are " + ChatColor.YELLOW + getHighestNum() + ChatColor.RESET + " total suggestions.");
		sendMessage(player, "There are " + ChatColor.YELLOW + getOpen().size() + ChatColor.RESET + " open suggestions.");
		sendMessage(player, "There are " + ChatColor.YELLOW + getSaved().size() + ChatColor.RESET + " saved suggestions.");
		sendMessage(player, "There are " + ChatColor.YELLOW + getClosed().size() + ChatColor.RESET + " closed suggestions.");
	}
	
	public void processError(Player player) {
		sendMessage(player, "Unknown command. Type " + ChatColor.YELLOW + "/sh help" + ChatColor.RESET + " to see a command list.");
	}
	
	public void sendMessage(Player player, String line){
		player.sendMessage(prefix + " " + line);
	}
}

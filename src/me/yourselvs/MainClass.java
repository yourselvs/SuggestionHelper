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

import com.mythicacraft.voteroulette.utils.InteractiveMessageAPI.FormattedText;
import com.mythicacraft.voteroulette.utils.InteractiveMessageAPI.InteractiveMessage;
import com.mythicacraft.voteroulette.utils.InteractiveMessageAPI.InteractiveMessageElement;
import com.mythicacraft.voteroulette.utils.InteractiveMessageAPI.InteractiveMessageElement.ClickEvent;
import com.mythicacraft.voteroulette.utils.InteractiveMessageAPI.InteractiveMessageElement.HoverEvent;

public class MainClass extends JavaPlugin {
	
	
	final String prefix = "[" + ChatColor.GOLD + ChatColor.BOLD + "SH" + ChatColor.RESET + "]";
	final String linkPrefix = ChatColor.AQUA + "[" + ChatColor.GOLD + ChatColor.BOLD + "SH" + ChatColor.RESET + ChatColor.AQUA + "]" + ChatColor.RESET;
	final String[] info = {prefix + " SuggestionHelper plugin v1.2", prefix + " Created by " + ChatColor.YELLOW + "yourselvs"};
	final int pageSize = 6;
	
	String textUri;

	MongoDBStorage mongoStorage;
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	final String suggestionType = "suggestion";
	final String savedStatus = "saved";
	final String openStatus = "open";
	final String closedStatus = "closed";
	final String nullCloseReason = "<NULL>";
	
	public void setOpen(int id, Player player) {
		mongoStorage.updateDocument(new Document("_id",id), new Document("$set",new Document("status", openStatus)));
		mongoStorage.updateDocument(new Document("_id",id), new Document("$set",new Document("closeReason", nullCloseReason)));
		mongoStorage.updateDocument(new Document("_id",id), new Document("$set",new Document("statusUpdater", player.getName())));
	}
	
	public void setSaved(int id, Player player) {
		mongoStorage.updateDocument(new Document("_id",id), new Document("$set",new Document("status", savedStatus)));
		mongoStorage.updateDocument(new Document("_id",id), new Document("$set",new Document("closeReason", nullCloseReason)));
		mongoStorage.updateDocument(new Document("_id",id), new Document("$set",new Document("statusUpdater", player.getName())));
	}
	
	public void setClosed(int id, String reason, Player player) {
		mongoStorage.updateDocument(new Document("_id",id), new Document("$set",new Document("status", closedStatus)));
		mongoStorage.updateDocument(new Document("_id",id), new Document("$set",new Document("closeReason", reason)));
		mongoStorage.updateDocument(new Document("_id",id), new Document("$set",new Document("statusUpdater", player.getName())));
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
	
	public String getClosedReason(int num) {
		return mongoStorage.findDocument(new Document("type", "suggestion").append("_id", num)).getString("closeReason");
	}
	
	public String getStatusUpdater(int num) {
		return mongoStorage.findDocument(new Document("type", "suggestion").append("_id", num)).getString("statusUpdater");
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
				.append("_id", getHighestNum())
				.append("closeReason", nullCloseReason)
				.append("statusUpdater", player);
		
		mongoStorage.insertDocument(suggestion);
		
		updateSuggestions();
	}
	
	public void addSuggestion(Document suggestion) {
		mongoStorage.insertDocument(suggestion);
	}
	
	@Override
	public void onEnable() {
		String username = (String) getConfig().get("dbUser");
		String password = (String) getConfig().get("dbPass");
		textUri = "mongodb://" + username + ":" + password + "@ds056288.mongolab.com:56288/minecraft";
		mongoStorage = new MongoDBStorage(textUri,"minecraft","suggestions");
		
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
		sendMessage(player, ChatColor.YELLOW + "/sh list" + ChatColor.RESET + " Lists unclosed suggestions");
		sendMessage(player, ChatColor.YELLOW + "/sh listall" + ChatColor.RESET + " Lists all suggestion.");
		sendMessage(player, ChatColor.YELLOW + "/sh listsaved" + ChatColor.RESET + " Lists saved suggestions");
		sendMessage(player, ChatColor.YELLOW + "/sh listclosed" + ChatColor.RESET + " Lists closed suggestions");
		sendMessage(player, ChatColor.YELLOW + "/sh listopen" + ChatColor.RESET + " Lists open and unsaved suggestions");
		sendMessage(player, ChatColor.YELLOW + "/sh view <ID>" + ChatColor.RESET + " Views a suggestion by ID.");
		sendMessage(player, ChatColor.YELLOW + "/sh num" + ChatColor.RESET + " Gives the number of all open suggestions.");
		player.sendMessage(ChatColor.GRAY + "(Click one)");
		
	}
	
	public void processSave(String[] args, Player player) {
		boolean proceed = true;
		int docId = -1;
		if(args.length > 1){
			try {
				docId = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {proceed = false; e.printStackTrace();}
		}
		if(proceed && docId >= 0 && docId < getHighestNum()){
			if(getStatus(docId).equals(savedStatus))
				sendMessage(player, "This suggestion is already saved.");
			else{
				setSaved(docId, player);
				sendMessage(player, "Suggestion " + ChatColor.YELLOW + docId + ChatColor.RESET + " saved.");
			}
		}
	}
	
	
	
	public void processOpen(String[] args, Player player) {
		boolean proceed = true;
		int docId = -1;
		if(args.length > 1){
			try {
				docId = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {proceed = false; e.printStackTrace();}
		}
		if(proceed && docId >= 0 && docId < getHighestNum()){
			if(getStatus(docId).equals(openStatus))
				sendMessage(player, "This suggestion is already open.");
			else{
				setOpen(docId, player);
				sendMessage(player, "Suggestion " + ChatColor.YELLOW + docId + ChatColor.RESET + " opened.");
			}
		}	
	}
	
	public void processClose(String[] args, Player player) {
		boolean proceed = true;
		int docId = -1;
		if(args.length > 2){
			try {
				docId = Integer.parseInt(args[1]);
				
			} catch (NumberFormatException e) {proceed = false; e.printStackTrace();}
			
		}
		else
			proceed = false;
		if(proceed && docId >= 0 && docId < getHighestNum()){
			if(getStatus(docId).equals(closedStatus))
				sendMessage(player, "This suggestion is already closed.");
			else{
				String reason = "";
				for(int i = 2; i < args.length; i++)
					reason = reason + args[i] + " ";
				setClosed(docId, reason, player);
				sendMessage(player, "Suggestion " + ChatColor.YELLOW + docId + ChatColor.RESET + " closed.");
			}
		}
		else
			sendMessage(player, "Correct format is \"/sh close <ID> <reason>\"");
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
						sendClickMessage(player, "#" + list.get(i).getInteger("_id") + " | " + ChatColor.YELLOW + ChatColor.ITALIC + list.get(i).getString("author") + ChatColor.RESET + " | " + status, "Click to view suggestion #" + list.get(i).getInteger("_id"), "/sh view " + list.get(i).getInteger("_id"));
					}
				}
				if(pageNum > 1)
					sendClickMessage(player, ChatColor.YELLOW + "Previous page", "Click to view previous page", "/sh list " + (pageNum - 1));
				if(pageNum < maxPage)
					sendClickMessage(player, ChatColor.YELLOW + "Next page", "Click to view next page", "/sh list " + (pageNum + 1));
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
						sendClickMessage(player, "#" + list.get(i).getInteger("_id") + " | " + ChatColor.YELLOW + ChatColor.ITALIC + list.get(i).getString("author") + ChatColor.RESET, "Click to view suggestion #" + list.get(i).getInteger("_id"), "/sh view " + list.get(i).getInteger("_id"));
				if(pageNum > 1)
					sendClickMessage(player, ChatColor.YELLOW + "Previous page", "Click to view previous page", "/sh listsaved " + (pageNum - 1));
				if(pageNum < maxPage)
					sendClickMessage(player, ChatColor.YELLOW + "Next page", "Click to view next page", "/sh listsaved " + (pageNum + 1));
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
						sendClickMessage(player, "#" + i + " | " + ChatColor.YELLOW + ChatColor.ITALIC + getAuthor(i) + ChatColor.RESET + " | " + status, "Click to view suggestion #" + i, "/sh view " + i);
					}
				}
				if(pageNum > 1)
					sendClickMessage(player, ChatColor.YELLOW + "Previous page", "Click to view previous page", "/sh listall " + (pageNum - 1));
				if(pageNum < maxPage)
					sendClickMessage(player, ChatColor.YELLOW + "Next page", "Click to view next page", "/sh listall " + (pageNum + 1));
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
						sendClickMessage(player, "#" + list.get(i).getInteger("_id") + " | " + ChatColor.YELLOW + ChatColor.ITALIC + list.get(i).getString("author") + ChatColor.RESET , "Click to view suggestion #" + list.get(i).getInteger("_id"), "/sh view " + list.get(i).getInteger("_id"));
				
				if(pageNum > 1)
					sendClickMessage(player, ChatColor.YELLOW + "Previous page", "Click to view previous page", "/sh listopen " + (pageNum - 1));
				if(pageNum < maxPage)
					sendClickMessage(player, ChatColor.YELLOW + "Next page", "Click to view next page", "/sh listopen " + (pageNum + 1));
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
						sendClickMessage(player, "#" + list.get(i).getInteger("_id") + " | " + ChatColor.YELLOW + ChatColor.ITALIC + list.get(i).getString("author") + ChatColor.RESET , "Click to view suggestion" + "#" + list.get(i).getInteger("_id"), "/sh view " + list.get(i).getInteger("_id"));

				if(pageNum > 1)
					sendClickMessage(player, ChatColor.YELLOW + "Previous page", "Click to view previous page", "/sh listclosed " + (pageNum - 1));
				if(pageNum < maxPage)
					sendClickMessage(player, ChatColor.YELLOW + "Next page", "Click to view next page", "/sh listclosed " + (pageNum + 1));
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
				status = "" + ChatColor.RED + ChatColor.BOLD + "CLOSED" + ChatColor.RESET + " | OPEN | SAVED";
			else if(getStatus(i).equals(savedStatus))
				status = "CLOSED | OPEN | " + ChatColor.GREEN + ChatColor.BOLD + "SAVED";
			else
				status = "CLOSED | " + ChatColor.GOLD + ChatColor.BOLD + "OPEN" + ChatColor.RESET + " | SAVED";
			sendMessage(player, "Suggestion #" + i + " by: " + ChatColor.YELLOW + ChatColor.ITALIC + getAuthor(i));
			
			sendMessage(player, "Status: " + status);
			if(getStatus(i).equals(closedStatus)){
				sendMessage(player, "Reason for closing: " + getClosedReason(i));
				sendClickMessage(player, "Click to " + ChatColor.GOLD + "OPEN", "Click to open suggestion #" + i, "/sh open " + i);
				sendClickMessage(player, "Click to " + ChatColor.GREEN + "SAVE", "Click to save suggestion #" + i, "/sh save " + i);
			}
			if(getStatus(i).equals(openStatus)){
				
				sendSuggestMessage(player, "Click to " + ChatColor.RED + "CLOSE", "Click to close suggestion #" + i, "/sh close " + i + " ");
				sendClickMessage(player, "Click to " + ChatColor.GREEN + "SAVE", "Click to save suggestion #" + i, "/sh save " + i);
			}
			if(getStatus(i).equals(savedStatus)){
				sendSuggestMessage(player, "Click to " + ChatColor.RED + "CLOSE", "Click to close suggestion #" + i, "/sh close " + i + " ");
				sendClickMessage(player, "Click to " + ChatColor.GOLD + "OPEN", "Click to open suggestion #" + i, "/sh open " + i);
			}
			sendMessage(player, "Last updated by: " + ChatColor.YELLOW + getStatusUpdater(i));
			sendMessage(player, "Suggestion: " + getSuggestion(i));
		}
		else
			sendMessage(player, "Error: Invalid ID");
		
	}

	public void processNum(Player player) {
		sendClickMessage(player, "There are " + ChatColor.YELLOW + getHighestNum() + ChatColor.RESET + " total suggestions.", "Click to view all suggestions", "/sh listall");
		sendClickMessage(player, "There are " + ChatColor.YELLOW + getOpen().size() + ChatColor.RESET + " open suggestions.", "Click to view open suggestions", "/sh listopen");
		sendClickMessage(player, "There are " + ChatColor.YELLOW + getSaved().size() + ChatColor.RESET + " saved suggestions.", "Click to view saved suggestions", "/sh listsaved");
		sendClickMessage(player, "There are " + ChatColor.YELLOW + getClosed().size() + ChatColor.RESET + " closed suggestions.", "Click to view closed suggestions", "/sh listclosed");
	}
	
	public void processError(Player player) {
		sendClickMessage(player, "Unknown command. Click here to see a command list.", "/sh help");
	}
	
	public void sendMessage(Player player, String line){
		player.sendMessage(prefix + " " + line);
	}
	
	public void sendClickMessage(Player player, String line, String hoverMessage, String command){
		InteractiveMessage message = new InteractiveMessage(new InteractiveMessageElement(new FormattedText(linkPrefix + " " + line), HoverEvent.SHOW_TEXT, new FormattedText(hoverMessage), ClickEvent.RUN_COMMAND, command));	
		message.sendTo(player);
	}
	
	public void sendClickMessage(Player player, String line, String command){
		InteractiveMessage message = new InteractiveMessage(new InteractiveMessageElement(new FormattedText(linkPrefix + " " + line), HoverEvent.NONE, new FormattedText(""), ClickEvent.RUN_COMMAND, command));	
		message.sendTo(player);
	}
	
	public void sendSuggestMessage(Player player, String line, String hoverMessage, String command){
		InteractiveMessage message = new InteractiveMessage(new InteractiveMessageElement(new FormattedText(linkPrefix + " " + line), HoverEvent.SHOW_TEXT, new FormattedText(hoverMessage), ClickEvent.SUGGEST_COMMAND, command));	
		message.sendTo(player);
	}

	public void sendSuggestMessage(Player player, String line, String command){
		InteractiveMessage message = new InteractiveMessage(new InteractiveMessageElement(new FormattedText(linkPrefix + " " + line), HoverEvent.NONE, new FormattedText(""), ClickEvent.SUGGEST_COMMAND, command));
		message.sendTo(player);
	}
}

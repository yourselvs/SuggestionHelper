package resources;

import org.bukkit.entity.Player;

public class Suggestion{
	String description;
	Player player;
	
	public Suggestion(String description, Player player){this.description = description; this.player = player;}
	
	public String getDescription(){return description;}
	public Player getPlayer(){return player;}
}
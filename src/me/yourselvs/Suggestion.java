package me.yourselvs;

public class Suggestion{
	private String description;
	private String player;
	private boolean isClosed;
	private boolean isSaved;
	
	public Suggestion(String description, String player, boolean isClosed, boolean isSaved)
						{this.description = description; this.player = player; 
						this.isClosed = isClosed; this.isSaved = isSaved;}
	
	public String getDescription(){return description;}
	public String getPlayer(){return player;}
	
	public boolean isClosed(){return isClosed;}
	public boolean isSaved(){return isSaved;}
	
	public void close(){isClosed = true;}
}
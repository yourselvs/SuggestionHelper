package me.yourselvs;

public class Suggestion{
	private String id;
	private String description;
	private String player;
	private boolean isClosed;
	private boolean isSaved;
	
	
	public Suggestion(String ID, String description, String player, boolean isClosed, boolean isSaved)
						{this.description = description; this.player = player; 
						this.isClosed = isClosed; this.isSaved = isSaved;}
	
	public String getID(){return id;}
	public String getDescription(){return description;}
	public String getPlayer(){return player;}
	
	public boolean isClosed(){return isClosed;}
	public boolean isSaved(){return isSaved;}
	
	public void setID(String id){this.id = id;} 
	
	public void close(){isClosed = true; isSaved = false;}
	public void open(){isClosed = false;}
	public void save(){isSaved = true; isClosed = false;}
	public void unsave(){isSaved = false;}
}
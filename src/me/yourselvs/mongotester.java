package me.yourselvs;
import org.bson.Document;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

import me.yourselvs.MongoDBStorage;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class mongotester {
	static String textUri;
	static MongoDBStorage mongoStorage;
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static Document counters;

	public static void main(String[] args) {		
		Object object = null;
		try {
		YamlReader reader = new YamlReader(new FileReader("config.yml"));
			object = reader.read();
		} catch (YamlException | FileNotFoundException e) {e.printStackTrace();}
		
	    System.out.println(object);
	    Map<?, ?> map = (Map<?, ?>)object;
	        
		String username = (String) map.get("dbUser");
		String password = (String) map.get("dbPass");
		textUri = "mongodb://" + username + ":" + password + "@ds056288.mongolab.com:56288/minecraft";
		mongoStorage = new MongoDBStorage(textUri,"minecraft","suggestions");
		counters = mongoStorage.findDocument(new Document("type","counter"));
		
		//System.out.println("high count: " + getHighestNum());
		printDocumentList("Fetching every single doc in collection", mongoStorage.findDocuments(new Document()));
		//addSuggestion("testPlayer", "test suggestion");
		//printDocumentList("Added suggestion", mongoStorage.findDocuments(new Document()));		
		//setStatus(getHighestNum() - 1, "saved");
		//printDocumentList("Changing last suggestion status", mongoStorage.findDocuments(new Document()));
		//System.out.println("Getting status of last suggestion: " + getStatus(getHighestNum() - 1));
		//System.out.println("Getting author of last suggestion: " + getAuthor(getHighestNum() - 1));
		//System.out.println("Getting description of last suggestion: " + getSuggestion(getHighestNum() - 1));
		//printDocumentList("Getting open and saved suggestions", getOpenAndSaved());
		mongoStorage.updateDocument(new Document("type", "counter"), new Document("$set",new Document("highCount", 0)));
		
		System.out.println("Deleted suggestions: " + deleteSuggestions());
		printDocumentList("Fetching every single doc in collection", mongoStorage.findDocuments(new Document()));
		
		
	}
	
	public static void setStatus(int id, String status) {
		// Sets the status of a suggestion based on id
		//mongoStorage.updateDocument("{type:\"suggestion\", id:\"" + id + "\"}", "{$set: {status:\"" + status + "\"} }");
		mongoStorage.updateDocument(new Document("_id",id), new Document("$set",new Document("status", status)));
	}

	public static String getStatus(int id) {
		// Gets the status of a suggestion based on id
		return mongoStorage.findDocument(new Document("type", "suggestion").append("_id", id)).getString("status");
	}
	
	public static int getHighestNum() {
		// Gets the highestNum count
		return mongoStorage.findDocument(new Document("type","counter")).getInteger("highCount");
	}
	
	public static String getAuthor(int num) {
		// Gets the of a suggestion by id
		return mongoStorage.findDocument(new Document("type", "suggestion").append("_id", num)).getString("author");
	}
	
	public static String getSuggestion(int num) {
		// Gets the description of a suggestion by id
		return mongoStorage.findDocument(new Document("type", "suggestion").append("_id", num)).getString("description");
	}
	
	public static List<Document> getOpenAndSaved() {
		// Gets both open and saved suggestions in a list
		List<Document> list = mongoStorage.findDocuments(new Document("status", "open"));
		list.addAll(mongoStorage.findDocuments(new Document("status", "saved")));
		return list;
	}
	
	public static List<Document> getOpen() {
		// Gets open suggestions in a list
		return mongoStorage.findDocuments(new Document("status", "open"));
	}
	public static List<Document> getClosed(){
		// Gets closed suggestions in a list
		return mongoStorage.findDocuments(new Document("status", "closed"));
	}
	public static List<Document> getSaved(){
		// Gets saved suggestions in a list
		return mongoStorage.findDocuments(new Document("status", "saved"));
	}
	
	public static int updateSuggestions(){
		mongoStorage.updateDocument("{type:\"counter\"}", "{$inc: {highCount:1} }");
		
		counters = mongoStorage.findDocument(new Document("type","counter"));
		return counters.getInteger("highCount");
	}
	
	public static long deleteSuggestions(){
		return mongoStorage.deleteDocuments(new Document("type","suggestion"));
	}
	
	public static long deleteFiles(){
		return mongoStorage.deleteDocuments(new Document());
	}
	
	public static void addSuggestion(String player, String description) {
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

	private static void printDocumentList(String title, List<Document> documentList) {
		System.out.println(title+":");
		for(Document doc: documentList)
		{
			System.out.println(doc.toJson());
		}
	}

}

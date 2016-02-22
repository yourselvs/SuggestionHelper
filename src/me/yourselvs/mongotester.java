package me.yourselvs;
import org.bson.Document;

import me.yourselvs.MongoDBStorage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class mongotester {

	public static void main(String[] args) {
		final String textUri = "mongodb://testUser:Weird1?@ds056288.mongolab.com:56288/minecraft";

		MongoDBStorage mongoStorage = new MongoDBStorage(textUri,"minecraft","suggestions");
		
		
		Document counters = mongoStorage.findDocument(new Document("type","counter"));
		int highestId = counters.getInteger("highCount");
		
		System.out.println("high count: " + highestId);
		
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// sample suggestion
		Document sampleSuggestion = new Document("type","suggestion")
				.append("description", "too few pixels to play with")
				.append("author", "david")
				.append("status", "submitted")
				.append("time", sdf.format(new Date()))
				.append("id", highestId);
		
		MainClass test = new MainClass();
		test.addSuggestion(sampleSuggestion);
		
		mongoStorage.updateDocument("{type:\"counter\"}", "{$inc: {highCount:1} }");
		counters = mongoStorage.findDocument(new Document("type","counter"));
		highestId = counters.getInteger("highCount");
		System.out.println("high count got updated to: " + highestId);
		
		printDocumentList("Fetching every single doc in collection", mongoStorage.findDocuments(new Document()));
		
		long deletedCount = mongoStorage.deleteDocuments(new Document("type","suggestion"));
		System.out.println("Deleted " + deletedCount + " suggestions.") ;
		
	}

	private static void printDocumentList(String title, List<Document> documentList) {
		System.out.println(title+":");
		for(Document doc: documentList)
		{
			System.out.println(doc.toJson());
		}
	}

}

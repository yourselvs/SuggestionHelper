package com.minecraft.suggestions;
import com.minecraft.suggestions.MongoDBStorage;

import java.util.ArrayList;
import org.bson.Document;
import java.util.List;

public class mongotester {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final String textUri = "<connection string>";

		MongoDBStorage mongoStorage = new MongoDBStorage(textUri,"minecraft","suggestions");
		
		
		Document counters = mongoStorage.findDocument(new Document("type","id"));
		double highestId = counters.getDouble("highCount");
		System.out.println("high count: " + highestId);
		
		
		// sample suggestion
		Document sampleSuggestion = new Document("type","suggestion")
				.append("title", "go hi res")
				.append("description", "too few pixels to play with")
				.append("author", "david")
				.append("status", "submitted")
				.append("id", highestId);
		
		
		String newDoc = "{name:\"David\",age:48,type:\"person\"}";
		mongoStorage.insertDocument(sampleSuggestion);
		
		mongoStorage.updateDocument("{type:\"id\"}", "{$inc: {highCount:1} }");
		 counters = mongoStorage.findDocument(new Document("type","id"));
		 highestId = counters.getDouble("highCount");
		System.out.println("high count got updated to: " + highestId);
		
		printDocumentList("Fetching every single doc in collection", mongoStorage.findDocuments(new Document()));

		Document agesDocString = Document.parse("{age:{$gt:40}, name: \"David\"}");
		printDocumentList("Using document.parse",mongoStorage.findDocuments(agesDocString));
		
		Document agesDoc = new Document("age", new Document("$gt",40))
				.append("name", "David")
				.append("type", "person");
		printDocumentList("Using Document construction", mongoStorage.findDocuments(agesDoc));
	
		mongoStorage.updateDocument(agesDoc, new Document("$set",new Document("name","Michael")
				.append("age", 17)));
		printDocumentList("Searching for Michael", mongoStorage.findDocuments("{name:\"Michael\"}"));	
		
		printDocumentList("All suggestions",mongoStorage.findDocuments(new Document("type","suggestion")));
		long deletedCount = mongoStorage.deleteDocuments("{name: {$in:[ \"Michael\",\"David\"]}}");
		System.out.println("Deleted " + deletedCount + " documents named Michael or David") ;
		
		deletedCount = mongoStorage.deleteDocuments(new Document("type","suggestion"));
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

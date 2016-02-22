package me.yourselvs;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.MongoClientURI;


import java.util.List;

import java.util.ArrayList;
import org.bson.Document;

public class MongoDBStorage {

	MongoClient client = null;
	MongoDatabase db = null;
	MongoCollection<Document> coll = null;
	
	public MongoDBStorage()
	{
	}
	
	public MongoDBStorage (String connectionString, String dbName, String collectionName)
	{
		client = new MongoClient(new MongoClientURI(connectionString));
		db = client.getDatabase(dbName);
		coll = db.getCollection(collectionName); 
	}
	
	public List<Document> findDocuments(String propertiesToFindJson) {
		return findDocuments(Document.parse(propertiesToFindJson));
	}
	
	public List<Document> findDocuments(Document propertiesToFind) {
		return coll.find(propertiesToFind).into(new ArrayList<Document>());
	}
	
	public Document findDocument(Document propertiesToFind) {
		return coll.find(propertiesToFind).first();
	}
	
	public Document insertDocument(Document newDocument) {
		coll.insertOne(newDocument);
		return newDocument;
	}
	
	public Document insertDocument(String newDocumentJson) {
		return insertDocument( Document.parse(newDocumentJson));
	}
	
	public boolean updateDocument(Document docToFind, Document propertiesToUpdate) {
		coll.updateMany(docToFind, propertiesToUpdate);
		return true;
	}
	
	public boolean updateDocument(String docToFind, String propertiesToUpdate) {
		return updateDocument(Document.parse(docToFind), Document.parse(propertiesToUpdate));
	}
	
	public long deleteDocuments(String docToDeleteJson) {
		return deleteDocuments(Document.parse(docToDeleteJson));
	}

	public long deleteDocuments(Document docToDelete) {
		return coll.deleteMany(docToDelete).getDeletedCount();
	}

}

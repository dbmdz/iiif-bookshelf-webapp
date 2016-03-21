package com.datazuul.iiif.catalog.portal.backend.repository.impl;

import com.datazuul.iiif.catalog.portal.backend.repository.IiifManifestRepository;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.springframework.stereotype.Repository;

/**
 *
 * @author ralf
 */
@Repository
public class IiifManifestRepositoryMongoDbImpl implements IiifManifestRepository {

    @Override
    public List<String> findAll() {
        List<String> result = new ArrayList<>();
        // Connect to a MongoDB instance running on the localhost on the default port 27017
        MongoClient mongoClient = new MongoClient();
        // Once successfully connected, access the test database
        MongoDatabase db = mongoClient.getDatabase("test");
        // You can use the find method to issue a query to retrieve data from a collection in MongoDB.
        // All queries in MongoDB have the scope of a single collection.
        FindIterable<Document> iterable = db.getCollection("restaurants").find().limit(25);
        MongoCursor<Document> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            Document document = iterator.next();
            result.add(document.getString("name"));
        }
//        iterable.forEach(new Block<Document>() {
//            @Override
//            public void apply(final Document document) {
//                System.out.println(document.getString("name"));
//            }
//        });
//        return iterable;
        return result;
    }
}

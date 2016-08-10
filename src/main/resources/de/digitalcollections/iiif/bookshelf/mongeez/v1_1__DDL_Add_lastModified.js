// mongeez formatted javascript

// changeset digitalcollections:ChangeSet-1_1
db.getCollection('iiif-manifest-summaries').update(
        {"lastModified": null},
        {$set: {"lastModified": new Date()}}, false, true
        )
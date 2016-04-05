// mongeez formatted javascript

// changeset datazuul:ChangeSet-1_1
db.getCollection('iiif-manifest-summaries').update(
        {"lastModified": null},
        {$set: {"lastModified": new Date()}}, false, true
        )
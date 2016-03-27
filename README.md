IIIF Bookshelf Webapp
=====================
This is a webapp for collecting <a href="http://iiif.io">IIIF</a> representations of books.
It is based on the functionality of the <a href="http://iiif.io/api/presentation/2.0/">IIIF Presentation API</a> for modelling books.
You can add books to your bookshelf by loading the manifest.json of the book.

Installation
------------
Requirements:

* Java 8
* MongoDB

Usage
-----

* Running from cloned code:

        mvn clean install jetty:run
        http://localhost:9898

* Accessing data from 'mongo' client:

        mongo
        > use iiif-bookshelf
        switched to db iiif-bookshelf

    * Getting all data:

        > db.getCollection('iiif-manifest-summaries').find()

    * Deleting all data (why do you want to do this?):

        > db.getCollection('iiif-manifest-summaries').drop()
        true

TODO
----

* UUID: use BSON4 instead BSON3
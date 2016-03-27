IIIF Bookshelf Webapp
=====================
This is a webapp for collecting IIIF represenations of books.
It is based on the functionality of the IIIF Presentation API for modelling books.
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


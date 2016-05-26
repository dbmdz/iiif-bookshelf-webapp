IIIF Bookshelf Webapp
=====================
This is a webapp for collecting <a href="http://iiif.io">IIIF</a> representations of books.
It is based on the functionality of the <a href="http://iiif.io/api/presentation/2.0/">IIIF Presentation API</a> for modelling books.
You can add books to your bookshelf by loading the manifest.json of the book.

Installation
------------
-------

Requirements:

* Java 8: You will need the Java Runtime Environment (JRE) version 1.8 or higher. At a command line, check your Java version like this:

        $ java -version

  On Windows:

        > java.exe -version

* MongoDB Version: 3.2.4+
* Apache Solr Version: 5.4.1+
* Apache Tomcat 8.0

Mongo DB Installation Steps
------------
### Installation on Linux
1. Download the binary files for the desired release of Mongo DB from [official Mongo DB Downloads Page](https://www.mongodb.org/downloads). 
   To download the latest release through the shell, type the following:

        $ curl -O https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-3.2.4.tgz

2. Extract the files from the downloaded archive.
   For example, from a system shell, you can extract through the tar command:

        $ tar -xvfz mongodb-linux-x86_64-3.2.4.tgz

3. Copy the extracted archive to the target directory.
   To copy the extracted folder to the location from which MongoDB will run:

        $ mkdir -p ~/DEV/PROGRAMS
        $ mv mongodb-linux-x86_64-3.2.4/ ~/DEV/PROGRAMS/mongodb

   Ensure the location of the binaries is in the PATH variable.
   For example, you can add the following line to your shell's rc file (e.g. ~/.bashrc):

```
        export PATH=<mongodb-install-directory>/bin:$PATH
```

   Installation directory for mongo ```<mongodb-install-directory>``` could be /home/username/DEV/DATA/mongodb/.
   Or in central '/etc/environment':

    MONGO_HOME="/home/```<username>```/DEV/PROGRAMS/mongodb"
    PATH="/home/```<username>```/DEV/PROGRAMS/mongodb/bin:...:$PATH"

   Replace ```<username>``` with your login username for this PC.  

### Installation on Windows

 1. Follow instructions at [Install MongoDB on Windows Page](https://docs.mongodb.com/manual/tutorial/install-mongodb-on-windows/)

Apache Solr Installation Steps
------------
Homepage: http://lucene.apache.org/solr/
Version: 5.4

### Installation on Linux

To install run the following commands:

      $ cd /opt
      $ sudo wget http://archive.apache.org/dist/lucene/solr/5.4.1/solr-5.4.1.tgz
      $ sudo tar -xzvf solr-5.4.1.tgz

### Installation on Windows

1. Download the .zip file. .zip archive from one of the mirrows at [Solr Download](http://www.apache.org/dyn/closer.lua/lucene/solr/5.4.1)
2. Extract the Solr distribution archive to a directory of your choosing. 

Apache Tomcat Installation Steps
------------

1. Download Tomcat 8 (the tar.gz file) and decompress it. 
2. Create a new directory /opt/tomcat
3. Move the files in step 2 to the directory /opt/tomcat
4. Run in /opt/tomcat/apache-tomcat-8.0.35

        $ bin/startup.sh   

Deploy Bookshelf WAR
------------

Copy iiif-bookshelf.war into webapps subdir in tomcat

## Usage
-------

To run iiif-bookshelf run local Mongo DB, Solr and Tomcat.

### Running Mongo DB

#### On Linux:
To run MongoDB, run the mongod process at the system prompt. If necessary, specify the path of the data directory using the --dbpath option.
If your system PATH variable includes the location of the mongod binary and if you use the default data directory (i.e., /data/db), enter at the system prompt:

    $ ```<path to binary>```/mongod

```<path to binary>``` could be "/home/username/DEV/PROGRAMS/mongodb/bin/"

If you do not use the default data directory (i.e., /data/db), specify the path to the data directory using the --dbpath option, which could be equal to "/home/username/DEV/DATA/":

    $ mongod --dbpath ```<path to data directory>```

To shutdown:

    $ mongod --dbpath ```<path to data directory>``` --shutdown

#### On Windows

Use the following commands to start the server process
Change to bin directory:

    > cd ```<path to binary>```

Type following command to start the mongod process:

    > mongod --dbpath ```<path to data directory>```

Use /home/username/DEV/DATA/mongodb/ for ```<path to data directory>```, for example.
While starting, Windows firewall may block the process.Click "Allow access" to proceed.
If you specify the logpath option, then logging will direct to that log file instead of showing up on standard console:

    > mongod --dbpath ```<path to data directory>``` --logpath ```<path to logs>```/mongod.log

By default logs are written into /var/log/mongodb/.

### Running Solr

#### On Linux

      $ cd /opt/solr-5.4.1
      $ sudo ./bin/solr start

After 30 seconds check at http://localhost:8983/ , whether the server has started.

Use  option -s for pointing custom path to located created cores. It sets the solr home system property.
Solr will create core directories under specified directory. This allows you to run multiple Solr instances on the same host.

    $ /opt/solr-5.4.1$ bin/solr start -s ~/```<custom-core-path>```/

For exampple, use /home/username/solrcores/ in place of ```<custom-core-path>```.
Copy solr.xml from /opt/solr-5.4.1/server/solr/ to your ```<custom-core-path>``` directory in order to create cores there later.
 
#### Creating cores

      $ bin/solr create_core -c ```<core-name>```

use -d option to specify configuration , if you have started solr with custom solr home as above.
See bin/solr create_core -help for options description.

####Adding/Deleting indexes

Post  to core .json documents from example directory:

    $ curl 'http/solr/```<core-name>```/update?commit=true' --data-binary example/exampledocs/books.json -H 'Content-type/application/json'

To delete certain document with id 20 use:

    $ java -Ddata=args -Dcommit=true -Durl=http://localhost:8983/solr/```<core-name>```/update -jar example/exampledocs/post.jar >```'<delete><id>20</id></delete>'```

#### On Windows

    > bin\solr.cmd start

### Stopping Solr

    $ sudo ./bin/solr stop

### Running Bookshelf webapp

1. To start Tomcat run start.sh within its bin directory.
2. Check on http://localhost:8080 whether it has started
3. Open webapp http://localhost:8080/iiif-bookshelf
 
* Running from cloned code:

        $ mvn clean install jetty:run
        http://localhost:9898

* Accessing data from 'mongo' client:

        $ mongo
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

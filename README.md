# IIIF Bookshelf Webapp
[![Build Status](https://travis-ci.org/dbmdz/iiif-bookshelf-webapp.svg?branch=master)](https://travis-ci.org/dbmdz/iiif-bookshelf-webapp)
[![MIT License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)


This is a webapp for collecting <a href="http://iiif.io">IIIF</a> representations of books.
It is based on the functionality of the <a href="http://iiif.io/api/presentation/2.0/">IIIF Presentation API</a> for modelling books.
You can add books to your bookshelf by loading the manifest.json of the book.

## Requirements

* Java 8: You will need the Java Runtime Environment (JRE) version 1.8 or higher. At a command line, check your Java version with "java -version".
* MongoDB Version: 3.2.4+
* Apache Solr Version: 5.4.1+
* Apache Tomcat 8.0

### Mongo DB

* Homepage: https://www.mongodb.com/
* Version: 3.2.4

#### Installation

1. Download the binary files for the desired release of Mongo DB from [official Mongo DB Downloads Page](https://www.mongodb.org/downloads).
   To download the latest release through the shell, type the following:

```shell
$ curl -O https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-3.2.4.tgz
```

2. Extract the files from the downloaded archive.
   For example, from a system shell, you can extract with the tar command:

```shell
$ tar -zxvf mongodb-linux-x86_64-3.2.4.tgz
```

3. Copy the extracted archive to the target directory.
   To copy the extracted folder to the location from which MongoDB will run:

```shell
$ mkdir -p /opt
$ mv mongodb-linux-x86_64-3.2.4/ /opt
```

#### Configuration

Ensure the location of the executables is in the PATH variable.

* SuSE Linux:

```shell
# vi /etc/profile.d/mongodb.sh

# Add paths for mongo db
if [ -d /opt/mongodb-linux-x86_64-3.2.4/bin ]; then
    COUNT=`ls -1 /opt/mongodb-linux-x86_64-3.2.4/bin/ | wc -l`
    if [ $COUNT -gt 0 ]; then
        PATH="$PATH:/opt/mongodb-linux-x86_64-3.2.4/bin"
    fi
fi
export PATH=$PATH
```

```shell
# vi /etc/profile.d/mongodb.csh

# Add paths for mongo db
if ( -d /opt/mongodb-linux-x86_64-3.2.4/bin ) then
    set COUNT=`ls -1 /opt/mongodb-linux-x86_64-3.2.4/bin/ | wc -l`
    if ( $COUNT > 0 ) then
        setenv PATH "${PATH}:/opt/mongodb-linux-x86_64-3.2.4/bin"
    endif
endif
```

* Ubuntu Linux:

```shell
# vi /etc/environment
...
PATH="/opt/mongodb-linux-x86_64-3.2.4/bin:$PATH"
...
```

Test it (after reboot or sourcing of file):

```shell
mongod --version
db version v3.2.4
git version: e2ee9ffcf9f5a94fad76802e28cc978718bb7a30
allocator: tcmalloc
modules: none
build environment:
    distarch: x86_64
    target_arch: x86_64
```

#### Usage

To run MongoDB, run the mongod process at the system prompt. If you do not use the default data directory (i.e., /data/db), specify the path of the data directory using the --dbpath option. Example:

```shell
$ mkdir -p /local/mongodb/data
$ mongod --dbpath /local/mongodb/data
```

To shutdown mongod you have to specify the data directory, too (if you are not using the default directory):

```shell
$ mongod --dbpath /local/mongodb/data --shutdown
```

If you specify the logpath option, then logging will direct to that log file instead of showing up on standard console:

```shell
$ mongod --dbpath /local/mongodb/data --logpath /var/log/mongodb.log
```

### Apache Solr

* Homepage: http://lucene.apache.org/solr/
* Version: 5.4

#### Installation

Download and extract a Solr release:

```shell
$ cd /opt
$ sudo wget http://archive.apache.org/dist/lucene/solr/5.4.1/solr-5.4.1.tgz
$ sudo tar -xzvf solr-5.4.1.tgz
```

Resulting Solr installation directory is ```/opt/solr-5.4.1```.

#### Configuration

##### Set SOLR_JAVA_HOME

Set the path to the Java home that Solr should use (SOLR_JAVA_HOME variable):

* SuSE Linux:

```shell
# vi /etc/profile.d/solr.sh

# /etc/profile.d/solr.sh
# Adds SOLR_JAVA_HOME for solr
export SOLR_JAVA_HOME="/opt/jdk1.8.0_60"
```

```shell
# vi /etc/profile.d/solr.csh

# /etc/profile.d/solr.csh
# Adds SOLR_JAVA_HOME for solr
setenv SOLR_JAVA_HOME="/opt/jdk1.8.0_60"
```

* Ubuntu Linux:

```shell
# vi /etc/environment
...
SOLR_JAVA_HOME="/opt/jdk1.8.0_60"
...
```

##### Create core

In Solr, the term ```core``` is used to refer to a single index and associated transaction log and configuration files (including the solrconfig.xml and Schema files, among others). Your Solr installation can have multiple cores if needed, which allows you to index data with different structures in the same server, and maintain more control over how your data is presented to different audiences.

Help information for creating a core:

```shell
# bin/solr create_core -help

Usage: solr create_core [-c core] [-d confdir] [-p port]

  -c <core>     Name of core to create

  -d <confdir>  Configuration directory to copy when creating the new core, built-in options are:

      basic_configs: Minimal Solr configuration
      data_driven_schema_configs: Managed schema with field-guessing support enabled
      sample_techproducts_configs: Example configuration with many optional features enabled to
         demonstrate the full power of Solr

      If not specified, default is: data_driven_schema_configs

      Alternatively, you can pass the path to your own configuration directory instead of using
      one of the built-in configurations, such as: bin/solr create_core -c mycore -d /tmp/myconfig

  -p <port>     Port of a local Solr instance where you want to create the new core
                  If not specified, the script will search the local system for a running
                  Solr instance and will use the port of the first server it finds.
```

Note: The configuration directories (source of copy) are located in /opt/solr-5.4.1/server/solr/configsets/.

Creating a core needs a running solr server. As we want a custom location for data (e.g. /local/data-solr), we have to start the server with param -s, see help:

```shell
# bin/solr start -help

Usage: solr start [-f] [-c] [-h hostname] [-p port] [-d directory] [-z zkHost] [-m memory] [-e example] [-s solr.solr.home] [-a "additional-options"] [-V]

...
  -s <dir>      Sets the solr.solr.home system property; Solr will create core directories under
                  this directory. This allows you to run multiple Solr instances on the same host
                  while reusing the same server directory set using the -d parameter. If set, the
                  specified directory should contain a solr.xml file, unless solr.xml exists in ZooKeeper.
                  This parameter is ignored when running examples (-e), as the solr.solr.home depends
                  on which example is run. The default value is server/solr.
...
```

Start server with custom home directory "/local/data-solr":

```shell
# mkdir -p /local/data-solr
$ /opt/solr-5.4.1/bin/solr start -s /local/data-solr

Solr home directory /local/data-solr must contain a solr.xml file!
```
Ok, copy the file "solr.xml" to the custom home directory:

```shell
# cp /opt/solr-5.4.1/server/solr/solr.xml /local/data-solr/
```

Start solr:

```shell
# cd /opt/solr-5.4.1/
# bin/solr start -s /local/data-solr
Waiting up to 30 seconds to see Solr running on port 8983 [/]  
Started Solr server on port 8983 (pid=7763). Happy searching!
```

Create a core named "bookshelf" for our bookshelf index, using a copy of "data_driven_schema_configs":

```shell
# cd /opt/solr-5.4.1
# bin/solr create_core -c bookshelf -d data_driven_schema_configs

Copying configuration to new core instance directory:
/local/data-solr/bookshelf

Creating new core 'bookshelf' using command:
http://localhost:8983/solr/admin/cores?action=CREATE&name=bookshelf&instanceDir=bookshelf

{
  "responseHeader":{
    "status":0,
    "QTime":1492},
  "core":"bookshelf"}

```

The new core has been created in directory "/local/data-solr/bookshelf".

### Usage

To run the Solr server with the custom home directory:

```shell
$ cd /opt/solr-5.4.1
$ sudo bin/solr start -s /local/data-solr
Waiting up to 30 seconds to see Solr running on port 8983 [/]  
Started Solr server on port 8983 (pid=2771). Happy searching!
```

After 30 seconds check at http://localhost:8983/, whether the server has started.
You can browse the Admin GUI under http://localhost:8983/solr/.

If you're not sure if Solr is running locally, you can use the status command:

```shell
$ bin/solr status

Found 1 Solr nodes:

Solr process 2771 running on port 8983
{
  "solr_home":"/local/data-solr",
  "version":"5.4.1 1725212 - jpountz - 2016-01-18 11:51:45",
  "startTime":"2016-06-06T08:13:25.42Z",
  "uptime":"0 days, 0 hours, 3 minutes, 15 seconds",
  "memory":"73.9 MB (%15.1) of 490.7 MB"}
```

Stopping Solr

```shell
$ cd /opt/solr-5.4.1
$ sudo ./bin/solr stop
Sending stop command to Solr running on port 8983 ... waiting 5 seconds to allow Jetty process 7763 to stop gracefully.
```

## Apache Tomcat

* Homepage: http://tomcat.apache.org/
* Version: 8.0.35

### Installation

Download Tomcat 8 and decompress it to target directory.

```shell
$ cd /opt
$ sudo wget http://mirror.synyx.de/apache/tomcat/tomcat-8/v8.0.35/bin/apache-tomcat-8.0.35.tar.gz
$ sudo tar -xvfz apache-tomcat-8.0.35.tar.gz
```

### Usage

Start Tomcat:

```shell
$ cd /opt/tomcat/apache-tomcat-8.0.35
$ sudo bin/startup.sh
```

Stop Tomcat:
```shell
$ cd /opt/tomcat/apache-tomcat-8.0.35
$ sudo bin/shutdown.sh
```

## Bookshelf webapp

### Installation

Build the webapp locally and copy it to server:

```shell
$ cd <source_directory_bookshelf>
$ mvn clean install
$ scp target/iiif-bookshelf.war <user>@<server>:/tmp
```

Deploy Bookshelf WAR into Tomcat:

```shell
$ mv /tmp/iiif-bookshelf.war /opt/tomcat/apache-tomcat-8.0.35/webapps
```

### Usage

* To run iiif-bookshelf run Mongo DB, Solr and Tomcat.

```shell
# mongod --dbpath /local/mongodb/data --logpath /var/log/mongodb.log &
# /opt/solr-5.4.1/bin/solr start -s /local/data-solr
# /opt/apache-tomcat-8.0.35/bin/startup.sh
```

Open webapp in browser: http://localhost:8080/iiif-bookshelf

* To stop iiif-bookshelf stop all servers:

```shell
# /opt/apache-tomcat-8.0.35/bin/shutdown.sh
# /opt/solr-5.4.1/bin/solr stop -s /local/data-solr
# mongod --dbpath /local/mongodb/data --shutdown
```

* Accessing data from 'mongo' client:

```shell
$ mongo
> use iiif-bookshelf
switched to db iiif-bookshelf
```

Getting all data:

```shell
> db.getCollection('iiif-manifest-summaries').find()
```

Deleting all data (why do you want to do this?):

```shell
> db.getCollection('iiif-manifest-summaries').drop()
true
```

* To enable annotations install and run SimpleAnnotationServer from https://github.com/glenrobson/SimpleAnnotationServer.

The server requeres Java 1.7 and maven installed.
To install SimpleAnnotationServer run the following commands in Linux:

  ```shell
  $ git clone https://github.com/glenrobson/SimpleAnnotationServer.git

  $ cd SimpleAnnotationServer

  $ mvn jetty:run

  ```

Check at  http://localhost:8888/index.html wheter the server has started. After that one can write annotations from IIIF Bookshelf Webapp.

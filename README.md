EPRTR2 Content Management
=========================

The application uses Liquibase to create and upgrade the database, and Thymeleaf as the templating engine.
You can find the layout template at src/main/webapp/WEB-INF/thymeleaf/layout.html. The database is in-memory
for the production and file-based for test. You can therefore just drop the WAR file into Tomcat, and it will
create tables, load demo data and launch.

Dependencies
------------
* Tomcat 7
* Java 1.7
* Spring 4
* Thymeleaf 2.1.4
* H2 Database Engine

Features
--------
The rich text editor was implemented with [CKEditor](http://ckeditor.com/). The package was downloaded and placed in src/main/webapp/scripts.

Automated tests
---------------
There are test examples of both controllers and data access objects using the Spring test package.
Note that loading demo data is part of the liquibase changelog, and the tests use that data instead
of initialising with their own test data.

How to build
------------
You need Git to check the code out from the repository and to build you need Java and Maven.  All other dependencies will automatically be downloaded by Maven.

For Windows see the pages on:
* [Git for Windows](http://git-scm.com/downloads)
* [Maven for Windows](http://maven.apache.org/guides/getting-started/windows-prerequisites.html).

To build you do:
```
git clone https://github.com/eea/eprtr2.cms.git
cd eprtr2.cms
mvn.bat install
```

This will create a `target` subdirectory, build the code, run the tests and put a WAR file in target. You can then deploy this file to Tomcat. It contains an embedded database with demo data.

Deployment
----------
The default configuration is to allow you to deploy to your own workstation directly. You install the target/transfer.war to Tomcat's webapps directory as ROOT.war. You can make it create an initial user with administrator rights by setting system properties to configure the application.

On a CentOS system you can start Tomcat with the environment variable JAVA_OPTS set to some value or add lines to /etc/sysconfig/tomcat that looks like this:
```
JAVA_OPTS="-Dcas.service=http://transfers.com -Dinitial.username=myname"
JAVA_OPTS="$JAVA_OPTS -Ddb.url=jdbc:h2:tcp://localhost:8043//work/transferdb -Dstorage.dir=/work -Dupload.dir=/work"
```
These are the properties you can set:
```
db.driver
db.url
db.username
db.password
storage.dir
upload.dir
initial.username
initial.password
cas.service
cas.server.host
```
The default values are in src/main/resources/application.properties and src/main/resources/cas.properties.


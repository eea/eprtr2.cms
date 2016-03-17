EPRTR2 Content Management
=========================

----------------------- THIS PACKAGE IS NOW INTEGRATED INTO EPRTR2 -----------------------

The application uses Liquibase to create and upgrade the database, and Thymeleaf as the templating engine.
You can find the layout template at src/main/webapp/WEB-INF/thymeleaf/layout.html. The database is in-memory
for the production and file-based for test. You can therefore just drop the WAR file into Tomcat, and it will
create tables, load demo data and launch.

Dependencies
------------
* Tomcat 7 or 8
* Java 1.7
* Spring 4
* Thymeleaf 2.1.4
* H2 Database Engine

Features
--------
The rich text editor was implemented with [CKEditor](http://ckeditor.com/). The package was downloaded and placed in src/main/webapp/js.

Automated tests
---------------
There are test examples of both controllers and data access objects using the Spring test package.
Note that loading demo data is part of the liquibase changelog, and the tests use that data instead
of initialising with their own test data.

How to build
------------
You need Git to check the code out from the repository and to build you need Java and Maven.  All other dependencies will automatically be downloaded by Maven.


To build you do:
```
git clone https://github.com/eea/eprtr2.cms.git
cd eprtr2.cms
mvn install
```

This will create a `target` subdirectory, build the code, run the tests and put a WAR file in target. You can then deploy this file to Tomcat. It contains an embedded database with demo data.

Deployment
----------
The application needs a database. It won't create or populate it automatically. To do it manually, do:

```
mysqladmin -p -u root create EPRTRcms
mysqladmin -p -u root
SQL> CREATE USER 'eprtradmin'@'localhost' IDENTIFIED BY 'password-here';
SQL> GRANT ALL PRIVILEGES ON EPRTRcms.* TO 'eprtradmin'@'localhost';
SQL> exit;
edit the prod-liquibase.properties file
mvn liquibase:update
```

The default configuration is to allow you to deploy to your own workstation directly. You install the target/eprtr-cms.war to Tomcat's webapps directory as cms.war. You can make it create an initial user with administrator rights by setting system properties to configure the application.

On a CentOS system you can start Tomcat with the environment variable CATALINA_OPTS set to some value or add lines to /etc/sysconfig/tomcat that looks like this:
```
CATALINA_OPTS="-Dcmsdb.url=jdbc:h2:tcp://localhost:8043//work/eprtrcms -Dstorage.dir=/work -Dupload.dir=/work"
```
These are the properties you can set:
```
cmsdb.driver
cmsdb.url
cmsdb.username
cmsdb.password
cmsdb.createtables = false - If true then the application will create the database
storage.dir
upload.dir
ldap.service

deploy.contexts = prod - If set to demo, then some demo data will be inserted
initial.username = - Username will become administrator
initial.password =

```
The default values are in src/main/resources/application.properties and src/main/resources/cas.properties.

Deployment as Docker container
------------------------------

The application can be deployed as a Docker container. To do so you first build the image using Maven:

```
mvn -Pdocker clean install docker:push
```
Then you can deploy it with docker-compose using a YAML file derived from the one you see in the top directory.


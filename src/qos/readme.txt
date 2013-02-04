CloudTM Sla Web Application installation

Requirements: Java 5 or later. A servlet container supporting Servlet 2.4 and JSP 2.0 ( recommended Tomcat v 6.0 ). MySQL database management system.
The CloudTM Sla Web Application comes packaged as a standard Java web application, CloudTMSla.war, which can be easily deployed in any compatible servlet container. The rest of this section describes how to install CloudTM Sla Web Application on a Tomcat server.

1) Copy the file CloudTMSla.war to TOMCAT_HOME/webapps.
2) In the root directory of your server create a folder named config and copy in it the config.properties file contained in the installation package.
3) Open up config.properties with a text editor and fill in your database connection details and the path used by CloudTM Sla application to store customer's templates and applications.
4) Connect to your MySQL database and run cloutm.sql script contained in the installation package
5) Point your web browser to http://localhost/CloudTMSla/ and login with default username admin and password: admin.

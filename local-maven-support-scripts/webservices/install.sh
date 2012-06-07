#!/bin/bash
mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=webservices-extra-api.jar \
                       -DgroupId=external \
                       -DartifactId=webservices-extra-api \
                       -Dversion=2003-09-04 \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true

mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=webservices-extra.jar \
                       -DgroupId=external \
                       -DartifactId=webservices-extra \
                       -Dversion=2008-03-12 \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true


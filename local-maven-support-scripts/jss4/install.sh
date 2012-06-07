#!/bin/bash
mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=jss4.jar \
                       -DgroupId=external \
                       -DartifactId=jss4 \
                       -Dversion=2007-08-11 \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true


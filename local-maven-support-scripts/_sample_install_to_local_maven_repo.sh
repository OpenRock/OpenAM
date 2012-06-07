#!/bin/bash
mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=<jar filename> \
                       -DgroupId=<groupid> \
                       -DartifactId=<artifactid> \
                       -Dversion=<version> \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true


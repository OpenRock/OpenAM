#!/bin/bash
mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=je.jar \
                       -DgroupId=com.sleepycat \
                       -DartifactId=je \
                       -Dversion=external \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true


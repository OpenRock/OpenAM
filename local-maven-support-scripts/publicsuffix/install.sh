#!/bin/bash
mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=publicsuffix-1.0.1.jar \
                       -DgroupId=external \
                       -DartifactId=publicsuffix \
                       -Dversion=1.0.1 \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true


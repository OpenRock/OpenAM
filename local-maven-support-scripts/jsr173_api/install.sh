#!/bin/bash
mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=jsr173_api.jar \
                       -DgroupId=external \
                       -DartifactId=jsr173_api \
                       -Dversion=2004-30-01 \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true


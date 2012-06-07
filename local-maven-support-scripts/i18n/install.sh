#!/bin/bash
mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=i18n-core-1.4.0.jar \
                       -DgroupId=external \
                       -DartifactId=i18-core \
                       -Dversion=1.4.0 \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true


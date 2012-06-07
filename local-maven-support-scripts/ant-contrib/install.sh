#!/bin/bash
mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=ant-contrib-1.0b3.jar \
                       -DgroupId=external \
                       -DartifactId=ant-contrib \
                       -Dversion=1.0b3 \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true


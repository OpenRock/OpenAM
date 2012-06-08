#!/bin/bash
mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=functionaljava-2.19.jar \
                       -DgroupId=org.functionaljava \
                       -DartifactId=fj \
                       -Dversion=2.19 \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true


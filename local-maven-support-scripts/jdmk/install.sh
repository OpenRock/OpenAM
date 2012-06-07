#!/bin/bash
mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=jdmkrt.jar \
                       -DgroupId=external \
                       -DartifactId=jdmkrt \
                       -Dversion=2007-01-10 \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true

mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=jdmktk.jar \
                       -DgroupId=external \
                       -DartifactId=jdmktk \
                       -Dversion=2007-01-10 \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true


#!/bin/bash
mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=OpenDJ.jar \
                       -DgroupId=external \
                       -DartifactId=OpenDJ \
                       -Dversion=2012-20-02 \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true

mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=opendj-ldap-sdk-3.0.0-M2.jar \
                       -DgroupId=external \
                       -DartifactId=opendj-ldap-sdk \
                       -Dversion=3.0.0-M2 \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true


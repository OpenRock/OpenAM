#!/bin/bash
mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=jato.jar \
                       -DgroupId=com.iplanet.jato \
                       -DartifactId=jato \
                       -Dversion=2005-05-04 \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true


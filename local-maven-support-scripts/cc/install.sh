#!/bin/bash
mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=cc.jar \
                       -DgroupId=com.sun.web.ui \
                       -DartifactId=cc \
                       -Dversion=2008-08-08 \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true

mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=cc_de.jar \
                       -DgroupId=com.sun.web.ui \
                       -DartifactId=cc_de \
                       -Dversion=2008-08-08 \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true

mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=cc_es.jar \
                       -DgroupId=com.sun.web.ui \
                       -DartifactId=cc_es \
                       -Dversion=2008-08-08 \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true

mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=cc_fr.jar \
                       -DgroupId=com.sun.web.ui \
                       -DartifactId=cc_fr \
                       -Dversion=2008-08-08 \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true

mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=cc_it.jar \
                       -DgroupId=com.sun.web.ui \
                       -DartifactId=cc_it \
                       -Dversion=2008-08-08 \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true

mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=cc_ja.jar \
                       -DgroupId=com.sun.web.ui \
                       -DartifactId=cc_ja \
                       -Dversion=2008-08-08 \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true

mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=cc_ko.jar \
                       -DgroupId=com.sun.web.ui \
                       -DartifactId=cc_ko \
                       -Dversion=2008-08-08 \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true

mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=cc_sv.jar \
                       -DgroupId=com.sun.web.ui \
                       -DartifactId=cc_sv \
                       -Dversion=2008-08-08 \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true

mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=cc_zh.jar \
                       -DgroupId=com.sun.web.ui \
                       -DartifactId=cc_zh \
                       -Dversion=2008-08-08 \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true

mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=cc_zh_CN.jar \
                       -DgroupId=com.sun.web.ui \
                       -DartifactId=cc_zh_CC \
                       -Dversion=2008-08-08 \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true

mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=cc_zh_HK.jar \
                       -DgroupId=com.sun.web.ui \
                       -DartifactId=cc_zh_HK \
                       -Dversion=2008-08-08 \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true

mvn -e -X org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                       -DlocalRepositoryPath=../../local-maven-support-repository \
                       -Dfile=cc_zh_TW.jar \
                       -DgroupId=com.sun.web.ui \
                       -DartifactId=cc_zh_TW \
                       -Dversion=2008-08-08 \
                       -Dpackaging=jar \
                       -DgeneratePom=true \
                       -DuniqueVersion=true


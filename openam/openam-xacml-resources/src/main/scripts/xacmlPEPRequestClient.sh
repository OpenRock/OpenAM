#!/bin/bash
# ****************************************************************
#
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2011-2013 ForgeRock AS. All Rights Reserved
#
# The contents of this file are subject to the terms
# of the Common Development and Distribution License
# (the License). You may not use this file except in
# compliance with the License.
#
# You can obtain a copy of the License at
# http://forgerock.org/license/CDDLv1.0.html
# See the License for the specific language governing
# permission and limitations under the License.
#
# When distributing Covered Code, include this CDDL
# Header Notice in each file and include the License file
# at http://forgerock.org/license/CDDLv1.0.html
# If applicable, add the following below the CDDL Header,
# with the fields enclosed by brackets [] replaced by
# your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# ****************************************************************

if [ -z "$JAVA_HOME" ] ; then
        JAVA_HOME="@JAVA_HOME@"
fi

TOOLS_HOME="@TOOLS_HOME@"
EXT_CLASSPATH=$CLASSPATH

CLASSPATH="@CONFIG_DIR@"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/classes:$TOOLS_HOME/resources"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/

CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/bcprov-jdk16-1.46.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/bcprov-ext-jdk16-1.46.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/bctsp-jdk16-1.46.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/bcmail-jdk16-1.46.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/httpclient-4.2.2.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/httpcore-4.2.2.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/commons-logging-1.1.1.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/commons-codec-1.6.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/commons-digester-2.1.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/commons-beanutils-1.8.3.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/jackson-core-2.1.2.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/jackson-databind-2.1.2.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/jackson-annotations-2.1.1.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/guice-1.0.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/guice-annotations-2.0.1.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/annotations-7.0.3.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/json-20090211.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/json-resource-2.0.0-Xpress2.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/org.forgerock.util-1.0.0.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/json-fluent-2.0.0-Xpress2.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/org.forgerock.json.resource-1.2.2.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/openam-xacml3-schema-${project.version}.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/openam-shared-10.2.0-SNAPSHOT.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/openam-xacml-resources-10.2.0-SNAPSHOT.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/openam-locale-${project.version}.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/i18n-core-1.4.0.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/openam-build-tools-10.2.0-SNAPSHOT.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/openam-entitlements-10.2.0-SNAPSHOT.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/openam-federation-library-${project.version}.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/openam-core-10.2.0-SNAPSHOT.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/openam-coretoken-${project.version}.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/openam-rest-${project.version}.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/jsr311-api-1.1.1.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/openam-idsvcs-schema-${project.version}.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/jaxb-api-1.0.6.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/jaxp-api-1.4.2.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/jaxrpc-api-1.1.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/jaxrpc-spi-1.1.3_01.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/jaxrpc-impl-1.1.3_01-041406.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/mail-1.4.5.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/activation-1.1.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/openam-mib-schema-${project.version}.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/opendj-server-2.4.6.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/opendj-ldap-sdk-3.0.0-OPENAMp3.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/grizzly-framework-2.1.11.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/gmbal-api-only-3.1.0-b001.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/management-api-3.0.0-b012.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/jato-2005-05-04.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/cc-2008-08-08.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/cc_de-2008-08-08.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/cc_es-2008-08-08.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/cc_fr-2008-08-08.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/cc_it-2008-08-08.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/cc_ja-2008-08-08.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/cc_ko-2008-08-08.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/cc_sv-2008-08-08.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/cc_zh-2008-08-08.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/cc_zh_CN-2008-08-08.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/cc_zh_HK-2008-08-08.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/cc_zh_TW-2008-08-08.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/asm-3.3.1.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/backport-util-concurrent-3.1.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/click-extras-2.3.0.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/click-nodeps-2.3.0.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/commons-collections-3.2.1.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/commons-fileupload-1.2.2.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/commons-io-2.3.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/commons-lang-2.6.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/ognl-2.6.9.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/velocity-1.7.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/commons-logging-api-1.1.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/esapiport-2009-26-07.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/imq-4.4.2.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/jdom-2.0.1.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/sleepycat-je-2011-04-07.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/jersey-bundle-1.1.1-ea.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/jms-4.4.2.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/stax-api-1.0-2.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/jstl-1.1.2.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/log4j-1.2.16.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/oauth-client-1.1.5.2.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/jersey-client-1.1.5.2.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/jersey-core-1.1.5.2.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/oauth-server-1.1.5.2.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/oauth-signature-1.1.5.2.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/relaxngDatatype-20020414.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/saaj-api-1.3.4.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/saaj-impl-1.3.19.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/mimepull-1.7.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/jaxws-api-2.2.8.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/javax.annotation-3.1.1.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/jsr181-api-1.0-MR1.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/jaxws-rt-2.2.7-promoted-b73.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/streambuffer-1.5.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/stax-ex-1.7.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/woodstox-core-asl-4.1.2.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/stax2-api-3.1.1.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/policy-2.3.1.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/txw2-20110809.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/stack-commons-runtime-2.4.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/ha-api-3.1.8.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/webservices-tools-2.1-b16.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/webservices-api-2.1-b16.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/jsr250-api-1.0.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/xalan-2.7.1.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/serializer-2.7.1.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/xercesImpl-2.11.0.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/xml-serializer-2.11.0.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/xmlsec-1.3.0.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/xsdlib-20060615.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/isorelax-20030108.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/openam-saml2-schema-${project.version}.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/openam-wsfederation-schema-${project.version}.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/openam-liberty-schema-${project.version}.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/xml-apis-2.11.0.jar"
CLASSPATH="$CLASSPATH:$TOOLS_HOME/lib/xml-resolver-2.11.0.jar"

if [ -n "$EXT_CLASSPATH" ] ; then
        CLASSPATH=$EXT_CLASSPATH:$CLASSPATH
fi

$JAVA_HOME/bin/java -Xms256m -Xmx512m -cp "${CLASSPATH}" \
    -D"sun.net.client.defaultConnectTimeout=3000" \
    -D"openam.naming.sitemonitor.disabled=true" \
    -D"com.iplanet.am.serverMode=false" \
    -D"com.sun.identity.sm.notification.enabled=false" \
    -D"bootstrap.dir=@CONFIG_DIR@" \
    -D"com.iplanet.services.debug.directory=@DEBUG_DIR@" \
    -D"com.sun.identity.log.dir=@LOG_DIR@" \
    -D"file.encoding=UTF-8" \
    org.forgerock.identity.openam.xacml.v3.tools.XacmlPEPRequestClient \
    --url http//localhost:18080/openam/xacml/pdp/authorization \
    --method POST \
    --contenttype XML \
    --principal amadmin \
    --credential cangetin \
    --requestfile ./request-curtiss.xml

#!/bin/bash

# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2011 ForgeRock AS. All Rights Reserved
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

# This script generates Java Help Search indices and should be running after
# every change to the javahelp files.

JHALL_JAR=`pwd`/../lib/jhall.jar
JAVAHELP_HOME=`pwd`/../../../products/amserver/docs/console/javahelp
LOCALES="de_DE en es_ES fr_FR ja_JP ko_KR zh zh_CN zh_TW"

for locale in $LOCALES
do
	echo "Generating Java Help for locale: $locale"
	DIR="${JAVAHELP_HOME}/$locale"
	rm -rf "${DIR}/JavaHelpSearch/"{DOCS,DOCS.TAB,OFFSETS,POSITIONS,SCHEMA,TMAP}
	cd "${DIR}"
	java -cp ".:${JHALL_JAR}" com.sun.java.help.search.Indexer -locale $locale "${DIR}"
done

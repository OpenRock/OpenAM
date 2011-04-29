#!/bin/sh

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
#

AMSESSION_DB=`pwd`
CONFIG_DIR=$AMSESSION_DB/../config
LIB_DIR=$AMSESSION_DB/../lib
DEBUG_OPT=-agentlib:jdwp=transport=dt_socket,address=9000,server=y,suspend=n

java -Djava.util.logging.config.file="$CONFIG_DIR/amsessiondblog.properties" -cp $LIB_DIR/AMSessionStore-0.1-SNAPSHOT.jar:$LIB_DIR/AMSessionStoreCommon-0.1-SNAPSHOT.jar:$LIB_DIR/org.restlet-2.0.6.jar:$LIB_DIR/commons-net-2.2.jar:$LIB_DIR/jackson-core-asl-1.4.3.jar:$LIB_DIR/jackson-mapper-asl-1.4.3.jar:$LIB_DIR/org.osgi.core-4.0.0.jar:$LIB_DIR/org.restlet.ext.jackson-2.0.6.jar:$LIB_DIR/org.restlet.ext.json-2.0.6.jar:$LIB_DIR/org.restlet.lib.org.json-2.0.jar:$LIB_DIR/orient-commons-1.0rc1-SNAPSHOT.jar:$LIB_DIR/orientdb-core-1.0rc1-SNAPSHOT.jar:$LIB_DIR/orientdb-enterprise-1.0rc1-SNAPSHOT.jar:$LIB_DIR/orientdb-server-1.0rc1-SNAPSHOT.jar:$LIB_DIR/orientdb-client-1.0rc1-SNAPSHOT.jar:$LIB_DIR/persistence-api-1.0.jar:../config $DEBUG_OPT org.forgerock.openam.amsessionstore.AMSessionStoreServer


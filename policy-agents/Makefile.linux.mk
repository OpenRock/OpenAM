#
# The contents of this file are subject to the terms of the Common Development and
# Distribution License (the License). You may not use this file except in compliance with the
# License.
#
# You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
# specific language governing permission and limitations under the License.
#
# When distributing Covered Software, include this CDDL Header Notice in each file and include
# the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
# Header, with the fields enclosed by brackets [] replaced by your own identifying
# information: "Portions copyright [year] [name of copyright owner]".
#
# Copyright 2014 - 2015 ForgeRock AS.
#

ifndef	LINUX_MK_INCLUDED
LINUX_MK_INCLUDED := true
	
CC := gcc
SHARED := -shared

CFLAGS  += -fPIC -pthread -D_REENTRANT -DLINUX -D_GNU_SOURCE -D_FORTIFY_SOURCE=2 -fstack-protector \
	    -Wno-unused-value -Wno-deprecated-declarations 

ifdef DEBUG
 CFLAGS += -g3 -fno-inline -O0 -DDEBUG -Wall
else
 CFLAGS += -g -O2 -DNDEBUG
endif

ifdef 64
 CFLAGS += -m64 -DLINUX_64
 LDFLAGS += -m64
else
 CFLAGS += -m32 -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_LARGEFILE64_SOURCE
endif

LDFLAGS += -Wl,-rpath,'$$ORIGIN/../lib' -Wl,-rpath,'$$ORIGIN' -lpthread -lrt -ldl

libopenam: $(OUT_OBJS)
	@$(ECHO) "[*** Creating "$@" shared library ***]"
	${CC} $(SHARED) -fPIC -Wl,-soname,libopenam.so  $(LDFLAGS) $(OUT_OBJS) -o build/libopenam.so
	
apache: $(OUT_OBJS) $(APACHE_OUT_OBJS)
	@$(ECHO) "[*** Creating "$@" shared library ***]"
	${CC} $(SHARED) -fPIC -Wl,-soname,mod_openam.so $(LDFLAGS) \
	    $(OUT_OBJS) -Wl,--version-script=source/apache/agent.map $(APACHE_OUT_OBJS) -o build/mod_openam.so

iis: 
	@$(ECHO) "[*** IIS target is not supported on this platform ***]"

varnish: 
	@$(ECHO) "[*** Varnish target is not supported on this platform ***]"
	
agentadmin: $(OUT_OBJS) $(ADMIN_OUT_OBJS)
	@$(ECHO) "[*** Creating "$@" binary ***]"
	${CC} $(LDFLAGS) $(OUT_OBJS) $(ADMIN_OUT_OBJS) -o build/agentadmin

endif

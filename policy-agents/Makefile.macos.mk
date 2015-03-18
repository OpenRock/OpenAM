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
# Copyright 2014 ForgeRock AS.
#

ifndef	MACOS_MK_INCLUDED
MACOS_MK_INCLUDED := true
	
CC := clang
SHARED := -dynamiclib

CFLAGS  += -std=c99 -fPIC -fno-common -D_REENTRANT -arch x86_64 -Wno-unused-value \
	    -Wno-deprecated-declarations

ifdef DEBUG
 CFLAGS += -g3 -fno-inline -O0 -DDEBUG -Wall
else
 CFLAGS += -g -O2 -DNDEBUG
endif

ifndef 64
 $(error 32bit build on this platform is not supported)
endif

LDFLAGS += -Wl,-flat_namespace -Wl,-undefined,suppress -Wl,-rpath,'$$ORIGIN/../lib' -Wl,-rpath,'$$ORIGIN' \
	     -lpthread -lc -ldl

libopenam: $(OUT_OBJS)
	@$(ECHO) "[*** Creating "$@" shared library ***]"
	${CC} $(SHARED) -Wl,-install_name,libopenam.dylib  $(LDFLAGS) $(OUT_OBJS) -o build/libopenam.dylib
	
apache: $(OUT_OBJS) $(APACHE_OUT_OBJS)
	@$(ECHO) "[*** Creating "$@" shared library ***]"
	${CC} $(SHARED) -Wl,-install_name,mod_openam.so -Wl,-exported_symbols_list,source/apache/agent.exp \
	    $(LDFLAGS) $(OUT_OBJS) $(APACHE_OUT_OBJS) -o build/mod_openam.so

iis: 
	@$(ECHO) "[*** IIS target is not supported on this platform ***]"

varnish: 
	@$(ECHO) "[*** Varnish target is not supported on this platform ***]"
	
agentadmin: $(OUT_OBJS) $(ADMIN_OUT_OBJS)
	@$(ECHO) "[*** Creating "$@" binary ***]"
	${CC} $(LDFLAGS) $(OUT_OBJS) $(ADMIN_OUT_OBJS) -o build/agentadmin

endif

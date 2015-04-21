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

ifndef	SOLARIS_MK_INCLUDED
SOLARIS_MK_INCLUDED := true
	
CC := suncc
SHARED := -G

CFLAGS  += -xc99=all -mt -D_REENTRANT -DSOLARIS -D_POSIX_PTHREAD_SEMANTICS 

ifdef DEBUG
 CFLAGS += -xO0 -DDEBUG
else
 CFLAGS += -xO3 -DNDEBUG
endif

ifeq ($(OS_MARCH), i86pc)
 CFLAGS += -KPIC
else
 CFLAGS += -xcode=pic32
endif

ifdef 64
 CFLAGS += -m64
 LDFLAGS += -m64
else
 CFLAGS += -m32 -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_LARGEFILE64_SOURCE
endif

LDFLAGS += -i -z ignore -z lazyload -z nodefs -z combreloc -z origin -R'$$ORIGIN/../lib' -R'$$ORIGIN' -lc -lsocket -lnsl -ldl -lrt

libopenam: $(OUT_OBJS)
	@$(ECHO) "[*** Creating "$@" shared library ***]"
	${CC} $(SHARED) -h libopenam.so  $(LDFLAGS) $(OUT_OBJS) -o build/libopenam.so
	
apache: $(OUT_OBJS) $(APACHE_OUT_OBJS)
	@$(ECHO) "[*** Creating "$@" shared library ***]"
	${CC} $(SHARED) -h mod_openam.so -Wl,-M,source/apache/agent.map $(LDFLAGS) $(OUT_OBJS) \
	    $(APACHE_OUT_OBJS) -o build/mod_openam.so

iis: 
	$(error IIS target is not supported on this platform)

varnish: 
	$(error Varnish target is not supported on this platform)
	
agentadmin: $(OUT_OBJS) $(ADMIN_OUT_OBJS)
	@$(ECHO) "[*** Creating "$@" binary ***]"
	${CC} $(LDFLAGS) $(OUT_OBJS) $(ADMIN_OUT_OBJS) -o build/agentadmin

endif

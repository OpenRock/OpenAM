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

ifndef	AIX_MK_INCLUDED
AIX_MK_INCLUDED := true
	
CC := xlc_r
SHARED := -G -qmkshrobj

CFLAGS += -qpic -D_REENTRANT -D_THREAD_SAFE -DAIX -qlanglvl=extc99:noucs -DPIC -D_LARGEFILE64_SOURCE

ifdef DEBUG
 CFLAGS += -DDEBUG
else
 CFLAGS += -O -qmaxmem=-1 -DNDEBUG
endif

ifdef 64
 LDFLAGS += -q64
 CFLAGS += -q64
endif

LINKOPTS := -bnoentry -bquiet -brtl
	
LDFLAGS += -lpthread -ldl -lc -lm

libopenam: $(OUT_OBJS)
	@$(ECHO) "[*** Creating "$@" shared library ***]"
	${CC} $(SHARED) $(LDFLAGS) $(OUT_OBJS) -o build/libopenam.so
	
apache: $(OUT_OBJS) $(APACHE_OUT_OBJS)
	@$(ECHO) "[*** Creating "$@" shared library ***]"
	${CC} $(SHARED) -bE:source/apache/agent.exp $(LDFLAGS) $(OUT_OBJS) $(APACHE_OUT_OBJS) -o build/mod_openam.so

iis: 
	$(error IIS target is not supported on this platform)

varnish: 
	$(error Varnish target is not supported on this platform)
	
agentadmin: $(OUT_OBJS) $(ADMIN_OUT_OBJS)
	@$(ECHO) "[*** Creating "$@" binary ***]"
	${CC} $(LDFLAGS) $(OUT_OBJS) $(ADMIN_OUT_OBJS) -o build/agentadmin

endif

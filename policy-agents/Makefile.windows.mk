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

ifndef	WINDOWS_MK_INCLUDED
WINDOWS_MK_INCLUDED := true
	
CC = cl
LINK = link
RC = rc
SHARED = /DLL

CFLAGS  += /O2 /Oi /GL /Gy /D _CRT_SECURE_NO_WARNINGS /wd4996 /wd4101 /wd4244 /wd4995 /wd4275 \
	/EHa /nologo /Zi /errorReport:none /MP /Gm- /W3 /c /TC /D WIN32 /D ZLIB_WINAPI /D PCRE_STATIC /MTd /D _DEBUG

LDFLAGS += /SUBSYSTEM:CONSOLE /NOLOGO /INCREMENTAL:NO /errorReport:none /MANIFEST:NO \
	/OPT:REF /OPT:ICF /LTCG /DYNAMICBASE /NXCOMPAT /DEBUG \
	/MACHINE:X64
	
LIBS = kernel32.lib user32.lib ws2_32.lib crypt32.lib advapi32.lib shlwapi.lib shell32.lib iphlpapi.lib

$(IIS_OUT_OBJS): COMPILEOPTS += /TP

ifndef 64
$(error Only 64bit targets are supported)
endif

libopenam: $(OUT_OBJS)
	@$(ECHO) "[*** Creating "$@" shared library ***]"
	${LINK} $(SHARED) $(LDFLAGS) $(OUT_OBJS) /OUT:build\$@.dll /PDB:build\$@.pdb \
	    $(LIBS)
	
apache: $(OUT_OBJS) $(APACHE_OUT_OBJS)
	@$(ECHO) "[*** Creating "$@" shared library ***]"
	${LINK} $(SHARED) $(LDFLAGS) $(OUT_OBJS) $(APACHE_OUT_OBJS) /OUT:build\mod_openam.dll /PDB:build\mod_openam.pdb \
	    $(LIBS) \
	    extlib/Windows/apache24/lib/libapr-1.lib extlib/Windows/apache24/lib/libaprutil-1.lib \
	    extlib/Windows/apache24/lib/libhttpd.lib

iis: $(OUT_OBJS) $(IIS_OUT_OBJS)
	@$(ECHO) "[*** Creating "$@" shared library ***]"
	${LINK} $(SHARED) $(LDFLAGS) $(OUT_OBJS) $(IIS_OUT_OBJS) /OUT:build\mod_iis_openam.dll /PDB:build\mod_iis_openam.pdb \
	    $(LIBS) /EXPORT:RegisterModule oleaut32.lib

varnish: 
	$(error Varnish target is not supported on this platform)
	
agentadmin: $(OUT_OBJS) $(ADMIN_OUT_OBJS)
	@$(ECHO) "[*** Creating "$@" binary ***]"
	${LINK} $(LDFLAGS) $(OUT_OBJS) $(ADMIN_OUT_OBJS) /OUT:build\$@.exe /PDB:build\$@.pdb $(LIBS) \
	    ole32.lib oleaut32.lib ahadmin.lib

endif

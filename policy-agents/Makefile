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

# make options:
#  64=1 builds 64bit binary
#  DEBUG=1 builds debug binary version

64=1
DEBUG=1

VERSION := 4.0.0-SNAPSHOT

ifneq ("$(PROGRAMFILES)$(ProgramFiles)","")
 OS_ARCH := Windows
 RMALL := cmd /c del /F /Q
 RMDIR := cmd /c rmdir /S /Q
 SED := cmd /c sed.exe
 ECHO := cmd /c echo
 MKDIR := cmd /c mkdir
 CP := cmd /c copy /Y
 CD := cd
 EXEC := 
 REVISION := $(shell svn info . | findstr "Revision:")
 BUILD_MACHINE := $(shell hostname)
 IDENT_DATE := $(shell powershell get-date -format "{dd.MM.yyyy}")
 PATHSEP=\\
 SUB=/
 COMPILEFLAG=/
 COMPILEOPTS=/Fd$@.pdb /Fo$(dir $@)
 OBJ=obj
else
 OS_ARCH := $(shell uname -s)
 OS_MARCH := $(shell uname -m)
 RMALL := rm -fr
 RMDIR := $(RMALL)
 SED := sed
 ECHO := echo
 MKDIR := mkdir -p
 CP := cp
 CD := cd
 EXEC := ./
 REVISION := $(shell svn info . | grep Revision:)
 BUILD_MACHINE := $(shell hostname)
 IDENT_DATE := $(shell date +'%d.%m.%y')
 PATHSEP=/
 SUB=%
 COMPILEFLAG=-
 COMPILEOPTS=-c -o $@
 OBJ=o
endif

ifdef 64
 OS_BITS := _64bit
else
 OS_BITS :=
endif

PS=$(strip $(PATHSEP))

CFLAGS := $(COMPILEFLAG)I.$(PS)source $(COMPILEFLAG)I.$(PS)zlib $(COMPILEFLAG)I.$(PS)expat $(COMPILEFLAG)I.$(PS)pcre \
	  $(COMPILEFLAG)DHAVE_EXPAT_CONFIG_H $(COMPILEFLAG)DHAVE_PCRE_CONFIG_H
OBJDIR := build

APACHE_SOURCES := source/apache/agent.c
IIS_SOURCES := source/iis/agent.c
VARNISH_SOURCES := source/varnish/agent.c
ADMIN_SOURCES := source/admin.c source/admin_iis.c
SOURCES := $(filter-out $(ADMIN_SOURCES), $(wildcard source/*.c)) $(wildcard expat/*.c) $(wildcard pcre/*.c) $(wildcard zlib/*.c)
OBJECTS := $(SOURCES:.c=.$(OBJ))
OUT_OBJS := $(addprefix $(OBJDIR)/,$(OBJECTS))
ADMIN_OBJECTS := $(ADMIN_SOURCES:.c=.$(OBJ))
ADMIN_OUT_OBJS := $(addprefix $(OBJDIR)/,$(ADMIN_OBJECTS))
APACHE_OBJECTS := $(APACHE_SOURCES:.c=.$(OBJ))
APACHE_OUT_OBJS := $(addprefix $(OBJDIR)/,$(APACHE_OBJECTS))
IIS_OBJECTS := $(IIS_SOURCES:.c=.$(OBJ))
IIS_OUT_OBJS := $(addprefix $(OBJDIR)/,$(IIS_OBJECTS))
VARNISH_OBJECTS := $(VARNISH_SOURCES:.c=.$(OBJ))
VARNISH_OUT_OBJS := $(addprefix $(OBJDIR)/,$(VARNISH_OBJECTS))
	
$(APACHE_OUT_OBJS): CFLAGS += $(COMPILEFLAG)Iextlib/$(OS_ARCH)/apache24/include $(COMPILEFLAG)Iextlib/$(OS_ARCH)_$(OS_MARCH)/apache24/include -DAPACHE2 -DAPACHE24
$(VARNISH_OUT_OBJS): CFLAGS += $(COMPILEFLAG)Iextlib/$(OS_ARCH)/varnish/include
	
ifeq ($(OS_ARCH), Linux)
 include Makefile.linux.mk
endif
ifeq ($(OS_ARCH), SunOS)
 include Makefile.solaris.mk
endif
ifeq ($(OS_ARCH), AIX)
 include Makefile.aix.mk
endif
ifeq ($(OS_ARCH), Darwin)
 include Makefile.macos.mk
endif
ifeq ($(OS_ARCH), Windows)
 include Makefile.windows.mk
endif

$(OBJDIR)/%.$(OBJ): %.c
	@$(ECHO) "[*** Compiling "$<" ***]"
	$(CC) $(CFLAGS) $< $(COMPILEOPTS)

.DEFAULT_GOAL := all

all: apachezip
	
build:
	$(MKDIR) $(OBJDIR)$(PS)expat
	$(MKDIR) $(OBJDIR)$(PS)pcre
	$(MKDIR) $(OBJDIR)$(PS)zlib
	$(MKDIR) $(OBJDIR)$(PS)source$(PS)apache
	$(MKDIR) $(OBJDIR)$(PS)source$(PS)iis
	$(MKDIR) $(OBJDIR)$(PS)source$(PS)varnish

version:
	@$(ECHO) "[***** Updating version.h *****]"
	-$(RMALL) source$(PS)version.h
	$(SED) -e "s$(SUB)_REVISION_$(SUB)$(REVISION)$(SUB)g" \
	    -e "s$(SUB)_IDENT_DATE_$(SUB)$(IDENT_DATE)$(SUB)g" \
	    -e "s$(SUB)_BUILD_MACHINE_$(SUB)$(BUILD_MACHINE)$(SUB)g" \
	    -e "s$(SUB)_VERSION_$(SUB)$(VERSION)$(SUB)g" < source$(PS)version.template > source$(PS)version.h
clean:
	-$(RMDIR) $(OBJDIR)
	-$(RMDIR) log
	-$(RMALL) source$(PS)version.h
	
apachezip: clean build version apache agentadmin
	@$(ECHO) "[***** Building Apache agent archive *****]"
	-$(MKDIR) $(OBJDIR)$(PS)web_agents
	-$(MKDIR) $(OBJDIR)$(PS)web_agents$(PS)apache24_agent
	-$(MKDIR) $(OBJDIR)$(PS)web_agents$(PS)apache24_agent$(PS)bin
	-$(MKDIR) $(OBJDIR)$(PS)web_agents$(PS)apache24_agent$(PS)lib
	-$(MKDIR) $(OBJDIR)$(PS)web_agents$(PS)apache24_agent$(PS)legal
	-$(MKDIR) $(OBJDIR)$(PS)web_agents$(PS)apache24_agent$(PS)instances
	-$(MKDIR) $(OBJDIR)$(PS)web_agents$(PS)apache24_agent$(PS)log
	-$(MKDIR) $(OBJDIR)$(PS)web_agents$(PS)apache24_agent$(PS)config
	-$(CP) $(OBJDIR)$(PS)agentadmin* $(OBJDIR)$(PS)web_agents$(PS)apache24_agent$(PS)bin$(PS)
	-$(CP) $(OBJDIR)$(PS)mod_openam.so $(OBJDIR)$(PS)web_agents$(PS)apache24_agent$(PS)lib$(PS)
	-$(CP) $(OBJDIR)$(PS)mod_openam.dll $(OBJDIR)$(PS)web_agents$(PS)apache24_agent$(PS)lib$(PS)
	-$(CP) $(OBJDIR)$(PS)mod_openam.pdb $(OBJDIR)$(PS)web_agents$(PS)apache24_agent$(PS)lib$(PS)
	-$(CP) config$(PS)* $(OBJDIR)$(PS)web_agents$(PS)apache24_agent$(PS)config$(PS)
	-$(CP) legal$(PS)* $(OBJDIR)$(PS)web_agents$(PS)apache24_agent$(PS)legal$(PS)
	$(CD) $(OBJDIR) && $(EXEC)agentadmin --a Apache_v24_$(OS_ARCH)$(OS_BITS)_$(VERSION).zip web_agents

iiszip: clean build version iis agentadmin
	@$(ECHO) "[***** Building IIS agent archive *****]"
	-$(MKDIR) $(OBJDIR)$(PS)web_agents
	-$(MKDIR) $(OBJDIR)$(PS)web_agents$(PS)iis_agent
	-$(MKDIR) $(OBJDIR)$(PS)web_agents$(PS)iis_agent$(PS)bin
	-$(MKDIR) $(OBJDIR)$(PS)web_agents$(PS)iis_agent$(PS)lib
	-$(MKDIR) $(OBJDIR)$(PS)web_agents$(PS)iis_agent$(PS)legal
	-$(MKDIR) $(OBJDIR)$(PS)web_agents$(PS)iis_agent$(PS)instances
	-$(MKDIR) $(OBJDIR)$(PS)web_agents$(PS)iis_agent$(PS)log
	-$(MKDIR) $(OBJDIR)$(PS)web_agents$(PS)iis_agent$(PS)config
	-$(CP) $(OBJDIR)$(PS)agentadmin* $(OBJDIR)$(PS)web_agents$(PS)iis_agent$(PS)bin$(PS)
	-$(CP) $(OBJDIR)$(PS)mod_iis_openam.dll $(OBJDIR)$(PS)web_agents$(PS)iis_agent$(PS)lib$(PS)
	-$(CP) $(OBJDIR)$(PS)mod_iis_openam.pdb $(OBJDIR)$(PS)web_agents$(PS)iis_agent$(PS)lib$(PS)
	-$(CP) config$(PS)* $(OBJDIR)$(PS)web_agents$(PS)iis_agent$(PS)config$(PS)
	-$(CP) legal$(PS)* $(OBJDIR)$(PS)web_agents$(PS)iis_agent$(PS)legal$(PS)
	$(CD) $(OBJDIR) && $(EXEC)agentadmin --a IIS_$(OS_ARCH)$(OS_BITS)_$(VERSION).zip web_agents

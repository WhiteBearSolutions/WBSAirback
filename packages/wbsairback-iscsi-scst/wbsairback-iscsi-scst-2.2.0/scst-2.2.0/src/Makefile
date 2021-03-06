#
#  SCSI target mid-level makefile
#  
#  Copyright (C) 2004 - 2011 Vladislav Bolkhovitin <vst@vlnb.net>
#  Copyright (C) 2004 - 2005 Leonid Stoljar
#  Copyright (C) 2007 - 2010 ID7 Ltd.
#  Copyright (C) 2010 - 2011 SCST Ltd.
#  
#  This program is free software; you can redistribute it and/or
#  modify it under the terms of the GNU General Public License
#  as published by the Free Software Foundation, version 2
#  of the License.
# 
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
#  GNU General Public License for more details.
#
#
# Main targets:
#    all (the default) : make all
#    clean             : clean files
#    extraclean        : clean + clean dependencies
#    install           : install 
#    uninstall         : uninstall 
#
# Notes :
#    - install and uninstall must be made as root
#

SHELL=/bin/bash

DEV_HANDLERS_DIR = dev_handlers

ifneq ($(PATCHLEVEL),)
SCST_INC_DIR := $(SUBDIRS)/../include

obj-m := scst.o

scst-y        += scst_main.o
scst-y        += scst_targ.o
scst-y        += scst_lib.o
#scst-y        += scst_proc.o
scst-y        += scst_sysfs.o
scst-y        += scst_mem.o
scst-y        += scst_debug.o
scst-y        += scst_pres.o
scst-y        += scst_tg.o
obj-$(CONFIG_SCST)   += scst.o dev_handlers/

obj-$(BUILD_DEV) += $(DEV_HANDLERS_DIR)/

else
#ifeq ($(KVER),)
#  ifeq ($(KDIR),)
#    KVER = $(shell uname -r)
#    KDIR := /lib/modules/$(KVER)/build
#  endif
#else
#  KDIR := /lib/modules/$(KVER)/build
#endif

all:
	$(MAKE) -C $(KDIR) SUBDIRS=$(shell pwd) BUILD_DEV=m

scst:
	$(MAKE) -C $(KDIR) SUBDIRS=$(shell pwd) BUILD_DEV=n

MODS_VERS := $(shell ls Modules.symvers 2>/dev/null)
# It's renamed in 2.6.18
MOD_VERS := $(shell ls Module.symvers 2>/dev/null)

install: all
	-rm -f $(INSTALL_DIR)/scsi_tgt.ko
	install -d $(INSTALL_DIR)
	install -d $(INSTALL_DIR)/dev_handlers
	install -m 644 dev_handlers/*.ko $(INSTALL_DIR)/dev_handlers
	install -m 644 scst.ko $(INSTALL_DIR)
	install -d $(INSTALL_DIR_H)
	install -m 644 ../include/scst.h $(INSTALL_DIR_H)
	install -m 644 ../include/scst_sgv.h $(INSTALL_DIR_H)
	install -m 644 ../include/scst_debug.h $(INSTALL_DIR_H)
	install -m 644 ../include/scst_user.h $(INSTALL_DIR_H)
	install -m 644 ../include/scst_const.h $(INSTALL_DIR_H)
ifneq ($(MODS_VERS),)
	rm -f $(INSTALL_DIR_H)/Module.symvers
	install -m 644 Modules.symvers $(INSTALL_DIR_H)
endif
ifneq ($(MOD_VERS),)
	rm -f $(INSTALL_DIR_H)/Modules.symvers
	install -m 644 Module.symvers $(INSTALL_DIR_H)
endif
	-/sbin/depmod -a $(KVER)
	mkdir -p $(DESTDIR)/var/lib/scst/pr
	@echo "****************************************************************"
	@echo "*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*"
	@echo "*!!                                                          !!*"
	@echo "*!!  Now don't forget to rebuild and reinstall all your      !!*"
	@echo "*!!  target drivers, custom dev handlers and necessary user  !!*"
	@echo "*!!  space applications. Otherwise, because of the versions  !!*"
	@echo "*!!  mismatch, you could have many problems and crashes.     !!*"
	@echo "*!!  See IMPORTANT note in the \"Installation\" section of     !!*"
	@echo "*!!  SCST's README file for more info.                       !!*"
	@echo "*!!                                                          !!*"
	@echo "*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*"
	@echo "****************************************************************"

uninstall:
	cd $(DEV_HANDLERS_DIR) && $(MAKE) $@
	rm -f $(INSTALL_DIR)/scst.ko
	-rmdir $(INSTALL_DIR) 2>/dev/null
	-/sbin/depmod -a $(KVER)
	rm -rf $(INSTALL_DIR_H)
endif

KVER := 3.2.43-wbsairback
INSTALL_DIR := $(DESTDIR)/lib/modules/$(KVER)/extra
INSTALL_DIR_H := $(DESTDIR)/usr/local/include/scst

enable-Wextra = $(shell uname_r="$$(uname -r)"; if [ "$${uname_r%.el5}" = "$${uname_r}" ]; then echo "$(1)"; fi)

EXTRA_CFLAGS += -I$(SCST_INC_DIR) $(call enable-Wextra,-Wextra \
	-Wno-unused-parameter -Wno-missing-field-initializers)

#EXTRA_CFLAGS += -DCONFIG_SCST_STRICT_SERIALIZING

EXTRA_CFLAGS += -DCONFIG_SCST_EXTRACHECKS

#EXTRA_CFLAGS += -DCONFIG_SCST_USE_EXPECTED_VALUES
#EXTRA_CFLAGS += -DCONFIG_SCST_TEST_IO_IN_SIRQ
#EXTRA_CFLAGS += -DCONFIG_SCST_ABORT_CONSIDER_FINISHED_TASKS_AS_NOT_EXISTING

#EXTRA_CFLAGS += -fno-inline

#EXTRA_CFLAGS += -DCONFIG_SCST_TRACING

EXTRA_CFLAGS += -DCONFIG_SCST_DEBUG -g -fno-inline -fno-inline-functions
#EXTRA_CFLAGS += -DCONFIG_SCST_DEBUG_TM -DCONFIG_SCST_TM_DBG_GO_OFFLINE
#EXTRA_CFLAGS += -DCONFIG_SCST_DEBUG_RETRY
#EXTRA_CFLAGS += -DCONFIG_SCST_DEBUG_OOM
#EXTRA_CFLAGS += -DCONFIG_SCST_DEBUG_SN

#EXTRA_CFLAGS += -DCONFIG_SCST_MEASURE_LATENCY

# If defined, makes SCST zero allocated data buffers. 
# Undefining it considerably improves performance and eases CPU load, 
# but could create a security hole (information leakage), so 
# enable it if you have strict security requirements.
#EXTRA_CFLAGS += -DSCST_STRICT_SECURITY

clean:
	rm -f *.o *.ko .*.cmd *.mod.c .*.d .depend Modules.symvers \
		Module.symvers Module.markers modules.order
	rm -rf .tmp_versions
	cd $(DEV_HANDLERS_DIR) && $(MAKE) $@

extraclean: clean
	cd $(DEV_HANDLERS_DIR) && $(MAKE) $@
	rm -f *.orig *.rej

debug2release:
	sed -i.aa s/"^EXTRA_CFLAGS += \-DCONFIG_SCST_EXTRACHECKS"/"#EXTRA_CFLAGS += \-DCONFIG_SCST_EXTRACHECKS"/ Makefile
	@cmp Makefile Makefile.aa >/dev/null; if [[ $$? = 0 ]]; then rm Makefile.aa; echo "sed failed: wrong original state?"; false; fi
	sed -i.aa s/"#EXTRA_CFLAGS += \-DCONFIG_SCST_TRACING"/"EXTRA_CFLAGS += \-DCONFIG_SCST_TRACING"/ Makefile
	@cmp Makefile Makefile.aa >/dev/null; if [[ $$? = 0 ]]; then rm Makefile.aa; echo "sed failed: wrong original state?"; false; fi
	sed -i.aa s/"^EXTRA_CFLAGS += \-DCONFIG_SCST_DEBUG -g -fno-inline -fno-inline-functions"/"#EXTRA_CFLAGS += \-DCONFIG_SCST_DEBUG -g -fno-inline -fno-inline-functions"/ Makefile
	@cmp Makefile Makefile.aa >/dev/null; if [[ $$? = 0 ]]; then rm Makefile.aa; echo "sed failed: wrong original state?"; false; fi
	rm Makefile.aa
	cd $(DEV_HANDLERS_DIR) && $(MAKE) $@

release2debug:
	sed -i.aa s/"#EXTRA_CFLAGS += \-DCONFIG_SCST_EXTRACHECKS"/"EXTRA_CFLAGS += \-DCONFIG_SCST_EXTRACHECKS"/ Makefile
	@cmp Makefile Makefile.aa >/dev/null; if [[ $$? = 0 ]]; then rm Makefile.aa; echo "sed failed: wrong original state?"; false; fi
	sed -i.aa s/"^EXTRA_CFLAGS += \-DCONFIG_SCST_TRACING"/"#EXTRA_CFLAGS += \-DCONFIG_SCST_TRACING"/ Makefile
	@cmp Makefile Makefile.aa >/dev/null; if [[ $$? = 0 ]]; then rm Makefile.aa; echo "sed failed: wrong original state?"; false; fi
	sed -i.aa s/"#EXTRA_CFLAGS += \-DCONFIG_SCST_DEBUG -g -fno-inline -fno-inline-functions"/"EXTRA_CFLAGS += \-DCONFIG_SCST_DEBUG -g -fno-inline -fno-inline-functions"/ Makefile
	@cmp Makefile Makefile.aa >/dev/null; if [[ $$? = 0 ]]; then rm Makefile.aa; echo "sed failed: wrong original state?"; false; fi
	rm Makefile.aa
	cd $(DEV_HANDLERS_DIR) && $(MAKE) $@

debug2perf:
	sed -i.aa s/"^EXTRA_CFLAGS += \-DCONFIG_SCST_EXTRACHECKS"/"#EXTRA_CFLAGS += \-DCONFIG_SCST_EXTRACHECKS"/ Makefile
	@cmp Makefile Makefile.aa >/dev/null; if [[ $$? = 0 ]]; then rm Makefile.aa; echo "sed failed: wrong original state?"; false; fi
	sed -i.aa s/"^EXTRA_CFLAGS += \-DCONFIG_SCST_DEBUG -g -fno-inline -fno-inline-functions"/"#EXTRA_CFLAGS += \-DCONFIG_SCST_DEBUG -g -fno-inline -fno-inline-functions"/ Makefile
	@cmp Makefile Makefile.aa >/dev/null; if [[ $$? = 0 ]]; then rm Makefile.aa; echo "sed failed: wrong original state?"; false; fi
	rm Makefile.aa
	cd $(DEV_HANDLERS_DIR) && $(MAKE) $@

perf2debug:
	sed -i.aa s/"#EXTRA_CFLAGS += \-DCONFIG_SCST_EXTRACHECKS"/"EXTRA_CFLAGS += \-DCONFIG_SCST_EXTRACHECKS"/ Makefile
	@cmp Makefile Makefile.aa >/dev/null; if [[ $$? = 0 ]]; then rm Makefile.aa; echo "sed failed: wrong original state?"; false; fi
	sed -i.aa s/"#EXTRA_CFLAGS += \-DCONFIG_SCST_DEBUG -g -fno-inline -fno-inline-functions"/"EXTRA_CFLAGS += \-DCONFIG_SCST_DEBUG -g -fno-inline -fno-inline-functions"/ Makefile
	@cmp Makefile Makefile.aa >/dev/null; if [[ $$? = 0 ]]; then rm Makefile.aa; echo "sed failed: wrong original state?"; false; fi
	rm Makefile.aa
	cd $(DEV_HANDLERS_DIR) && $(MAKE) $@

disable_proc:
	sed -i.aa s/"^#define CONFIG_SCST_PROC"/"\/* #define CONFIG_SCST_PROC *\/"/ ../include/scst.h
	@cmp ../include/scst.h ../include/scst.h.aa >/dev/null; if [[ $$? = 0 ]]; then rm ../include/scst.h.aa; echo "sed failed: wrong original state?"; false; fi
	rm ../include/scst.h.aa
	sed -i.aa s/"^scst-y        += scst_proc.o"/"#scst\-y        += scst_proc.o"/ Makefile
	@cmp Makefile Makefile.aa >/dev/null; if [[ $$? = 0 ]]; then rm Makefile.aa; echo "sed failed: wrong original state?"; false; fi
	sed -i.aa s/"#scst\-y        += scst_sysfs.o"/"scst\-y        += scst_sysfs.o"/ Makefile
	@cmp Makefile Makefile.aa >/dev/null; if [[ $$? = 0 ]]; then rm Makefile.aa; echo "sed failed: wrong original state?"; false; fi
	rm Makefile.aa

enable_proc:
	sed -i.aa s/"\/\* #define CONFIG_SCST_PROC \*\/"/"#define CONFIG_SCST_PROC"/ ../include/scst.h
	@cmp ../include/scst.h ../include/scst.h.aa >/dev/null; if [[ $$? = 0 ]]; then rm ../include/scst.h.aa; echo "sed failed: wrong original state?"; false; fi
	rm ../include/scst.h.aa
	sed -i.aa s/"#scst\-y        += scst_proc.o"/"scst\-y        += scst_proc.o"/ Makefile
	@cmp Makefile Makefile.aa >/dev/null; if [[ $$? = 0 ]]; then rm Makefile.aa; echo "sed failed: wrong original state?"; false; fi
	sed -i.aa s/"^scst\-y        += scst_sysfs.o"/"#scst\-y        += scst_sysfs.o"/ Makefile
	@cmp Makefile Makefile.aa >/dev/null; if [[ $$? = 0 ]]; then rm Makefile.aa; echo "sed failed: wrong original state?"; false; fi
	rm Makefile.aa

.PHONY: all install uninstall clean extraclean debug2release release2debug debug2perf perf2debug disable_proc enable_proc

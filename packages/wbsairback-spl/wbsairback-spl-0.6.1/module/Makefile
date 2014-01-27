subdir-m += spl
subdir-m += splat

INSTALL_MOD_DIR ?= extra

SPL_MODULE_CFLAGS  = -I/opt/git-clone/wbsairback/packages/wbsairback-spl/wbsairback-spl-0.6.1/include
SPL_MODULE_CFLAGS += -include /opt/git-clone/wbsairback/packages/wbsairback-spl/wbsairback-spl-0.6.1/spl_config.h
export SPL_MODULE_CFLAGS

modules:
	$(MAKE) -C /opt/git-clone/wbsairback/packages/wbsairback-spl/wbsairback-spl-0.6.1/../../wbsairback-kernel-image/wbsairback-kernel-image-3.2.43/ SUBDIRS=`pwd`  CONFIG_SPL=m $@

clean:
	@# Only cleanup the kernel build directories when CONFIG_KERNEL
	@# is defined.  This indicates that kernel modules should be built.
	$(MAKE) -C /opt/git-clone/wbsairback/packages/wbsairback-spl/wbsairback-spl-0.6.1/../../wbsairback-kernel-image/wbsairback-kernel-image-3.2.43/ SUBDIRS=`pwd`  $@

	if [ -f Module.symvers ]; then $(RM) Module.symvers; fi
	if [ -f Module.markers ]; then $(RM) Module.markers; fi

modules_install:
	@# Install the kernel modules
	$(MAKE) -C /opt/git-clone/wbsairback/packages/wbsairback-spl/wbsairback-spl-0.6.1/../../wbsairback-kernel-image/wbsairback-kernel-image-3.2.43/ SUBDIRS=`pwd` $@ \
		INSTALL_MOD_PATH=$(DESTDIR)$(INSTALL_MOD_PATH) \
		INSTALL_MOD_DIR=$(INSTALL_MOD_DIR)
	@# Remove extraneous build products when packaging
	kmoddir=$(DESTDIR)$(INSTALL_MOD_PATH)/lib/modules/3.2.43-wbsairback; \
	if [ -n $$kmoddir ]; then \
		find $$kmoddir -name 'modules.*' | xargs $(RM); \
	fi
	sysmap=$(DESTDIR)$(INSTALL_MOD_PATH)/boot/System.map-3.2.43-wbsairback; \
	if [ -f $$sysmap ]; then \
		depmod -ae -F $$sysmap 3.2.43-wbsairback; \
	fi

modules_uninstall:
	@# Uninstall the kernel modules
	kmoddir=$(DESTDIR)$(INSTALL_MOD_PATH)/lib/modules/3.2.43-wbsairback
	list='$(subdir-m)'; for subdir in $$list; do \
		$(RM) -R $$kmoddir/$(INSTALL_MOD_DIR)/$$subdir; \
	done

distdir:
	list='$(subdir-m)'; for subdir in $$list; do \
		(find ../module/$$subdir -name '*.c' -o -name '*.h' |\
		 xargs /bin/cp -t $$distdir/$$subdir); \
	done

distclean maintainer-clean: clean
install: modules_install
uninstall: modules_uninstall
all: modules
check:

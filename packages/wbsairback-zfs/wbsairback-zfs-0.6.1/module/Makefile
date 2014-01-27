subdir-m += avl
subdir-m += nvpair
subdir-m += unicode
subdir-m += zcommon
subdir-m += zfs
subdir-m += zpios

INSTALL_MOD_DIR ?= extra

ZFS_MODULE_CFLAGS += -include /opt/git-clone/wbsairback/packages/wbsairback-zfs/wbsairback-zfs-0.6.1/../../wbsairback-spl/wbsairback-spl-0.6.1/spl_config.h
ZFS_MODULE_CFLAGS += -include /opt/git-clone/wbsairback/packages/wbsairback-zfs/wbsairback-zfs-0.6.1/zfs_config.h
ZFS_MODULE_CFLAGS += -I/opt/git-clone/wbsairback/packages/wbsairback-zfs/wbsairback-zfs-0.6.1/include -I/opt/git-clone/wbsairback/packages/wbsairback-zfs/wbsairback-zfs-0.6.1/../../wbsairback-spl/wbsairback-spl-0.6.1/include -I/opt/git-clone/wbsairback/packages/wbsairback-zfs/wbsairback-zfs-0.6.1/../../wbsairback-spl/wbsairback-spl-0.6.1
export ZFS_MODULE_CFLAGS

modules:
	@# Make the exported SPL symbols available to these modules.
	@# They may be in the root of SPL_OBJ when building against
	@# installed devel headers, or they may be in the module
	@# subdirectory when building against the spl source tree.
	@if [ -f /opt/git-clone/wbsairback/packages/wbsairback-zfs/wbsairback-zfs-0.6.1/../../wbsairback-spl/wbsairback-spl-0.6.1/Module.symvers ]; then \
		/bin/cp /opt/git-clone/wbsairback/packages/wbsairback-zfs/wbsairback-zfs-0.6.1/../../wbsairback-spl/wbsairback-spl-0.6.1/Module.symvers .; \
	elif [ -f /opt/git-clone/wbsairback/packages/wbsairback-zfs/wbsairback-zfs-0.6.1/../../wbsairback-spl/wbsairback-spl-0.6.1/module/Module.symvers ]; then \
		/bin/cp /opt/git-clone/wbsairback/packages/wbsairback-zfs/wbsairback-zfs-0.6.1/../../wbsairback-spl/wbsairback-spl-0.6.1/module/Module.symvers .; \
	else \
		echo -e "\n" \
		"*** Missing spl symbols ensure you have built the spl:\n" \
		"*** - /opt/git-clone/wbsairback/packages/wbsairback-zfs/wbsairback-zfs-0.6.1/../../wbsairback-spl/wbsairback-spl-0.6.1/Module.symvers, or\n" \
		"*** - /opt/git-clone/wbsairback/packages/wbsairback-zfs/wbsairback-zfs-0.6.1/../../wbsairback-spl/wbsairback-spl-0.6.1/module/Module.symvers\n"; \
		exit 1; \
	fi
	$(MAKE) -C /opt/git-clone/wbsairback/packages/wbsairback-zfs/wbsairback-zfs-0.6.1/../../wbsairback-kernel-image/wbsairback-kernel-image-3.2.43 SUBDIRS=`pwd`  CONFIG_ZFS=m $@

clean:
	@# Only cleanup the kernel build directories when CONFIG_KERNEL
	@# is defined.  This indicates that kernel modules should be built.
	$(MAKE) -C /opt/git-clone/wbsairback/packages/wbsairback-zfs/wbsairback-zfs-0.6.1/../../wbsairback-kernel-image/wbsairback-kernel-image-3.2.43 SUBDIRS=`pwd`  $@

	if [ -f Module.symvers ]; then $(RM) Module.symvers; fi
	if [ -f Module.symvers ]; then $(RM) Module.symvers; fi
	if [ -f Module.markers ]; then $(RM) Module.markers; fi

modules_install:
	@# Install the kernel modules
	$(MAKE) -C /opt/git-clone/wbsairback/packages/wbsairback-zfs/wbsairback-zfs-0.6.1/../../wbsairback-kernel-image/wbsairback-kernel-image-3.2.43 SUBDIRS=`pwd` $@ \
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

distclean maintainer-clean: clean
install: modules_install
uninstall: modules_uninstall
all: modules
check:

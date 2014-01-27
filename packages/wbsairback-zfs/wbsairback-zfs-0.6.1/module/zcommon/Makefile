MODULE := zcommon

EXTRA_CFLAGS = $(ZFS_MODULE_CFLAGS)   -DHAVE_SPL -D_KERNEL -DTEXT_DOMAIN=\"zfs-linux-kernel\" -DNDEBUG 

obj-$(CONFIG_ZFS) := $(MODULE).o

$(MODULE)-objs += ../../module/zcommon/zfs_deleg.o
$(MODULE)-objs += ../../module/zcommon/zfs_prop.o
$(MODULE)-objs += ../../module/zcommon/zprop_common.o
$(MODULE)-objs += ../../module/zcommon/zfs_namecheck.o
$(MODULE)-objs += ../../module/zcommon/zfs_comutil.o
$(MODULE)-objs += ../../module/zcommon/zfs_fletcher.o
$(MODULE)-objs += ../../module/zcommon/zfs_uio.o
$(MODULE)-objs += ../../module/zcommon/zpool_prop.o

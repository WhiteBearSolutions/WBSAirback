MODULE := znvpair

EXTRA_CFLAGS = $(ZFS_MODULE_CFLAGS)   -DHAVE_SPL -D_KERNEL -DTEXT_DOMAIN=\"zfs-linux-kernel\" -DNDEBUG 

obj-$(CONFIG_ZFS) := $(MODULE).o

$(MODULE)-objs += ../../module/nvpair/nvpair.o
$(MODULE)-objs += ../../module/nvpair/fnvpair.o
$(MODULE)-objs += ../../module/nvpair/nvpair_alloc_spl.o
$(MODULE)-objs += ../../module/nvpair/nvpair_alloc_fixed.o

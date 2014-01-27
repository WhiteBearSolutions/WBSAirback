MODULE := zunicode

EXTRA_CFLAGS = $(ZFS_MODULE_CFLAGS)   -DHAVE_SPL -D_KERNEL -DTEXT_DOMAIN=\"zfs-linux-kernel\" -DNDEBUG 

obj-$(CONFIG_ZFS) := $(MODULE).o

$(MODULE)-objs += ../../module/unicode/u8_textprep.o
$(MODULE)-objs += ../../module/unicode/uconv.o

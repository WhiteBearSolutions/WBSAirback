# Makefile.in for splat kernel module

MODULE := splat
EXTRA_CFLAGS = $(SPL_MODULE_CFLAGS)  -DHAVE_GPL_ONLY_SYMBOLS -Wstrict-prototypes -DNDEBUG -DDEBUG_LOG -DDEBUG_KMEM

# Solaris Porting LAyer Tests
obj-$(CONFIG_SPL) := $(MODULE).o

$(MODULE)-objs += ../../module/splat/splat-ctl.o
$(MODULE)-objs += ../../module/splat/splat-kmem.o
$(MODULE)-objs += ../../module/splat/splat-taskq.o
$(MODULE)-objs += ../../module/splat/splat-random.o
$(MODULE)-objs += ../../module/splat/splat-mutex.o
$(MODULE)-objs += ../../module/splat/splat-condvar.o
$(MODULE)-objs += ../../module/splat/splat-thread.o
$(MODULE)-objs += ../../module/splat/splat-rwlock.o
$(MODULE)-objs += ../../module/splat/splat-time.o
$(MODULE)-objs += ../../module/splat/splat-vnode.o
$(MODULE)-objs += ../../module/splat/splat-kobj.o
$(MODULE)-objs += ../../module/splat/splat-atomic.o
$(MODULE)-objs += ../../module/splat/splat-list.o
$(MODULE)-objs += ../../module/splat/splat-generic.o
$(MODULE)-objs += ../../module/splat/splat-cred.o
$(MODULE)-objs += ../../module/splat/splat-zlib.o
$(MODULE)-objs += ../../module/splat/splat-linux.o

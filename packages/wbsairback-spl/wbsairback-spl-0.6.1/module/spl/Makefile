# Makefile.in for spl kernel module

MODULE := spl
EXTRA_CFLAGS = $(SPL_MODULE_CFLAGS)  -DHAVE_GPL_ONLY_SYMBOLS -Wstrict-prototypes -DNDEBUG -DDEBUG_LOG -DDEBUG_KMEM

# Solaris porting layer module
obj-$(CONFIG_SPL) := $(MODULE).o

$(MODULE)-objs += ../../module/spl/spl-debug.o
$(MODULE)-objs += ../../module/spl/spl-proc.o
$(MODULE)-objs += ../../module/spl/spl-kmem.o
$(MODULE)-objs += ../../module/spl/spl-thread.o
$(MODULE)-objs += ../../module/spl/spl-taskq.o
$(MODULE)-objs += ../../module/spl/spl-rwlock.o
$(MODULE)-objs += ../../module/spl/spl-vnode.o
$(MODULE)-objs += ../../module/spl/spl-err.o
$(MODULE)-objs += ../../module/spl/spl-time.o
$(MODULE)-objs += ../../module/spl/spl-kobj.o
$(MODULE)-objs += ../../module/spl/spl-generic.o
$(MODULE)-objs += ../../module/spl/spl-atomic.o
$(MODULE)-objs += ../../module/spl/spl-mutex.o
$(MODULE)-objs += ../../module/spl/spl-kstat.o
$(MODULE)-objs += ../../module/spl/spl-condvar.o
$(MODULE)-objs += ../../module/spl/spl-xdr.o
$(MODULE)-objs += ../../module/spl/spl-cred.o
$(MODULE)-objs += ../../module/spl/spl-tsd.o
$(MODULE)-objs += ../../module/spl/spl-zlib.o

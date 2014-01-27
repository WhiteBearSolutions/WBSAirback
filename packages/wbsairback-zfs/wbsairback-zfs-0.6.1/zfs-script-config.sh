#!/bin/bash

KERNELSRC=/opt/git-clone/wbsairback/packages/wbsairback-zfs/wbsairback-zfs-0.6.1/../../wbsairback-kernel-image/wbsairback-kernel-image-3.2.43
KERNELBUILD=/opt/git-clone/wbsairback/packages/wbsairback-zfs/wbsairback-zfs-0.6.1/../../wbsairback-kernel-image/wbsairback-kernel-image-3.2.43
KERNELSRCVER=3.2.43-wbsairback
KERNELMOD=/lib/modules/${KERNELSRCVER}/kernel

SPLSRC=/opt/git-clone/wbsairback/packages/wbsairback-zfs/wbsairback-zfs-0.6.1/../../wbsairback-spl/wbsairback-spl-0.6.1
SPLBUILD=/opt/git-clone/wbsairback/packages/wbsairback-zfs/wbsairback-zfs-0.6.1/../../wbsairback-spl/wbsairback-spl-0.6.1
SPLSRCVER=0.6.1-1

SRCDIR=/opt/git-clone/wbsairback/packages/wbsairback-zfs/wbsairback-zfs-0.6.1
BUILDDIR=/opt/git-clone/wbsairback/packages/wbsairback-zfs/wbsairback-zfs-0.6.1
LIBDIR=${BUILDDIR}/lib
CMDDIR=${BUILDDIR}/cmd
MODDIR=${BUILDDIR}/module
SCRIPTDIR=${BUILDDIR}/scripts
ZPOOLDIR=${BUILDDIR}/scripts/zpool-config
ZPIOSDIR=${BUILDDIR}/scripts/zpios-test
ZPIOSPROFILEDIR=${BUILDDIR}/scripts/zpios-profile
ETCDIR=${SRCDIR}/etc

ZDB=${CMDDIR}/zdb/zdb
ZFS=${CMDDIR}/zfs/zfs
ZINJECT=${CMDDIR}/zinject/zinject
ZPOOL=${CMDDIR}/zpool/zpool
ZTEST=${CMDDIR}/ztest/ztest
ZPIOS=${CMDDIR}/zpios/zpios

COMMON_SH=${SCRIPTDIR}/common.sh
ZFS_SH=${SCRIPTDIR}/zfs.sh
ZPOOL_CREATE_SH=${SCRIPTDIR}/zpool-create.sh
ZPIOS_SH=${SCRIPTDIR}/zpios.sh
ZPIOS_SURVEY_SH=${SCRIPTDIR}/zpios-survey.sh

INTREE=1
LDMOD=/sbin/insmod

KERNEL_MODULES=(                                      \
        ${KERNELMOD}/lib/zlib_deflate/zlib_deflate.ko \
        ${KERNELMOD}/lib/zlib_inflate/zlib_inflate.ko \
)

SPL_MODULES=(                                         \
        ${SPLBUILD}/module/spl/spl.ko                 \
        ${SPLBUILD}/module/splat/splat.ko             \
)

ZFS_MODULES=(                                         \
        ${MODDIR}/avl/zavl.ko                         \
        ${MODDIR}/nvpair/znvpair.ko                   \
        ${MODDIR}/unicode/zunicode.ko                 \
        ${MODDIR}/zcommon/zcommon.ko                  \
        ${MODDIR}/zfs/zfs.ko                          \
)

ZPIOS_MODULES=(                                       \
        ${MODDIR}/zpios/zpios.ko                      \
)

MODULES=(                                             \
        ${SPL_MODULES[*]}                             \
        ${ZFS_MODULES[*]}                             \
)

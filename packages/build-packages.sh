#!/bin/bash
if [ -z $CONFIG ] ; then
  CONFIG="/opt/git-repository/wbsairback/config.sh"
  export CONFIG=$CONFIG
fi

if ! [ -x $CONFIG ] ; then
  echo "No se encuentra el fichero $CONFIG"
  exit 1
fi

. $CONFIG

LOG_FILE=$PACKAGES_DIR/build-packages.log
cd $PACKAGES_DIR

build_all() {
  $PRODUCT_DIR/update-classes.sh

  cat /dev/null > $LOG_FILE

  echo -n "Actualizando aplicaciones: " 
  /bin/cp $WBSGO/wbs-api.jar $PACKAGES_DIR/wbsairback-admin/wbsairback-admin-1.1/var/www/webadministration/WEB-INF/lib/ >> $LOG_FILE || echo "[error]" && echo "[hecho]"
  
  PACKAGES=$(ls -Ad $PACKAGES_DIR/*)
  for PACK in $PACKAGES; do
     if [ -d $PACK ]; then
       PACKAGE_VERSIONS=$(ls -Ad $PACK/${PACK##*\/}*)
       for VERSION in $PACKAGE_VERSIONS; do
         PACKAGE_NAME=${VERSION##*\/}
         PACKAGE_NAME=${PACKAGE_NAME%%1.1*}
         PACKAGE_NAME=${PACKAGE_NAME%-*}
         if [[ ${VERSION##*\/} == wbsairback-kernel-image* ]] ; then
           if [ -z $1 ] ; then
             continue
           fi
         fi
         if [[ ${VERSION##*\/} == wbsairback-rsync* ]] ; then
           if [ -z $1 ] ; then
             continue
           fi
         fi
         if [[ ${VERSION##*\/} == wbsairback-winexe* ]] ; then
           if [ -z $1 ] ; then
             continue
           fi
         fi
         if [[ ${VERSION##*\/} == wbsairback-btrfs-tools* ]] ; then
           if [ -z $1 ] ; then
             continue
           fi
         fi
         if [[ ${VERSION##*\/} == wbsairback-iscsi-scst* ]] ; then
           if [ -z $1 ] ; then
             continue
           fi
         fi
         if [[ ${VERSION##*\/} == wbsairback-mail-transport* ]] ; then
           if [ -z $1 ] ; then
             continue
           fi
         fi
         if [[ ${VERSION##*\/} == wbsairback-parted* ]] ; then
           if [ -z $1 ] ; then
             continue
           fi
         fi
         if [[ ${VERSION##*\/} == wbsairback-cdp* ]] ; then
           if [ -z $1 ] ; then
             continue
           fi
         fi
         if [[ ${VERSION##*\/} == wbsairback-plymouth* ]] ; then
           if [ -z $1 ] ; then
             continue
           fi
         fi
         if [[ ${VERSION##*\/} == wbsairback-lessfs* ]] ; then
           if [ -z $1 ] ; then
             continue
           fi
         fi
         if [[ ${VERSION##*\/} == wbsairback-mhvtl* ]] ; then
           if [ -z $1 ] ; then
             continue
           fi
         fi
         if [[ ${VERSION##*\/} == wbsairback-lvm2* ]] ; then
           if [ -z $1 ] ; then
             continue
           fi
         fi
         if [[ ${VERSION##*\/} == wbsairback-tomcat* ]] ; then
           if [ -z $1 ] ; then
             continue
           fi
         fi
         if [[ ${VERSION##*\/} == wbsairback-java* ]] ; then
           if [ -z $1 ] ; then
             continue
           fi
         fi
         if [[ -d $VERSION ]]; then
	   echo -n "Borrando paquete anterior [$PACKAGE_NAME]: "
           rm $PRODUCT_REPOSITORY/dists/1.2/main/binary-amd64/$PACKAGE_NAME*.deb || echo "[error]" && echo "[hecho]"
           echo -n "Generando [${VERSION##*\/}]: "
           cd $VERSION
           rm ../$PACKAGE_NAME*.deb || echo -n ""
           if [[ ! -d debian/$VERSION ]]; then
             mkdir debian/$VERSION
           else
             rm -fr debian/$VERSION/*
           fi
           tar --exclude=debian -cvO * | gzip > ../${VERSION##*\/}.tar.gz && dpkg-buildpackage -rfakeroot -b -uc >> $LOG_FILE || echo "[error]" && echo "[hecho]"
           echo -n "Copiando paquetes de [${PACK##*\/}]: "
           cp $PACK/*.deb $PRODUCT_REPOSITORY/dists/1.2/main/binary-amd64/ >> $LOG_FILE || echo "[error]" && echo "[hecho]"
         fi
       done
     fi
  done
}

if [ -z $1 ] ; then
  build_all
else
  if [ "$1" == "build-all" ] ; then
    build_all
  fi
fi

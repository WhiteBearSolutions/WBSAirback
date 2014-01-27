#!/bin/sh

# Cada 5 minutos se recolectan datos
# Cada 30 minutos se generan las graficas

dir=/var/www/webadministration/graphics/

export LANG=en_US
export LC_ALL=en_US

DELAY=300
TIMES="0"

cd /etc/wbsairback-admin/rrd/scripts/

while [ 1 ] ; do
  find /etc/wbsairback-admin/rrd/scripts/ -type f -name '*recollect.sh' -exec sh {} \;
  sleep $DELAY
  TIMES=`expr $TIMES + 1`

  if [ ! -e $dir ]; then
    mkdir -p $dir
    find /etc/wbsairback-admin/rrd/scripts/ -type f -name '*graph.sh' -exec sh {} \;
  fi

  if [ $TIMES = "6" ]; then
    TIMES="0"
    find /etc/wbsairback-admin/rrd/scripts/ -type f -name '*graph.sh' -exec sh {} \;
  fi
done


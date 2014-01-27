#!/bin/bash

dir="../db"

if [ ! -e $dir ]; then
  mkdir $dir
fi

file="../db/diskspace.rrd"

if [ ! -e $file ]; then
  rrdtool create $file \
  --start N --step 300 \
  DS:used:GAUGE:600:0:1000 \
  RRA:AVERAGE:0.5:1:1440 \
  RRA:MIN:0.5:1:1440 \
  RRA:MAX:0.5:1:1440 \
  RRA:AVERAGE:0.5:12:1440 \
  RRA:MIN:0.5:12:1440 \
  RRA:MAX:0.5:12:1440 \
  RRA:AVERAGE:0.5:288:3650 \
  RRA:MIN:0.5:288:3650 \
  RRA:MAX:0.5:288:3650
fi

used=`/bin/df -Pm | grep "/$" | sed s/\ \ */\ /g | cut -d" " -f3`
used=0`echo "scale=2; $used / 1024" | bc -l`

/usr/bin/rrdtool update $file "N:$used"

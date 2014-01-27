#!/bin/bash

dir="../db"

if [ ! -e $dir ]; then
  mkdir $dir
fi

file="../db/uptime.rrd"

if [ ! -e $file ]; then
  rrdtool create $file \
  --start N --step 300 \
  DS:uptime:GAUGE:600:0:10000 \
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

up=`cut -f1 -d' ' /proc/uptime | cut -f1 -d.`
up=`expr $up / 60 / 60 / 24`

/usr/bin/rrdtool update $file "N:$up"

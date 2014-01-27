#!/bin/bash

dir="../db"

if [ ! -e $dir ]; then
  mkdir $dir
fi

file="../db/mem.rrd"

if [ ! -e $file ]; then
  rrdtool create $file \
  --start N --step 300 \
  DS:used:GAUGE:600:0:10000 \
  DS:swap:GAUGE:600:0:10000 \
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

used=`top -b -d3 -n2  | grep "Mem" | tail -n1 | cut -d ":" -f 2 | tr -d " " | cut -d "," -f 2 | tr -d kused`
swap=`top -b -d3 -n2  | grep "Swap" | tail -n1 | cut -d ":" -f 2 | tr -d " " | cut -d "," -f 2 | tr -d kused`
cached=`cat /proc/meminfo | grep -w Cached | awk '{print $2}'`
buffers=`cat /proc/meminfo | grep -w Buffers | awk '{print $2}'`
used=`echo "scale=2; ($used-$cached-$buffers) / 1024 / 1024" | bc -l`
swap=`echo "scale=2; $swap / 1024 / 1024" | bc -l`

/usr/bin/rrdtool update $file "N:$used:$swap"


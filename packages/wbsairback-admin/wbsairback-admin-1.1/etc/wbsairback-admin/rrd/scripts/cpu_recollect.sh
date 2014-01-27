#!/bin/bash

dir="../db"

if [ ! -e $dir ]; then
  mkdir $dir
fi

file="../db/cpu.rrd"

if [ ! -e $file ]; then
  rrdtool create $file \
  --start N --step 300 \
  DS:user:GAUGE:600:0:100 \
  DS:sys:GAUGE:600:0:100 \
  DS:nice:GAUGE:600:0:100 \
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

cpu=`top -b -d3 -n2  | grep "Cpu" | tail -n1 | cut -d ":" -f 2 | tr -d " "`
user=`echo $cpu | cut -d "," -f1 | tr -d "%us"`
sys=`echo $cpu | cut -d "," -f2 | tr -d "%sy"`
nice=`echo $cpu | cut -d "," -f3 | tr -d "%ni"`

/usr/bin/rrdtool update $file "N:$user:$sys:$nice"

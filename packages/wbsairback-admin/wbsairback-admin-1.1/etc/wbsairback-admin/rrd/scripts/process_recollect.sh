#!/bin/bash

dir="../db"

if [ ! -e $dir ]; then
  mkdir $dir
fi

file="../db/process.rrd"

if [ ! -e $file ]; then
  rrdtool create $file \
  --start N --step 300 \
  DS:all:GAUGE:600:0:10000 \
  DS:run:GAUGE:600:0:10000 \
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

proc=`top -b -d3 -n2  | grep "Tasks" | tail -n1 | cut -d ":" -f 2 | tr -d " "`
all=`echo $proc | cut -d "," -f 1 | tr -d total`
run=`echo $proc | cut -d "," -f 2 | tr -d running`

/usr/bin/rrdtool update $file "N:$all:$run"


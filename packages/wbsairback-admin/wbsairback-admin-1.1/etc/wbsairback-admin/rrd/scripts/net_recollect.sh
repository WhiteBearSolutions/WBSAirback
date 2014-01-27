#!/bin/bash

dir="../db"

if [ ! -e $dir ]; then
  mkdir $dir
fi

if_file=interfaces.txt

if [ ! -e $if_file ]; then
  ifconfig -a | grep eth | tr -s " " | cut -d " " -f 1 > $if_file
fi

interfaces=$(cat $if_file)

for if in $interfaces; do
  file=../db/$if.rrd
  if [ ! -e $file ]; then
    rrdtool create $file \
    --start N --step 300 \
    DS:input:DERIVE:600:0:U \
    DS:output:DERIVE:600:0:U \
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
  index=$(snmpwalk -Os -c public -v 1 localhost ifDescr 2> /dev/null | grep $if | cut -d " " -f 1 | tr -d ifDescr.)
  In=$(snmpget -v 1 -c public -Oqv localhost IF-MIB::ifInOctets.$index 2> /dev/null)
  Out=$(snmpget -v 1 -c public -Oqv localhost IF-MIB::ifOutOctets.$index 2> /dev/null)
  /usr/bin/rrdtool update $file N:$In:$Out
done

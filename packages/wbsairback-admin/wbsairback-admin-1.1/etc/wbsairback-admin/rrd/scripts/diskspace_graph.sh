#!/bin/bash

export LANG=en_US
export LC_ALL=en_US

dir=/var/www/webadministration/graphics/
units=G
cap=`/bin/df -Pm | grep "/$" | sed s/\ \ */\ /g | cut -d" " -f2`
cap=`echo "scale=2; $cap / 1024" | bc -l`
cap75=`echo $cap \* 0.75 | bc `

/usr/bin/rrdtool graph "$dir"diskspace_daily.png -a PNG --title="Uso de Disco en particion raiz" \
--vertical-label "Uso ("$units"B)" \
--lower-limit 0 --start "-1day" -w 600 -h 200 \
'DEF:used=../db/diskspace.rrd:used:AVERAGE' \
'LINE1:'$cap'#000000:Capacidad '$cap' GB\j' \
'LINE3:'$cap75'#FF0000:75% Capacidad '$cap75' GB\j' \
'AREA:used#444444:Usada' \
'GPRINT:used:LAST: %5.2lf GB' \
'LINE3:'$cap75'#FF0000:'

/usr/bin/rrdtool graph "$dir"diskspace_hour.png -a PNG --title="Uso de Disco en particion raiz" \
--vertical-label "Uso ("$units"B)" \
--lower-limit 0 --start "-6hour" -w 600 -h 200 \
'DEF:used=../db/diskspace.rrd:used:AVERAGE' \
'LINE1:'$cap'#000000:Capacidad '$cap' GB\j' \
'LINE3:'$cap75'#FF0000:75% Capacidad '$cap75' GB\j' \
'AREA:used#444444:Usada' \
'GPRINT:used:LAST: %5.2lf GB' \
'LINE3:'$cap75'#FF0000:'

/usr/bin/rrdtool graph "$dir"diskspace_week.png -a PNG --title="Uso de Disco en particion raiz" \
--vertical-label "Uso ("$units"B)" \
--lower-limit 0 --start "-1week" -w 600 -h 200 \
'DEF:used=../db/diskspace.rrd:used:AVERAGE' \
'LINE1:'$cap'#000000:Capacidad '$cap' GB\j' \
'LINE3:'$cap75'#FF0000:75% Capacidad '$cap75' GB\j' \
'AREA:used#444444:Usada' \
'GPRINT:used:LAST: %5.2lf GB' \
'LINE3:'$cap75'#FF0000:'

/usr/bin/rrdtool graph "$dir"diskspace_month.png -a PNG --title="Uso de Disco en particion raiz" \
--vertical-label "Uso ("$units"B)" \
--lower-limit 0 --start "-1month" -w 600 -h 200 \
'DEF:used=../db/diskspace.rrd:used:AVERAGE' \
'LINE1:'$cap'#000000:Capacidad '$cap' GB\j' \
'LINE3:'$cap75'#FF0000:75% Capacidad '$cap75' GB\j' \
'AREA:used#444444:Usada' \
'GPRINT:used:LAST: %5.2lf GB' \
'LINE3:'$cap75'#FF0000:'

/usr/bin/rrdtool graph "$dir"diskspace_year.png -a PNG --title="Uso de Disco en particion raiz" \
--vertical-label "Uso ("$units"B)" \
--lower-limit 0 --start "-1year" -w 600 -h 200 \
'DEF:used=../db/diskspace.rrd:used:AVERAGE' \
'LINE1:'$cap'#000000:Capacidad '$cap' GB\j' \
'LINE3:'$cap75'#FF0000:75% Capacidad '$cap75' GB\j' \
'AREA:used#444444:Usada' \
'GPRINT:used:LAST: %5.2lf GB' \
'LINE3:'$cap75'#FF0000:'

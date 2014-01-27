#!/bin/bash

dir=/var/www/webadministration/graphics/

/usr/bin/rrdtool graph "$dir"cpu_daily.png -a PNG --title="Uso de CPU" \
--upper-limit 100 --lower-limit 0 --rigid \
--vertical-label "Uso (%)" -w 600 -h 200 \
--start "-1day" \
'DEF:user=../db/cpu.rrd:user:AVERAGE' \
'DEF:sys=../db/cpu.rrd:sys:AVERAGE' \
'DEF:nice=../db/cpu.rrd:nice:AVERAGE' \
'CDEF:sum=user,sys,nice,+,+' \
'AREA:user#EACC00:Usuario:STACK' \
'GPRINT:user:LAST:Actual \: %5.2lf%%' \
'GPRINT:user:AVERAGE:Medio \: %5.2lf%%' \
'GPRINT:user:MAX:Maximo \: %5.2lf%%\j' \
'AREA:sys#EA8F00:Sistema:STACK' \
'GPRINT:sys:LAST:Actual \: %5.2lf%%' \
'GPRINT:sys:AVERAGE:Medio \: %5.2lf%%' \
'GPRINT:sys:MAX:Maximo \: %5.2lf%%\j' \
'AREA:nice#FF0000:Nice' \
'GPRINT:nice:LAST:Actual \: %5.2lf%%' \
'GPRINT:nice:AVERAGE:Medio \: %5.2lf%%' \
'GPRINT:nice:MAX:Maximo \: %5.2lf%%\j' \
'LINE1:sum#000000'

/usr/bin/rrdtool graph "$dir"cpu_hour.png -a PNG --title="Uso de CPU" \
--upper-limit 100 --lower-limit 0 --rigid \
--vertical-label "Uso (%)" -w 600 -h 200 \
--start "-6hour" \
'DEF:user=../db/cpu.rrd:user:AVERAGE' \
'DEF:sys=../db/cpu.rrd:sys:AVERAGE' \
'DEF:nice=../db/cpu.rrd:nice:AVERAGE' \
'CDEF:sum=user,sys,nice,+,+' \
'AREA:user#EACC00:Usuario:STACK' \
'GPRINT:user:LAST:Actual \: %5.2lf%%' \
'GPRINT:user:AVERAGE:Medio \: %5.2lf%%' \
'GPRINT:user:MAX:Maximo \: %5.2lf%%\j' \
'AREA:sys#EA8F00:Sistema:STACK' \
'GPRINT:sys:LAST:Actual \: %5.2lf%%' \
'GPRINT:sys:AVERAGE:Medio \: %5.2lf%%' \
'GPRINT:sys:MAX:Maximo \: %5.2lf%%\j' \
'AREA:nice#FF0000:Nice' \
'GPRINT:nice:LAST:Actual \: %5.2lf%%' \
'GPRINT:nice:AVERAGE:Medio \: %5.2lf%%' \
'GPRINT:nice:MAX:Maximo \: %5.2lf%%\j' \
'LINE1:sum#000000'

/usr/bin/rrdtool graph "$dir"cpu_week.png -a PNG --title="Uso de CPU" \
--upper-limit 100 --lower-limit 0 --rigid \
--vertical-label "Uso (%)" -w 600 -h 200 \
--start "-1week" \
'DEF:user=../db/cpu.rrd:user:AVERAGE' \
'DEF:sys=../db/cpu.rrd:sys:AVERAGE' \
'DEF:nice=../db/cpu.rrd:nice:AVERAGE' \
'CDEF:sum=user,sys,nice,+,+' \
'AREA:user#EACC00:Usuario:STACK' \
'GPRINT:user:LAST:Actual \: %5.2lf%%' \
'GPRINT:user:AVERAGE:Medio \: %5.2lf%%' \
'GPRINT:user:MAX:Maximo \: %5.2lf%%\j' \
'AREA:sys#EA8F00:Sistema:STACK' \
'GPRINT:sys:LAST:Actual \: %5.2lf%%' \
'GPRINT:sys:AVERAGE:Medio \: %5.2lf%%' \
'GPRINT:sys:MAX:Maximo \: %5.2lf%%\j' \
'AREA:nice#FF0000:Nice' \
'GPRINT:nice:LAST:Actual \: %5.2lf%%' \
'GPRINT:nice:AVERAGE:Medio \: %5.2lf%%' \
'GPRINT:nice:MAX:Maximo \: %5.2lf%%\j' \
'LINE1:sum#000000'

/usr/bin/rrdtool graph "$dir"cpu_month.png -a PNG --title="Uso de CPU" \
--upper-limit 100 --lower-limit 0 --rigid \
--vertical-label "Uso (%)" -w 600 -h 200 \
--start "-1month" \
'DEF:user=../db/cpu.rrd:user:AVERAGE' \
'DEF:sys=../db/cpu.rrd:sys:AVERAGE' \
'DEF:nice=../db/cpu.rrd:nice:AVERAGE' \
'CDEF:sum=user,sys,nice,+,+' \
'AREA:user#EACC00:Usuario:STACK' \
'GPRINT:user:LAST:Actual \: %5.2lf%%' \
'GPRINT:user:AVERAGE:Medio \: %5.2lf%%' \
'GPRINT:user:MAX:Maximo \: %5.2lf%%\j' \
'AREA:sys#EA8F00:Sistema:STACK' \
'GPRINT:sys:LAST:Actual \: %5.2lf%%' \
'GPRINT:sys:AVERAGE:Medio \: %5.2lf%%' \
'GPRINT:sys:MAX:Maximo \: %5.2lf%%\j' \
'AREA:nice#FF0000:Nice' \
'GPRINT:nice:LAST:Actual \: %5.2lf%%' \
'GPRINT:nice:AVERAGE:Medio \: %5.2lf%%' \
'GPRINT:nice:MAX:Maximo \: %5.2lf%%\j' \
'LINE1:sum#000000'

/usr/bin/rrdtool graph "$dir"cpu_year.png -a PNG --title="Uso de CPU" \
--upper-limit 100 --lower-limit 0 --rigid \
--vertical-label "Uso (%)" -w 600 -h 200 \
--start "-1year" \
'DEF:user=../db/cpu.rrd:user:AVERAGE' \
'DEF:sys=../db/cpu.rrd:sys:AVERAGE' \
'DEF:nice=../db/cpu.rrd:nice:AVERAGE' \
'CDEF:sum=user,sys,nice,+,+' \
'AREA:user#EACC00:Usuario:STACK' \
'GPRINT:user:LAST:Actual \: %5.2lf%%' \
'GPRINT:user:AVERAGE:Medio \: %5.2lf%%' \
'GPRINT:user:MAX:Maximo \: %5.2lf%%\j' \
'AREA:sys#EA8F00:Sistema:STACK' \
'GPRINT:sys:LAST:Actual \: %5.2lf%%' \
'GPRINT:sys:AVERAGE:Medio \: %5.2lf%%' \
'GPRINT:sys:MAX:Maximo \: %5.2lf%%\j' \
'AREA:nice#FF0000:Nice' \
'GPRINT:nice:LAST:Actual \: %5.2lf%%' \
'GPRINT:nice:AVERAGE:Medio \: %5.2lf%%' \
'GPRINT:nice:MAX:Maximo \: %5.2lf%%\j' \
'LINE1:sum#000000'


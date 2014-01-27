#!/bin/bash

dir=/var/www/webadministration/graphics/


file=interfaces.txt
interfaces=$(cat $file)

for if in $interfaces; do

/usr/bin/rrdtool graph "$dir""$if"_daily.png -a PNG --title="Trafico de $if" \
--rigid --vertical-label "Kbytes/sec" \
--start "-1day"  -w 600 -h 200 \
'DEF:in=../db/'$if'.rrd:input:AVERAGE' \
'DEF:out=../db/'$if'.rrd:output:AVERAGE' \
'CDEF:ikb=in,1024,/' \
'CDEF:okb_graph=out,-1024,/' \
'CDEF:okb=out,1024,/' \
'AREA:ikb#8DCD89:Entrada:STACK' \
'GPRINT:ikb:LAST:Actual \: %5.2lf KB/s' \
'GPRINT:ikb:AVERAGE:Medio \: %5.2lf KB/s' \
'GPRINT:ikb:MAX:Maximo \: %5.2lf KB/s\j' \
'LINE1:ikb#000000' \
'AREA:okb_graph#4568E4:Salida' \
'GPRINT:okb:LAST:Actual \: %5.2lf KB/s' \
'GPRINT:okb:AVERAGE:Medio \: %5.2lf KB/s' \
'GPRINT:okb:MAX:Maximo \: %5.2lf KB/s\j' \
'LINE1:okb_graph#000000'

/usr/bin/rrdtool graph "$dir""$if"_hour.png -a PNG --title="Trafico de $if" \
--rigid --vertical-label "Kbytes/sec" \
--start "-6hour" -w 600 -h 200 \
'DEF:in=../db/'$if'.rrd:input:AVERAGE' \
'DEF:out=../db/'$if'.rrd:output:AVERAGE' \
'CDEF:ikb=in,1024,/' \
'CDEF:okb_graph=out,-1024,/' \
'CDEF:okb=out,1024,/' \
'AREA:ikb#8DCD89:Entrada:STACK' \
'GPRINT:ikb:LAST:Actual \: %5.2lf KB/s' \
'GPRINT:ikb:AVERAGE:Medio \: %5.2lf KB/s' \
'GPRINT:ikb:MAX:Maximo \: %5.2lf KB/s\j' \
'LINE1:ikb#000000' \
'AREA:okb_graph#4568E4:Salida' \
'GPRINT:okb:LAST:Actual \: %5.2lf KB/s' \
'GPRINT:okb:AVERAGE:Medio \: %5.2lf KB/s' \
'GPRINT:okb:MAX:Maximo \: %5.2lf KB/s\j' \
'LINE1:okb_graph#000000'


/usr/bin/rrdtool graph "$dir""$if"_week.png -a PNG --title="Trafico de $if" \
--rigid --vertical-label "Kbytes/sec" \
--start "-1week" -w 600 -h 200 \
'DEF:in=../db/'$if'.rrd:input:AVERAGE' \
'DEF:out=../db/'$if'.rrd:output:AVERAGE' \
'CDEF:ikb=in,1024,/' \
'CDEF:okb_graph=out,-1024,/' \
'CDEF:okb=out,1024,/' \
'AREA:ikb#8DCD89:Entrada:STACK' \
'GPRINT:ikb:LAST:Actual \: %5.2lf KB/s' \
'GPRINT:ikb:AVERAGE:Medio \: %5.2lf KB/s' \
'GPRINT:ikb:MAX:Maximo \: %5.2lf KB/s\j' \
'LINE1:ikb#000000' \
'AREA:okb_graph#4568E4:Salida' \
'GPRINT:okb:LAST:Actual \: %5.2lf KB/s' \
'GPRINT:okb:AVERAGE:Medio \: %5.2lf KB/s' \
'GPRINT:okb:MAX:Maximo \: %5.2lf KB/s\j' \
'LINE1:okb_graph#000000'


/usr/bin/rrdtool graph "$dir""$if"_month.png -a PNG --title="Trafico de $if" \
--rigid --vertical-label "Kbytes/sec" \
--start "-1month" -w 600 -h 200 \
'DEF:in=../db/'$if'.rrd:input:AVERAGE' \
'DEF:out=../db/'$if'.rrd:output:AVERAGE' \
'CDEF:ikb=in,1024,/' \
'CDEF:okb_graph=out,-1024,/' \
'CDEF:okb=out,1024,/' \
'AREA:ikb#8DCD89:Entrada:STACK' \
'GPRINT:ikb:LAST:Actual \: %5.2lf KB/s' \
'GPRINT:ikb:AVERAGE:Medio \: %5.2lf KB/s' \
'GPRINT:ikb:MAX:Maximo \: %5.2lf KB/s\j' \
'LINE1:ikb#000000' \
'AREA:okb_graph#4568E4:Salida' \
'GPRINT:okb:LAST:Actual \: %5.2lf KB/s' \
'GPRINT:okb:AVERAGE:Medio \: %5.2lf KB/s' \
'GPRINT:okb:MAX:Maximo \: %5.2lf KB/s\j' \
'LINE1:okb_graph#000000'


/usr/bin/rrdtool graph "$dir""$if"_year.png -a PNG --title="Trafico de $if" \
--rigid --vertical-label "Kbytes/sec" \
--start "-1year" -w 600 -h 200 \
'DEF:in=../db/'$if'.rrd:input:AVERAGE' \
'DEF:out=../db/'$if'.rrd:output:AVERAGE' \
'CDEF:ikb=in,1024,/' \
'CDEF:okb_graph=out,-1024,/' \
'CDEF:okb=out,1024,/' \
'AREA:ikb#8DCD89:Entrada:STACK' \
'GPRINT:ikb:LAST:Actual \: %5.2lf KB/s' \
'GPRINT:ikb:AVERAGE:Medio \: %5.2lf KB/s' \
'GPRINT:ikb:MAX:Maximo \: %5.2lf KB/s\j' \
'LINE1:ikb#000000' \
'AREA:okb_graph#4568E4:Salida' \
'GPRINT:okb:LAST:Actual \: %5.2lf KB/s' \
'GPRINT:okb:AVERAGE:Medio \: %5.2lf KB/s' \
'GPRINT:okb:MAX:Maximo \: %5.2lf KB/s\j' \
'LINE1:okb_graph#000000'

done

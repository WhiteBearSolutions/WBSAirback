#!/bin/bash

dir=/var/www/webadministration/graphics/

up=`cut -f1 -d' ' /proc/uptime | cut -f1 -d.`
up=`expr $up / 60 / 60 / 24`
up=`expr $up + 10`

/usr/bin/rrdtool graph "$dir"uptime_week.png -a PNG --title="Tiempo de Actividad" \
--vertical-label "dias" \
--upper-limit $up --lower-limit 0 --start "-1week" -w 600 -h 200 \
'DEF:uptime=../db/uptime.rrd:uptime:MAX' \
'AREA:uptime#444444:Dias' \
'GPRINT:uptime:LAST:%2.0lf'

/usr/bin/rrdtool graph "$dir"uptime_month.png -a PNG --title="Tiempo de Actividad" \
--vertical-label "dias" \
--upper-limit $up --lower-limit 0 --start "-1week" -w 600 -h 200 \
'DEF:uptime=../db/uptime.rrd:uptime:MAX' \
'AREA:uptime#444444:Dias' \
'GPRINT:uptime:LAST:%2.0lf'

/usr/bin/rrdtool graph "$dir"uptime_year.png -a PNG --title="Tiempo de Actividad" \
--vertical-label "dias" \
--upper-limit $up --lower-limit 0 --start "-1week" -w 600 -h 200 \
'DEF:uptime=../db/uptime.rrd:uptime:MAX' \
'AREA:uptime#444444:Dias' \
'GPRINT:uptime:LAST:%2.0lf'


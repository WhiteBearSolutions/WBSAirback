#!/bin/bash

dir=/var/www/webadministration/graphics/
proc=`top -b -d3 -n2  | grep "Tasks" | tail -n1 | cut -d ":" -f 2 | tr -d " " | cut -d "," -f 1 | tr -d total`
all=`expr $proc + 50`

/usr/bin/rrdtool graph "$dir"process_daily.png -a PNG --title="Numero de Procesos" \
--upper-limit $all --lower-limit 0 --rigid \
--vertical-label "Procesos" -w 600 -h 200 \
'DEF:all=../db/process.rrd:all:AVERAGE' \
'DEF:run=../db/process.rrd:run:AVERAGE' \
'AREA:all#F51C2F:Todos' \
'GPRINT:all:LAST:Actual \: %5.2lf' \
'GPRINT:all:AVERAGE:Medio \: %5.2lf' \
'GPRINT:all:MAX:Maximo \: %5.2lf\j' \
'AREA:run#002997:Ejecucion' \
'GPRINT:run:LAST:Actual \: %5.2lf' \
'GPRINT:run:AVERAGE:Medio \: %5.2lf' \
'GPRINT:run:MAX:Maximo \: %5.2lf\j' \
'LINE1:run#000000' \
'LINE1:all#000000'

/usr/bin/rrdtool graph "$dir"process_hour.png -a PNG --title="Numero de Procesos" \
--upper-limit $all --lower-limit 0 --rigid \
--vertical-label "Procesos" -w 600 -h 200 \
--start "-6hour" \
'DEF:all=../db/process.rrd:all:AVERAGE' \
'DEF:run=../db/process.rrd:run:AVERAGE' \
'AREA:all#F51C2F:Todos' \
'GPRINT:all:LAST:Actual \: %5.2lf' \
'GPRINT:all:AVERAGE:Medio \: %5.2lf' \
'GPRINT:all:MAX:Maximo \: %5.2lf\j' \
'AREA:run#002997:Ejecucion' \
'GPRINT:run:LAST:Actual \: %5.2lf' \
'GPRINT:run:AVERAGE:Medio \: %5.2lf' \
'GPRINT:run:MAX:Maximo \: %5.2lf\j' \
'LINE1:run#000000' \
'LINE1:all#000000'

/usr/bin/rrdtool graph "$dir"process_week.png -a PNG --title="Numero de Procesos" \
--upper-limit $all --lower-limit 0 --rigid \
--vertical-label "Procesos" -w 600 -h 200 \
--start "-1week" \
'DEF:all=../db/process.rrd:all:AVERAGE' \
'DEF:run=../db/process.rrd:run:AVERAGE' \
'AREA:all#F51C2F:Todos' \
'GPRINT:all:LAST:Actual \: %5.2lf' \
'GPRINT:all:AVERAGE:Medio \: %5.2lf' \
'GPRINT:all:MAX:Maximo \: %5.2lf\j' \
'AREA:run#002997:Ejecucion' \
'GPRINT:run:LAST:Actual \: %5.2lf' \
'GPRINT:run:AVERAGE:Medio \: %5.2lf' \
'GPRINT:run:MAX:Maximo \: %5.2lf\j' \
'LINE1:run#000000' \
'LINE1:all#000000'

/usr/bin/rrdtool graph "$dir"process_month.png -a PNG --title="Numero de Procesos" \
--upper-limit $all --lower-limit 0 --rigid \
--vertical-label "Procesos" -w 600 -h 200 \
--start "-1month" \
'DEF:all=../db/process.rrd:all:AVERAGE' \
'DEF:run=../db/process.rrd:run:AVERAGE' \
'AREA:all#F51C2F:Todos' \
'GPRINT:all:LAST:Actual \: %5.2lf' \
'GPRINT:all:AVERAGE:Medio \: %5.2lf' \
'GPRINT:all:MAX:Maximo \: %5.2lf\j' \
'AREA:run#002997:Ejecucion' \
'GPRINT:run:LAST:Actual \: %5.2lf' \
'GPRINT:run:AVERAGE:Medio \: %5.2lf' \
'GPRINT:run:MAX:Maximo \: %5.2lf\j' \
'LINE1:run#000000' \
'LINE1:all#000000'

/usr/bin/rrdtool graph "$dir"process_year.png -a PNG --title="Numero de Procesos" \
--upper-limit $all --lower-limit 0 --rigid \
--vertical-label "Procesos" -w 600 -h 200 \
--start "-1year" \
'DEF:all=../db/process.rrd:all:AVERAGE' \
'DEF:run=../db/process.rrd:run:AVERAGE' \
'AREA:all#F51C2F:Todos' \
'GPRINT:all:LAST:Actual \: %5.2lf' \
'GPRINT:all:AVERAGE:Medio \: %5.2lf' \
'GPRINT:all:MAX:Maximo \: %5.2lf\j' \
'AREA:run#002997:Ejecucion' \
'GPRINT:run:LAST:Actual \: %5.2lf' \
'GPRINT:run:AVERAGE:Medio \: %5.2lf' \
'GPRINT:run:MAX:Maximo \: %5.2lf\j' \
'LINE1:run#000000' \
'LINE1:all#000000'


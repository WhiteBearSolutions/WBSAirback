#!/bin/bash

dir=/var/www/webadministration/graphics/
total=`top -b -d3 -n2  | grep "Mem" | tail -n1 | cut -d ":" -f 2 | tr -d " " | cut -d "," -f 1 | tr -d ktotal`
if [ $? -eq 0 ]; then
  total=`echo "scale=2; $total / 1024 / 1024" | bc -l`
else
  total="1"
fi


/usr/bin/rrdtool graph "$dir"mem_daily.png -a PNG --title="Uso de Memoria" \
--upper-limit $total --lower-limit 0 --rigid \
--vertical-label "Uso (GB)" -w 600 -h 200 \
'DEF:used=../db/mem.rrd:used:AVERAGE' \
'DEF:swap=../db/mem.rrd:swap:AVERAGE' \
'AREA:used#F51C2F:Usada' \
'GPRINT:used:LAST:Actual \: %5.2lf GB' \
'GPRINT:used:AVERAGE:Medio \: %5.2lf GB' \
'GPRINT:used:MAX:Maximo \: %5.2lf GB\j' \
'AREA:swap#002997:Swap' \
'GPRINT:swap:LAST:Actual \: %5.2lf GB' \
'GPRINT:swap:AVERAGE:Medio \: %5.2lf GB' \
'GPRINT:swap:MAX:Maximo \: %5.2lf GB\j' \
'LINE1:swap#000000' \
'LINE1:used#000000'

/usr/bin/rrdtool graph "$dir"mem_hour.png -a PNG --title="Uso de Memoria" \
--upper-limit $total --lower-limit 0 --rigid \
--vertical-label "Uso (GB)" -w 600 -h 200 \
--start "-6hour" \
'DEF:used=../db/mem.rrd:used:AVERAGE' \
'DEF:swap=../db/mem.rrd:swap:AVERAGE' \
'AREA:used#F51C2F:Usada' \
'GPRINT:used:LAST:Actual \: %5.2lf GB' \
'GPRINT:used:AVERAGE:Medio \: %5.2lf GB' \
'GPRINT:used:MAX:Maximo \: %5.2lf GB\j' \
'AREA:swap#002997:Swap' \
'GPRINT:swap:LAST:Actual \: %5.2lf GB' \
'GPRINT:swap:AVERAGE:Medio \: %5.2lf GB' \
'GPRINT:swap:MAX:Maximo \: %5.2lf GB\j' \
'LINE1:swap#000000' \
'LINE1:used#000000'

/usr/bin/rrdtool graph "$dir"mem_week.png -a PNG --title="Uso de Memoria" \
--upper-limit $total --lower-limit 0 --rigid \
--vertical-label "Uso (GB)" -w 600 -h 200 \
--start "-1week" \
'DEF:used=../db/mem.rrd:used:AVERAGE' \
'DEF:swap=../db/mem.rrd:swap:AVERAGE' \
'AREA:used#F51C2F:Usada' \
'GPRINT:used:LAST:Actual \: %5.2lf GB' \
'GPRINT:used:AVERAGE:Medio \: %5.2lf GB' \
'GPRINT:used:MAX:Maximo \: %5.2lf GB\j' \
'AREA:swap#002997:Swap' \
'GPRINT:swap:LAST:Actual \: %5.2lf GB' \
'GPRINT:swap:AVERAGE:Medio \: %5.2lf GB' \
'GPRINT:swap:MAX:Maximo \: %5.2lf GB\j' \
'LINE1:swap#000000' \
'LINE1:used#000000'

/usr/bin/rrdtool graph "$dir"mem_month.png -a PNG --title="Uso de Memoria" \
--upper-limit $total --lower-limit 0 --rigid \
--vertical-label "Uso (GB)" -w 600 -h 200 \
--start "-1month" \
'DEF:used=../db/mem.rrd:used:AVERAGE' \
'DEF:swap=../db/mem.rrd:swap:AVERAGE' \
'AREA:used#F51C2F:Usada' \
'GPRINT:used:LAST:Actual \: %5.2lf GB' \
'GPRINT:used:AVERAGE:Medio \: %5.2lf GB' \
'GPRINT:used:MAX:Maximo \: %5.2lf  GB\j' \
'AREA:swap#002997:Swap' \
'GPRINT:swap:LAST:Actual \: %5.2lf GB' \
'GPRINT:swap:AVERAGE:Medio \: %5.2lf GB' \
'GPRINT:swap:MAX:Maximo \: %5.2lf GB\j' \
'LINE1:swap#000000' \
'LINE1:used#000000'

/usr/bin/rrdtool graph "$dir"mem_year.png -a PNG --title="Uso de Memoria" \
--upper-limit $total --lower-limit 0 --rigid \
--vertical-label "Uso (GB)" -w 600 -h 200 \
--start "-1year" \
'DEF:used=../db/mem.rrd:used:AVERAGE' \
'DEF:swap=../db/mem.rrd:swap:AVERAGE' \
'AREA:used#F51C2F:Usada' \
'GPRINT:used:LAST:Actual \: %5.2lf GB' \
'GPRINT:used:AVERAGE:Medio \: %5.2lf GB' \
'GPRINT:used:MAX:Maximo \: %5.2lf\j GB' \
'AREA:swap#002997:Swap' \
'GPRINT:swap:LAST:Actual \: %5.2lf GB' \
'GPRINT:swap:AVERAGE:Medio \: %5.2lf GB' \
'GPRINT:swap:MAX:Maximo \: %5.2lf GB\j' \
'LINE1:swap#000000' \
'LINE1:used#000000'


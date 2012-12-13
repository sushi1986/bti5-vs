#!/bin/bash
i=0
while [ $i -lt 20 ]
do
echo Starting TDMA Nr.: $i

java datasource.DataSource 4 $i 50 | java -cp bin/ Main 225.10.1.2 15004 &

sleep 3

i=`expr $i + 1`
done

sleep 30





ps ax | grep java | cut -f1 -s -d' ' > tmp

while read line
do
#value=`expr $value + 1`;
kill $line &
done < tmp

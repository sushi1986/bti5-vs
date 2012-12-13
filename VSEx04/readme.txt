TDMA mit Multicast
zum Starten des Programms 

<PATH TO DATASOURCE>/DataSource <TEAM NR> <STATION-NR> 50 | java -cp <PATH TO BIN FOLDER> Main <GROUP> <PORT>

oder

java -cp<PATH TO DATASOURCE> datasource.DataSource <TEAM NR> <STATION-NR> 50 | java -cp <PATH TO BIN FOLDER> Main <GROUP> <PORT>



Falls keine Route dann:

ip route add 224.0.0.0/4 dev eth0


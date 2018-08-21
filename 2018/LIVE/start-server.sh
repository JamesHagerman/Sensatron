#!/bin/bash

echo "Starting the Sensatron server..."
cd /mnt/CODE/Sensatron/2016/sensatron-server/
echo "Changed to: `pwd`"
echo "Running maven..."
export LD_LIBRARY_PATH="/usr/local/lib/"
mvn jetty:run >> ~pi/run.log &
#echo "Should be running inside screen. Use 'screen -r' to reconnect"


#!/usr/bin/env /bin/bash

# terror-turret-loader
python3 /etc/terror-turret/rpi-turret-manager.py --serial-port /dev/ttyUSB0
sleep 10
/etc/start-stream.sh
#!/usr/bin/env /bin/bash

# terror-turret-loader
python3 /etc/terror-turret/rpi-turret-manager.py --test-mode false --serial-port COM1
sleep 10
/etc/start-stream.sh
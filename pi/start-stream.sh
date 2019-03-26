#!/bin/sh

# This file contains a simple script to start the UV4L video stream

echo "Killing any existing UV4L stream stuff that may get in the way..."
sudo /usr/bin/pkill uv4l

# Give some time so that hopefully the process is dead before continuing
sleep 3

echo "Starting UV4L server..."
uv4l --driver uvc \
  --config-file "./uv4l-config.conf" \
  --driver-config-file "uv4l-config.conf"

echo -e "\nTurret stream *should* be up and running!\n"

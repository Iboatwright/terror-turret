#!/usr/bin/env bash

# This script sets up the Raspberry Pi 3B+ as needed for our project

starttime=$(date +%s)

# Colors that can be used in the output
OUTPUT_RED='\033[0;31m'
OUTPUT_GREEN='\033[0;32m'
OUTPUT_NO_COLOR='\033[039m'

# Prints the provided string red to indicate an error
error() {
  printf "${OUTPUT_RED}"
  printf "$@\n"
  printf "${OUTPUT_NO_COLOR}"
}

# Just like error(), but exits with exit code 1
exiterror() {
  error "$@"
  endtime=$(date +%s)
  runtime=$((endtime - starttime))
  runtime_minutes=$((runtime/60))
  error "FAILED after ${runtime} seconds (${runtime_minutes} minutes)."
  exit 1
}

# Called when the script finished successfully - prints a message in green
success() {
  printf "${OUTPUT_GREEN}"
  printf "Setup completed successfully; cya!\n"
  endtime=$(date +%s)
  runtime=$((endtime - starttime))
  runtime_minutes=$((runtime/60))
  printf "SUCCESS after ${runtime} seconds (${runtime_minutes} minutes)."
  printf "${OUTPUT_NO_COLOR}"
  exit 0
}

printf "\nNow setting up RPi 3B+ for use with Terror-Turret...\n\n"
sleep 1s

# First make sure the user is root so we can do what we need to
echo "Ensuring you have root privileges..."
if [[ $EUID != 0 ]]
then
  exiterror "This script must be run as root. Please re-run with sudo."
fi
echo -e "COMPLETE\n"

echo "Installing needed packages..."
sudo apt-get update || exiterror "Failed to run apt-get update."
sudo apt-get upgrade || exiterror "Failed to run apt-get upgrade."
echo "Installing Arduino IDE..."
sudo apt-get install arduino || exiterror
echo "Installing Git..."
sudo apt-get install git || exiterror
echo "Installing Vim..."
sudo apt-get install vim || exiterror
echo "Installing fswebcam..."
sudo apt-get install fswebcam || exiterror

echo -e "\nInstalling pip package manager for Python..."
sudo apt-get install python-pip || exiterror
echo "Installing needed Python packages..."
echo "Installing pyserial..."
sudo pip install pyserial || exiterror
echo "Installing colorama for Python..."
sudo pip install colorama || exiterror
echo "Installing SimpleWebsocketServer for Python..."
sudo pip install git+https://github.com/dpallot/simple-websocket-server.git || exiterror

echo -e "\nInstalling UV4L..."
curl http://www.linux-projects.org/listing/uv4l_repo/lpkey.asc | sudo apt-key add -
echo "deb http://www.linux-projects.org/listing/uv4l_repo/raspbian/stretch stretch main" | sudo tee -a /etc/apt/sources.list
sudo apt-get install uv4l || exiterror
sudo apt-get install uv4l-raspicam || exiterror
sudo apt-get install uv4l-raspicam-extras || exiterror
sudo apt-get install uv4l-decoder || exiterror
sudo apt-get install uv4l-encoder || exiterror
sudo apt-get install uv4l-renderer || exiterror
sudo apt-get install uv4l-mjpegstream || exiterror
sudo apt-get install uv4l-server || exiterror
sudo apt-get install uv4l-webrtc || exiterror
sudo apt-get install uv4l-demos || exiterror
sudo apt-get install uv4l-uvc || exiterror

echo -e "Finished installing all needed packages.\n"

echo "It is recommended to restart the Pi now."
echo "It is also recommended to unplug and replug the USB webcam after restart."

success

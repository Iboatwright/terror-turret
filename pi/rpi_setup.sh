#!/usr/bin/env /bin/bash

# This script sets up the Raspberry Pi 3B+ as needed for our project

starttime=$(date +%s)
OS_VERSION=${cat /etc/os-release | grep 'VERSION=' | awk -F'[(|)]' '{print $2}'}
BASE_PACKAGES="git python-pip"
UV4L_PACKAGES="uv4l uv4l-raspicam uv4l-raspicam uv4l-decoder uv4l-encoder"\
              "uv4l-renderer uv4l-mjpegstream uv4l-server uv4l-webrtc"\
              "uv4l-demos uv4l-uvc"

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
  printf "SUCCESS after ${runtime} seconds (${runtime_minutes} minutes).
  printf "${OUTPUT_NO_COLOR}"
  printf "\n Rebooting the Pi to make sure everything works as expected..."
  printf "You have 5 seconds to cancel with Ctrl + C."
  sleep 5
  sudo reboot
  exit 0
}

printf "\nNow setting up RPi 3B+ for use with Terror-Turret...\n\n"
sleep 1s

echo "Installing system updates..."
sudo apt update && sudo apt upgrade -y || exiterror "apt failed to update and upgrade."
echo "Installing git and pip if needed..."
sudo apt install $BASE_PACKAGES -y || exiterror "apt failed to install $BASE_PACKAGES."

echo "Installing needed Python packages..."
echo "Installing pyserial..."
sudo pip3 install pyserial || exiterror
echo "Installing colorama for Python..."
sudo pip3 install colorama || exiterror
echo "Installing SimpleWebsocketServer for Python..."
sudo pip3 install git+https://github.com/dpallot/simple-websocket-server.git || exiterror

echo -e "\nInstalling UV4L..."
curl http://www.linux-projects.org/listing/uv4l_repo/lpkey.asc | apt-key add -
echo "deb http://www.linux-projects.org/listing/uv4l_repo/raspbian/stretch stretch main" | tee -a /etc/apt/sources.list
sudo apt install $UV4L_PACKAGES -y || exiterror

echo -e "Finished installing all needed packages.\n"

echo -e "\n Cloning project code from github..."
sudo mkdir /usr/share/doc/terror-turret
sudo mkdir /etc/terror-turret
mkdir /tmp/code
git clone https://www.github.com/maillouxc/terror-turret.git /tmp/code/terror-turret
cp /tmp/code/terror-turret/docs /usr/share/doc/terror-turret
cp /tmp/code/terror-turret/pi/config.py /etc/terror-turret
cp /tmp/code/terror-turret/pi/start-stream.sh /etc/terror-turret
cp /tmp/code/terror-turret/pi/uv4l-config.conf /etc/terror-turret
cp /tmp/code/terror-turret/pi/terror-turret-loader.sh /etc/terror-turret
cp /tmp/code/terror-turret/pi/turret-manager/rpi-turret-manager.py /etc/terror-turret
sudo cp /tmp/code/terror-turret/pi/terror-turret.service /etc/systemd/system
sudo chmod 644 /etc/systemd/system/terror-turret.service
chmod +x /etc/terror-turret/start-stream.sh
chmod +x /etc/terror-turret/terror-turret-loader.sh
#sudo systemctrl daemon-reload
#sudo systemctrl enable terror-turret.service
#sudo systemctrl start terror-turret.service

success

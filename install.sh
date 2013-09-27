#!/bin/sh

#Exit on error
set -e

num=1
if [ -n "$1" ]
then
	num="$1"
fi

if [ -z "$(adb devices | grep 'device$')" ]
then
	echo "Device not found"
	exit 1
fi
device_found=$(adb devices | grep 'device$' | sed -e 's/\ *device//' | head -n $num)
echo "device found: "$device_found

echo "Attempting to uninstall previous versions"
adb -s $device_found  uninstall za.co.csir.walkiemesh

echo "Installing new version"
adb -s $device_found install bin/batphone-debug.apk

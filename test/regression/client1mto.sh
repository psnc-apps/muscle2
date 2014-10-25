#!/bin/bash

export MUSCLE_PORT_MIN=6002
export MUSCLE_PORT_MAX=6099
export MUSCLE_MTO=127.0.0.1:6001

muscle2 -i --bindport 6002 --bindaddr 127.0.0.1 -mc "$@"

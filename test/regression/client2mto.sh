#!/bin/bash

export MUSCLE_PORT_MIN=5002
export MUSCLE_PORT_MAX=5099
export MUSCLE_MTO=127.0.0.1:5001

muscle2 -i -M 127.0.0.1:6002 -c "$@"


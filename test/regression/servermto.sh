#!/bin/bash

export MUSCLE_HOME=/usr/local
mto --topology mto-topology.cfg --config mto-config-$1.cfg --logFile server-$1.log --debug --MPWide --logLevel CONFIG

#!/usr/bin/env bash

# Start drone daemons.  Run this on master node.

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

. "$bin"/drone-env.sh

"$bin"/drone-daemon.sh stop webserver

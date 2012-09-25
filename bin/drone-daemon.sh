#!/usr/bin/env bash
# 
# Runs a drone command as a daemon.
#
# Environment Variables
#
##

usage="Usage: drone-daemon.sh (start|stop) <command>"

# if no args specified, show usage
if [ $# -le 1 ]; then
  echo $usage
  exit 1
fi

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

. "$bin"/../bin/drone-env.sh

startStop=$1
shift
command=$1
shift

drone_rotate_log ()
{
    log=$1;
    num=5;
    if [ -n "$2" ]; then
	num=$2
    fi
    if [ -f "$log" ]; then # rotate logs
	while [ $num -gt 1 ]; do
	    prev=`expr $num - 1`
	    [ -f "$log.$prev" ] && mv "$log.$prev" "$log.$num"
	    num=$prev
	done
	mv "$log" "$log.$num";
    fi
}

if [ "$DRONE_LOG_DIR" = "" ]; then
  export DRONE_LOG_DIR="$DRONE_HOME/logs"
fi
mkdir -p "$DRONE_LOG_DIR"

if [ "$DRONE_PID_DIR" = "" ]; then
  DRONE_PID_DIR=/tmp
fi

if [ "$DRONE_IDENT_STRING" = "" ]; then
  export DRONE_IDENT_STRING="$USER"
fi

# some variables
export DRONE_LOGFILE=drone-$DRONE_IDENT_STRING-$command-`hostname`.log
export DRONE_ROOT_LOGGER="INFO,DRFA"
log=$DRONE_LOG_DIR/drone-$DRONE_IDENT_STRING-$command-`hostname`.out
pid=$DRONE_PID_DIR/drone-$DRONE_IDENT_STRING-$command.pid

if [ ! -e $DRONE_PID_DIR ]; then
    mkdir $DRONE_PID_DIR
fi
    
case $startStop in

  (start)

    if [ -f $pid ]; then
      if kill -0 `cat $pid` > /dev/null 2>&1; then
        echo $command running as process `cat $pid`.  Stop it first.
        exit 1
      fi
    fi

    drone_rotate_log $log
    echo starting $command, logging to $log
    nohup nice -n 0 "$DRONE_HOME"/bin/drone $command "$@" > "$log" 2>&1 < /dev/null &
    echo $! > $pid
    sleep 1; head "$log"
    ;;
          
  (stop)

    if [ -f $pid ]; then
      if kill -9 `cat $pid` > /dev/null 2>&1; then
        echo stopping $command
        kill `cat $pid`
      else
        echo no $command to stop
      fi
    else
      echo no $command to stop
    fi
    ;;

  (*)
    echo $usage
    exit 1
    ;;

esac

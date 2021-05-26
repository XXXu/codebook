#!/bin/bash
#
# Starts a mongodb mongos server
#
# chkconfig: 345 85 15
# description: Transwarp Mongodb Mongos Server
#
### BEGIN INIT INFO
# Provides:          transwarp-mongodb-mongosserver
# Short-Description: Transwarp Mongodb Mongos Server
# Default-Start:     3 4 5
# Default-Stop:      0 1 2 6
# Required-Start:    $syslog $remote_fs
# Required-Stop:     $syslog $remote_fs
# Should-Start:
# Should-Stop:
### END INIT INFO

export _SYSTEMCTL_SKIP_REDIRECT="true"
. /etc/init.d/functions

RETVAL_SUCCESS=0
STATUS_RUNNING=0
STATUS_DEAD=1
STATUS_DEAD_AND_LOCK=2
STATUS_NOT_RUNNING=3
STATUS_OTHER_ERROR=102
ERROR_PROGRAM_NOT_INSTALLED=5
ERROR_PROGRAM_NOT_CONFIGURED=6
RETVAL=0
SLEEP_TIME=5
PROC_NAME="mongos"

DESC="Transwarp Mongodb Mongos Server"
ROOT_DIR=/opt/transwarp-mongodb
MONGO_ROUTER_PID_DIR="/var/run/mongodb"
EXEC_PATH=$ROOT_DIR/bin/mongos
CONF_FILE=$ROOT_DIR/conf/mongos.conf
PIDFILE="/var/run/mongodb/mongos.pid"
LOCKDIR="/var/lock/subsys"
LOCKFILE="$LOCKDIR/mongodb-mongosserver"
DB_PATH=/mnt/disk1/mongodb/mongos
LOG_PATH=/var/log/mongodb

function log_success_msg() {
    echo "$@" "[ OK ]"
}

function log_end_msg() {
    echo "$@" 
}

start() {
  checkstatusofproc
  status=$?
  if [ $status -eq $STATUS_RUNNING ]; then
    echo "$DESC is already running"
    exit
  fi

  [ -x $EXEC_PATH ] || exit $ERROR_PROGRAM_NOT_INSTALLED
  [ -f $CONF_FILE ] || exit $ERROR_PROGRAM_NOT_CONFIGURED
  log_success_msg "Starting ${DESC}: "
  if [ ! -d $MONGO_ROUTER_PID_DIR ]; then
    mkdir -p $MONGO_ROUTER_PID_DIR
    chown -R mongod:mongod $MONGO_ROUTER_PID_DIR
  fi

  sudo -u mongod $EXEC_PATH --port 27017 --configdb TranswarpMongoConfigServer/172.22.33.33:27019,172.22.33.34:27019,172.22.33.35:27019 --pidfilepath $PIDFILE --logpath $LOG_PATH/mongodb-mongos-$(hostname).log --config $CONF_FILE $* &
  # Some processes are slow to start
  sleep $SLEEP_TIME
  checkstatusofproc
  RETVAL=$?

  log_end_msg $RETVAL
  [ $RETVAL -eq $RETVAL_SUCCESS ] && touch $LOCKFILE
  return $RETVAL
}


stop() {
  log_success_msg "Stopping ${DESC}: "

  count=0
  while [ $count -lt 3 ]; do
    checkstatusofproc
    status=$?
    if [ $status -eq $STATUS_RUNNING ]; then
      kill $(cat $PIDFILE)
    else
      break
    fi
    sleep $SLEEP_TIME
    (( count+=1 ))
  done

  checkstatusofproc
  status=$?
  if [ $status -eq $STATUS_RUNNING ]; then
      kill -9 $(cat $PIDFILE)
  fi

  RETVAL=0
  log_end_msg $RETVAL
  [ $RETVAL -eq $RETVAL_SUCCESS ] && rm -f $LOCKFILE $PIDFILE
}

restart() {
  stop
  start
}

checkstatusofproc(){
  pidofproc -p $PIDFILE $PROC_NAME > /dev/null
}

checkstatus(){
  checkstatusofproc
  status=$?

  case "$status" in
    $STATUS_RUNNING)
      log_success_msg "${DESC} is running"
      ;;
    $STATUS_DEAD)
      log_failure_msg "${DESC} is dead and pid file exists"
      ;;
    $STATUS_DEAD_AND_LOCK)
      log_failure_msg "${DESC} is dead and lock file exists"
      ;;
    $STATUS_NOT_RUNNING)
      log_failure_msg "${DESC} is not running"
      ;;
    *)
      log_failure_msg "${DESC} status is unknown"
      ;;
  esac
  return $status
}

check_for_root() {
  if [ $(id -ur) -ne 0 ]; then
    echo 'Error: root user required'
    echo
    exit 1
  fi
}

service() {
  case "$1" in
    start)
      check_for_root
      shift
      start $*
      ;;
    stop)
      check_for_root
      stop
      ;;
    status)
      checkstatus
      RETVAL=$?
      ;;
    restart)
      check_for_root
      restart
      ;;
    *)
      echo $"Usage: $0 {start|stop|status|restart}"
      exit 1
  esac
}
service $*


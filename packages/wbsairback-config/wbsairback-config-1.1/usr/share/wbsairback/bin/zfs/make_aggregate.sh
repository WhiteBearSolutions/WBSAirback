#! /bin/bash
### BEGIN INIT INFO
# Provides:          zfs-fuse
# Required-Start:    fuse $remote_fs
# Required-Stop:     fuse $remote_fs
# Default-Start:     S
# Default-Stop:      0 6
# Short-Description: Daemon for ZFS support via FUSE
# Description:       Mounts and makes available ZFS volumes
### END INIT INFO

set -u # Error on uninitialized variabled
set -e # Error on uncaught non-zero exit codes

PATH=/sbin:/bin:/usr/sbin:/usr/bin:/usr/local/sbin:/usr/local/bin
DAEMON=/usr/share/wbsairback/bin/zfs/zfs-fuse
ZPOOL_BIN=/usr/share/wbsairback/bin/zfs/zpool
NAME=zfs-fuse
DESC=zfs-fuse

### Fallback functions in case lsb-base isn't available (eg in postinst)
log_action_begin_msg() {
    echo -n "$1..."
}

log_action_cont_msg() {
    echo -n "$1..."
}

log_action_end_msg() {
    echo "$2"
}

log_daemon_msg() {
    echo -n "$1: $2"
}

log_end_msg() {
    if [ "x$1" = "x0" ]; then
        echo "ok"
    else
        echo "failed"
    fi
}

if [ -r /lib/lsb/init-functions ]; then
    . /lib/lsb/init-functions
fi

zpool_cache()
{
	cache=/var/lib/zfs/zpool.cache
        mkdir -p ${cache%*\/*} 
        if [ -e  $cache ]; then
                rm $cache
        fi
}

is_running() {
    ### XXX: this produces output for some reason
	start-stop-daemon --stop --test --quiet --pidfile \
		/var/run/$NAME.pid --exec $DAEMON &>/dev/null
}

do_stop() {
    if is_running; then
        log_daemon_msg "Stopping $NAME" "zfs-fuse"
        if start-stop-daemon --stop --quiet --pidfile \
            /var/run/$NAME.pid --exec $DAEMON
        then
            ## wait for it to stop, up to 12 seconds
            ## 10 seconds is the wait time for worker threads to complete their
            ## work before the daemon forcibly terminates them and completes
            ## shutdown 
            COUNTER=0
            while is_running; do
                sleep 1
                COUNTER=$(($COUNTER + 1))
                if [ $COUNTER -ge 12 ]; then
                    log_end_msg 1 "Timed out"
                    exit 1
                fi
            done
            rm -f /var/run/$NAME.pid

            log_end_msg 0
            return 0
        else
            log_end_msg 1
            return 1
        fi
    fi
}

do_start() {
    if is_running; then
        log_action_msg "zfs-fuse is already running"
        return 0
    fi
    zpool_cache
    log_daemon_msg "Starting $NAME" "zfs-fuse"
    unset LANG
    if start-stop-daemon --start --quiet --pidfile \
        /var/run/$NAME.pid --exec $DAEMON -- --pidfile /var/run/$NAME.pid
    then
    log_end_msg 0
    fi
}

test -x $DAEMON || exit 0

if [ -z $1 ]; then
  echo "aggregate name undefined"
  exit 1
fi

if [ -z $2 ]; then
  echo "at least one VDEV must be defined"
  exit 1
fi

COMMAND="$ZPOOL_BIN create -f $1 $2"
for arg in "${*:3}"; do
 COMMAND="$COMMAND $arg" 
done

do_start
$COMMAND
do_stop
zpool import -f $1 || exit 1
zpool upgrade $1 >/dev/null || exit 1
exit 0

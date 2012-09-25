# Set beaver-specific environment variables here.
# JAVA_HOME, DRONE_HOME, HADOOP_HOME required

#JAVA HOME dir
export JAVA_HOME=/usr/java/default

#drone home dir
export DRONE_HOME=/usr/local/drone

#drone conf dir
export DRONE_CONF_DIR="${DRONE_HOME}/conf"

# JVM Heap Size (MB) of each baas component
export DRONE_WEBSERVER_HEAPSIZE="-Xmx512m"

# Extra Java CLASSPATH elements.  Optional.
#export DRONE_CLASSPATH=

# The directory where pid files are stored. /tmp by default.
export DRONE_PID_DIR=~/.drone_pids

# A string representing this instance of drone. $USER by default.
# export DRONE_IDENT_STRING=$USER

# Hadoop Home dir
export HADOOP_HOME=/usr/local/hadoop-1.0.0

# Hadoop Conf dir
export HADOOP_CONF_DIR="${HADOOP_HOME}/conf"

# JVM Options of each drone component
export DRONE_WEBSERVER_OPTS="$DRONE_WEBSERVER_HEAPSIZE"

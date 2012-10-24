#!/bin/bash
# Wrap command to execure liner2

# Get liner2 location
DIR="$( cd "$( dirname "$0" )" && pwd )"

# Run liner2
java -Xmx4024M -cp $DIR/lib/mysql-connector-java-5.1.10.jar -Djava.library.path=$DIR/lib:/lib -jar $DIR/liner2.jar $@

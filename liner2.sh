#!/bin/bash
# Wrap command to execure liner2

# Get liner2 location
DIR="$( cd "$( dirname "$0" )" && pwd )"

# Run liner2
java -Djava.library.path=./lib -jar $DIR/liner2.jar $@

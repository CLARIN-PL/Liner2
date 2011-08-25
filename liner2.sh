#!/bin/bash
# Wrap command to execure liner2

# Get liner2 location
DIR="$( cd "$( dirname "$0" )" && pwd )"

# Run liner2
java -jar $DIR/liner2.jar $@

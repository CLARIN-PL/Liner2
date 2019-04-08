#!/bin/bash

# wait for the port to be open
while ! nc -z rabbitmq 5672; do sleep 3; done

# when the port is open wait another minute for the server to start
echo "Liner2 process will start in 60 second ..."
sleep 60

echo "Starting Liner2 process ..."
/liner2/liner2-daemon rabbitmq -H rabbitmq -m /liner2/liner26_model_ner_nkjp/config-nkjp-poleval2018.ini -i plain:wcrft
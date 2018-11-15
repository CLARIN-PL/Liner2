Liner2.6
========

[![Build Status](https://travis-ci.org/CLARIN-PL/Liner2.svg)](https://travis-ci.org/CLARIN-PL/Liner2) [![Coverage Status](https://coveralls.io/repos/github/CLARIN-PL/Liner2/badge.svg?branch=feature%2Fspatial-dynamic)](https://coveralls.io/github/CLARIN-PL/Liner2?branch=feature%2Fspatial-dynamic) [![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)
 
Copyright (C) Wrocław University of Science and Technology (PWr), 2010-2018. 
All rights reserved.

        
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.


Contributors
------------
* **Michał Marcińczuk** (2010–present), 
* **Jan Kocoń** (2014–present), 
* Adam Kaczmarek (2014–2015),
* Michał Krautforst (2013-2015), 
* Dominik Piasecki (2013), 
* Maciej Janicki (2011)


Requirements
------------

### Compilation

* Java 8
* C++ compiler (gcc 3.0 or higher) for CRF++ (https://taku910.github.io/crfpp/)

### Runtime

* Java 8
* CRF++ 0.57 (https://taku910.github.io/crfpp/)

Optional libraries:

* Polem (https://github.com/CLARIN-PL/Polem) — required by models using Polem to lemmatize phrases.
* RabbitMQ (https://www.rabbitmq.com) — required to run Liner2 in service mode.

Installation
------------

### Compile

If you do not have CRF++ installed then do the following steps:
```bash
cd g419-external-dependencies
tar -xvf CRF++-0.57.tar.gz
cd CRF++-0.57
./configure
make
sudo make install
sudo ldconfig
```

Then:

```bash
./gradlew jar
```

### Runtime test


```bash
./liner2-cli
```

Output:

```bash
*-----------------------------------------------------------------------------------------------*
* A framework for multitask sequence labeling, including: named entities, temporal expressions. *
*                                                                                               *
* Authors: Michał Marcińczuk (2010–2016), Jan Kocoń (2014–2016), Adam Kaczmarek (2014–2015)     *
*    Past: Michał Krautforst (2013-2015), Dominik Piasecki (2013), Maciej Janicki (2011)        *
* Contact: michal.marcinczuk@pwr.wroc.pl                                                        *
*                                                                                               *
*          G4.19 Research Group, Wrocław University of Technology                               *
*-----------------------------------------------------------------------------------------------*


Use one of the following tools:
 - agreement           -- checks agreement (of annotations) between suplied documents
 - agreement2          -- compare sets of annotations for each pair of corpora. One set is
                          treated as a reference set and the other as a set to evaluate. It is a
                          refactored version of the agreement action.
 - annotations         -- generates an arff file with a list of annotations and their features
 - constituents-eval   -- evaluates normalizer against a specific set of documents (-i
                          batch:FORMAT, -i FORMAT)
 - convert             -- converts documents from one format to another and applies defined
                          converters
 - curve               -- brak opisu
 - eval                -- evaluates chunkers against a specific set of documents (-i
                          batch:FORMAT, -i FORMAT) #or perform cross validation (-i cv:{format})
 - eval-unique         -- evaluates chunkers against a specific set of documents (-i
                          batch:FORMAT, -i FORMAT) #or perform cross validation (-i
                          cv:{format}). The evaluation is performed on the sets#with unique
                          annotations, i.e. annotations with the same orth/base are treated as a
                          single annotation
 - inplace             -- process documents in place
 - interactive         -- processes text entered directly into the terminal
 - lemmatize           -- ToDo
 - normalizer-eval3    -- processes data with given model
 - normalizer-validate -- Read all annotation and their metadata and look for errors.
 - pipe                -- processes data with given model
 - search              -- earches for a phrases matching given pattern based on a set of token
                          features
 - selection           -- todo
 - stats               -- prints corpus statistics
 - train               -- trains chunkers

usage: ./liner2-cli [action] [options]

```


Pre-trained models
------------------

### NER for Polish

DSpace page: https://clarin-pl.eu/dspace/handle/11321/263 

Direct link to the package: https://clarin-pl.eu/dspace/bitstream/handle/11321/263/liner25_model_ner_rev1.7z

Download the package with models:
```bash
cd Liner2
wget -O liner25_model_ner_rev1.7z https://clarin-pl.eu/dspace/bitstream/handle/11321/263/liner25_model_ner_rev1.7z 
```

Unpack the package:
```bash
7z x liner25_model_ner_rev1.7z
```

Process a sample CCL file:
```bash
./liner2-cli pipe -i ccl -o tuples -f stuff/resources/sample-sentence.xml -m liner25_model_ner_rev1/config-top9.ini
```

Expected output:
```bash
(4,11,nam_liv,"Ala Nowak")
(20,28,nam_loc,"Warszawie")
```


### PolEval 2018 Task 2: Named Entity Recognition

DSpace page: https://clarin-pl.eu/dspace/handle/11321/598 

Direct link to the package: https://clarin-pl.eu/dspace/bitstream/handle/11321/598/liner26_model_ner_nkjp.zip

Liner2 participated in [PolEval 2018 Task 2 on named entity recognition](http://poleval.pl/results/). 
It won a third place with the following scores:

| Metric  | F1 score |
| ------- |   -----: |
| Final   |    0.810 |
| Exact   |    0.778 |
| Overlap |    0.818 |

Download the package with model:
```bash
cd Liner2
wget -O liner26_model_ner_nkjp.zip https://clarin-pl.eu/dspace/bitstream/handle/11321/598/liner26_model_ner_nkjp.zip 
```

Unpack the model:
```bash
unzip liner26_model_ner_nkjp.zip
```

Process a sample CCL file:
```bash
./liner2-cli pipe -i ccl -o tuples -f stuff/resources/sample-sentence.xml -m liner26_model_ner_nkjp/config-nkjp-poleval2018.ini
```

Expected output:
```bash
(4,6,null,persname_forename,"Ala","Ala")
(4,11,null,persName,"Ala Nowak","Ala Nowak")
(7,11,null,persname_surname,"Nowak","Nowak")
(20,28,null,placename_settlement,"Warszawie","Warszawie")
```

Service mode
============

Introduction
------------

Liner2 can be run a service which listen to a RabbitMQ queue for upcomming requests. 
By default the *liner2-input* queue is used as an input for the service and *liner2-output* as an output queue.
The input message (send by the client) should have the following format:
```text
ROUTE_KEY PATH
```
Where:
* ROUTE_KEY — after processing the request Liner2 service will send message to the output queue with the provided routing key. The routing key is used by the client to receive the response for their request ignoring other,
* PATH — an absolute path to a file to process.

For example:
```text
client-001 /tmp/document.txt
```

The message send by the service will contain path to a file which contain the output of processing.

Running the service
-------------------
```bash
./liner2-daemon rabbitmq -m liner26_model_ner_nkjp/config-nkjp-poleval2018.ini -i plain:wcrft
```

Expected output:
```bash
 INFO [Thread-1] (RabbitMqWorker.java:91) - Listing to RabbitMQ on channel liner2-input ...
Consumer amq.ctag-m6D9fIMI_Qsm61BH7HoxlA registered
```

It is possible to run more than one instance of `./liner2-daemon rabbitmq`. However, all of them should use the same model and input format.

Testing
-------

Folder `stuff/python` contains a Python script to test the communication with the service. 
The script takes a text to process, stores the texts in a temporal file, generates a routing key, send both to the `liner2-input` queue and listen to `liner2-output`.
After receiving the response it reads the output file, removes both temporal files and prints the output. 

```bash
python3 stuff/python/liner2rmq.py "Ala z Krakowa ma kota"
```

Expected output:
```text
[INFO] Temp route: route-KO4KN5
[INFO] Temp input file: /tmp/siu94xgg
[INFO] Sent msg 'route-KO4KN5 /tmp/siu94xgg' to liner2-input
[INFO] Temp output file: b'/tmp/siu94xgg-ner.xml'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE chunkList SYSTEM "ccl.dtd">
<chunkList>
 <chunk id="ch1">
  <sentence id="s1">
   <tok>
    <orth>Ala</orth>
    <lex disamb="1"><base>Ala</base><ctag>subst:sg:nom:f</ctag></lex>
    <ann chan="placename_settlement">0</ann>
   </tok>
   <tok>
    <orth>z</orth>
    <lex disamb="1"><base>z</base><ctag>prep:gen:nwok</ctag></lex>
    <ann chan="placename_settlement">0</ann>
   </tok>
   <tok>
    <orth>Krakowa</orth>
    <lex disamb="1"><base>Kraków</base><ctag>subst:sg:gen:m3</ctag></lex>
    <ann chan="placename_settlement" head="1">1</ann>
   </tok>
   <tok>
    <orth>ma</orth>
    <lex disamb="1"><base>mieć</base><ctag>fin:sg:ter:imperf</ctag></lex>
    <ann chan="placename_settlement">0</ann>
   </tok>
   <tok>
    <orth>kota</orth>
    <lex disamb="1"><base>kot</base><ctag>subst:sg:acc:m2</ctag></lex>
    <ann chan="placename_settlement">0</ann>
   </tok>
  </sentence>
 </chunk>
</chunkList>
```

Logs on the server side:
```
 INFO [pool-1-thread-4] (RabbitMqWorker.java:99) - Received path: '/tmp/siu94xgg'
 INFO [pool-1-thread-4] (RabbitMqWorker.java:108) - Output saved to /tmp/siu94xgg
 INFO [pool-1-thread-4] (RabbitMqWorker.java:121) - Sent /tmp/siu94xgg-ner.xml to liner2-output:route-KO4KN5'
 INFO [pool-1-thread-4] (RabbitMqWorker.java:84) - Request processing done
```
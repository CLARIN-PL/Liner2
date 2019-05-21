FROM ubuntu:16.04

MAINTAINER Michał Marcińczuk <marcinczuk@gmail.com>

RUN apt-get update && \
    apt-get -y upgrade

RUN apt-get install -y openjdk-8-jdk netcat unzip && \
    apt-get clean;

RUN apt-get update && \
    apt-get -y install git libboost-all-dev libicu-dev git-core wget cmake libantlr-dev libloki-dev python-dev swig libxml2-dev libsigc++-2.0-dev libglibmm-2.4-dev libxml++2.6-dev p7zip-full

WORKDIR /liner2

COPY ./g419-corpus /liner2/g419-corpus
COPY ./g419-lib-cli /liner2/g419-lib-cli
COPY ./g419-liner2-cli /liner2/g419-liner2-cli
COPY ./g419-liner2-core /liner2/g419-liner2-core
COPY ./g419-liner2-daemon /liner2/g419-liner2-daemon
COPY ./g419-external-dependencies /liner2/g419-external-dependencies
COPY ./g419-toolbox /liner2/g419-toolbox
COPY ./lib /liner2/lib
COPY ./gradle /liner2/gradle
COPY ./gradlew /liner2/
COPY ./build.gradle /liner2/
COPY ./settings.gradle /liner2/
COPY liner2-daemon /liner2/
COPY ./docker/liner2/liner2-daemon-run.sh /liner2/
COPY log4j.properties /liner2/

RUN /liner2/gradlew :g419-liner2-daemon:jar

WORKDIR /liner2/g419-external-dependencies
RUN tar -xvf CRF++-0.57.tar.gz
WORKDIR /liner2/g419-external-dependencies/CRF++-0.57
RUN ./configure
RUN make
RUN make install
RUN ldconfig

WORKDIR /build
RUN git clone http://nlp.pwr.edu.pl/corpus2.git
RUN git clone http://nlp.pwr.edu.pl/toki.git
RUN git clone http://nlp.pwr.edu.pl/maca.git
RUN git clone http://nlp.pwr.edu.pl/wccl.git
RUN git clone http://nlp.pwr.edu.pl/wcrft2.git

#### ... and building them
# corpus2
RUN cd corpus2
RUN mkdir bin
WORKDIR /build/corpus2/bin
RUN cmake ..
RUN make -j
RUN make -j
RUN make install
RUN ldconfig

# toki
RUN mkdir /build/toki/bin
WORKDIR /build/toki/bin
RUN cmake ..
RUN make -j
RUN make -j
RUN make install
RUN ldconfig

# morfeusz
RUN mkdir /build/morfeusz
WORKDIR /build/morfeusz
RUN wget http://tools.clarin-pl.eu/share/morfeusz-SGJP-linux64-20130413.tar.bz2
RUN tar -jxvf morfeusz-SGJP-linux64-20130413.tar.bz2
RUN mv libmorfeusz* /usr/local/lib/
RUN mv morfeusz /usr/local/bin/
RUN mv morfeusz.h /usr/local/include/
RUN ldconfig

# maca
RUN mkdir /build/maca/bin
WORKDIR /build/maca/bin
RUN cmake ..
RUN make -j
RUN make -j
RUN make install
RUN ldconfig

# wccl
RUN mkdir /build/wccl/bin
WORKDIR /build/wccl/bin
RUN cmake ..
RUN make -j
RUN make -j
RUN make install
RUN ldconfig

# wcrft2
RUN mkdir /build/wcrft2/bin
WORKDIR /build/wcrft2/bin
RUN cmake ..
RUN make -j
RUN make -j
RUN make install
RUN ldconfig

WORKDIR /liner2

RUN wget -O liner26_model_ner_nkjp.zip https://clarin-pl.eu/dspace/bitstream/handle/11321/598/liner26_model_ner_nkjp.zip
RUN unzip liner26_model_ner_nkjp.zip
#!/usr/bin/env bash
echo "Install dependencies"
sudo apt-get install -y libboost-all-dev libicu-dev libxml++2.6-dev bison flex libloki-dev cmake g++ swig python-dev

mkdir install
cd install

# Install corpus2
git clone http://nlp.pwr.edu.pl/corpus2.git
cd corpus2
mkdir bin
cd bin
cmake -D CORPUS2_BUILD_POLIQARP:BOOL=True ..
make -j
make -j
sudo make install
sudo ldconfig
cd ..

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

# Install wccl
sudo apt-get install -y libantlr-dev
git clone http://nlp.pwr.edu.pl/wccl.git
cd wccl
mkdir bin
cd bin
cmake ..
make -j
make -j
sudo make install
sudo ldconfig
cd ..

# Install Morfeusz2 form generator
wget -O morfeusz2-2.0.0-Linux-amd64.deb https://nextcloud.clarin-pl.eu/index.php/s/VVIvx4w20azcWbp/download
sudo dpkg -i morfeusz2-2.0.0-Linux-amd64.deb

# Install Polem
git clone https://github.com/CLARIN-PL/Polem.git
cd Polem
mkdir build
cd build
cmake ..
make
sudo make install
sudo ldconfig
cd ..

# Exit install folder
cd ..

polem -h

cp Polem/build/PolemJava.jar ../lib
cp Polem/build/libPolemJava.so ../g419-liner2-core/src/main/resources/
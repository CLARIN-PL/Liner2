#!/usr/bin/env bash
echo "Install dependencies"

#################################################
# Create working folder
#################################################
mkdir install
cd install
#==== END =======================================

#################################################
# Install corpus2
#################################################
sudo apt-get install -y libboost-all-dev libicu-dev libxml++2.6-dev bison flex libloki-dev cmake g++ swig python-dev git
git clone http://nlp.pwr.edu.pl/corpus2.git
cd corpus2
mkdir build
cd build
cmake -D CORPUS2_BUILD_POLIQARP:BOOL=True ..
make -j
sudo make install
sudo ldconfig
cd ../..
#==== END =======================================

#################################################
# Install wccl
#################################################
sudo apt-get install -y libboost-all-dev libicu-dev libxml++2.6-dev bison flex libloki-dev cmake g++ swig python-dev libantlr-dev git
git clone http://nlp.pwr.edu.pl/wccl.git
cd wccl
mkdir build
cd build
cmake ..
make -j
sudo make install
sudo ldconfig
cd ../..
#==== END =======================================

#################################################
# Install Morfeusz2 form generator
#################################################
# wget -O morfeusz2-2.0.0-Linux-amd64.deb https://nextcloud.clarin-pl.eu/index.php/s/VVIvx4w20azcWbp/download
# sudo dpkg -i morfeusz2-2.0.0-Linux-amd64.deb
sudo apt-get install -y p7zip-full
wget -O morfeusz2-unfolded-pack.7z https://nextcloud.clarin-pl.eu/index.php/s/AENL5gAYuuJTx4h/download
7z x morfeusz2-unfolded-pack.7z
cd morfeusz2-unfolded-pack
bash install.sh
cd ..
#==== END =======================================

#################################################
# Install Polem
#################################################
git clone https://github.com/CLARIN-PL/Polem.git
cd Polem
mkdir build
cd build
cmake ..
make
sudo make install
sudo ldconfig
cd ../..
#==== END =======================================

#################################################
# Copy resources to Liner2 folder
#################################################
cp Polem/build/PolemJava.jar ../lib
cp Polem/build/libPolemJava.so ../g419-liner2-core/src/main/resources/
#==== END =======================================

#################################################
# Exit working folder
#################################################
cd ..
#==== END =======================================

polem -h

#!/usr/bin/env bash
echo "Install dependencies"

#---- Ubuntu 14.04 docker --------------------------------
# sudo apt-get install -y software-properties-common
# sudo apt-add-repository ppa:webupd8team/java -y
# sudo apt-get update
# sudo apt-get install -y oracle-java8-installer
# export JAVA_INCLUDE_PATH=/usr/lib/jvm/java-8-oracle/include/
# sudo apt-get install -y wget git p7zip-full vim
#---- END ------------------------------------------------

sudo apt-get install -y libboost-all-dev libicu-dev libxml++2.6-dev bison flex libloki-dev cmake g++ swig python-dev

# sudo add-apt-repository ppa:ubuntu-toolchain-r/test -y
# sudo apt-get update
# sudo apt-get install gcc-5 g++-5
# sudo update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-5 1
# sudo update-alternatives --install /usr/bin/g++ g++ /usr/bin/g++-5 1
# gcc --version
# g++ --version

mkdir install
cd install

# Install Morfeusz2 form generator
wget http://sgjp.pl/morfeusz/download/20141120/morfeusz-src-20141120.tar.gz
tar -xvf morfeusz-src-20141120.tar.gz
wget http://sgjp.pl/morfeusz/download/20141120/sgjp-20141120.tab.gz
gunzip sgjp-20141120.tab.gz
wget https://nextcloud.clarin-pl.eu/index.php/s/8kywlbHvThZpW9C/download -O 2014_11_29_morfeusz2_unfolded.7z
7z x 2014_11_29_morfeusz2_unfolded.7z
python 2014_11_29_morfeusz2_unfolded/dict_unfold.py sgjp-20141120
cp 2014_11_29_morfeusz2_unfolded/trunk/CMakeLists.txt trunk
cp 2014_11_29_morfeusz2_unfolded/trunk/input/segmenty.dat trunk/input
cd trunk
mkdir build
cd build
cmake -D INPUT_DICTIONARIES=${PWD}/../../sgjp-20141120.new.tab -D DEFAULT_DICT_NAME=unfolded -D INPUT_TAGSET=${PWD}/../../sgjp-20141120.new.tagset ..
make
sudo make install

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
cd ../..

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
cd ../..

# Install Morfeusz2 form generator
# wget -O morfeusz2-2.0.0-Linux-amd64.deb https://nextcloud.clarin-pl.eu/index.php/s/VVIvx4w20azcWbp/download
# sudo dpkg -i morfeusz2-2.0.0-Linux-amd64.deb

sudo apt-get install -y libcppunit-dev python-pyparsing

# Install Polem
git clone https://github.com/CLARIN-PL/Polem.git
cd Polem
mkdir build
cd build
cmake ..
make
sudo make install
sudo ldconfig
cd ../..

# Exit install folder
cd ..

polem -h

cp Polem/build/PolemJava.jar ../lib
cp Polem/build/libPolemJava.so ../g419-liner2-core/src/main/resources/

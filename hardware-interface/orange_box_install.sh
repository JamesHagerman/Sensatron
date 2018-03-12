#!/bin/bash

# Install the binary library:
#
# These are for x86_64:
# echo
# echo "Installing the FTDI binary library..."
# sudo cp FTDI_Hack/release/build/x86_64/lib* /usr/local/lib/
# sudo chmod 0755 /usr/local/lib/libftd2xx.so.1.1.12
# sudo ln -sf /usr/local/lib/libftd2xx.so.1.1.12 /usr/local/lib/libftd2xx.so
# echo "Done installing FTDI binary library."
# ls -al /usr/local/lib/ | grep ftd

# These are for ARM HF:
echo
echo "Installing the FTDI binary library..."
sudo cp FTDI_Hack/release/build/arm926-hf/lib* /usr/local/lib/
sudo chmod 0755 /usr/local/lib/libftd2xx.so.1.2.7
sudo ln -sf /usr/local/lib/libftd2xx.so.1.2.7 /usr/local/lib/libftd2xx.so
echo "Done installing FTDI binary library."
ls -al /usr/local/lib/ | grep ftd

# Install the library header files:
echo
echo "Installing the FTDI header files..."
sudo cp FTDI_Hack/release/*.h /usr/local/include/
echo "Done installing the FTDI header files."
ls -al /usr/local/include/ftd2xx.h /usr/local/include/WinTypes.h

# Install the p9813 driver:
echo
echo "Building and installing the p9813 FTDI driver..."
pushd p9813/
make clean
sudo make
sudo make install
popd
echo "Done building and installing the p9813 FTDI driver."

# Compile the Processing library:
echo
echo "Building the Processing library"
pushd p9813/processing/
make clean
make
make install
echo "Done building the Processing library"
echo
echo "Installing the native TotalControl library"
sudo cp libTotalControl.so /usr/local/lib/
ls -al /usr/local/lib/libTotalControl.so
popd
echo "Done installing the native TotalControl library"


echo
echo "Setting LD_LIBRARY_PATH..."
export LD_LIBRARY_PATH="/usr/local/lib/"
echo "You should probably add the following to .profile:"
echo "export LD_LIBRARY_PATH=\"/usr/local/lib/\""

echo
echo "Done!"

# pacman-fb

A Pac Man (Arcade) emulator, written in Java.

Most of the emulator infrastructure is borrowed from the [helios](https://github.com/fedex81/helios) project.
This emulator requires and supports the following romSets (see below), in MAME format, stored uncompressed in a folder.
By default it will try to open and launch from the "data" sub-folder within the installation directory.

# How to Run
Requires java 8+ installed.

Get the most recent zip file from the download section,
for example `pacman-fb-20.1101.zip` and extract to a folder.

## Windows
Double click on `launcher.bat`

## Linux
Open a terminal and run:
`chmod +x launcher.sh`
`./lanucher.sh`

# Credits

## Code

J. Sanchez, Z80 core: [Z80](https://github.com/jsanchezv/Z80Core)

Mark Longstaff-Tyrrell: [Sound implementation](https://github.com/frisnit/pacman-emulator)

MAME team,
romSet definition and code reference: [pacman.cpp](https://github.com/mamedev/mame/blob/master/src/mame/drivers/pacman.cpp)

## Docs

Pac-Man Emulation Guide
Chris Lomont, www.lomont.org, v0.1, October 2008

Alessandro Scotti, [Pacman hardware](https://www.walkofmind.com/programming/pie/hardware.htm)

Simon Owen, [Pac-Man Emulator](https://simonowen.com/articles/pacemu/)

# Supported romSets

## Pac Man

    RomInfo{fileName='pacman.6e', sha1='e87e059c5be45753f7e9f33dff851f16d6751181', romStart=0, romEnd=4096, type=CPU}
    RomInfo{fileName='pacman.6f', sha1='674d3a7f00d8be5e38b1fdc208ebef5a92d38329', romStart=4096, romEnd=8192, type=CPU}
    RomInfo{fileName='pacman.6h', sha1='8e47e8c2c4d6117d174cdac150392042d3e0a881', romStart=8192, romEnd=12288, type=CPU}
    RomInfo{fileName='pacman.6j', sha1='d4a70d56bb01d27d094d73db8667ffb00ca69cb9', romStart=12288, romEnd=16384, type=CPU}
    RomInfo{fileName='82s126.1m', sha1='bbcec0570aeceb582ff8238a4bc8546a23430081', romStart=0, romEnd=256, type=SOUND}
    RomInfo{fileName='82s126.3m', sha1='0c4d0bee858b97632411c440bea6948a74759746', romStart=256, romEnd=512, type=SOUND}
    RomInfo{fileName='pacman.5e', sha1='06ef227747a440831c9a3a613b76693d52a2f0a9', romStart=-1, romEnd=-1, type=TILE}
    RomInfo{fileName='pacman.5f', sha1='4a937ac02216ea8c96477d4a15522070507fb599', romStart=-1, romEnd=-1, type=SPRITE}
    RomInfo{fileName='82s123.7f', sha1='8d0268dee78e47c712202b0ec4f1f51109b1f2a5', romStart=-1, romEnd=-1, type=CROM}
    RomInfo{fileName='82s126.4a', sha1='19097b5f60d1030f8b82d9f1d3a241f93e5c75d6', romStart=-1, romEnd=-1, type=PAL}

## Puck Man

    RomInfo{fileName='pm1-3.1m', sha1='bbcec0570aeceb582ff8238a4bc8546a23430081', romStart=0, romEnd=256, type=SOUND}
    RomInfo{fileName='pm1-2.3m', sha1='0c4d0bee858b97632411c440bea6948a74759746', romStart=256, romEnd=512, type=SOUND}
    RomInfo{fileName='pm1_prg1.6e', sha1='813cecf44bf5464b1aed64b36f5047e4c79ba176', romStart=0, romEnd=2048, type=CPU}
    RomInfo{fileName='pm1_prg2.6k', sha1='b9ca52b63a49ddece768378d331deebbe34fe177', romStart=2048, romEnd=4096, type=CPU}
    RomInfo{fileName='pm1_prg3.6f', sha1='9b5ddaaa8b564654f97af193dbcc29f81f230a25', romStart=4096, romEnd=6144, type=CPU}
    RomInfo{fileName='pm1_prg4.6m', sha1='c2f00e1773c6864435f29c8b7f44f2ef85d227d3', romStart=6144, romEnd=8192, type=CPU}
    RomInfo{fileName='pm1_prg5.6h', sha1='afe72fdfec66c145b53ed865f98734686b26e921', romStart=8192, romEnd=10240, type=CPU}
    RomInfo{fileName='pm1_prg6.6n', sha1='08759833f7e0690b2ccae573c929e2a48e5bde7f', romStart=10240, romEnd=12288, type=CPU}
    RomInfo{fileName='pm1_prg7.6j', sha1='d249fa9cdde774d5fee7258147cd25fa3f4dc2b3', romStart=12288, romEnd=14336, type=CPU}
    RomInfo{fileName='pm1_prg8.6p', sha1='eb462de79f49b7aa8adb0cc6d31535b10550c0ce', romStart=14336, romEnd=16384, type=CPU}
    RomInfo{fileName='pm1-1.7f', sha1='8d0268dee78e47c712202b0ec4f1f51109b1f2a5', romStart=-1, romEnd=-1, type=CROM}
    RomInfo{fileName='pm1-4.4a', sha1='19097b5f60d1030f8b82d9f1d3a241f93e5c75d6', romStart=-1, romEnd=-1, type=PAL}
    RomInfo{fileName='pm1_chg1.5e', sha1='6d4ccc27d6be185589e08aa9f18702b679e49a4a', romStart=0, romEnd=2048, type=TILE}
    RomInfo{fileName='pm1_chg2.5h', sha1='79bb456be6c39c1ccd7d077fbe181523131fb300', romStart=2048, romEnd=4096, type=TILE}
    RomInfo{fileName='pm1_chg3.5f', sha1='be933e691df4dbe7d12123913c3b7b7b585b7a35', romStart=0, romEnd=2048, type=SPRITE}
    RomInfo{fileName='pm1_chg4.5j', sha1='53771c573051db43e7185b1d188533056290a620', romStart=2048, romEnd=4096, type=SPRITE}
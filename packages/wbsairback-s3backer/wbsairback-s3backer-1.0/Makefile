VERSION = RELEASE

all: 

build:
	cd ./src/s3backer && ./cleanup.sh
	cd ./src/s3backer && ./autogen.sh
	cd ./src/s3backer && ./configure
	cd ./src/s3backer && make
	echo "Configuration package builded."

clean:
	echo ""

install:
	/bin/rm -rf $(DESTDIR)/usr/share/s3backer
	/bin/mkdir -p $(DESTDIR)/usr/share/s3backer
	/bin/cp -r src/s3backer/s3backer $(DESTDIR)/usr/share/s3backer/


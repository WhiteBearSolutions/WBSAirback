VERSION = RELEASE
MAKE = make

all: 

build:
	echo "Configuration package builded."

clean:
	$(MAKE) -C src clean

install:
	$(MAKE) -C src
	/bin/cp -fr etc/init.d $(DESTDIR)/etc/
	/bin/cp -fr etc/insserv $(DESTDIR)/etc/
	/bin/cp -f src/imaginewbs-config $(DESTDIR)/usr/sbin/
	/bin/cp -f etc/default/functions $(DESTDIR)/etc/default/functions	
	/bin/cp -fr usr/share/wbsairback $(DESTDIR)/usr/share/
	/bin/cp -f etc/ha.d/resource.d/LVS-DR $(DESTDIR)/etc/ha.d/resource.d/LVS-DR
	/bin/cp -f etc/ha.d/resource.d/PostgreIfRdata $(DESTDIR)/etc/ha.d/resource.d/PostgreIfRdata
	/bin/cp -f opt/bacula/bin/vsphere-ctl $(DESTDIR)/opt/bacula/bin
	/bin/cp -f etc/udev/rules.d/60-zvol.rules $(DESTDIR)/etc/udev/rules.d
	/bin/cp -f etc/udev/rules.d/69-vdev.rules $(DESTDIR)/etc/udev/rules.d
	/bin/cp -f etc/udev/rules.d/90-zfs.rules $(DESTDIR)/etc/udev/rules.d


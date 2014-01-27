
RCFILE = mhvtl
DEVICE_FILE = device.conf.default
USR ?= vtl
MHVTL_CONFIG_PATH ?= /etc/mhvtl

CONFIG_PATH = $(shell echo $(MHVTL_CONFIG_PATH) | sed -e s'/\//\\\//g')
$(RCFILE): $(RCFILE).in
	sed -e s'/@USR@/$(USR)/' $< > $@.1
	sed -e s'/@CONF_PATH@/$(CONFIG_PATH)/' $@.1 > $@
	rm -f $@.1

.PHONY: rcfile
rcfile: $(RCFILE)

.PHONY: distclean
distclean:
	rm -f $(RCFILE)

.PHONY: clean
clean:
	rm -f $(RCFILE)

.PHONY: install
install:
	install -d -m 755 $(DESTDIR)/etc
	install -d -m 755 $(DESTDIR)/etc/init.d
	install -d -m 755 $(DESTDIR)/etc/mhvtl
	install -m 700 $(RCFILE) $(DESTDIR)/etc/init.d/
	install -m 640 $(DEVICE_FILE) $(DESTDIR)/etc/mhvtl/
	#test ! -x /sbin/insserv || /sbin/insserv $(RCFILE)


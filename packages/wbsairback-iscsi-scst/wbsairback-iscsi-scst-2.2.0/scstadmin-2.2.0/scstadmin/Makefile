MODULE_VERSION = 0.9.10
TOOL = scstadmin

SBINDIR := /usr/sbin

all: perl-module

install: all
	$(MAKE) -C ./scst-$(MODULE_VERSION) install
	install -d $(DESTDIR)$(SBINDIR)
	install -m 755 $(TOOL) $(DESTDIR)$(SBINDIR)

uninstall:
	-rm -f $(DESTDIR)$(SBINDIR)/$(TOOL)
	$(MAKE) -C scst-$(MODULE_VERSION) uninstall

perl-module:
	cd $(shell pwd)/scst-$(MODULE_VERSION); \
	$(MAKE) -C $(shell pwd)/scst-$(MODULE_VERSION)

clean:
	-$(MAKE) -C scst-$(MODULE_VERSION) clean

distclean: clean
	-rm -f scst-$(MODULE_VERSION)/Makefile.old
	rm -f *.orig *.rej

extraclean: distclean

.PHONY: all install uninstall perl-module clean distclean extraclean

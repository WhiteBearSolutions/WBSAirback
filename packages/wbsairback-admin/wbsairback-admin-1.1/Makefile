VERSION = RELEASE
SHELL := /bin/bash
SOURCESJAVA=$(CURDIR)/sourcesJava.list
WBSAIRBACK_SRC=$(CURDIR)/../../../src/
WBSAIRBACK_BIN=$(CURDIR)/var/www/webadministration/WEB-INF/classes
LIB_PATH = $(shell find $(CURDIR)/var/www/webadministration/WEB-INF/lib/ -iname '*.jar' | tr "\n" ":")
SERVLET_PATH = $(CURDIR)/var/www/webadministration/servlet/servlet-api.jar

all: 

build:
	if [ ! -e $(WBSAIRBACK_BIN)  ]; then mkdir -p $(WBSAIRBACK_BIN); fi 
	
	find $(WBSAIRBACK_SRC) -name \*.java -print > $(SOURCESJAVA)
	
	echo -n "Compilando [WBSAirback]: "
	javac -g -cp $(LIB_PATH):$(SERVLET_PATH) -sourcepath $(WBSAIRBACK_SRC) -d $(WBSAIRBACK_BIN) @$(SOURCESJAVA)

	rm $(SOURCESJAVA);
	if [ $$? -gt 0 ]; then \
		echo -n "Errors building WBSAirback"; \
		false; \
	else \
		echo -n "WBSAirback compilado correctamente."; \
		true; \
	fi

clean:
	echo ""

install:
	/bin/cp -r var/axis2 $(DESTDIR)/var/
	/bin/cp -r var/www/webadministration $(DESTDIR)/var/www/
	/bin/cp -r etc/wbsairback-admin $(DESTDIR)/etc/
	/bin/cp usr/sbin/bacula-job $(DESTDIR)/usr/sbin/
	/bin/cp usr/sbin/gen-wsdl $(DESTDIR)/usr/sbin/
	/bin/cp usr/sbin/wbsairback-scsi-rescan $(DESTDIR)/usr/sbin/
	/bin/cp usr/sbin/wbsairback-scsi-login $(DESTDIR)/usr/sbin/
	/bin/cp usr/sbin/wbsairback-scsi-block $(DESTDIR)/usr/sbin/
	/bin/cp usr/sbin/baculaBase64Decode $(DESTDIR)/usr/sbin/
	/bin/cp etc/sudoers.d/bacula $(DESTDIR)/etc/sudoers.d/
	/bin/cp etc/init.d/wbsairback-admin $(DESTDIR)/etc/init.d/wbsairback-admin
	/bin/cp etc/init.d/wbs-watchdog $(DESTDIR)/etc/init.d/wbs-watchdog
	/bin/cp etc/init.d/rrdstats $(DESTDIR)/etc/init.d/rrdstats
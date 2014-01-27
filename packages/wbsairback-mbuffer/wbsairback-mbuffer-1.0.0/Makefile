CC		= gcc -std=gnu99
DEFS		= -DHAVE_CONFIG_H
CFLAGS		= -g -O2 $(DEFS) -I. -I.
LDFLAGS		= 
LIBS		= -lmhash -lm -lrt -lpthread 
EXE		= 
SHELL		= /bin/sh


prefix		= /usr/local
exec_prefix     = /usr/local
datarootdir	= ${prefix}/share
bindir          = ${exec_prefix}/bin
mandir		= ${datarootdir}/man/man1

RM		= /bin/rm
INSTALL		= /usr/bin/install -c

TARGET		= mbuffer$(EXE)
SOURCES		= log.c network.c mbuffer.c
OBJECTS		= $(SOURCES:.c=.o)

TESTTREE	= /opt/csw/gcc4

.PHONY: clean all distclean install check testcleanup

all: $(TARGET)

$(OBJECTS): config.h Makefile

$(TARGET): $(OBJECTS)
	$(CC) $(CFLAGS) $(LDFLAGS) $(OBJECTS) $(LIBS) -o $@

clean:
	-$(RM) $(OBJECTS)

distclean: clean
	-$(RM) $(TARGET) config.h config.log \
	config.status Makefile mbuffer.1 core

install: $(TARGET)
	-$(INSTALL) -d $(DESTDIR)$(bindir)
	$(INSTALL) $(TARGET) $(DESTDIR)$(bindir)
	-$(INSTALL) -d $(DESTDIR)$(mandir)
	$(INSTALL) mbuffer.1 $(DESTDIR)$(mandir)

lint:
	lint $(DEFS) $(SOURCES)

check: $(TARGET) test0 test1 test2 test3 testcleanup

testcleanup:
	rm -f test0 test1 test2 test3 test0.md5 test1.md5 test2.md5 test3.md5 \
		test.tar test.md5

test.tar:
	tar cf test.tar $(TESTTREE)

test.md5: test.tar
	openssl md5 < test.tar > test.md5

test0: test.md5
	./mbuffer -i test.tar -p10 | ./mbuffer -q -P 90 | openssl md5 > test0.md5
	sync
	diff test0.md5 test.md5
	touch test0

test1: test.md5
	./mbuffer -i test.tar -f -o $@.tar -o /dev/null -H
	openssl md5 < $@.tar > $@.md5
	rm -f $@.tar
	sync
	diff $@.md5 test.md5
	touch $@

test2: test.md5
	./mbuffer -q -I :8000 -o $@.tar -o - | openssl md5 > $@.md5 & \
		sleep 1; \
		rm -f $@.tar; \
		./mbuffer -i test.tar -o /dev/null -O localhost:8000 -H; \
		wait
	sync
	diff $@.md5 test.md5
	rm $@.md5
	openssl md5 < $@.tar > $@.md5 
	rm -f $@.tar
	sync
	diff $@.md5 test.md5
	touch $@

test3: test.md5
	./mbuffer -q -6 -I :8000 | openssl md5 > $@.md5 & \
		sleep 1; \
		rm -f $@.tar; \
		./mbuffer -i test.tar -o /dev/null -6 -O localhost:8000 -o $@.tar -H; \
		wait
	sync
	diff $@.md5 test.md5
	rm $@.md5
	openssl md5 < $@.tar > $@.md5 
	rm -f $@.tar
	sync
	diff $@.md5 test.md5
	touch $@


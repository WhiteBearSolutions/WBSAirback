CXX           = g++
CXXFLAGS      = -g3 -Wall $(LIBS) -DLCURSES=\"-lncurses\" -O2
CXXFLAGS      = -O2 -g -Wall -lmenu -lform -lncurses -lxml2 -lssl
LINK          = g++

OBJS =		main.o imaginewbs-window.o imaginewbs-login.o imaginewbs-message.o imaginewbs-status.o imaginewbs-menu.o imaginewbs-network.o imaginewbs-password.o system-tools.o

LIBS = -I/usr/include -I/usr/include/libxml2 -I. -Iinclude

TARGET = imaginewbs-config

$(TARGET):	$(OBJS)
	$(CXX) -o $(TARGET) $(CXXFLAGS) $(OBJS) $(LIBS)

imaginewbs-config.h:

system-tools.o: imaginewbs-config.h
	$(CXX) -c system-tools.cpp $(CXXFLAGS) $(LIBS)

imaginewbs-window.o: imaginewbs-config.h
	$(CXX) -c imaginewbs-window.cpp $(CXXFLAGS) $(LIBS)
	
imaginewbs-message.o: imaginewbs-config.h
	$(CXX) -c imaginewbs-message.cpp $(CXXFLAGS) $(LIBS)

imaginewbs-login.o: imaginewbs-window.o imaginewbs-config.h
	$(CXX) -c imaginewbs-login.cpp $(CXXFLAGS) $(LIBS)

imaginewbs-password.o: imaginewbs-window.o imaginewbs-config.h
	$(CXX) -c imaginewbs-password.cpp $(CXXFLAGS) $(LIBS)

imaginewbs-network.o: imaginewbs-window.o imaginewbs-config.h
	$(CXX) -c imaginewbs-network.cpp $(CXXFLAGS) $(LIBS)

imaginewbs-menu.o: imaginewbs-window.o imaginewbs-config.h
	$(CXX) -c imaginewbs-menu.cpp $(CXXFLAGS) $(LIBS)
	
imaginewbs-status.o: imaginewbs-window.o imaginewbs-config.h
	$(CXX) -c imaginewbs-status.cpp $(CXXFLAGS) $(LIBS)

main.o: main.cpp imaginewbs-window.o imaginewbs-config.h
	$(CXX) -c main.cpp $(CXXFLAGS) $(LIBS)

all:	$(TARGET)

clean:
	rm -f $(OBJS) $(TARGET)

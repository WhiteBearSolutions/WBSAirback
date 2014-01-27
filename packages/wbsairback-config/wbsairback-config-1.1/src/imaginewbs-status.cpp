/****************************************************************************
 **
 ** Copyright (C) 2003-2007 WhiteBearSolutions. All rights reserved.
 **
 ** This file may be used under the terms of the GNU General Public
 ** License version 2.0 as published by the Free Software Foundation
 ** and appearing in the file LICENSE.GPL included in the packaging of
 ** this file.  Please review the following information to ensure GNU
 ** General Public Licensing requirements will be met:
 ** http://www.trolltech.com/products/qt/opensource.html
 **
 ** If you are unsure which license is appropriate for your use, please
 ** review the following information:
 ** http://www.trolltech.com/products/qt/licensing.html or contact the
 ** sales department at sales@trolltech.com.
 **
 ** This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 ** WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 **
 ****************************************************************************/

#include <fstream>
#include <sstream>

#include "include/imaginewbs-config.h"

ImagineWBSStatus::ImagineWBSStatus(ImagineWBSWindow *window) {
        screen_title = "IMAGINE SYSTEM STATUS";
	setParentWindow(this);
	systemTools = new ImagineWBSSystemTools();
	display = true;
}

ImagineWBSStatus::~ImagineWBSStatus() {};

void ImagineWBSStatus::createWindow(void) {
	main_win = initscr();
	start_color();
	noecho();
	cbreak();
	keypad(stdscr, TRUE);
	nodelay(main_win, TRUE);

	//init_color(COLOR_BLUE, 42, 43, 66);
	init_pair(1, COLOR_WHITE, COLOR_BLUE);
	init_pair(2, COLOR_WHITE, COLOR_BLUE);
	init_pair(3, COLOR_WHITE, COLOR_BLACK);
	init_pair(4, COLOR_WHITE, COLOR_BLACK);

	color_set(COLOR_PAIR(1), NULL);
	wbkgd(main_win, COLOR_PAIR(3));
	bkgd(COLOR_PAIR(1)|' ');

	refresh();
	wrefresh(main_win);
	screen = newwin(20, 50, 2, 10);
	box(screen, ACS_VLINE, ACS_HLINE);
}

void ImagineWBSStatus::displayContent(void) {
	curs_set(0);
	print_in_middle(3, 6, 30, (char*)screen_title.c_str(), COLOR_PAIR(1));
        mvwprintw(screen,5,6,"System: %s", systemName.c_str());
        mvwprintw(screen,6,6,"System mode: %s", systemMode.c_str());
        mvwprintw(screen,8,6,"Current date/time: %d-%d-%d %d:%d:%d", now_day, now_month, now_year, now_hour, now_min, now_sec);
        mvwprintw(screen,9,6,"Uptime: %s", upTime.c_str());
        mvwprintw(screen,11,6,"               ");
        mvwprintw(screen,11,6,"CPU load: %s", systemLoad.c_str());
        mvwprintw(screen,12,6,"Total memory: %s", totalMemory.c_str());
        mvwprintw(screen,13,6,"Free memory: %s", freeMemory.c_str());
        mvwprintw(screen,15,6,"Management: http(s)://%s/", networkAddress.c_str());
        mvprintw(LINES - 3, 0, "                                    ");
	mvprintw(LINES - 3, 0, "Press <F8> to menu");
	wrefresh(screen);
	refresh();
};

void ImagineWBSStatus::hideContent(void) {};

void ImagineWBSStatus::print_in_middle(int starty, int startx, int width, char* string, chtype color) {
	int length, x, y;
	float temp;

	if(screen == NULL) {
		screen = stdscr;
	}
	getyx(screen, y, x);
	if(startx != 0) {
		x = startx;
	}
	if(starty != 0) {
		y = starty;
	}
	if(width == 0) {
		width = 80;
	}

	length = strlen(string);
	temp = (width - length)/ 2;
	x = startx + (int)temp;
	wattron(screen, color);
	wattron(screen, A_BOLD);
	mvwprintw(screen, y, x, "%s", string);
	wattroff(screen, A_BOLD);
	wattroff(screen, color);
	//wborder(win, '|', 'a', '=','c','d','e','f','g');
	wrefresh(screen);
}

void ImagineWBSStatus::destroyWindow(void) {
   endwin();
}

void ImagineWBSStatus::maketime(void) {
	now = time(NULL);
	now_tm = localtime(&now);
    now_sec = now_tm->tm_sec;
	now_min = now_tm->tm_min;
	now_hour = now_tm->tm_hour;
	now_day = now_tm->tm_mday;
	now_wday = now_tm->tm_wday;
	now_month = now_tm->tm_mon + 1;
	now_year = now_tm->tm_year + 1900;
}

void ImagineWBSStatus::show(void) {
	createWindow();

	while(display) {
		maketime();
                loadSystemData();
                displayContent();

		switch(getch()) {
			case KEY_F(8): {
                                        if(systemTools->isFile("/etc/wbsagnitio-admin/config.xml")) {
                                                ImagineWBSLogin *login = new ImagineWBSLogin(this);
                                                login->show();
                                                free(login);
                                        } else if(systemTools->isFile("/etc/wbsairback-admin/config.xml")) {
                                                ImagineWBSLogin *login = new ImagineWBSLogin(this);
                                                login->show();
                                                free(login);
                                        } else {
                                                ImagineWBSMenu *config_menu = new ImagineWBSMenu(this);
                                                config_menu->show();
                                                free(config_menu);
                                                display = false;
                                        }
				}
				break;
			case 298:
				display = false;
				break;
		}
		sleep(1);
	}
	destroyWindow();
}

void ImagineWBSStatus::loadSystemData(void) {
        int _value;
        string line;

        ifstream mem ("/proc/meminfo");
        if(mem.is_open()) {
                while(!mem.eof()) {
                        getline(mem, line);
                        if(line.find("MemTotal") != string::npos) {
                                line = line.substr(9, line.size() - 2);
                                line = systemTools->trim(line);
                                _value = atoi(line.c_str()) / 1000;

                                std:: stringstream _svalue;
                                _svalue << _value << " MB";
                                totalMemory = _svalue.str();
                        } else if(line.find("MemFree") != string::npos) {
                                line = line.substr(8, line.size() - 2);
                                line = systemTools->trim(line);
                                _value = atoi(line.c_str()) / 1000;

                                std:: stringstream _svalue;
                                _svalue << _value << " MB";
                                freeMemory = _svalue.str();
                        }
                }
                mem.close();
        }

        ifstream stat ("/proc/stat");
        if(stat.is_open()) {
                while(!stat.eof()) {
                        getline(stat, line);
                        if(line.find("btime") != string::npos) {
                                line = line.substr(5, line.size());
                                _value = atoi(line.c_str());

                                _value = (time(NULL) - _value) / 3600;

                                stringstream _svalue;
                                _svalue << _value << " hours";
                                upTime = _svalue.str();
                        } else if(line.find("cpu ") != string::npos) {
                                int _old_CPUStat[sizeof(CPUstat)];
                                vector<string> _tokens = systemTools->stringSpaceTokenizer((char*)line.c_str(), (char*)" ");

                                memcpy(_old_CPUStat, CPUstat, sizeof(_old_CPUStat));

                                for(int i = 0; i < 4; i++) {
                                        CPUstat[i] = atoi(_tokens.at(i + 1).c_str());
                                        _old_CPUStat[i] = CPUstat[i] - _old_CPUStat[i];
                                }

                                _value = 0;
                                for(int i = 0; i < 4; i++) {
                                        _value += _old_CPUStat[i];
                                }
                                _value = 100 - ((_old_CPUStat[3] * 100) / _value);

                                stringstream _svalue;
                                _svalue << _value << "%";
                                systemLoad = _svalue.str();
                        }
                }
                stat.close();
        }

        if(systemName.empty() && systemMode.empty()) {
                systemName = "ImagineWBS";
                systemMode = "standalone";

                if(systemTools->isDirectory("/etc/wbsagnitio-admin")) {
                        systemName = "WBSAgnitio";
                        string _status = systemTools->getXMLProperty((char*)"/etc/wbsagnitio-admin/config.xml", (char*)"directory.status");
                        if(!_status.empty()) {
                                systemMode = _status;
                        }
                } else if(systemTools->isDirectory("/etc/wbsairback-admin")) {
                        systemName = "WBSAirback";
                        string _status = systemTools->getXMLProperty((char*)"/etc/wbsairback-admin/config.xml", (char*)"directory.status");
                        if(!_status.empty()) {
                                systemMode = _status;
                        }
                }
        }

        networkAddress = "";
        vector<string> _address = systemTools->getNetworkData();
        if(_address.size() > 0) {
                networkAddress += _address[0];
                networkAddress += ".";
                networkAddress += _address[1];
                networkAddress += ".";
                networkAddress += _address[2];
                networkAddress += ".";
                networkAddress += _address[3];
        }
}


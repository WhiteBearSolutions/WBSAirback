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

#include "include/imaginewbs-config.h"

#define ARRAY_SIZE(a) (sizeof(a) / sizeof(a[0]))

ImagineWBSNetworkForm::ImagineWBSNetworkForm(ImagineWBSWindow *window) {
	setParentWindow(window);
	/* Initialize colors */
	init_pair(1, COLOR_WHITE, COLOR_BLUE);
	init_pair(2, COLOR_WHITE, COLOR_BLUE);
	init_pair(3, COLOR_WHITE, COLOR_BLACK);
	init_pair(4, COLOR_WHITE, COLOR_BLACK);
	systemTools = new ImagineWBSSystemTools();
	/* Initialize the fields */
	fields[0] = new_field(1, 6, 0, 15, 0, 0);
	fields[1] = new_field(1, 4, 2, 15, 0, 0);
	fields[2] = new_field(1, 4, 2, 20, 0, 0);
	fields[3] = new_field(1, 4, 2, 25, 0, 0);
	fields[4] = new_field(1, 4, 2, 30, 0, 0);
	fields[5] = new_field(1, 4, 4, 15, 0, 0);
	fields[6] = new_field(1, 4, 4, 20, 0, 0);
	fields[7] = new_field(1, 4, 4, 25, 0, 0);
	fields[8] = new_field(1, 4, 4, 30, 0, 0);
	fields[9] = new_field(1, 4, 6, 15, 0, 0);
	fields[10] = new_field(1, 4, 6, 20, 0, 0);
	fields[11] = new_field(1, 4, 6, 25, 0, 0);
	fields[12] = new_field(1, 4, 6, 30, 0, 0);
	fields[13] = NULL;
	int nfields = ARRAY_SIZE(fields) - 1;
	for(int i = 0; i < nfields; i++) {
		set_field_back(fields[i], COLOR_PAIR(2));
		//set_field_fore(fields[i], COLOR_PAIR(3));
		field_opts_off(fields[i], O_AUTOSKIP);
		//field_opts_off(fields[i], O_BLANK);
		if(i == 0) {
			set_field_type(fields[i], TYPE_REGEXP, "[a-z0-9]{3,7}");
		} else {
			set_field_type(fields[i], TYPE_REGEXP, "[0-9]{1,3}");
		}
	}
	form = new_form(fields);
	display = true;
}

ImagineWBSNetworkForm::~ImagineWBSNetworkForm(void) {
	int nfields = ARRAY_SIZE(fields) - 1;
	free_form(form);
	for(int i = 0; i < nfields; i++) {
		free_field(fields[i]);
	}
};

void ImagineWBSNetworkForm::createWindow(void) {
	form_win = newwin(14, 46, 6, 20);
	keypad(form_win, TRUE);

	wattron(form_win, COLOR_PAIR(3));
	box(form_win, 0, 0);
	wattroff(form_win, COLOR_PAIR(3));

	mvprintw(LINES - 3, 0, "Press <ENTER> to save network data");
	refresh();
}

void ImagineWBSNetworkForm::loadNetworkData(void) {
	string line;

	ifstream network ("/etc/network/interfaces");
	if(network.is_open()) {
		while(!network.eof()) {
			getline(network, line);
			if(line.find("iface ") != string::npos) {
				line = line.substr(6, line.find(' ', 6) - 6);
				if(line.compare("lo") == 0) {
					continue;
				}
				set_field_buffer(fields[0], 0, line.c_str());
				while(!network.eof()) {
					getline(network, line);
					if(line.length() == 0) {
						break;
					} else if(line.find("address ") != string::npos) {
						line = line.substr(8, line.size() - 8);
						line = systemTools->trim(line);
						vector<string> _values = systemTools->stringSpaceTokenizer((char*)line.c_str(), (char*)".");
						int _size = _values.size();
						for(int i = 0; i < _size; i++) {
							set_field_buffer(fields[i + 1], 0, _values.at(i).c_str());
						}
					} else if(line.find("netmask ") != string::npos) {
						line = line.substr(8, line.size() - 8);
						line = systemTools->trim(line);
						vector<string> _values = systemTools->stringSpaceTokenizer((char*)line.c_str(), (char*)".");
						int _size = _values.size();
						for(int i = 0; i < _size; i++) {
							set_field_buffer(fields[i + 5], 0, _values.at(i).c_str());
						}
					}  else if(line.find("gateway ") != string::npos) {
						line = line.substr(8, line.size() - 8);
						line = systemTools->trim(line);
						vector<string> _values = systemTools->stringSpaceTokenizer((char*)line.c_str(), (char*)".");
						int _size = _values.size();
						for(int i = 0; i < _size; i++) {
							set_field_buffer(fields[i + 9], 0, _values.at(i).c_str());
						}
					}
				}
				break;
			}
		}
		network.close();
	}
}

void ImagineWBSNetworkForm::writeNetworkData(void) {
	ofstream network ("/etc/network/interfaces", ios::out | ios::binary);
	if(network) {
		network << "auto lo" << endl;
		network << "iface lo inet loopback" << endl;
		network << endl;
		network << "auto " << systemTools->trim(field_buffer(fields[0],0)) << endl;
		network << "iface " << systemTools->trim(field_buffer(fields[0],0)) << " inet static" << endl;
		network << "\t" << "address ";
		network << systemTools->trim(field_buffer(fields[1],0)) << ".";
		network << systemTools->trim(field_buffer(fields[2],0)) << ".";
		network << systemTools->trim(field_buffer(fields[3],0)) << ".";
		network << systemTools->trim(field_buffer(fields[4],0)) << endl;
		network << "\t" << "netmask ";
		network << systemTools->trim(field_buffer(fields[5],0)) << ".";
		network << systemTools->trim(field_buffer(fields[6],0)) << ".";
		network << systemTools->trim(field_buffer(fields[7],0)) << ".";
		network << systemTools->trim(field_buffer(fields[8],0)) << endl;
		network << "\t" << "gateway ";
		network << systemTools->trim(field_buffer(fields[9],0)) << ".";
		network << systemTools->trim(field_buffer(fields[10],0)) << ".";
		network << systemTools->trim(field_buffer(fields[11],0)) << ".";
		network << systemTools->trim(field_buffer(fields[12],0)) << endl;
		network << endl;

		network.close();
	}
}

void ImagineWBSNetworkForm::destroyWindow(void) {
	endwin();
	werase(form_win);
	wrefresh(form_win);
	repaintParentWindow();
}

void ImagineWBSNetworkForm::displayContent(void) {
	set_form_win(form, form_win);
	set_form_sub(form, derwin(form_win, 10, 42, 2, 2));
	post_form(form);

	mvwprintw(form_win, 2, 5, "Interface:");
	mvwprintw(form_win, 4, 7, "Address:");
	mvwprintw(form_win, 6, 7, "Netmask:");
	mvwprintw(form_win, 8, 7, "Gateway:");
	mvwprintw(form_win, 12, 5, "Fill network data and press <ENTER>");
	wrefresh(form_win);
	refresh();
}

void ImagineWBSNetworkForm::hideContent(void) {
	unpost_form(form);
}

void ImagineWBSNetworkForm::show(void) {
	createWindow();
	loadNetworkData();
	displayContent();

	/* Loop through to get user requests */
	while(display) {
		int c = wgetch(form_win);
		switch(c) {
			case KEY_DOWN:
				nextField();
				break;
			case KEY_RIGHT:
				nextField();
				break;
			case KEY_UP:
				backField();
				break;
			case KEY_LEFT:
				backField();
				break;
			case 9:
				nextField();
				break;
			case 46:
				nextField();
				break;
			case KEY_DC:
				form_driver(form, REQ_DEL_LINE);
				break;
			case KEY_BACKSPACE:
				form_driver(form, REQ_DEL_LINE);
				break;
			case 27:
				display = false;
				break;
			case 10: {
				form_driver(form, REQ_VALIDATION);
				writeNetworkData();
				ImagineWBSMessage *_m = new ImagineWBSMessage(this);
				_m->setMessage((char*)"Network configuration saved");
				_m->show();
				free(_m);
				display = false;
				}
				break;
			default:
				//FIELD* _field = current_field(form);
				if(isdigit(c)) {
					form_driver(form, c);
				}
				break;
		}
	}

	hideContent();
	destroyWindow();
}

void ImagineWBSNetworkForm::nextField(void) {
	form_driver(form, REQ_NEXT_FIELD);
	form_driver(form, REQ_END_LINE);
}

void ImagineWBSNetworkForm::backField(void) {
	form_driver(form, REQ_PREV_FIELD);
	form_driver(form, REQ_END_LINE);
}

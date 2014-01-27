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

ImagineWBSLogin::ImagineWBSLogin(ImagineWBSWindow *window) {
	setParentWindow(window);
	/* Initialize colors */
	init_pair(1, COLOR_WHITE, COLOR_BLUE);
	init_pair(2, COLOR_WHITE, COLOR_BLUE);
	init_pair(3, COLOR_WHITE, COLOR_BLACK);
	init_pair(4, COLOR_WHITE, COLOR_BLACK);
	systemTools = new ImagineWBSSystemTools();
	/* Initialize the fields */
	fields[0] = new_field(1, 10, 0, 15, 0, 0);
	fields[1] = NULL;
	int nfields = ARRAY_SIZE(fields) - 1;
	for(int i = 0; i < nfields; i++) {
		set_field_back(fields[i], COLOR_PAIR(2));
		//set_field_fore(fields[i], COLOR_PAIR(3));
		field_opts_off(fields[i], O_AUTOSKIP);
		//field_opts_off(fields[i], O_BLANK);
		set_field_type(fields[i], TYPE_REGEXP, "[0-9]{1,3}");
	}
	form = new_form(fields);
	display = true;
	validated = false;
}

ImagineWBSLogin::~ImagineWBSLogin(void) {
	int nfields = ARRAY_SIZE(fields) - 1;
	free_form(form);
	for(int i = 0; i < nfields; i++) {
		free_field(fields[i]);
	}
};

void ImagineWBSLogin::createWindow(void) {
	form_win = newwin(6, 36, 6, 20);
	keypad(form_win, TRUE);

	wattron(form_win, COLOR_PAIR(3));
	box(form_win, 0, 0);
	wattroff(form_win, COLOR_PAIR(3));

	mvprintw(LINES - 3, 0, "                                      ");
	mvprintw(LINES - 3, 0, "Press <ENTER> to verify password");
	refresh();
}

bool ImagineWBSLogin::verifyPassword(void) {
	string password = systemTools->trim(field_buffer(fields[0],0));
	return systemTools->verifyRootPassword((char*)password.c_str());
}

void ImagineWBSLogin::destroyWindow(void) {
	endwin();
	werase(form_win);
	wrefresh(form_win);
	repaintParentWindow();
}

void ImagineWBSLogin::displayContent(void) {
	set_form_win(form, form_win);
	set_form_sub(form, derwin(form_win, 3, 30, 2, 2));
	post_form(form);

	mvwprintw(form_win, 2, 5, "Password:");
	wrefresh(form_win);
	refresh();
}

void ImagineWBSLogin::hideContent(void) {
	unpost_form(form);
}

void ImagineWBSLogin::show(void) {
	createWindow();
	displayContent();

	/* Loop through to get user requests */
	while(display) {
		int c = wgetch(form_win);
		switch(c) {
			case 27:
				display = false;
				break;
			case 10: {
				form_driver(form, REQ_VALIDATION);
				if(verifyPassword()) {
					validated = true;
					display = false;
				} else {
					ImagineWBSMessage *_m = new ImagineWBSMessage(this);
					_m->setMessage((char*)"Root password incorrect!");
					_m->show();
					free(_m);
					display = false;
				}
				}
				break;
			default:
				//FIELD* _field = current_field(form);
				if(isalnum(c)) {
					form_driver(form, c);
				}
				break;
		}
	}

	hideContent();
	destroyWindow();

	if(validated) {
		ImagineWBSMenu *config_menu = new ImagineWBSMenu(getParentWindow());
		config_menu->show();
		free(config_menu);
	}
}

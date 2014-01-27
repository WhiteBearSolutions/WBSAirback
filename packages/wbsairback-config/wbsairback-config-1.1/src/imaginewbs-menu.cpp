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

#include "include/imaginewbs-config.h"

#define ARRAY_SIZE(a) (sizeof(a) / sizeof(a[0]))
#define CTRLD 4

ImagineWBSMenu::ImagineWBSMenu(ImagineWBSWindow *window) {
	menu_title = "IMAGINEWBS SYSTEM MENU";
	setParentWindow(window);
	systemTools = new ImagineWBSSystemTools();
	choices[0] = "Network configuration";
	choices[1] = "Set root password";
	choices[2] = "Restart web administration";
	choices[3] = "Reboot system";
	choices[4] = "Shutdown system";
	choices[5] = "Exit";
	items = (ITEM **)calloc(sizeof(choices) + 1, sizeof(ITEM *));
	display = true;
 }

ImagineWBSMenu::~ImagineWBSMenu() {};

void ImagineWBSMenu::show(void) {
	ITEM *cur_item;

	createWindow();
	displayContent();

	while(display) {
		switch(wgetch(menu_win)) {
			case KEY_DOWN:
				menu_driver(menu, REQ_DOWN_ITEM);
				break;
			case KEY_UP:
				menu_driver(menu, REQ_UP_ITEM);
				break;
			case 10:
				cur_item = current_item(menu);
				//item_name(cur_item);
				//item_description(cur_item);
				switch(item_index(cur_item)) {
					case 0: {
						ImagineWBSNetworkForm *networkForm = new ImagineWBSNetworkForm(this);
						networkForm->show();
						free(networkForm);
						}
						break;
					case 1: {
						ImagineWBSPassword *_p = new ImagineWBSPassword(this);
						_p->show();
						free(_p);
						}
						break;
					case 2: {
						if(systemTools->isDirectory("/etc/wbsagnitio-admin")) {
							systemTools->executeCommand("/etc/init.d/wbsagnitio-admin restart");
						} else if(systemTools->isDirectory("/etc/wbsairback-admin")) {
							systemTools->executeCommand("/etc/init.d/wbsairback-admin restart");
						}
						ImagineWBSMessage *_m = new ImagineWBSMessage(this);
						_m->setMessage((char*)"Web administration restarted");
						_m->show();
						free(_m);
						display = false;
						}
						break;
					case 3: {
						systemTools->executeCommand("reboot");
						ImagineWBSMessage *_m = new ImagineWBSMessage(this);
						_m->setMessage((char*)"Reboot started");
						_m->show();
						free(_m);
						display = false;
						}
						break;
					case 4: {
						systemTools->executeCommand("shutdown -h 0");
						ImagineWBSMessage *_m = new ImagineWBSMessage(this);
						_m->setMessage((char*)"Shutdown started");
						_m->show();
						free(_m);
						display = false;
						}
						break;
					case 5:
						display = false;
						break;
				}
				break;

		}
		wrefresh(menu_win);
	}

	hideContent();
	destroyWindow();
}

void ImagineWBSMenu::displayContent(void) {
	int schoices = ARRAY_SIZE(choices);

	/* Create items */
	for(int i = 0; i < schoices; ++i) {
		items[i] = new_item(" ", (char*)choices[i].c_str());
	}

	items[schoices] = (ITEM *)NULL;

	/* Crate menu */
	menu = new_menu((ITEM **)items);

	/* Set main window and sub window */
	set_menu_win(menu, menu_win);
	set_menu_sub(menu, derwin(menu_win, 6, 58, 3, 1));

	/* Set menu mark to the string " > " */
	set_menu_mark(menu, "  ");

	/* Set fore ground and back ground of the menu */
	set_menu_fore(menu, COLOR_PAIR(2) | A_REVERSE);
	set_menu_back(menu, COLOR_PAIR(3));
	set_menu_grey(menu, COLOR_PAIR(4));

	/* Post the menu */
	post_menu(menu);
	wrefresh(menu_win);
}

void ImagineWBSMenu::hideContent(void) {
	int schoices = ARRAY_SIZE(choices);

	/* Unpost and free all the memory taken up */
	unpost_menu(menu);
	free_menu(menu);
	for(int i = 0; i < schoices; ++i) {
		free_item(items[i]);
	}
}

void ImagineWBSMenu::createWindow(void) {
	menu_win = newwin(12, 60, 4, 14);
	keypad(menu_win, TRUE);

	/* Initialize colors */
	init_pair(1, COLOR_WHITE, COLOR_BLUE);
	init_pair(2, COLOR_WHITE, COLOR_BLUE);
	init_pair(3, COLOR_WHITE, COLOR_BLACK);
	init_pair(4, COLOR_WHITE, COLOR_BLACK);

	box(menu_win, 0, 0);
	print_in_middle(1, 0, 60, (char*)menu_title.c_str(), COLOR_PAIR(1));
	mvwaddch(menu_win, 2, 0, ACS_LTEE);
	mvwhline(menu_win, 2, 1, ACS_HLINE, 58);
	mvwaddch(menu_win, 2, 59, ACS_RTEE);
	mvprintw(LINES - 3, 0, "Press <ENTER> to select an option");
	//mvprintw(LINES - 2, 0, "F10 to exit");
	refresh();
}

void ImagineWBSMenu::destroyWindow(void) {
	endwin();
	werase(menu_win);
	wrefresh(menu_win);
	getParentWindow()->show();
}

void ImagineWBSMenu::print_in_middle(int starty, int startx, int width, char* string, chtype color) {
	int length, x, y;
	float temp;

	if(menu_win == NULL) {
		menu_win = stdscr;
	}
	getyx(menu_win, y, x);
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
	wattron(menu_win, color);
	wattron(menu_win, A_BOLD);
	mvwprintw(menu_win, y, x, "%s", string);
	wattroff(menu_win, A_BOLD);
	wattroff(menu_win, color);
	//wborder(win, '|', 'a', '=','c','d','e','f','g');
	wrefresh(menu_win);
}


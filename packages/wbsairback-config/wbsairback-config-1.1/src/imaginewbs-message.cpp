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

ImagineWBSMessage::ImagineWBSMessage(ImagineWBSWindow *window) {
	setParentWindow(window);
	/* Initialize colors */
	init_pair(1, COLOR_WHITE, COLOR_BLUE);
	init_pair(2, COLOR_WHITE, COLOR_BLUE);
	init_pair(3, COLOR_WHITE, COLOR_BLACK);
	init_pair(4, COLOR_WHITE, COLOR_BLACK);
	display = true;
}

ImagineWBSMessage::~ImagineWBSMessage(void) {
};

void ImagineWBSMessage::setMessage(char* message) {
	_message = message;
}

void ImagineWBSMessage::createWindow(void) {
	message_win = newwin(5, _message.size() + 4, 8, 25);
	keypad(message_win, TRUE);

	wattron(message_win, COLOR_PAIR(3));
	box(message_win, 0, 0);
	wattroff(message_win, COLOR_PAIR(3));
	refresh();
}

void ImagineWBSMessage::destroyWindow(void) {
	endwin();
	werase(message_win);
	wrefresh(message_win);
	repaintParentWindow();
}

void ImagineWBSMessage::displayContent(void) {
	mvwprintw(message_win, 2, 2, _message.c_str());
	wrefresh(message_win);
	refresh();
}

void ImagineWBSMessage::hideContent(void) {
}

void ImagineWBSMessage::show(void) {
	createWindow();
	displayContent();

	sleep(1);

	hideContent();
	destroyWindow();
}

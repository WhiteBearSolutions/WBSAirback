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

#include <stdio.h>
#include <iostream>
#include <string>
#include <string.h>
#include <vector>
#include <algorithm>
#include <dirent.h>
#include <menu.h>
#include <form.h>
#include <libxml/parser.h>
#include <libxml/tree.h>
#include <openssl/evp.h>
#include <openssl/bio.h>
#include <openssl/buffer.h>

using namespace std;
class ImagineWBSWindow {
	public:
		ImagineWBSWindow(void);
		~ImagineWBSWindow();
		ImagineWBSWindow(ImagineWBSWindow *window);

		virtual void createWindow(void) = 0;
		virtual void displayContent(void) = 0;
		virtual void hideContent(void) = 0;
		virtual void destroyWindow(void) = 0;
		virtual void show(void) = 0;

		ImagineWBSWindow *getParentWindow(void);
		void setParentWindow(ImagineWBSWindow *window);
		void repaintParentWindow();
	protected:

	private:
		ImagineWBSWindow *parent;
};

class ImagineWBSSystemTools {
	public:
		ImagineWBSSystemTools(void);
		~ImagineWBSSystemTools();
		int executeCommand(const char* command);
		int commandExitStatus(void);
		bool isCommandRunning(void);
		const char* readCommandOutput(void);
		vector<string> stringSpaceTokenizer(char* value, char* delim);
		bool isDirectory(const char* path);
		bool isFile(const char* path);
		string trim(string value);
		void changeRootPassword(char* value);
		bool verifyRootPassword(char* value);
		string getXMLProperty(char* file, char* property);
		void setXMLProperty(char* file, char* property, string value);
		string MD5digest(char* data);
		string base64encode(char* data);
		vector<string> getNetworkData(void);

	protected:
		const char* getFileContent(char*);

		string COMMAND_OUTPUT_FILE;
		string COMMAND_LOCK_FILE;
		string COMMAND_STATUS_FILE;

	private:
		int command_exit_status;
};

class ImagineWBSMessage : public ImagineWBSWindow {

 public:
	ImagineWBSMessage(ImagineWBSWindow *window);
	~ImagineWBSMessage();
	void createWindow(void);
	void displayContent(void);
	void hideContent(void);
	void destroyWindow(void);
	void show(void);

	void setMessage(char* message);

 protected:
	bool display;

 private:
	WINDOW *message_win;

	string _message;
};

class ImagineWBSLogin : public ImagineWBSWindow {

 public:
	ImagineWBSLogin(ImagineWBSWindow *window);
	~ImagineWBSLogin();
	void createWindow(void);
	void displayContent(void);
	void hideContent(void);
	void destroyWindow(void);
	void show(void);

 protected:
	bool display;
	bool validated;
	ImagineWBSSystemTools *systemTools;

 private:
	bool verifyPassword(void);

	FIELD *fields[2];
	FORM  *form;
	WINDOW *form_win;
};

class ImagineWBSPassword : public ImagineWBSWindow {

 public:
	ImagineWBSPassword(ImagineWBSWindow *window);
	~ImagineWBSPassword();
	void createWindow(void);
	void displayContent(void);
	void hideContent(void);
	void destroyWindow(void);
	void show(void);

 protected:
	bool display;
	ImagineWBSSystemTools *systemTools;

 private:
	void writePassword(void);

	FIELD *fields[2];
	FORM  *form;
	WINDOW *form_win;
};

class ImagineWBSNetworkForm : public ImagineWBSWindow {

 public:
	ImagineWBSNetworkForm(ImagineWBSWindow *window);
	~ImagineWBSNetworkForm();
	void createWindow(void);
	void displayContent(void);
	void hideContent(void);
	void destroyWindow(void);
	void show(void);

 protected:
	bool display;
	ImagineWBSSystemTools *systemTools;

 private:
	void print_in_middle(int starty, int startx, int width, char *string, chtype color);
	void nextField(void);
	void backField(void);
	void loadNetworkData(void);
	void writeNetworkData(void);

	FIELD *fields[14];
	FORM  *form;
	WINDOW *form_win;
};

class ImagineWBSMenu : public ImagineWBSWindow {

 public:
	ImagineWBSMenu(ImagineWBSWindow *window);
	~ImagineWBSMenu();
	void createWindow(void);
	void displayContent(void);
	void hideContent(void);
	void destroyWindow(void);
	void show(void);

 protected:
	bool display;
	ImagineWBSSystemTools *systemTools;

 private:
	void print_in_middle(int starty, int startx, int width, char *string, chtype color);

	MENU *menu;
	ITEM **items;
	WINDOW *menu_win;
	string menu_title;
	string choices[6];
};

class ImagineWBSStatus : public ImagineWBSWindow {

 public:
	ImagineWBSStatus(ImagineWBSWindow *window);
	~ImagineWBSStatus();
	void createWindow(void);
	void displayContent(void);
	void hideContent(void);
	void destroyWindow(void);
	void show();

 protected:
	bool display;
	ImagineWBSSystemTools *systemTools;

 private:
	void maketime(void);
	void loadSystemData(void);
	void print_in_middle(int starty, int startx, int width, char *string, chtype color);

	WINDOW *main_win;
	WINDOW *screen;

	int now_sec, now_min, now_hour, now_day, now_wday, now_month, now_year;
	time_t now;
	struct tm *now_tm;

	string screen_title;
	string systemName;
	string systemMode;
	string upTime;
	string systemLoad;
	string totalMemory;
	string freeMemory;
	string networkAddress;

	int CPUstat[4];
};


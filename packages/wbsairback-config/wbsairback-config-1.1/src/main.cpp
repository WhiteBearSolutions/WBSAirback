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
#include <stdlib.h>
#include <signal.h>

#include "include/imaginewbs-config.h"

 void catchTermination(int param) {
     //std::cout << "termination ignored\n";
 }

 int main(int argc, char *argv[]) {
	 signal(SIGABRT, catchTermination);
	 signal(SIGTERM, catchTermination);
	 signal(SIGINT, catchTermination);
	 ImagineWBSStatus *status = new ImagineWBSStatus(NULL);
	 status->show();
     return (0);
 }

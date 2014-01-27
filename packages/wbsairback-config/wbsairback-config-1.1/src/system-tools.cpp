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
#include <vector>

#include "include/imaginewbs-config.h"

ImagineWBSSystemTools::ImagineWBSSystemTools() {
	COMMAND_OUTPUT_FILE = "/tmp/command_output";
	COMMAND_LOCK_FILE = "/tmp/command_lock";
	COMMAND_STATUS_FILE = "/tmp/command_status";
	command_exit_status = -1;
};

ImagineWBSSystemTools::~ImagineWBSSystemTools() {};

const char* ImagineWBSSystemTools::getFileContent(char* filePath) {
	string line;
	stringstream _out;
	ifstream file (filePath);
	if(file.is_open()) {
		while(!file.eof()) {
			getline(file, line);
			_out << line << endl;
		}
		file.close();
	}

	return _out.str().c_str();
}

int ImagineWBSSystemTools::executeCommand(const char* command) {
	if(isCommandRunning()) {
	  return -1;
	}
	stringstream _command;
	_command << "touch " << COMMAND_LOCK_FILE << " && ";
	_command << command << " > " << COMMAND_OUTPUT_FILE;
	_command << " && echo $? > " << COMMAND_STATUS_FILE;
	_command << " && rm -f " << COMMAND_LOCK_FILE;
	_command << " || echo $? > " << COMMAND_STATUS_FILE;
	_command << " && rm -f " << COMMAND_LOCK_FILE << " &";
	int _result = system(_command.str().c_str());
	sleep(1);
	return _result;
}

int ImagineWBSSystemTools::commandExitStatus() {
	stringstream _command;
	int system_command = atoi(getFileContent((char*)COMMAND_STATUS_FILE.c_str()));
	_command << "rm -f " << COMMAND_STATUS_FILE;
	if(system(_command.str().c_str())) {
		// do nothing
	}
	return system_command;
}

bool ImagineWBSSystemTools::isCommandRunning() {
	sleep(1);
	ifstream file (COMMAND_LOCK_FILE.c_str());
	if(file.is_open()) {
		return true;
	}
	return false;
}

bool ImagineWBSSystemTools::isDirectory(const char* path) {
	DIR *_dir = opendir (path);
	if(_dir != NULL) {
		free(_dir);
		return true;
	}
	free(_dir);
	return false;
}

bool ImagineWBSSystemTools::isFile(const char* path) {
	ifstream config (path);
	if(config) {
		config.close();
		return true;
	}
	return false;
}

const char* ImagineWBSSystemTools::readCommandOutput() {
	return getFileContent((char*)COMMAND_OUTPUT_FILE.c_str());
}

vector<string> ImagineWBSSystemTools::stringSpaceTokenizer(char* value, char* delim) {
	char* _s = strdup(value);
	char* _token;
	vector<string> _tokens;
	_token = strtok(_s, delim);
	while(_token) {
		_tokens.push_back(_token);
		_token = strtok(NULL, delim);
	}
	free(_s);
	return _tokens;
}

string ImagineWBSSystemTools::trim(string value) {
	string _s = strdup(value.c_str());
	_s.erase(remove(_s.begin(), _s.end(), '\t'), _s.end());
	_s.erase(remove(_s.begin(), _s.end(), '\n'), _s.end());
	_s.erase(remove(_s.begin(), _s.end(), '\r'), _s.end());
	_s.erase(remove(_s.begin(), _s.end(), '\f'), _s.end());
	_s.erase(remove(_s.begin(), _s.end(), ' '), _s.end());
	return _s;
}

vector<string> ImagineWBSSystemTools::getNetworkData(void) {
	string line;
	vector<string> _network;
	ifstream network ("/etc/network/interfaces");
	if(network.is_open()) {
		while(!network.eof()) {
			getline(network, line);
			if(line.find("iface ") != string::npos) {
				line = line.substr(6, line.find(' ', 6) - 6);
				if(line.compare("lo") == 0) {
					continue;
				}
				while(!network.eof()) {
					getline(network, line);
					if(line.length() == 0) {
						break;
					} else if(line.find("address ") != string::npos) {
						line = line.substr(8, line.size() - 8);
						line = trim(line);
						vector<string> _values = stringSpaceTokenizer((char*)line.c_str(), (char*)".");
						int _size = _values.size();
						for(int i = 0; i < _size; i++) {
							_network.push_back(_values.at(i));
						}
					} else if(line.find("netmask ") != string::npos) {
						line = line.substr(8, line.size() - 8);
						line = trim(line);
						vector<string> _values = stringSpaceTokenizer((char*)line.c_str(), (char*)".");
						int _size = _values.size();
						for(int i = 0; i < _size; i++) {
							_network.push_back(_values.at(i));
						}
					}  else if(line.find("gateway ") != string::npos) {
						line = line.substr(8, line.size() - 8);
						line = trim(line);
						vector<string> _values = stringSpaceTokenizer((char*)line.c_str(), (char*)".");
						int _size = _values.size();
						for(int i = 0; i < _size; i++) {
							_network.push_back(_values.at(i));
						}
					}
				}
				break;
			}
		}
		network.close();
	}
	return _network;
}

void ImagineWBSSystemTools::changeRootPassword(char* value) {
	if(isFile("/etc/wbsagnitio-admin/config.xml")) {
		stringstream _password;
		_password << "{MD5}";
		_password << base64encode((char*)MD5digest(value).c_str());
		setXMLProperty((char*)"/etc/wbsagnitio-admin/config.xml", (char*)"system.password", _password.str());
	} else if(isFile("/etc/wbsairback-admin/config.xml")) {
		stringstream _password;
		_password << "{MD5}";
		_password << base64encode((char*)MD5digest(value).c_str());
		setXMLProperty((char*)"/etc/wbsairback-admin/config.xml", (char*)"system.password", _password.str());
	}
}

bool ImagineWBSSystemTools::verifyRootPassword(char* value) {
	if(isFile("/etc/wbsagnitio-admin/config.xml")) {
		string _new_password = MD5digest(value);
		string _password = getXMLProperty((char*)"/etc/wbsagnitio-admin/config.xml", (char*)"system.password");
		if(_password.find("{MD5}") != string::npos) {
			_password = _password.substr(5,_password.length());
		}
		_new_password = base64encode((char*)_new_password.c_str());
		if(!strcmp(_new_password.c_str(), _password.c_str())) {
			return true;
		}
	} else if(isFile("/etc/wbsairback-admin/config.xml")) {
		string _new_password = MD5digest(value);
		string _password = getXMLProperty((char*)"/etc/wbsairback-admin/config.xml", (char*)"system.password");
		if(_password.find("{MD5}") != string::npos) {
			_password = _password.substr(5,_password.length());
		}
		_new_password = base64encode((char*)_new_password.c_str());
		if(!strcmp(_new_password.c_str(), _password.c_str())) {
			return true;
		}
	}
	return false;
}

string ImagineWBSSystemTools::getXMLProperty(char* file, char* property) {
	string _value;
	xmlDoc *_doc = NULL;
	xmlNode *_root_node = NULL;

	_doc = xmlReadFile(file, NULL, 0);
	_root_node = xmlDocGetRootElement(_doc);
	if(_doc != NULL) {
		xmlNode *cur_node = NULL;
		for(cur_node = _root_node->children; cur_node; cur_node = cur_node->next) {
			if(cur_node->type == XML_ELEMENT_NODE && xmlStrEqual(cur_node->name, BAD_CAST property)) {
				_value = (char*)xmlNodeGetContent(cur_node);
				_value = trim(_value);
			}
		}

		xmlFreeDoc(_doc);
		xmlCleanupParser();
	}
	return _value;
}

void ImagineWBSSystemTools::setXMLProperty(char* file, char* property, string value) {
	xmlDoc *_doc = NULL;
	xmlNode *_root_node = NULL;
	bool updated = false;

	_doc = xmlReadFile(file, NULL, 0);
	_root_node = xmlDocGetRootElement(_doc);
	if(_doc != NULL) {
		xmlNode *cur_node = NULL;
		for(cur_node = _root_node->children; cur_node; cur_node = cur_node->next) {
			if(cur_node->type == XML_ELEMENT_NODE && xmlStrEqual(cur_node->name, BAD_CAST property)) {
				xmlNodeSetContent(cur_node, BAD_CAST value.c_str());
				updated = true;
			}
		}

		if(!updated) {
			xmlNode *new_node;
			new_node = xmlNewChild(_root_node, NULL, BAD_CAST property, BAD_CAST value.c_str());
		}

		/*
		 * Dumping document to stdio or file
		 */
		xmlSaveFormatFileEnc(file, _doc, "UTF-8", 1);

		xmlFreeDoc(_doc);
		xmlCleanupParser();
	}
}

string ImagineWBSSystemTools::MD5digest(char* _data) {
	const EVP_MD *_md;
	EVP_MD_CTX ctx;
	stringstream _out;
	unsigned char _result[EVP_MAX_MD_SIZE];
	unsigned int _length;

	OpenSSL_add_all_digests();
	_md = EVP_get_digestbyname("md5");

	EVP_MD_CTX_init(&ctx);
	EVP_DigestInit_ex(&ctx, _md, NULL);
	EVP_DigestUpdate(&ctx, _data, strlen(_data));
	EVP_DigestFinal_ex(&ctx, _result, &_length);
	EVP_MD_CTX_cleanup(&ctx);

	int _intlength = (unsigned int) _length;
	for(int i = 0; i < _intlength; i++) {
		_out << hex << _result[i];
	}

	return _out.str();
}

string ImagineWBSSystemTools::base64encode(char* _data) {
	char alphabet[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
	int codes[256];
	char out[((strlen(_data) + 2) / 3) * 4];

	for(int i = 0; i < 256; i++) {
		codes[i] = -1;
	}
	for(int i = 65; i <= 90; i++) {
		codes[i] = (i - 65);
	}
	for(int i = 97; i <= 122; i++) {
		codes[i] = ((26 + i) - 97);
	}
	for(int i = 48; i <= 57; i++) {
		codes[i] = ((52 + i) - 48);
	}

	codes[43] = 62;
	codes[47] = 63;

	int i = 0, _length = strlen(_data);
	for(int index = 0; i < _length; index += 4) {
		bool quad = false;
		bool trip = false;
		int val = 0xff & _data[i];
		val <<= 8;
		if(i + 1 < _length) {
			val |= 0xff & _data[i + 1];
			trip = true;
		}
		val <<= 8;
		if(i + 2 < _length) {
			val |= 0xff & _data[i + 2];
			quad = true;
		}
		if(quad) {
			out[index + 3] = alphabet[val & 0x3f];
		} else {
			out[index + 3] = alphabet[64];
		}
		val >>= 6;
		if(trip) {
			out[index + 2] = alphabet[val & 0x3f];
		} else {
			out[index + 2] = alphabet[64];
		}
		val >>= 6;
		out[index + 1] = alphabet[val & 0x3f];
		val >>= 6;
		out[index] = alphabet[val & 0x3f];
		i += 3;
	}

	out[strcspn(out, "==") + 2] = NULL;
	return out;
}

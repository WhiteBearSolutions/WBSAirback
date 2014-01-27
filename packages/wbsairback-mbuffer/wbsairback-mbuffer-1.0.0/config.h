/* config.h.  Generated from config.h.in by configure.  */
/*
 *  Copyright (C) 2000-2009, Thomas Maier-Komor
 *
 *  This is the source code of mbuffer.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

#ifndef CONFIG_H
#define CONFIG_H

/* package */
#define PACKAGE "mbuffer"

/* version of mbuffer */
#define VERSION "20130220"

/* Define if you want support for debugging messages. */
#define DEBUG 1

/* Undefine if you want asserts enabled. */
/* #undef NDEBUG */

/* md5hashing is enabled by default, 
   default is libmhash fallback is libmd5 */
#define HAVE_LIBMHASH 1
/* #undef HAVE_LIBMD5 */
/* #undef HAVE_LIBCRYPTO */

/* Define if you have a working `mmap' system call.  */
/* #undef HAVE_MMAP */

/* Define as the return type of signal handlers (int or void).  */
#define RETSIGTYPE void

/* Needed for thread safe compilation */
#define _REENTRANT 1

/* Define if you have the hstrerror function.  */
#define HAVE_HSTRERROR 1

/* Define if you have the getaddrinfo function.  */
#define HAVE_GETADDRINFO 1

/* Define to 1 if your `struct stat' has `st_blksize'. Deprecated, use
   `HAVE_STRUCT_STAT_ST_BLKSIZE' instead. */
#define HAVE_ST_BLKSIZE 1

/* Define if `st_blksize' is member of `struct stat'. */
#define HAVE_STRUCT_STAT_ST_BLKSIZE 1

/* Define if you have libsendfile. */
#define HAVE_SENDFILE 1
#define HAVE_SENDFILE_H 1

/* seteuid ? */
#define HAVE_SETEUID 1

/* atoll availble? */
/* #undef HAVE_ATOLL */

/* alloca in alloca.h */
#define HAVE_ALLOCA_H 1

/* largefile support */
/* #undef _LARGE_FILE */
/* #undef _LARGEFILE_SOURCE */
/* #undef _LARGEFILE64_SOURCE */
/* #undef _FILE_OFFSET_BITS */

#ifndef lint
#define restrict __restrict
/* #undef inline */
#endif

#endif

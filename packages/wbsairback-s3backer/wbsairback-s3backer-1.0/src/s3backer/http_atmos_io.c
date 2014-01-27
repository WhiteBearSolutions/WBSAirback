
/*
 * s3backer - FUSE-based single file backing store via Amazon S3
 * 
 * Copyright 2008-2011 Archie L. Cobbs <archie@dellroad.org>
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 *
 * $Id: http_io.c 462 2012-04-07 21:22:05Z archie.cobbs $
 */

#include "s3backer.h"
#include "block_part.h"
#include "atmos_util.h"
#include "crypto.h"
#include "http_atmos_io.h"

/* HTTP definitions */
#define HTTP_GET                    "GET"
#define HTTP_POST                   "POST"
#define HTTP_PUT                   	"PUT"
#define HTTP_DELETE                 "DELETE"
#define HTTP_HEAD                   "HEAD"
#define POSTTOPUT					141
#define HTTP_NOT_MODIFIED           304
#define HTTP_UNAUTHORIZED           401
#define HTTP_FORBIDDEN              403
#define HTTP_NOT_FOUND              404
#define HTTP_PRECONDITION_FAILED    412
#define HTTP_BAD_REQUEST			400
#define DATE_HEADER                 "Date"
#define CTYPE_HEADER                "Content-Type"
#define CONTENT_ENCODING_HEADER     "Content-Encoding"
#define ETAG_HEADER                 "ETag"
#define CONTENT_ENCODING_DEFLATE    "deflate"
#define CONTENT_ENCODING_ENCRYPT    "encrypt"
#define MD5_HEADER                  "Content-MD5"
#define ACL_HEADER                  "x-emc-acl"
#define STORAGE_CLASS_HEADER        "x-emc-storage-class"
#define SCLASS_STANDARD             "STANDARD"
#define SCLASS_REDUCED_REDUNDANCY   "REDUCED_REDUNDANCY"
#define FILE_SIZE_HEADER            "x-emc-meta-s3backer-filesize"
#define BLOCK_SIZE_HEADER           "x-emc-meta-s3backer-blocksize"
#define HMAC_HEADER                 "x-emc-meta-s3backer-hmac"
#define IF_MATCH_HEADER             "If-Match"
#define IF_NONE_MATCH_HEADER        "If-None-Match"

#define HTTP_HEADER_EMC_UID			 "x-emc-uid"
#define HTTP_HEADER_EMC_GROUPACL	 "x-emc-groupacl:other=NONE"
#define HTTP_HEADER_EMC_SIGNATURE	 "x-emc-signature"
#define HTTP_HEADER_EM_INCLUDE_META	 "x-emc-include-meta"
#define HTTP_HEADER_EM_LISTABLE_META "x-emc-listable-meta"

int IS_TRUNCATED = 1;
int NOT_TRUNCATED = 0;

/* MIME type for blocks */
#define CONTENT_TYPE                "application/octet-stream"

/* HTTP `Date' header format */
#define DATE_BUF_SIZE               64
#define DATE_BUF_FMT                "%a, %d %b %Y %H:%M:%S GMT"

/* Size required for URL buffer */
#define URL_BUF_SIZE(config)        (strlen((config)->baseURL) + strlen((config)->prefix) + S3B_BLOCK_NUM_DIGITS + 2)

/* Bucket listing API constants */
#define LIST_PARAM_MARKER           "x-emc-token"
#define LIST_PARAM_PREFIX           "prefix"
#define LIST_PARAM_MAX_KEYS         "x-emc-limit"
#define LIST_ELEM_LIST_RESLT 		"ListDirectoryResponse"
#define DIRECTORY_LIST     			"DirectoryList"
#define DIRECTORY_ENTRY				"DirectoryEntry"
#define FILENAME 					"Filename"

/* How many blocks to list at a time */
#define LIST_BLOCKS_CHUNK           0x100
//#define LIST_BLOCKS_CHUNK           0x001

/* PBKDF2 key generation iterations */
#define PBKDF2_ITERATIONS           5000

/* Misc */
#define WHITESPACE                  " \t\v\f\r\n"

/*
 * HTTP-based implementation of s3backer_store.
 *
 * This implementation does no caching or consistency checking.
 */

/* Internal definitions */
struct curl_holder {
    CURL                        *curl;
    LIST_ENTRY(curl_holder)     link;
};

/* Internal state */
struct http_atmos_io_private {
    struct http_atmos_io_conf         *config;
    struct http_atmos_io_stats        stats;
    LIST_HEAD(, curl_holder)    curls;
    pthread_mutex_t             mutex;
    u_int                       *non_zero;      // config->nonzero_bitmap is moved to here

    /* Encryption info */
    const EVP_CIPHER            *cipher;
    u_char                      key[EVP_MAX_KEY_LENGTH];        // key used to encrypt data
    u_char                      ivkey[EVP_MAX_KEY_LENGTH];      // key used to encrypt block number to get IV for data
};

/* I/O buffers */
struct http_atmos_io_bufs {
    size_t      rdremain;
    size_t      wrremain;
    char        *rddata;
    const char  *wrdata;
};

/* I/O state when reading/writing a block */
struct http_atmos_io {

    // I/O buffers
    struct http_atmos_io_bufs bufs;

    // XML parser and bucket listing info
    XML_Parser          	xml;                    // XML parser
    int                 	xml_error;              // XML parse error (if any)
    int                 	xml_error_line;         // XML parse error line
    int                 	xml_error_column;       // XML parse error column
    char                	*xml_path;              // Current XML path
    char                	*xml_text;              // Current XML text
    char					*xml_current_meta;
    int                 	xml_text_len;           // # chars in 'xml_text' buffer
    int                 	xml_text_max;           // max chars in 'xml_text' buffer
    int                     *list_truncated;         // returned list was truncated
    s3b_block_t         	last_block;             // last dirty block listed
    block_list_func_t   	*callback_func;         // callback func for listing blocks
    void                	*callback_arg;          // callback arg for listing blocks
    struct http_atmos_io_conf *config;                // configuration

    // Other info that needs to be passed around
    const char          *method;                // HTTP method
    const char          *url;                   // HTTP URL
    struct curl_slist   *headers;               // HTTP headers
    void                *dest;                  // Block data (when reading)
    const void          *src;                   // Block data (when writing)
    char				token[80];					// Token id when list is trunkated
    s3b_block_t         block_num;              // The block we're reading/writing
    u_int               buf_size;               // Size of data buffer
    u_int               *content_lengthp;       // Returned Content-Length
    uintmax_t           file_size;              // file size from "x-amz-meta-s3backer-filesize"
    u_int               block_size;             // block size from "x-amz-meta-s3backer-blocksize"
    u_int               expect_304;             // a verify request; expect a 304 response
    u_char              md5[MD5_DIGEST_LENGTH]; // parsed ETag header
    u_char              hmac[SHA_DIGEST_LENGTH];// parsed "x-amz-meta-s3backer-hmac" header
    char                content_encoding[32];   // received content encoding
    check_cancel_t      *check_cancel;          // write check-for-cancel callback
    void                *check_cancel_arg;      // write check-for-cancel callback argument
};

/* CURL prepper function type */
typedef void http_atmos_io_curl_prepper_t(CURL *curl, struct http_atmos_io *io);

/* s3backer_store functions */
static int http_atmos_io_read_block(struct s3backer_store *s3b, s3b_block_t block_num, void *dest,
  u_char *actual_md5, const u_char *expect_md5, int strict);
static int http_atmos_io_write_block(struct s3backer_store *s3b, s3b_block_t block_num, const void *src, u_char *md5,
  check_cancel_t *check_cancel, void *check_cancel_arg, int put);
static int http_atmos_io_read_block_part(struct s3backer_store *s3b, s3b_block_t block_num, u_int off, u_int len, void *dest);
static int http_atmos_io_write_block_part(struct s3backer_store *s3b, s3b_block_t block_num, u_int off, u_int len, const void *src);
static int http_atmos_io_list_blocks(struct s3backer_store *s3b, block_list_func_t *callback, void *arg);
static void http_atmos_io_destroy(struct s3backer_store *s3b);

/* Other functions */
static http_atmos_io_curl_prepper_t http_atmos_io_detect_prepper;
static http_atmos_io_curl_prepper_t http_atmos_io_read_prepper;
static http_atmos_io_curl_prepper_t http_atmos_io_write_prepper;
static http_atmos_io_curl_prepper_t http_atmos_io_list_prepper;

/* S3 REST API functions */
static char *http_atmos_io_get_url(char *buf, size_t bufsiz, struct http_atmos_io_conf *config, s3b_block_t block_num);
static void addCommonHeaders(struct http_atmos_io *io, char* urlbuf, char *hashString, const char* accessId, const char *accessKey, char* contentType );

/* Bucket listing functions */
static size_t http_atmos_io_curl_list_reader(const void *ptr, size_t size, size_t nmemb, void *stream);
static void http_atmos_io_list_elem_start(void *arg, const XML_Char *name, const XML_Char **atts);
static void http_atmos_io_list_elem_end(void *arg, const XML_Char *name);
static void http_atmos_io_list_text(void *arg, const XML_Char *s, int len);

/* HTTP and curl functions */
static int http_atmos_io_perform_io(struct http_atmos_io_private *priv, struct http_atmos_io *io, http_atmos_io_curl_prepper_t *prepper);
static size_t http_atmos_io_curl_reader(const void *ptr, size_t size, size_t nmemb, void *stream);
static size_t http_atmos_io_curl_writer(void *ptr, size_t size, size_t nmemb, void *stream);
static size_t http_atmos_io_curl_header(void *ptr, size_t size, size_t nmemb, void *stream);
static struct curl_slist *http_atmos_io_add_header(struct curl_slist *headers, const char *fmt, ...)
    __attribute__ ((__format__ (__printf__, 2, 3)));
static void http_atmos_io_get_date(char *buf, size_t bufsiz);
static CURL *http_atmos_io_acquire_curl(struct http_atmos_io_private *priv, struct http_atmos_io *io);
static void http_atmos_io_release_curl(struct http_atmos_io_private *priv, CURL **curlp, int may_cache);

/* Misc */
static void http_atmos_io_openssl_locker(int mode, int i, const char *file, int line);
static u_long http_atmos_io_openssl_ider(void);
static u_int http_atmos_io_crypt(struct http_atmos_io_private *priv, s3b_block_t block_num, int enc, const u_char *src, u_int len, u_char *dst);
static void http_atmos_io_authsig(struct http_atmos_io_private *priv, s3b_block_t block_num, const u_char *src, u_int len, u_char *hmac);
static int http_atmos_io_is_zero_block(const void *data, u_int block_size);
static int http_atmos_io_parse_hex(const char *str, u_char *buf, u_int nbytes);
static void http_atmos_io_prhex(char *buf, const u_char *data, size_t len);

/* Internal variables */
static pthread_mutex_t *openssl_locks;
static int num_openssl_locks;
static u_char zero_md5[MD5_DIGEST_LENGTH];
static u_char zero_hmac[SHA_DIGEST_LENGTH];

/*
 * Constructor
 *
 * On error, returns NULL and sets `errno'.
 */
struct s3backer_store *
http_atmos_io_create(struct http_atmos_io_conf *config)
{
    struct s3backer_store *s3b;
    struct http_atmos_io_private *priv;
    int nlocks;
    int r;

    /* Sanity check: we can really only handle one instance */
    if (openssl_locks != NULL) {
        (*config->log)(LOG_ERR, "http_atmos_io_create() called twice");
        r = EALREADY;
        goto fail0;
    }

    /* Initialize structures */
    if ((s3b = calloc(1, sizeof(*s3b))) == NULL) {
        r = errno;
        goto fail0;
    }

    s3b->read_block = http_atmos_io_read_block;
    s3b->write_block = http_atmos_io_write_block;
    s3b->read_block_part = http_atmos_io_read_block_part;
    s3b->write_block_part = http_atmos_io_write_block_part;
    s3b->list_blocks = http_atmos_io_list_blocks;
    s3b->destroy = http_atmos_io_destroy;
    if ((priv = calloc(1, sizeof(*priv))) == NULL) {
        r = errno;
        goto fail1;
    }
    priv->config = config;
    if ((r = pthread_mutex_init(&priv->mutex, NULL)) != 0)
        goto fail2;
    LIST_INIT(&priv->curls);
    s3b->data = priv;

    /* Initialize openssl */
    num_openssl_locks = CRYPTO_num_locks();
    if ((openssl_locks = malloc(num_openssl_locks * sizeof(*openssl_locks))) == NULL) {
        r = errno;
        goto fail3;
    }
    for (nlocks = 0; nlocks < num_openssl_locks; nlocks++) {
        if ((r = pthread_mutex_init(&openssl_locks[nlocks], NULL)) != 0) {
            while (nlocks > 0)
                pthread_mutex_destroy(&openssl_locks[--nlocks]);
            goto fail4;
        }
    }
    CRYPTO_set_locking_callback(http_atmos_io_openssl_locker);
    CRYPTO_set_id_callback(http_atmos_io_openssl_ider);

    /* Initialize encryption */
    if (config->encryption != NULL) {
        char saltbuf[strlen(config->prefix) + 1];

        /* Sanity checks */
        assert(config->password != NULL);
        assert(config->block_size % EVP_MAX_IV_LENGTH == 0);

        /* Find encryption algorithm */
        OpenSSL_add_all_ciphers();
        if ((priv->cipher = EVP_get_cipherbyname(config->encryption)) == NULL) {
            (*config->log)(LOG_ERR, "unknown encryption cipher `%s'", config->encryption);
            r = EINVAL;
            goto fail4;
        }
        if (EVP_CIPHER_block_size(priv->cipher) != EVP_CIPHER_iv_length(priv->cipher)) {
            (*config->log)(LOG_ERR, "invalid encryption cipher `%s': block size %d != IV length %d",
              config->encryption, EVP_CIPHER_block_size(priv->cipher), EVP_CIPHER_iv_length(priv->cipher));
            r = EINVAL;
            goto fail4;
        }

        /* Hash password to get bulk data encryption key */
        snprintf(saltbuf, sizeof(saltbuf), "%s", config->prefix);
        if ((r = PKCS5_PBKDF2_HMAC_SHA1(config->password, strlen(config->password),
          (u_char *)saltbuf, strlen(saltbuf), PBKDF2_ITERATIONS, sizeof(priv->key), priv->key)) != 1) {
            (*config->log)(LOG_ERR, "failed to create encryption key");
            r = EINVAL;
            goto fail4;
        }

        /* Hash the bulk encryption key to get the IV encryption key */
        if ((r = PKCS5_PBKDF2_HMAC_SHA1((char *)priv->key, sizeof(priv->key),
          priv->key, sizeof(priv->key), PBKDF2_ITERATIONS, sizeof(priv->ivkey), priv->ivkey)) != 1) {
            (*config->log)(LOG_ERR, "failed to create encryption key");
            r = EINVAL;
            goto fail4;
        }
    }
    /* Initialize cURL */
    curl_global_init(CURL_GLOBAL_ALL);
    /* Take ownership of non-zero block bitmap */
    priv->non_zero = config->nonzero_bitmap;
    config->nonzero_bitmap = NULL;

    /* Done */
    return s3b;

fail4:
    free(openssl_locks);
    openssl_locks = NULL;
    num_openssl_locks = 0;
fail3:
    pthread_mutex_destroy(&priv->mutex);
fail2:
    free(priv);
fail1:
    free(s3b);
fail0:
    (*config->log)(LOG_ERR, "http_atmos_io creation failed: %s", strerror(r));
    errno = r;
    return NULL;
}

/*
 * Destructor
 */
static void
http_atmos_io_destroy(struct s3backer_store *const s3b)
{
    struct http_atmos_io_private *const priv = s3b->data;
    struct curl_holder *holder;

    /* Clean up openssl */
    while (num_openssl_locks > 0)
        pthread_mutex_destroy(&openssl_locks[--num_openssl_locks]);
    free(openssl_locks);
    openssl_locks = NULL;
    CRYPTO_set_locking_callback(NULL);
    CRYPTO_set_id_callback(NULL);

    /* Clean up cURL */
    while ((holder = LIST_FIRST(&priv->curls)) != NULL) {
        curl_easy_cleanup(holder->curl);
        LIST_REMOVE(holder, link);
        free(holder);
    }
    curl_global_cleanup();

    /* Free structures */
    pthread_mutex_destroy(&priv->mutex);
    free(priv->non_zero);
    free(priv);
    free(s3b);
}

void
http_atmos_io_get_stats(struct s3backer_store *s3b, struct http_atmos_io_stats *stats)
{
    struct http_atmos_io_private *const priv = s3b->data;

    pthread_mutex_lock(&priv->mutex);
    memcpy(stats, &priv->stats, sizeof(*stats));
    pthread_mutex_unlock(&priv->mutex);
}

static int
http_atmos_io_list_blocks(struct s3backer_store *s3b, block_list_func_t *callback, void *arg)
{
    struct http_atmos_io_private *const priv = s3b->data;
    struct http_atmos_io_conf *const config = priv->config;
    char hashString[1024*1024];
    char urlbuf[URL_BUF_SIZE(config)+ 32];
    struct http_atmos_io io;
    int r;

    /* Initialize I/O info */
    memset(&io, 0, sizeof(io));
    io.url = urlbuf;
    io.method = HTTP_GET;
    io.config = config;
    io.xml_error = XML_ERROR_NONE;
    io.callback_func = callback;
    io.callback_arg = arg;


    /* Create XML parser */
    if ((io.xml = XML_ParserCreate(NULL)) == NULL) {
        (*config->log)(LOG_ERR, "failed to create XML parser");
        return ENOMEM;
    }

    /* Allocate buffers for XML path and tag text content */
    io.xml_text_max = strlen(config->prefix) + S3B_BLOCK_NUM_DIGITS + 10;
    if ((io.xml_text = malloc(io.xml_text_max + 1)) == NULL) {
        (*config->log)(LOG_ERR, "malloc: %s", strerror(errno));
        goto oom;
    }
    if ((io.xml_path = calloc(1, 1)) == NULL) {
        (*config->log)(LOG_ERR, "calloc: %s", strerror(errno));
        goto oom;
    }

    int listTruncated = 0;

    /* List blocks */
    do {

        /* Reset XML parser state */
        XML_ParserReset(io.xml, NULL);
        XML_SetUserData(io.xml, &io);
        XML_SetElementHandler(io.xml, http_atmos_io_list_elem_start, http_atmos_io_list_elem_end);
        XML_SetCharacterDataHandler(io.xml, http_atmos_io_list_text);

        /* Format URL */
        snprintf(urlbuf, sizeof(urlbuf), "%s%s", config->baseURL, config->prefix);

        if (listTruncated)
        	io.headers = http_atmos_io_add_header(io.headers, "%s:%s", LIST_PARAM_MARKER, io.token);

        /* Add URL parameters */
        // TODO: Cabecera limit, ver cómo replicamos el tema de truncar las listas
        io.headers = http_atmos_io_add_header(io.headers, "%s:%u", LIST_PARAM_MAX_KEYS, LIST_BLOCKS_CHUNK);

        io.headers = http_atmos_io_add_header(io.headers, "%s:%s", HTTP_HEADER_EM_INCLUDE_META, "true");
        addCommonHeaders(&io, urlbuf, hashString, config->accessId, config->accessKey, strdup(CONTENT_TYPE));
        /* Perform operation */
        r = http_atmos_io_perform_io(priv, &io, http_atmos_io_list_prepper);

        if (io.list_truncated && io.token && strlen(io.token)>0) {
        	(*config->log)(LOG_DEBUG, "LISTA TRUNCADA, token: %s", io.token);
			listTruncated = 1;
	    } else {
	    	(*config->log)(LOG_DEBUG, "LISTA NO TRUNCADA");
	    	listTruncated = 0;
	    }

        /* Clean up headers */
        curl_slist_free_all(io.headers);
        io.headers = NULL;

        /* Check for error */
        // Caso de llegar al final en un listado para borrar
        if (r == POSTTOPUT)
           	return 0;
        else if (r != 0)
            goto fail;

        /* Finalize parse */
        if (XML_Parse(io.xml, NULL, 0, 1) != XML_STATUS_OK) {
            io.xml_error = XML_GetErrorCode(io.xml);
            io.xml_error_line = XML_GetCurrentLineNumber(io.xml);
            io.xml_error_column = XML_GetCurrentColumnNumber(io.xml);
        }

        /* Check for XML error */
        if (io.xml_error != XML_ERROR_NONE) {
            (*config->log)(LOG_ERR, "XML parse error: line %d col %d: %s",
              io.xml_error_line, io.xml_error_column, XML_ErrorString(io.xml_error));
            r = EIO;
            goto fail;
        }
    } while (listTruncated);

    /* Done */
    XML_ParserFree(io.xml);
    free(io.xml_path);
    free(io.xml_text);
    return 0;

oom:
    /* Update stats */
    pthread_mutex_lock(&priv->mutex);
    priv->stats.out_of_memory_errors++;
    pthread_mutex_unlock(&priv->mutex);
    r = ENOMEM;

fail:
    /* Clean up after failure */
    if (io.xml != NULL)
        XML_ParserFree(io.xml);
    free(io.xml_path);
    free(io.xml_text);
    return r;
}

static void
http_atmos_io_list_prepper(CURL *curl, struct http_atmos_io *io)
{
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, http_atmos_io_curl_list_reader);
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, io);
    curl_easy_setopt(curl, CURLOPT_HTTPHEADER, io->headers);
    curl_easy_setopt(curl, CURLOPT_HEADERFUNCTION, http_atmos_io_curl_header);
    curl_easy_setopt(curl, CURLOPT_HEADERDATA, io);
    curl_easy_setopt(curl, CURLOPT_ENCODING, "");
    curl_easy_setopt(curl, CURLOPT_HTTP_CONTENT_DECODING, (long)1);
}

static size_t
http_atmos_io_curl_list_reader(const void *ptr, size_t size, size_t nmemb, void *stream)
{
    struct http_atmos_io *const io = (struct http_atmos_io *)stream;
    size_t total = size * nmemb;

    if (io->xml_error != XML_ERROR_NONE)
        return total;
    if (XML_Parse(io->xml, ptr, total, 0) != XML_STATUS_OK) {
        io->xml_error = XML_GetErrorCode(io->xml);
        io->xml_error_line = XML_GetCurrentLineNumber(io->xml);
        io->xml_error_column = XML_GetCurrentColumnNumber(io->xml);
    }
    return total;
}

static void
http_atmos_io_list_elem_start(void *arg, const XML_Char *name, const XML_Char **atts)
{
    struct http_atmos_io *const io = (struct http_atmos_io *)arg;
    const size_t plen = strlen(io->xml_path);
    char *newbuf;

    /* Update current path */
    if ((newbuf = realloc(io->xml_path, plen + 1 + strlen(name) + 1)) == NULL) {
        (*io->config->log)(LOG_DEBUG, "realloc: %s", strerror(errno));
        io->xml_error = XML_ERROR_NO_MEMORY;
        return;
    }
    io->xml_path = newbuf;
    io->xml_path[plen] = '/';
    strcpy(io->xml_path + plen + 1, name);

    /* Reset buffer */
    io->xml_text_len = 0;
    io->xml_text[0] = '\0';
}

static void
http_atmos_io_list_elem_end(void *arg, const XML_Char *name)
{
    struct http_atmos_io *const io = (struct http_atmos_io *)arg;
    s3b_block_t block_num;

    // Handle: /ListDirectoryResponse/DirectoryList/DirectoryEntry/Filename
    if (strcmp(io->xml_path, "/" LIST_ELEM_LIST_RESLT "/" DIRECTORY_LIST "/" DIRECTORY_ENTRY "/" FILENAME) == 0) {
    	//fprintf(stderr,"Encontramos elem!! %s: %s ... \n",io->xml_path,  io->xml_text);
        if (http_atmos_io_parse_block(io->config, io->xml_text, &block_num) == 0) {
        	//fprintf(stderr,"Parseo ok %d ... \n",block_num);
            (*io->callback_func)(io->callback_arg, block_num);
            io->last_block = block_num;
        }
    }

    /* Update current XML path */
    assert(strrchr(io->xml_path, '/') != NULL);
    *strrchr(io->xml_path, '/') = '\0';

    /* Reset buffer */
    io->xml_text_len = 0;
    io->xml_text[0] = '\0';
}

static void
http_atmos_io_list_text(void *arg, const XML_Char *s, int len)
{
    struct http_atmos_io *const io = (struct http_atmos_io *)arg;
    int avail;

    /* Append text to buffer */
    avail = io->xml_text_max - io->xml_text_len;
    if (len > avail)
        len = avail;
    memcpy(io->xml_text + io->xml_text_len, s, len);
    io->xml_text_len += len;
    io->xml_text[io->xml_text_len] = '\0';
}

/*
 * Parse a block's item name (including prefix) and set the corresponding bit in the bitmap.
 */
int
http_atmos_io_parse_block(struct http_atmos_io_conf *config, const char *name, s3b_block_t *block_nump)
{
    s3b_block_t block_num = 0;
    int i;

    /* Parse block number */
    for (i = 0; i < S3B_BLOCK_NUM_DIGITS; i++) {
        char ch = name[i];

        if (!isxdigit(ch))
            break;
        block_num <<= 4;
        block_num |= ch <= '9' ? ch - '0' : tolower(ch) - 'a' + 10;
    }

    /* Was parse successful? */
    if (i != S3B_BLOCK_NUM_DIGITS || name[i] != '\0' || block_num >= config->num_blocks)
        return -1;

    /* Done */
    *block_nump = block_num;
    return 0;
}

/*
 * Auto-detect block size and total size based on the first block.
 *
 * Returns:
 *
 *  0       Success
 *  ENOENT  Block not found
 *  ENXIO   Response was missing one of the two required headers
 *  Other   Other error
 */
int
http_atmos_io_detect_sizes(struct s3backer_store *s3b, off_t *file_sizep, u_int *block_sizep)
{
    struct http_atmos_io_private *const priv = s3b->data;
    struct http_atmos_io_conf *const config = priv->config;
    char urlbuf[URL_BUF_SIZE(config)];
    u_int content_len;
    char hashString[1024*1024];
    struct http_atmos_io io;
    int r;

    /* Initialize I/O info */
    memset(&io, 0, sizeof(io));
    io.url = urlbuf;
    io.method = HTTP_HEAD;
    io.content_lengthp = &content_len;

    /* Construct URL for the first block */
    http_atmos_io_get_url(urlbuf, sizeof(urlbuf), config, 0);
    io.headers = http_atmos_io_add_header(io.headers, "%s:%s", HTTP_HEADER_EM_INCLUDE_META, "true");
    addCommonHeaders(&io, urlbuf, hashString, config->accessId, config->accessKey, NULL);
    //printf("testOK: %s \n", sign("POST\napplication/octet-stream\n\nThu, 05 Jun 2008 16:38:19 GMT\n/rest/objects\nx-emc-date:Thu, 05 Jun 2008 16:38:19 GMT\nx-emc-groupacl:other=NONE\nx-emc-listable-meta:part4/part7/part8=quick\nx-emc-meta:part1=buy\nx-emc-uid:6039ac182f194e15b9261d73ce044939/user1\nx-emc-useracl:john=FULL_CONTROL,mary=WRITE", "LJLuryj6zs8ste6Y3jTGQp71xq0="));
    //printf("testOK: %s \n", sign("HEAD\n\n\nTue, 25 Sep 2012 12:05:19 GMT\n/rest/objects/4f6a13e4a1cd150a04f6a1a6f110c5050619dff7231f\nx-emc-uid:5eb187a6e41142c4ba278e967c6557ee/UID01.STSAN02.CLOUD01", "2P6QaQkGYCsYAUxmH07xtlM9+2Y="));

    /* Perform operation */
    if ((r = http_atmos_io_perform_io(priv, &io, http_atmos_io_detect_prepper)) != 0)
        goto done;

    /* Extract filesystem sizing information */
    if (io.file_size == 0) {
        r = ENXIO;
        goto done;
    }
    *file_sizep = (off_t)io.file_size;
    if (io.block_size != 0)
        *block_sizep = io.block_size;
    else if (content_len != 0)
        *block_sizep = content_len;         /* backward compatible */
    else
        r = ENXIO;

done:
    /*  Clean up */
    curl_slist_free_all(io.headers);
    return r;
}


static void addCommonHeaders(struct http_atmos_io *io, char* urlbuf, char *hashString, const char* accessId, const char *accessKey, char* contentType )
{
	char datebuf[64];
	if (accessId != NULL) {
		http_atmos_io_get_date(datebuf, sizeof(datebuf));
		io->headers = http_atmos_io_add_header(io->headers, "%s:%s", HTTP_HEADER_EMC_UID, accessId);
		getHash(hashString, io->headers, io->method, datebuf, urlbuf, contentType);
		io->headers = http_atmos_io_add_header(io->headers, "%s: %s", HTTP_HEADER_EMC_SIGNATURE, sign(hashString, accessKey));
		io->headers = http_atmos_io_add_header(io->headers, "%s: %s", DATE_HEADER, datebuf);
		if (contentType != NULL)
			io->headers = http_atmos_io_add_header(io->headers, "%s: %s", CTYPE_HEADER, contentType);
	}
}


static void
http_atmos_io_detect_prepper(CURL *curl, struct http_atmos_io *io)
{
    memset(&io->bufs, 0, sizeof(io->bufs));
    curl_easy_setopt(curl, CURLOPT_NOBODY, 1);
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, http_atmos_io_curl_reader);
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, io);
    curl_easy_setopt(curl, CURLOPT_HEADERFUNCTION, http_atmos_io_curl_header);
    curl_easy_setopt(curl, CURLOPT_HEADERDATA, io);
    curl_easy_setopt(curl, CURLOPT_HTTPHEADER, io->headers);
}

static int
http_atmos_io_read_block(struct s3backer_store *const s3b, s3b_block_t block_num, void *dest,
  u_char *actual_md5, const u_char *expect_md5, int strict)
{
    struct http_atmos_io_private *const priv = s3b->data;
    struct http_atmos_io_conf *const config = priv->config;
    char urlbuf[URL_BUF_SIZE(config)];
    int encrypted = 0;
    struct http_atmos_io io;
    char hashString[1024*1024];
    u_int did_read;
    char *layer;
    int r;

    /* Sanity check */
    if (config->block_size == 0 || block_num >= config->num_blocks)
        return EINVAL;

    /* Read zero blocks when bitmap indicates empty until non-zero content is written */
    if (priv->non_zero != NULL) {
        const int bits_per_word = sizeof(*priv->non_zero) * 8;
        const int word = block_num / bits_per_word;
        const int bit = 1 << (block_num % bits_per_word);

        pthread_mutex_lock(&priv->mutex);
        if ((priv->non_zero[word] & bit) == 0) {
            priv->stats.empty_blocks_read++;
            pthread_mutex_unlock(&priv->mutex);
            memset(dest, 0, config->block_size);
            if (actual_md5 != NULL)
                memset(actual_md5, 0, MD5_DIGEST_LENGTH);
            return 0;
        }
        pthread_mutex_unlock(&priv->mutex);
    }

    /* Initialize I/O info */
    memset(&io, 0, sizeof(io));
    io.url = urlbuf;
    io.method = HTTP_GET;
    io.block_num = block_num;

    /* Allocate a buffer in case compressed and/or encrypted data is larger */
    io.buf_size = compressBound(config->block_size) + EVP_MAX_IV_LENGTH;
    if ((io.dest = malloc(io.buf_size)) == NULL) {
        (*config->log)(LOG_ERR, "malloc: %s", strerror(errno));
        pthread_mutex_lock(&priv->mutex);
        priv->stats.out_of_memory_errors++;
        pthread_mutex_unlock(&priv->mutex);
        return ENOMEM;
    }

    /* Construct URL for this block */
    http_atmos_io_get_url(urlbuf, sizeof(urlbuf), config, block_num);

    /* Add If-Match or If-None-Match header as required */
    if (expect_md5 != NULL && memcmp(expect_md5, zero_md5, MD5_DIGEST_LENGTH) != 0) {
        char md5buf[MD5_DIGEST_LENGTH * 2 + 1];
        const char *header;

        if (strict)
            header = IF_MATCH_HEADER;
        else {
            header = IF_NONE_MATCH_HEADER;
            io.expect_304 = 1;
        }
        http_atmos_io_prhex(md5buf, expect_md5, MD5_DIGEST_LENGTH);
        io.headers = http_atmos_io_add_header(io.headers, "%s: \"%s\"", header, md5buf);
    }

    io.headers = http_atmos_io_add_header(io.headers, "%s:%s", HTTP_HEADER_EM_INCLUDE_META, "true");
    addCommonHeaders(&io, urlbuf, hashString, config->accessId, config->accessKey, NULL);

    /* Perform operation */
    r = http_atmos_io_perform_io(priv, &io, http_atmos_io_read_prepper);

    /* Determine how many bytes we read */
    did_read = io.buf_size - io.bufs.rdremain;

    /* Check Content-Encoding and decode if necessary */
    for ( ; r == 0 && *io.content_encoding != '\0'; *layer = '\0') {

        /* Find next encoding layer */
        if ((layer = strrchr(io.content_encoding, ',')) != NULL)
            *layer++ = '\0';
        else
            layer = io.content_encoding;

        /* Sanity check */
        if (io.dest == NULL)
            goto bad_encoding;

        /* Check for encryption (which must have been applied after compression) */
        if (strncasecmp(layer, CONTENT_ENCODING_ENCRYPT "-", sizeof(CONTENT_ENCODING_ENCRYPT)) == 0) {
            const char *const block_cipher = layer + sizeof(CONTENT_ENCODING_ENCRYPT);
            u_char hmac[SHA_DIGEST_LENGTH];
            u_char *buf;

            /* Encryption must be enabled */
            if (config->encryption == NULL) {
                (*config->log)(LOG_ERR, "block %0*jx is encrypted with `%s' but `--encrypt' was not specified",
                  S3B_BLOCK_NUM_DIGITS, (uintmax_t)block_num, block_cipher);
                r = EIO;
                break;
            }

            /* Verify encryption type */
            if (strcasecmp(block_cipher, EVP_CIPHER_name(priv->cipher)) != 0) {
                (*config->log)(LOG_ERR, "block %0*jx was encrypted using `%s' but `%s' encryption is configured",
                  S3B_BLOCK_NUM_DIGITS, (uintmax_t)block_num, block_cipher, EVP_CIPHER_name(priv->cipher));
                r = EIO;
                break;
            }

            /* Verify block's signature */
            if (memcmp(io.hmac, zero_hmac, sizeof(io.hmac)) == 0) {
                (*config->log)(LOG_ERR, "block %0*jx is encrypted, but no signature was found",
                  S3B_BLOCK_NUM_DIGITS, (uintmax_t)block_num);
                r = EIO;
                break;
            }
            http_atmos_io_authsig(priv, block_num, io.dest, did_read, hmac);
            if (memcmp(io.hmac, hmac, sizeof(hmac)) != 0) {
                (*config->log)(LOG_ERR, "block %0*jx has an incorrect signature (did you provide the right password?)",
                  S3B_BLOCK_NUM_DIGITS, (uintmax_t)block_num);
                r = EIO;
                break;
            }

            /* Allocate buffer for the decrypted data */
            if ((buf = malloc(did_read + EVP_MAX_IV_LENGTH)) == NULL) {
                (*config->log)(LOG_ERR, "malloc: %s", strerror(errno));
                pthread_mutex_lock(&priv->mutex);
                priv->stats.out_of_memory_errors++;
                pthread_mutex_unlock(&priv->mutex);
                r = ENOMEM;
                break;
            }

            /* Decrypt the block */
            did_read = http_atmos_io_crypt(priv, block_num, 0, io.dest, did_read, buf);
            memcpy(io.dest, buf, did_read);
            free(buf);

            /* Proceed */
            encrypted = 1;
            continue;
        }

        /* Check for compression */
        if (strcasecmp(layer, CONTENT_ENCODING_DEFLATE) == 0) {
            u_long uclen = config->block_size;

            switch (uncompress(dest, &uclen, io.dest, did_read)) {
            case Z_OK:
                did_read = uclen;
                free(io.dest);
                io.dest = NULL;         /* compression should have been first */
                r = 0;
                break;
            case Z_MEM_ERROR:
                (*config->log)(LOG_ERR, "zlib uncompress: %s", strerror(ENOMEM));
                pthread_mutex_lock(&priv->mutex);
                priv->stats.out_of_memory_errors++;
                pthread_mutex_unlock(&priv->mutex);
                r = ENOMEM;
                break;
            case Z_BUF_ERROR:
                (*config->log)(LOG_ERR, "zlib uncompress: %s", "decompressed block is oversize");
                r = EIO;
                break;
            case Z_DATA_ERROR:
                (*config->log)(LOG_ERR, "zlib uncompress: %s", "data is corrupted or truncated");
                r = EIO;
                break;
            default:
                (*config->log)(LOG_ERR, "unknown zlib compress2() error %d", r);
                r = EIO;
                break;
            }

            /* Proceed */
            continue;
        }

bad_encoding:
        /* It was something we don't recognize */
        (*config->log)(LOG_ERR, "read of block %0*jx returned unexpected encoding \"%s\"",
          S3B_BLOCK_NUM_DIGITS, (uintmax_t)block_num, layer);
        r = EIO;
        break;
    }

    /* Check for required encryption */
    if (r == 0 && config->encryption != NULL && !encrypted) {
        (*config->log)(LOG_ERR, "block %0*jx was supposed to be encrypted but wasn't", S3B_BLOCK_NUM_DIGITS, (uintmax_t)block_num);
        r = EIO;
    }

    /* Check for wrong length read */
    if (r == 0 && did_read != config->block_size) {
        (*config->log)(LOG_ERR, "read of block %0*jx returned %lu != %lu bytes",
          S3B_BLOCK_NUM_DIGITS, (uintmax_t)block_num, (u_long)did_read, (u_long)config->block_size);
        r = EIO;
    }

    /* Copy the data to the desination buffer (if we haven't already) */
    if (r == 0 && io.dest != NULL)
        memcpy(dest, io.dest, config->block_size);

    /* Free the buffer */
    if (io.dest != NULL)
        free(io.dest);

    /* Update stats */
    pthread_mutex_lock(&priv->mutex);
    switch (r) {
    case 0:
        priv->stats.normal_blocks_read++;
        break;
    case ENOENT:
        priv->stats.zero_blocks_read++;
        break;
    default:
        break;
    }
    pthread_mutex_unlock(&priv->mutex);

    /* Check expected MD5 */
    if (expect_md5 != NULL) {
        const int expected_not_found = memcmp(expect_md5, zero_md5, MD5_DIGEST_LENGTH) == 0;

        /* Compare result with expectation */
        switch (r) {
        case 0:
            if (expected_not_found)
                r = strict ? EIO : 0;
            break;
        case ENOENT:
            if (expected_not_found)
                r = strict ? 0 : EEXIST;
            break;
        default:
            break;
        }

        /* Update stats */
        if (!strict) {
            switch (r) {
            case 0:
                pthread_mutex_lock(&priv->mutex);
                priv->stats.http_mismatch++;
                pthread_mutex_unlock(&priv->mutex);
                break;
            case EEXIST:
                pthread_mutex_lock(&priv->mutex);
                priv->stats.http_verified++;
                pthread_mutex_unlock(&priv->mutex);
                break;
            default:
                break;
            }
        }
    }

    /* Treat `404 Not Found' all zeroes */
    if (r == ENOENT) {
        memset(dest, 0, config->block_size);
        r = 0;
    }

    /* Copy actual MD5 */
    if (actual_md5 != NULL)
        memcpy(actual_md5, io.md5, MD5_DIGEST_LENGTH);

    /*  Clean up */
    curl_slist_free_all(io.headers);
    return r;
}

static void
http_atmos_io_read_prepper(CURL *curl, struct http_atmos_io *io)
{
    memset(&io->bufs, 0, sizeof(io->bufs));
    io->bufs.rdremain = io->buf_size;
    io->bufs.rddata = io->dest;
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, http_atmos_io_curl_reader);
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, io);
    curl_easy_setopt(curl, CURLOPT_MAXFILESIZE_LARGE, (curl_off_t)io->buf_size);
    curl_easy_setopt(curl, CURLOPT_HTTPHEADER, io->headers);
    curl_easy_setopt(curl, CURLOPT_HEADERFUNCTION, http_atmos_io_curl_header);
    curl_easy_setopt(curl, CURLOPT_HEADERDATA, io);
    curl_easy_setopt(curl, CURLOPT_ENCODING, "");
    curl_easy_setopt(curl, CURLOPT_HTTP_CONTENT_DECODING, (long)0);
}

/*
 * Write block if src != NULL, otherwise delete block.
 */
static int
http_atmos_io_write_block(struct s3backer_store *const s3b, s3b_block_t block_num, const void *src, u_char *caller_md5,
  check_cancel_t *check_cancel, void *check_cancel_arg, int put)
{
    struct http_atmos_io_private *const priv = s3b->data;
    struct http_atmos_io_conf *const config = priv->config;
    char urlbuf[URL_BUF_SIZE(config)];
    //char md5buf[(MD5_DIGEST_LENGTH * 4) / 3 + 4];
    char hmacbuf[SHA_DIGEST_LENGTH * 2 + 1];
    u_char hmac[SHA_DIGEST_LENGTH];
    u_char md5[MD5_DIGEST_LENGTH];
    void *encoded_buf = NULL;
    char hashString[1024*1024];
    struct http_atmos_io io;
    int compressed = 0;
    int encrypted = 0;
    int r;

    /* Sanity check */
    if (config->block_size == 0 || block_num >= config->num_blocks)
        return EINVAL;

    /* Detect zero blocks (if not done already by upper layer) */
    if (src != NULL) {
        if (http_atmos_io_is_zero_block(src, config->block_size))
            src = NULL;
    }

    /* Don't write zero blocks when bitmap indicates empty until non-zero content is written */
    if (priv->non_zero != NULL) {
        const int bits_per_word = sizeof(*priv->non_zero) * 8;
        const int word = block_num / bits_per_word;
        const int bit = 1 << (block_num % bits_per_word);

        pthread_mutex_lock(&priv->mutex);
        if (src == NULL) {
            if ((priv->non_zero[word] & bit) == 0) {
                priv->stats.empty_blocks_written++;
                pthread_mutex_unlock(&priv->mutex);
                return 0;
            }
        } else
            priv->non_zero[word] |= bit;
        pthread_mutex_unlock(&priv->mutex);
    }

    /* Initialize I/O info */
    memset(&io, 0, sizeof(io));
    io.url = urlbuf;

    if (put == 0)
    	io.method = src != NULL ? HTTP_POST : HTTP_DELETE;
    else
    	io.method = src != NULL ? HTTP_PUT : HTTP_DELETE;

    io.src = src;
    io.buf_size = config->block_size;
    io.block_num = block_num;
    io.check_cancel = check_cancel;
    io.check_cancel_arg = check_cancel_arg;

    /* Compress block if desired */
    if (src != NULL && config->compress != Z_NO_COMPRESSION) {
        u_long compress_len;

        /* Allocate buffer */
        compress_len = compressBound(io.buf_size);
        if ((encoded_buf = malloc(compress_len)) == NULL) {
            (*config->log)(LOG_ERR, "malloc: %s", strerror(errno));
            pthread_mutex_lock(&priv->mutex);
            priv->stats.out_of_memory_errors++;
            pthread_mutex_unlock(&priv->mutex);
            r = ENOMEM;
            goto fail;
        }

        /* Compress data */
        r = compress2(encoded_buf, &compress_len, io.src, io.buf_size, config->compress);
        switch (r) {
        case Z_OK:
            break;
        case Z_MEM_ERROR:
            (*config->log)(LOG_ERR, "zlib compress: %s", strerror(ENOMEM));
            pthread_mutex_lock(&priv->mutex);
            priv->stats.out_of_memory_errors++;
            pthread_mutex_unlock(&priv->mutex);
            r = ENOMEM;
            goto fail;
        default:
            (*config->log)(LOG_ERR, "unknown zlib compress2() error %d", r);
            r = EIO;
            goto fail;
        }

        /* Update POST data */
        io.src = encoded_buf;
        io.buf_size = compress_len;
        compressed = 1;
    }

    /* Encrypt data if desired */
    if (src != NULL && config->encryption != NULL) {
        void *encrypt_buf;
        u_int encrypt_len;

        /* Allocate buffer */
        if ((encrypt_buf = malloc(io.buf_size + EVP_MAX_IV_LENGTH)) == NULL) {
            (*config->log)(LOG_ERR, "malloc: %s", strerror(errno));
            pthread_mutex_lock(&priv->mutex);
            priv->stats.out_of_memory_errors++;
            pthread_mutex_unlock(&priv->mutex);
            r = ENOMEM;
            goto fail;
        }

        /* Encrypt the block */
        encrypt_len = http_atmos_io_crypt(priv, block_num, 1, io.src, io.buf_size, encrypt_buf);

        /* Compute block signature */
        http_atmos_io_authsig(priv, block_num, encrypt_buf, encrypt_len, hmac);
        http_atmos_io_prhex(hmacbuf, hmac, SHA_DIGEST_LENGTH);

        /* Update POST data */
        io.src = encrypt_buf;
        io.buf_size = encrypt_len;
        free(encoded_buf);              /* OK if NULL */
        encoded_buf = encrypt_buf;
        encrypted = 1;
    }

    /* Set Content-Encoding HTTP header */
    if (compressed || encrypted) {
        char ebuf[128];

        snprintf(ebuf, sizeof(ebuf), "%s: ", CONTENT_ENCODING_HEADER);
        if (compressed)
            snprintf(ebuf + strlen(ebuf), sizeof(ebuf) - strlen(ebuf), "%s", CONTENT_ENCODING_DEFLATE);
        if (encrypted) {
            snprintf(ebuf + strlen(ebuf), sizeof(ebuf) - strlen(ebuf), "%s%s-%s",
              compressed ? ", " : "", CONTENT_ENCODING_ENCRYPT, config->encryption);
        }
        io.headers = http_atmos_io_add_header(io.headers, "%s", ebuf);
    }

    /* Compute MD5 checksum */
    if (src != NULL)
        MD5(io.src, io.buf_size, md5);
    else
        memset(md5, 0, MD5_DIGEST_LENGTH);

    /* Report MD5 back to caller */
    if (caller_md5 != NULL)
        memcpy(caller_md5, md5, MD5_DIGEST_LENGTH);

    /* Construct URL for this block */
    http_atmos_io_get_url(urlbuf, sizeof(urlbuf), config, block_num);

    /* Add PUT-only headers */
  //  if (src != NULL) {

        /* Add Content-MD5 header */
   //     http_atmos_io_base64_encode(md5buf, sizeof(md5buf), md5, MD5_DIGEST_LENGTH);
   //     io.headers = http_atmos_io_add_header(io.headers, "%s:%s", MD5_HEADER, md5buf);
  //  }

    /**** NOTE: we add the following "x-amz" headers in lexicographic order as required by http_atmos_io_get_auth() ****/

    /* Add ACL header (PUT only) */
   // if (src != NULL)
   //     io.headers = http_atmos_io_add_header(io.headers, "%s:%s", ACL_HEADER, config->accessType);

    /* Add file size meta-data to zero'th block */
    if (src != NULL && block_num == 0) {
    	char listableMetaBlock[1024];
    	char listableMetaBuf[1024*1024];

    	snprintf(listableMetaBlock, sizeof(listableMetaBlock), "%s=%u", BLOCK_SIZE_HEADER, config->block_size);
    	snprintf(listableMetaBuf, sizeof(listableMetaBuf), "%s,%s=%ju", listableMetaBlock, FILE_SIZE_HEADER, (uintmax_t)(config->block_size * config->num_blocks));
        io.headers = http_atmos_io_add_header(io.headers, "%s:%s", HTTP_HEADER_EM_LISTABLE_META , listableMetaBuf);
    }

    /* Add signature header (if encrypting) */
    if (src != NULL && config->encryption != NULL)
        io.headers = http_atmos_io_add_header(io.headers, "%s:\"%s\"", HMAC_HEADER, hmacbuf);

    /* Add storage class header (if needed) */
    if (config->rrs)
        io.headers = http_atmos_io_add_header(io.headers, "%s:%s", STORAGE_CLASS_HEADER, SCLASS_REDUCED_REDUNDANCY);

    addCommonHeaders(&io, urlbuf, hashString, config->accessId, config->accessKey, strdup(CONTENT_TYPE));

    /* Perform operation */
    r = http_atmos_io_perform_io(priv, &io, http_atmos_io_write_prepper);

    /* Update stats */
    if (r == 0) {
        pthread_mutex_lock(&priv->mutex);
        if (src == NULL)
            priv->stats.zero_blocks_written++;
        else
            priv->stats.normal_blocks_written++;
        pthread_mutex_unlock(&priv->mutex);
    } else if (r == POSTTOPUT) {
    	r = http_atmos_io_write_block(s3b,block_num, src, md5, check_cancel, check_cancel_arg, 1);
    }

    curl_slist_free_all(io.headers);
    return r;

fail:
    /*  Clean up */
    curl_slist_free_all(io.headers);
    if (encoded_buf != NULL)
        free(encoded_buf);
    return r;
}

static void
http_atmos_io_write_prepper(CURL *curl, struct http_atmos_io *io)
{
    memset(&io->bufs, 0, sizeof(io->bufs));
    if (io->src != NULL) {
        io->bufs.wrremain = io->buf_size;
        io->bufs.wrdata = io->src;
    }
    curl_easy_setopt(curl, CURLOPT_READFUNCTION, http_atmos_io_curl_writer);
    curl_easy_setopt(curl, CURLOPT_READDATA, io);
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, http_atmos_io_curl_reader);
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, io);
    if (io->src != NULL) {
        curl_easy_setopt(curl, CURLOPT_UPLOAD, 1);
        curl_easy_setopt(curl, CURLOPT_INFILESIZE_LARGE, (curl_off_t)io->buf_size);
    }
    curl_easy_setopt(curl, CURLOPT_CUSTOMREQUEST, io->method);
    curl_easy_setopt(curl, CURLOPT_HTTPHEADER, io->headers);
}

static int
http_atmos_io_read_block_part(struct s3backer_store *s3b, s3b_block_t block_num, u_int off, u_int len, void *dest)
{
    struct http_atmos_io_private *const priv = s3b->data;
    struct http_atmos_io_conf *const config = priv->config;

    return block_part_read_block_part(s3b, block_num, config->block_size, off, len, dest);
}

static int
http_atmos_io_write_block_part(struct s3backer_store *s3b, s3b_block_t block_num, u_int off, u_int len, const void *src)
{
    struct http_atmos_io_private *const priv = s3b->data;
    struct http_atmos_io_conf *const config = priv->config;

    return block_part_write_block_part(s3b, block_num, config->block_size, off, len, src);
}

/*
 * Perform HTTP operation.
 */
static int
http_atmos_io_perform_io(struct http_atmos_io_private *priv, struct http_atmos_io *io, http_atmos_io_curl_prepper_t *prepper)
{
    struct http_atmos_io_conf *const config = priv->config;
    struct timespec delay;
    CURLcode curl_code;
    u_int retry_pause = 0;
    u_int total_pause;
    long http_atmos_code;
    double clen;
    int attempt;
    CURL *curl;

    /* Debug */
    if (config->debug)
        (*config->log)(LOG_DEBUG, "%s %s", io->method, io->url);

    /* Make attempts */
    for (attempt = 0, total_pause = 0; 1; attempt++, total_pause += retry_pause) {

    	/*(*config->log)(LOG_INFO, " -- Headers --\n");
		struct curl_slist *run = io->headers;
		for ( ; run != NULL; run = run->next)
			(*config->log)(LOG_INFO, "%s\n",run->data);
		(*config->log)(LOG_INFO, " ------------\n");*/

        /* Acquire and initialize CURL instance */
        if ((curl = http_atmos_io_acquire_curl(priv, io)) == NULL)
            return EIO;
        (*prepper)(curl, io);

        /* Perform HTTP operation and check result */
        if (attempt > 0)
        	(*config->log)(LOG_INFO, "retrying query atmos (attempt #%d): %s %s", attempt + 1, io->method, io->url);
        curl_code = curl_easy_perform(curl);

        /* Find out what the HTTP result code was (if any) */
        switch (curl_code) {
        case CURLE_HTTP_RETURNED_ERROR:
        case 0:
            if (curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &http_atmos_code) != 0)
                http_atmos_code = 999;                                /* this should never happen */
            break;
        default:
            http_atmos_code = -1;
            break;
        }

        /* Work around the fact that libcurl converts a 304 HTTP code as success */
        if (curl_code == 0 && http_atmos_code == HTTP_NOT_MODIFIED)
            curl_code = CURLE_HTTP_RETURNED_ERROR;

        /* In the case of a DELETE, treat an HTTP_NOT_FOUND error as successful */
        if (curl_code == CURLE_HTTP_RETURNED_ERROR
          && http_atmos_code == HTTP_NOT_FOUND
          && strcmp(io->method, HTTP_DELETE) == 0)
            curl_code = 0;

        /* Handle success */
        if (curl_code == 0) {
            double curl_time;
            int r = 0;

            /* Extra debug logging */
            if (config->debug)
                (*config->log)(LOG_DEBUG, "success: %s %s", io->method, io->url);

            /* Extract timing info */
            if ((curl_code = curl_easy_getinfo(curl, CURLINFO_TOTAL_TIME, &curl_time)) != CURLE_OK) {
                (*config->log)(LOG_ERR, "can't get cURL timing: %s", curl_easy_strerror(curl_code));
                curl_time = 0.0;
            }

            /* Extract content-length (if required) */
            if (io->content_lengthp != NULL) {
                if ((curl_code = curl_easy_getinfo(curl, CURLINFO_CONTENT_LENGTH_DOWNLOAD, &clen)) == CURLE_OK)
                    *io->content_lengthp = (u_int)clen;
                else {
                    (*config->log)(LOG_ERR, "can't get content-length: %s", curl_easy_strerror(curl_code));
                    r = ENXIO;
                }
            }

            /* Update stats */
            pthread_mutex_lock(&priv->mutex);
            if (strcmp(io->method, HTTP_GET) == 0) {
                priv->stats.http_gets.count++;
                priv->stats.http_gets.time += curl_time;
            } else if (strcmp(io->method, HTTP_POST) == 0) {
                priv->stats.http_puts.count++;
                priv->stats.http_puts.time += curl_time;
            } else if (strcmp(io->method, HTTP_DELETE) == 0) {
                priv->stats.http_deletes.count++;
                priv->stats.http_deletes.time += curl_time;
            } else if (strcmp(io->method, HTTP_HEAD) == 0) {
                priv->stats.http_heads.count++;
                priv->stats.http_heads.time += curl_time;
            }
            pthread_mutex_unlock(&priv->mutex);

            /* Done */
            http_atmos_io_release_curl(priv, &curl, r == 0);
            return r;
        }

        /* Free the curl handle (and ensure we don't try to re-use it) */
        http_atmos_io_release_curl(priv, &curl, 0);

        /* Handle errors */
        switch (curl_code) {
        case CURLE_ABORTED_BY_CALLBACK:
            if (config->debug)
                (*config->log)(LOG_DEBUG, "write aborted: %s %s", io->method, io->url);
            pthread_mutex_lock(&priv->mutex);
            priv->stats.http_canceled_writes++;
            pthread_mutex_unlock(&priv->mutex);
            return ECONNABORTED;
        case CURLE_OPERATION_TIMEDOUT:
            (*config->log)(LOG_NOTICE, "operation timeout: %s %s", io->method, io->url);
            pthread_mutex_lock(&priv->mutex);
            priv->stats.curl_timeouts++;
            pthread_mutex_unlock(&priv->mutex);
            break;
        case CURLE_HTTP_RETURNED_ERROR:                 /* special handling for some specific HTTP codes */
            switch (http_atmos_code) {
            case HTTP_NOT_FOUND:
                if (config->debug)
                    (*config->log)(LOG_DEBUG, "rec'd %ld response: %s %s", http_atmos_code, io->method, io->url);
                return ENOENT;
            case HTTP_BAD_REQUEST:
            	(*config->log)(LOG_DEBUG, "rec'd %ld response: %s %s", http_atmos_code, io->method, io->url);
            	if ( (strcmp(io->method, HTTP_POST) == 0) || io->list_truncated) {
            		return POSTTOPUT;
            	}
            	break;
            case HTTP_UNAUTHORIZED:
                (*config->log)(LOG_ERR, "rec'd %ld response: %s %s", http_atmos_code, io->method, io->url);
                pthread_mutex_lock(&priv->mutex);
                priv->stats.http_unauthorized++;
                pthread_mutex_unlock(&priv->mutex);
                return EACCES;
            case HTTP_FORBIDDEN:
                (*config->log)(LOG_ERR, "rec'd %ld response: %s %s", http_atmos_code, io->method, io->url);
                pthread_mutex_lock(&priv->mutex);
                priv->stats.http_forbidden++;
                pthread_mutex_unlock(&priv->mutex);
                return EPERM;
            case HTTP_PRECONDITION_FAILED:
                (*config->log)(LOG_INFO, "rec'd stale content: %s %s", io->method, io->url);
                pthread_mutex_lock(&priv->mutex);
                priv->stats.http_stale++;
                pthread_mutex_unlock(&priv->mutex);
                break;
            default:
            	if (curl_code == HTTP_NOT_MODIFIED) {
            		if (io->expect_304) {
						if (config->debug)
							(*config->log)(LOG_DEBUG, "rec'd %ld response: %s %s", http_atmos_code, io->method, io->url);
						return EEXIST;
					}
            	}
                (*config->log)(LOG_ERR, "rec'd %ld response: %s %s", http_atmos_code, io->method, io->url);
                pthread_mutex_lock(&priv->mutex);
                switch (http_atmos_code / 100) {
                case 4:
                    priv->stats.http_4xx_error++;
                    break;
                case 5:
                    priv->stats.http_5xx_error++;
                    break;
                default:
                    priv->stats.http_other_error++;
                    break;
                }
                pthread_mutex_unlock(&priv->mutex);
                break;
            }
            break;
        default:
            (*config->log)(LOG_ERR, "operation failed: %s (%s)", curl_easy_strerror(curl_code),
              total_pause >= config->max_retry_pause ? "final attempt" : "will retry");
            pthread_mutex_lock(&priv->mutex);
            switch (curl_code) {
            case CURLE_OUT_OF_MEMORY:
                priv->stats.curl_out_of_memory++;
                break;
            case CURLE_COULDNT_CONNECT:
                priv->stats.curl_connect_failed++;
                break;
            case CURLE_COULDNT_RESOLVE_HOST:
                priv->stats.curl_host_unknown++;
                break;
            default:
                priv->stats.curl_other_error++;
                break;
            }
            pthread_mutex_unlock(&priv->mutex);
            break;
        }

        /* Retry with exponential backoff up to max total pause limit */
        if (total_pause >= config->max_retry_pause)
            break;
        retry_pause = retry_pause > 0 ? retry_pause * 2 : config->initial_retry_pause;
        if (total_pause + retry_pause > config->max_retry_pause)
            retry_pause = config->max_retry_pause - total_pause;
        delay.tv_sec = retry_pause / 1000;
        delay.tv_nsec = (retry_pause % 1000) * 1000000;
        nanosleep(&delay, NULL);            // TODO: check for EINTR

        /* Update retry stats */
        pthread_mutex_lock(&priv->mutex);
        priv->stats.num_retries++;
        priv->stats.retry_delay += retry_pause;
        pthread_mutex_unlock(&priv->mutex);
    }

    /* Give up */
    (*config->log)(LOG_ERR, "giving up on: %s %s", io->method, io->url);
    return EIO;
}


void
getHash(char* hashString, struct curl_slist *headers, const char* method,const char* datebuf, const char* url, const char* contentType) {
	int hc=0;

	struct curl_slist *run = headers;
	for ( ; run != NULL; run = run->next)
		hc++;

	int i=0;
	char *emc_sorted_headers[hc];
	for (; headers != NULL; headers = headers->next) {
		emc_sorted_headers[i]=strdup(headers->data);
		i++;
	}

	char *shortUrl=strstr(url,"/rest/namespace");
	build_hash_string(hashString, method, contentType, NULL, datebuf, shortUrl, emc_sorted_headers, hc);
}

/*
 * Create URL for a block, and return pointer to the URL's path not including any "/bucket" prefix.
 */
static char *
http_atmos_io_get_url(char *buf, size_t bufsiz, struct http_atmos_io_conf *config, s3b_block_t block_num)
{
    char *resource;
    int len;

   // len = snprintf(buf, bufsiz, "%s%s4f6a13e4a1cd150a04f6a1a6f110c5050619dff7231f",config->baseURL, config->prefix);
    len = snprintf(buf, bufsiz, "%s%s/%0*jx", config->baseURL, config->prefix, S3B_BLOCK_NUM_DIGITS, (uintmax_t)block_num);
    resource = buf + strlen(config->baseURL);

    assert(len < bufsiz);
    return resource;
}

/*
 * Get HTTP Date header value based on current time.
 */
static void
http_atmos_io_get_date(char *buf, size_t bufsiz)
{
    time_t now = time(NULL);
    struct tm tm;

    strftime(buf, bufsiz, DATE_BUF_FMT, gmtime_r(&now, &tm));
}

static struct curl_slist *
http_atmos_io_add_header(struct curl_slist *headers, const char *fmt, ...)
{
    char buf[1024];
    va_list args;

    va_start(args, fmt);
    vsnprintf(buf, sizeof(buf), fmt, args);
    headers = curl_slist_append(headers, buf);
    va_end(args);
    return headers;
}

static CURL *
http_atmos_io_acquire_curl(struct http_atmos_io_private *priv, struct http_atmos_io *io)
{
    struct http_atmos_io_conf *const config = priv->config;
    struct curl_holder *holder;
    CURL *curl;

    pthread_mutex_lock(&priv->mutex);
    if ((holder = LIST_FIRST(&priv->curls)) != NULL) {
        curl = holder->curl;
        LIST_REMOVE(holder, link);
        priv->stats.curl_handles_reused++;
        pthread_mutex_unlock(&priv->mutex);
        free(holder);
        curl_easy_reset(curl);
    } else {
        priv->stats.curl_handles_created++;             // optimistic
        pthread_mutex_unlock(&priv->mutex);
        if ((curl = curl_easy_init()) == NULL) {
            pthread_mutex_lock(&priv->mutex);
            priv->stats.curl_handles_created--;         // undo optimistic
            priv->stats.curl_other_error++;
            pthread_mutex_unlock(&priv->mutex);
            (*config->log)(LOG_ERR, "curl_easy_init() failed");
            return NULL;
        }
    }

    curl_easy_setopt(curl, CURLOPT_URL, io->url);
	curl_easy_setopt(curl, CURLOPT_NOPROGRESS, 1);
    curl_easy_setopt(curl, CURLOPT_FAILONERROR, 1);
    curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1);
    curl_easy_setopt(curl, CURLOPT_NOSIGNAL, (long)1);
    curl_easy_setopt(curl, CURLOPT_TIMEOUT, (long)config->timeout);
    curl_easy_setopt(curl, CURLOPT_NOPROGRESS, 1);
    curl_easy_setopt(curl, CURLOPT_USERAGENT, config->user_agent);
    if (config->max_speed[HTTP_UPLOAD] != 0)
        curl_easy_setopt(curl, CURLOPT_MAX_SEND_SPEED_LARGE, (curl_off_t)(config->max_speed[HTTP_UPLOAD] / 8));
    if (config->max_speed[HTTP_DOWNLOAD] != 0)
        curl_easy_setopt(curl, CURLOPT_MAX_RECV_SPEED_LARGE, (curl_off_t)(config->max_speed[HTTP_DOWNLOAD] / 8));
    if (strncmp(io->url, "https", 5) == 0) {
        if (config->insecure)
            curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, (long)0);
        if (config->cacert != NULL)
            curl_easy_setopt(curl, CURLOPT_CAINFO, config->cacert);
    }
    if (config->debug_http)
        curl_easy_setopt(curl, CURLOPT_VERBOSE, 1);
    return curl;
}

static size_t
http_atmos_io_curl_reader(const void *ptr, size_t size, size_t nmemb, void *stream)
{
    struct http_atmos_io *const io = (struct http_atmos_io *)stream;
    struct http_atmos_io_bufs *const bufs = &io->bufs;
    size_t total = size * nmemb;

    if (total > bufs->rdremain)     /* should never happen */
        total = bufs->rdremain;
    memcpy(bufs->rddata, ptr, total);
    bufs->rddata += total;
    bufs->rdremain -= total;
    return total;
}

static size_t
http_atmos_io_curl_writer(void *ptr, size_t size, size_t nmemb, void *stream)
{
    struct http_atmos_io *const io = (struct http_atmos_io *)stream;
    struct http_atmos_io_bufs *const bufs = &io->bufs;
    size_t total = size * nmemb;

    /* Check for canceled write */
    if (io->check_cancel != NULL && (*io->check_cancel)(io->check_cancel_arg, io->block_num) != 0)
        return CURL_READFUNC_ABORT;

    /* Copy out data */
    if (total > bufs->wrremain)     /* should never happen */
        total = bufs->wrremain;
    memcpy(ptr, bufs->wrdata, total);
    bufs->wrdata += total;
    bufs->wrremain -= total;
    return total;
}

static size_t
http_atmos_io_curl_header(void *ptr, size_t size, size_t nmemb, void *stream)
{
    struct http_atmos_io *const io = (struct http_atmos_io *)stream;
    const size_t total = size * nmemb;
    char fmtbuf[64];
    char buf[1024];

    /* Null-terminate header */
    if (total > sizeof(buf) - 1)
        return total;
    memcpy(buf, ptr, total);
    buf[total] = '\0';

    //fprintf(stderr,"Headers devueltas: %s", buf);

    /* Check for interesting headers */
    (void)sscanf(buf, HTTP_HEADER_EM_LISTABLE_META ": " BLOCK_SIZE_HEADER "=%u, " FILE_SIZE_HEADER "=%ju" , &io->block_size, &io->file_size);

    if (strncasecmp(buf, LIST_PARAM_MARKER ":", sizeof(LIST_PARAM_MARKER)) == 0) {
    	(void)sscanf(buf, LIST_PARAM_MARKER ": %s", &io->token);
    	//fprintf(stderr,"TOKEN ENCONTRADO: %s", &io->token);
    	io->list_truncated = &IS_TRUNCATED;
    } else {
    	io->list_truncated = &NOT_TRUNCATED;
    }

    /* ETag header requires parsing */
    if (strncasecmp(buf, ETAG_HEADER ":", sizeof(ETAG_HEADER)) == 0) {
        char md5buf[MD5_DIGEST_LENGTH * 2 + 1];

        snprintf(fmtbuf, sizeof(fmtbuf), " \"%%%uc\"", MD5_DIGEST_LENGTH * 2);
        if (sscanf(buf + sizeof(ETAG_HEADER), fmtbuf, md5buf) == 1)
            http_atmos_io_parse_hex(md5buf, io->md5, MD5_DIGEST_LENGTH);
    }

    /* "x-amz-meta-s3backer-hmac" header requires parsing */
    if (strncasecmp(buf, HMAC_HEADER ":", sizeof(HMAC_HEADER)) == 0) {
        char hmacbuf[SHA_DIGEST_LENGTH * 2 + 1];

        snprintf(fmtbuf, sizeof(fmtbuf), " \"%%%uc\"", SHA_DIGEST_LENGTH * 2);
        if (sscanf(buf + sizeof(HMAC_HEADER), fmtbuf, hmacbuf) == 1)
            http_atmos_io_parse_hex(hmacbuf, io->hmac, SHA_DIGEST_LENGTH);
    }

    /* Content encoding(s) */
    if (strncasecmp(buf, CONTENT_ENCODING_HEADER ":", sizeof(CONTENT_ENCODING_HEADER)) == 0) {
        size_t celen;
        char *state;
        char *s;

        *io->content_encoding = '\0';
        for (s = strtok_r(buf + sizeof(CONTENT_ENCODING_HEADER), WHITESPACE ",", &state);
          s != NULL; s = strtok_r(NULL, WHITESPACE ",", &state)) {
            celen = strlen(io->content_encoding);
            snprintf(io->content_encoding + celen, sizeof(io->content_encoding) - celen, "%s%s", celen > 0 ? "," : "", s);
        }
    }

    /* Done */
    return total;
}

static void
http_atmos_io_release_curl(struct http_atmos_io_private *priv, CURL **curlp, int may_cache)
{
    struct curl_holder *holder;
    CURL *const curl = *curlp;

    *curlp = NULL;
    assert(curl != NULL);
    if (!may_cache) {
        curl_easy_cleanup(curl);
        return;
    }
    if ((holder = calloc(1, sizeof(*holder))) == NULL) {
        curl_easy_cleanup(curl);
        pthread_mutex_lock(&priv->mutex);
        priv->stats.out_of_memory_errors++;
        pthread_mutex_unlock(&priv->mutex);
        return;
    }
    holder->curl = curl;
    pthread_mutex_lock(&priv->mutex);
    LIST_INSERT_HEAD(&priv->curls, holder, link);
    pthread_mutex_unlock(&priv->mutex);
}

static void
http_atmos_io_openssl_locker(int mode, int i, const char *file, int line)
{
    if ((mode & CRYPTO_LOCK) != 0)
        pthread_mutex_lock(&openssl_locks[i]);
    else
        pthread_mutex_unlock(&openssl_locks[i]);
}

static u_long
http_atmos_io_openssl_ider(void)
{
    return (u_long)pthread_self();
}

static int
http_atmos_io_is_zero_block(const void *data, u_int block_size)
{
    static const u_long zero;
    const u_int *ptr;
    int i;

    if (block_size <= sizeof(zero))
        return memcmp(data, &zero, block_size) == 0;
    ptr = (const u_int *)data;
    for (i = 0; i < block_size / sizeof(*ptr); i++) {
        if (*ptr++ != 0)
            return 0;
    }
    return 1;
}

/*
 * Encrypt or decrypt one block
 */
static u_int
http_atmos_io_crypt(struct http_atmos_io_private *priv, s3b_block_t block_num, int enc, const u_char *src, u_int len, u_char *dest)
{
    u_char ivec[EVP_MAX_IV_LENGTH];
    EVP_CIPHER_CTX ctx;
    u_int total_len;
    char blockbuf[EVP_MAX_IV_LENGTH];
    int clen;
    int r;

#ifdef NDEBUG
    /* Avoid unused variable warning */
    (void)r;
#endif

    /* Sanity check */
    assert(EVP_MAX_IV_LENGTH >= MD5_DIGEST_LENGTH);

    /* Initialize cipher context */
    EVP_CIPHER_CTX_init(&ctx);

    /* Generate initialization vector by encrypting the block number using previously generated IV */
    memset(blockbuf, 0, sizeof(blockbuf));
    snprintf(blockbuf, sizeof(blockbuf), "%0*jx", S3B_BLOCK_NUM_DIGITS, (uintmax_t)block_num);

    /* Initialize cipher for IV generation */
    r = EVP_EncryptInit_ex(&ctx, priv->cipher, NULL, priv->ivkey, priv->ivkey);
    assert(r == 1);
    EVP_CIPHER_CTX_set_padding(&ctx, 0);

    /* Encrypt block number to get IV for bulk encryption */
    r = EVP_EncryptUpdate(&ctx, ivec, &clen, (const u_char *)blockbuf, EVP_CIPHER_CTX_block_size(&ctx));
    assert(r == 1 && clen == EVP_CIPHER_CTX_block_size(&ctx));
    r = EVP_EncryptFinal_ex(&ctx, NULL, &clen);
    assert(r == 1 && clen == 0);

    /* Re-initialize cipher for bulk data encryption */
    assert(EVP_CIPHER_CTX_block_size(&ctx) == EVP_CIPHER_CTX_iv_length(&ctx));
    r = EVP_CipherInit_ex(&ctx, priv->cipher, NULL, priv->key, ivec, enc);
    assert(r == 1);
    EVP_CIPHER_CTX_set_padding(&ctx, 1);

    /* Encrypt/decrypt */
    r = EVP_CipherUpdate(&ctx, dest, &clen, src, (int)len);
    assert(r == 1 && clen >= 0);
    total_len = (u_int)clen;
    r = EVP_CipherFinal_ex(&ctx, dest + total_len, &clen);
    assert(r == 1 && clen >= 0);
    total_len += (u_int)clen;

    /* Done */
    EVP_CIPHER_CTX_cleanup(&ctx);
    return total_len;
}

static void
http_atmos_io_authsig(struct http_atmos_io_private *priv, s3b_block_t block_num, const u_char *src, u_int len, u_char *hmac)
{
    const char *const ciphername = EVP_CIPHER_name(priv->cipher);
    char blockbuf[64];
    u_int hmac_len;
    HMAC_CTX ctx;

    /* Sign the block number, the name of the encryption algorithm, and the block data */
    snprintf(blockbuf, sizeof(blockbuf), "%0*jx", S3B_BLOCK_NUM_DIGITS, (uintmax_t)block_num);
    HMAC_CTX_init(&ctx);
    HMAC_Init_ex(&ctx, (const u_char *)priv->key, sizeof(priv->key), EVP_sha1(), NULL);
    HMAC_Update(&ctx, (const u_char *)blockbuf, strlen(blockbuf));
    HMAC_Update(&ctx, (const u_char *)ciphername, strlen(ciphername));
    HMAC_Update(&ctx, (const u_char *)src, len);
    HMAC_Final(&ctx, (u_char *)hmac, &hmac_len);
    assert(hmac_len == SHA_DIGEST_LENGTH);
    HMAC_CTX_cleanup(&ctx);
}

/*
 * Parse exactly "nbytes" contiguous 2-digit hex bytes.
 * On failure, zero out the buffer and return -1.
 */
static int
http_atmos_io_parse_hex(const char *str, u_char *buf, u_int nbytes)
{
    int i;

    /* Parse hex string */
    for (i = 0; i < nbytes; i++) {
        int byte;
        int j;

        for (byte = j = 0; j < 2; j++) {
            const char ch = str[2 * i + j];

            if (!isxdigit(ch)) {
                memset(buf, 0, nbytes);
                return -1;
            }
            byte <<= 4;
            byte |= ch <= '9' ? ch - '0' : tolower(ch) - 'a' + 10;
        }
        buf[i] = byte;
    }

    /* Done */
    return 0;
}

static void
http_atmos_io_prhex(char *buf, const u_char *data, size_t len)
{
    static const char *hexdig = "0123456789abcdef";
    int i;

    for (i = 0; i < len; i++) {
        buf[i * 2 + 0] = hexdig[data[i] >> 4];
        buf[i * 2 + 1] = hexdig[data[i] & 0x0f];
    }
    buf[i * 2] = '\0';
}


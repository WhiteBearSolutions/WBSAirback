#include <ctype.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <time.h>


#include "atmos_util.h"
int cstring_cmp(const void *a, const void *b)
{
    return strcmp(* (char * const *) a, * (char * const *) b);
}

int build_hash_string (char *hash_string, const char* method, const char *content_type, const char *range,const char *date, char *uri, char **emc_sorted_headers, const int header_count)
{
    char *loweruri = malloc(strlen(uri)+1);

    int is = 0;
	int i = 0;	
	int length = 0;
    for(is = 0; is < header_count; is++) {
    	lowercaseheader(emc_sorted_headers[is]);
    }

    qsort(emc_sorted_headers, header_count, sizeof(char*),cstring_cmp);

    sprintf(hash_string,"%s\n",method);

    if(content_type!=NULL) {
    	sprintf(hash_string,"%s%s\n",hash_string, content_type);
    } else{
    	sprintf(hash_string, "%s\n", hash_string);
    }

    if(range!=NULL) {
    	sprintf(hash_string,"%s%s\n",hash_string,range);
    } else{
    	sprintf(hash_string, "%s\n", hash_string);
    }
    
    if(date!=NULL) {
    	sprintf(hash_string,"%s%s\n",hash_string,date);
    } else{
    	sprintf(hash_string, "%s\n", hash_string);
    }

    strcpy(loweruri, uri);
    lowercase(loweruri);

    sprintf(hash_string,"%s%s\n",hash_string,loweruri);
    for(i = 0; i < header_count; i++) {
	if (i < header_count-1)
	    {
		sprintf(hash_string,"%s%s\n",hash_string, emc_sorted_headers[i]);
	    } 
	else 
	    {
		sprintf(hash_string,"%s%s",hash_string, emc_sorted_headers[i]);
	    }
    }

    free(loweruri);
   // printf("\nHASHSTRING\n %s \n", hash_string);

    return length;

}



void get_date(char *formated_time)
{
    //strftime adds a leading 0 to the day...
    time_t t = time(NULL);
    struct tm *a = gmtime(&t);

    strftime(formated_time, 256, "%a, %d %b %Y %H:%M:%S GMT", a);
    
}

void lowercaseheader(char *s) {
    int i = 0;
    for( ; s[i] != ':'; i++)
	s[i] = tolower(s[i]);
}

void lowercase(char *s) {
    int i = 0;
    for( ; s[i] != '\0'; i++)
	s[i] = tolower(s[i]);
}

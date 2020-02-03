#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Pass in the FILE *,
// uses ftell(FILE *):long to get position of file seek
// returns: 0 = not end of file. 1 = end of file
int isEndOfFile(FILE * f)
{
	if(f != NULL)
	{
		int scur = ftell(f);	//the old seek location, to recover the old location
		char c = fgetc(f);	//get the next char to check for a non empty character
		while(c != (char)-1)	//loop if its not end of file 
		{
			
			if((int)c > 32) //if character is non-empty
			{
				fseek(f, scur, SEEK_SET); //reset the seek location to old location
				return 0;
			}
			c = fgetc(f);
		}
		
		fseek(f, scur, SEEK_SET); //reset the seek location to old location
		return 1;
	}
	return 1;
}

//User input the string to search for
//set int *value to the value associated with that string
int load_line(FILE* f, char *name, int *value)
{
	rewind(f);		//reset file seek location
	char temp[11] = "";	//temporary string to read strings in file
	int temp2 = 0;		//the temporary int to be set to int *value
	
	//loop if not at the end of file
	while(isEndOfFile(f) == 0)
	{
		fscanf(f, "%s", temp); 	//set temp to the next string

		if(!isEndOfFile(f))	//check if theres an associated integer
			fscanf(f, "%d", &temp2);

		if(strcmp(temp, name) == 0)	//if temp matches char *name, set the value
		{
			*value = temp2;
			return 1;		// return 1 for successful
		}
		temp2 = 0;
			
	}
	return 0;	//return 0 for non-successful
}


/// SPLIT AND FREE /////////////////////
// pass in an already malloc char ** and the char * text.
// len will store how many elements in the char ** args
// split based on  the characters in char *toks
void split(char* str, char** args, int* len, char *toks)
{
	char line[100];		//hard-coded buffer size of 100
	char *splitted[100];	// buff 100
	char *tempTok;	
	int count = 0;	
	
	// while loop to duplicate the command string because strtok alter string	
	while(1)
	{
		// sets all command line to end wih \0 no matter what
		line[count] = str[count];	
		if(line[count] == '\n' || line[count] == '\0')
		{
			line[count] = '\0';
			//count++;
			break;
		}
		count++;
	
	}

	int i = 0;
	tempTok = strtok(line, toks);
	
	while(tempTok != NULL)
	{
		splitted[i] = strdup(tempTok);
		tempTok = strtok(NULL, toks);
		i++;
	}
	splitted[i] = (char*)malloc(sizeof(char));
	splitted[i] = NULL;
	
	// after splitting and temporarily storing in *splitted[], store it into *args[]
	int l = 0;
	for(l = 0; l < i+1; l++)
	{
		args[l] = splitted[l];
	}
	*len = l-1;
	
}


/////
// Get the next line of a text file.
// Returns NULL if line is empty or EOF
char* get_line(FILE *bat, int *len)
{
	char tempLine[200]; // hard-coded buffer size of 200
	int count = 0;
	char c;
	
	c = fgetc(bat); //get the next character
	while(c != EOF && c != '\n' && c != '\0') //if it is not new line, end of string, or end of file
	{
		// continue reading and storing the character read into line[]
		tempLine[count] = c;
		count++;
		c = fgetc(bat);
	}
	// last two characters become \n\0
	tempLine[count] = '\0';
	
	*len = count;

	char *line = (char*)malloc((*len + 1) * sizeof(char));
	
	int i;
	for(i = 0; i <= *len; i++)
	{
		line[i] = tempLine[i];
	}
	
	if(c == EOF)
	{
		free(line);
		line = NULL;
	}
	return line;
}


// load a text file line by line into memory
void load_words(char ***words, int *wrdlen, char *path)
{
	char **dic = NULL;
	int dicLen = 0;

	FILE *f = fopen(path, "r");	
	
	if(f != NULL)
	{
		int len = 0;
		char *line = get_line(f, &len); 

		int i = 1;
		while(line != NULL)
		{
			dicLen++; // increase the dictionary length counter
		
			// allocate memory for a new string (new dictionary word)
			dic = realloc(dic, dicLen * sizeof(char*));
			dic[dicLen-1] = (char*)malloc(len * sizeof(char));
			strcpy(dic[dicLen-1], line);
		
			// free the line received from file
			free(line);
			line = get_line(f, &len);
		}
		dic[dicLen] = NULL;
		
		*words = dic;
		
		*wrdlen = dicLen;
	
		
		printf("%d words loaded\n", dicLen);
	}
}

// free and nullifies the array of words
void free_words(char ***words, int *wrdlen)
{
	int i;
	for(i = 0; i < *wrdlen; i++)
	{
		if(*words[i] != NULL)
		{
			free(*words[i]);
			*words[i] = NULL;
		}
	}
	
	if(*words != NULL)
	{
		free(*words);
		*words = NULL;
	}

	*wrdlen = 0;
}




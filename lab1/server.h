#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include <unistd.h> // for read() and write()

#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>  //for htons()

#include <pthread.h>  //for threading

#include "queue.h" //queue is a queue that supports generic typing

#define BUFF_SIZE 256
#define WORKERS_NO 2  //how many workers there are
#define WELCOME_MSG "Hello!"
#define DICT_PATH "words.txt"
#define PORTNO 6969

// structure containing client socket fd. queue.h compatibility
typedef struct{
	int fd;
}client;

// structure containing log text. queue.h compatibility
typedef struct{
	int fd;
	char text[BUFF_SIZE];
}log;

// structure containing the global queues and monitor variables
typedef struct{
	//char **words;		// the words/dictionary list
	//int wrdlen;		// the length of the dictionary
	queue *clients_queue;	// the queue for connected clients
	queue *logs_queue;	// the queue for logs
	int num_workers;	// the number of worker threads

	pthread_mutex_t cliLock;	// mutex for dealing with clients
	pthread_mutex_t logLock;	// mutex for dealing wth logs
	pthread_cond_t hasClient;	// condition var for CLIENTS
	pthread_cond_t hasLog;		// cond var for logs
}shared_data;

// OTHER STUFF
void load_words(char***, int*, char*);	// load words into shared_data.words
void free_words(char***, int*);	// free/nullify shared_data.words
void init_shared_data(shared_data *);		// initialize a shared_data object
void destroy_shared_data(shared_data *);	// free/nullify shared_data obj
int encryption_stuff(int, shared_data *, char *, char *); //ret 1 = word exist. 0 = word no exist

// MONITOR FUNCTIONS
log *get_log(shared_data *);		  // monitor function that returns a log if exist
void add_log(shared_data *, int, char *); // monitor function that add new log to queue
client *get_client(shared_data *);	  // monitor function that returns a client if exist
void add_client(shared_data *, int);	  // monitor function that add new client to queue

// THREAD FUNCTIONS
void *logger(void *);		// THREAD FOR LOGGER
void *serve_client(void *);	// THREAD FOR WORKERS



#include "server.h"
#include "filereader.h"

// setting default values to struct shared_data object
void init_shared_data(shared_data *glob){
	load_words(&glob->words, &glob->wrdlen, "words.txt");
	glob->clients_queue = createQueue();
	glob->logs_queue = createQueue();
	glob->num_workers = WORKERS_NO;
	pthread_mutex_init(&(glob->cliLock), NULL);
	pthread_mutex_init(&(glob->logLock), NULL);
	pthread_cond_init(&(glob->hasClient), NULL);
	pthread_cond_init(&(glob->hasLog), NULL);
}

// nullifying the shared_data struct
void destroy_shared_data(shared_data *glob){
	free_words(&glob->words, &glob->wrdlen);
	destroyQueue(glob->clients_queue);
	destroyQueue(glob->logs_queue);
	glob->num_workers = 0;
	pthread_mutex_init(&(glob->cliLock), NULL);
	pthread_mutex_init(&(glob->logLock), NULL);
	pthread_cond_init(&(glob->hasClient), NULL);
	pthread_cond_init(&(glob->hasLog), NULL);
}


// word the list of word for a match. return true if match.
// The word to be searched is char *word. char *send is the output text of the result
int encryption_stuff(int clientfd, shared_data *glob, char *send, char *recv)
{
	/*
	int i;
	
	for(i = 0; i < glob->wrdlen; i++)
	{
		if(strcmp(glob->words[i], recv) == 0)
		{
			strcpy(send, "(CORRECT)\t");
			strcat(send, recv);
			return 1;
		}
	}

	
	strcpy(send, "(INCORRECT)\t");
	strcat(send, recv);
	*/

	strcpy(send, "I received ");
	strcat(send, recv);

	return 0;	
}


// monitor function to return the next log in  a queue
log *get_log(shared_data *glob)
{
	// start critical section
	pthread_mutex_lock(&(glob->logLock));  //log lock obtained
	
	// wait for cond hasLock
	while(glob->logs_queue->count <= 0)
		pthread_cond_wait(&(glob->hasLog), &(glob->logLock)); 

	log *delog = (log *)dequeue(glob->logs_queue);	// remove the next log entry

	pthread_mutex_unlock(&(glob->logLock));  //log lock released
	// end critical section

	return delog;
}

// monitor function to add a new log
void add_log(shared_data *glob, int clientfd, char *msgsend)
{
	// start critical section
	pthread_mutex_lock(&(glob->logLock));   //log lock obtained

	log *msglog = (log *)malloc(sizeof(log));
	msglog->fd = clientfd;			// store the client socket in the log obj
	strcpy(msglog->text, msgsend);		// copy the msg to log.text
	enqueue(glob->logs_queue, msglog);	// add log to queue

	pthread_cond_signal(&(glob->hasLog)); //signal hasLog. new log added

	pthread_mutex_unlock(&(glob->logLock)); //log lock released
	// end critical section
}

//  monitor func to return next client in queue
client *get_client(shared_data *glob)
{
	// start critical section
	pthread_mutex_lock(&(glob->cliLock)); //client lock obtained
	
	// check cond variable hasClient
	while(glob->clients_queue->count <= 0)	
		pthread_cond_wait(&(glob->hasClient), &(glob->cliLock));	
	
	client *cli = (client *)dequeue(glob->clients_queue);	// remove next client from queue
	
	//// notify the client that they're being served
	char *msgsend = (char *)malloc(BUFF_SIZE * sizeof(char));
	memset(msgsend, 0, BUFF_SIZE);
	strcpy(msgsend, "...Now being served");	
	send(cli->fd, msgsend, BUFF_SIZE, 0);
	
	//// MONITOR FUNCTION TO  ADD LOG
	add_log(glob, cli->fd, msgsend);

	free(msgsend);

	pthread_mutex_unlock(&(glob->cliLock)); // client lock released
	// end critical section

	return cli;
}

// monitor func to add a new client to queue
void add_client(shared_data *glob, int clientfd)
{
	// start critical section
	pthread_mutex_lock(&(glob->cliLock)); //client lock obtained
	
	// create a queue-compatible client structure
	client *cli = (client *)malloc(sizeof(client));
	cli->fd = clientfd;
	enqueue(glob->clients_queue, cli);

	// notify client that they're enqueued but not yet served
	char *msgsend = (char *)malloc(BUFF_SIZE * sizeof(char));
	memset(msgsend, 0, BUFF_SIZE);
	strcpy(msgsend, "Awaiting service...");	
	send(clientfd, msgsend, BUFF_SIZE, 0);

	//// MONITOR FUNCTION TO ADD LOG
	add_log(glob, clientfd, msgsend);
	
	free(msgsend);

	pthread_cond_signal(&(glob->hasClient)); //signal hasClient because new client is added

	pthread_mutex_unlock(&(glob->cliLock)); //client lock release
	// end critical section
}

// function for the logger thread
// aka the LOGGER
void *logger(void *args) /// arguments = struct shared_data *
{
	FILE *file = fopen("log.txt", "a+");	// open a new log file
	fputs("\n=== NEW SESSION ===\n", file);	// append NEW SESSION separator
	shared_data *data = args;		// parse void *args to shared_data *data
	log *delog = NULL;			// temporary variable that stores dequeue'd logs

	char *msgsend = NULL;  // temp variable that store message to be logged

	while(1)
	{	
		/// CALL MONITOR FUNCTION TO GET NEXT LOG
		delog = get_log(data);		
	
		// nullifies the memory	
		msgsend = (char*)malloc(BUFF_SIZE * sizeof(char));			
		memset(msgsend, 0, BUFF_SIZE);

		// format the string
		sprintf(msgsend, "[CLIENT %d] %s\n", delog->fd, delog->text);		

		// write the string to file
		fputs(msgsend, file);
		fflush(file); // flush just in case

		// free the temporary log and char * variables
		free(delog);
		free(msgsend);
		delog = NULL;
		msgsend = NULL;
	}
}

// fuction for the worker threads
// aka THE WORKERS
void *serve_client(void *args)	// args = struct shared_data *
{
	shared_data *data = args;	// parsing arg to shared_data
	client *tempcli = NULL;		// temporary variable that stores dequeue'd client
	int clientfd;			// temporary variable stores connected client sockets

	while(1)
	{
		// CALL MONITOR FUNCTION TO GET NEXT CLIENT
		client *temp = get_client(data);
		clientfd = temp->fd;
		free(tempcli);

		// initializing buffers
		char *msgsend = (char*)malloc(BUFF_SIZE * sizeof(char));
		char *msgrec = (char*)malloc(BUFF_SIZE * sizeof(char));	
		// ------------------------------------


		// START SERVING CLIENTS		
		printf("[SERVING CLIENT %d]\n", clientfd);
	
		// nullifies send and receive buffers		
		memset(msgsend, 0, BUFF_SIZE);
		memset(msgrec, 0, BUFF_SIZE);
	

		// wait for a message from user
		int recvReturn = recv(clientfd, msgrec, BUFF_SIZE, 0); 
		int msglen = strlen(msgrec); // length of the message received

		// service loop
		while(recvReturn > 0)
		{
			// if string ends with a \n then replace it with \0
			if(msgrec[msglen-1] == '\n')
				msgrec[msglen-1] = '\0';
			
			// search dictionary for the word and send the result to user
			encryption_stuff(clientfd, data, msgsend, msgrec);
			
			send(clientfd, msgsend, BUFF_SIZE, 0);			

			//// CALL MONITOR FUNCTION TO ADD A LOG
			add_log(data, clientfd, msgsend);
		

			printf("[CLIENT %d] %s\n", clientfd, msgrec);
			printf("[RESULT %d] %s\n", clientfd, msgsend);
			

			recvReturn = recv(clientfd, msgrec, BUFF_SIZE, 0);
			msglen = strlen(msgrec);
		}
			
		//CLOSE THE CLIENT SOCKET ONCE SERVICE ENDS
		close(clientfd); 
		memset(msgsend, 0, BUFF_SIZE);
		strcpy(msgsend, "DISCONNECTED");
		add_log(data, clientfd, msgsend);

		printf("[CLIENT %d] DISCONNECTED\n", clientfd);

		// FREE MEMORY
		if(msgsend != NULL)
			free(msgsend);
		if(msgrec != NULL)	
			free(msgrec);
		msgsend = NULL;
		msgrec = NULL;
	}	
	
	return NULL;
}



int main(int argc, char **argv)
{
	fclose(fopen("log.txt", "a+")); // open/create new file for log

	// INITIALIZING SERVER AND SOCKET
	int servfd = -1, optval = 1;
	servfd = socket(AF_INET, SOCK_STREAM, 0);
	// set socket to reusable
	setsockopt(servfd, SOL_SOCKET, SO_REUSEADDR, (const void *)&optval , sizeof(int));	

	struct sockaddr_in ipv4; 
	bzero((char *)&ipv4, sizeof(ipv4));
	ipv4.sin_family = AF_INET;
	if(argc > 1)
	{	
		printf("ATTEMPTING PORT [%s]\n", argv[1]);
		ipv4.sin_port = htons(atoi(argv[1]));
	}else 
	{
		printf("ATTEMPTING PORT [%d]\n", PORTNO);
		ipv4.sin_port = htons(PORTNO);
	}
	ipv4.sin_addr.s_addr = INADDR_ANY;//inet_addr("10.109.78.173");
	//-----


	// LOOP TO BIND PORT
	int bindres = bind(servfd, (struct sockaddr *)&ipv4, sizeof(ipv4));
	while(bindres == -1)
	{
		printf("BIND FAILED. REATTEMPTING...\n"); 
		sleep(3);
		bindres = bind(servfd, (struct sockaddr *)&ipv4, sizeof(ipv4));
	}
	printf("BIND SUCCESSFUL\n");
	// -----


	// INITIALIZE GLOBALS /// contain queues and stuff
	shared_data globals;
	init_shared_data(&globals);

	
	// SET UP WORKER THREADS ARRAY
	// CREATE A # OF WORKER THREADS. # = WORKERS_NO 
	int i = 0;	
	pthread_t workers[WORKERS_NO + 1]; // WORKERS_NO + 1  is the logger thread	
	for(i = 0; i < WORKERS_NO; i++)
		pthread_create(&workers[i], NULL, serve_client, &globals);
	pthread_create(&workers[WORKERS_NO], NULL, logger, &globals); // create logger thread
		


	/// ACCEPTING CLIENTS LOOP
	int clientfd; //temp var for client sockets
	i = 0;
	while(1){
		listen(servfd, 20);	// listen to 1 conection
		clientfd = accept(servfd, NULL, NULL); // accept the 1 connection
		
		// CALL MONITOR FUNCTION TO ADD A CLIENT
		add_client(&globals, clientfd);
	}
	/// -----------------------------------------

	///// program might not reach this area	

	/// join all the worker threads
	for(i = 0; i < WORKERS_NO+1; i++)
		pthread_join(workers[i], NULL);

	close(servfd); // close the socket
	destroy_shared_data(&globals);

	return 0;
}

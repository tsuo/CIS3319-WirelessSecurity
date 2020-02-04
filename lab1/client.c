#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h> // for read() and write()

#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>  //for htons()

#define PORTNO 6969

//#include "filereader.h"

int main(int argc, char **argv)
{
	size_t buffsize = 256;
	char *msgrec = (char*)malloc(buffsize * sizeof(char));

	char *msgsend = (char*)malloc(buffsize * sizeof(char));

	int servfd = -1; 
	servfd = socket(AF_INET, SOCK_STREAM, 0);
	
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
	ipv4.sin_addr.s_addr = INADDR_ANY;
	
	// client will connect
	printf("CONNECTION STATUS: %d\n", connect(servfd, (struct sockaddr *)&ipv4, sizeof(ipv4)));

	printf("\nESCAPE CHARACTER: \"ctrl + [\"\n\n");

	// MUST RECEIVE TWO WELCOME MESSAGES FIRST BEFORE SERVICE
	// receive first message
	recv(servfd, msgrec, buffsize, 0);
	printf("[SERVER %d] %s\n", servfd, msgrec);
	// receive second message
	recv(servfd, msgrec, buffsize, 0);
	printf("[SERVER %d] %s\n", servfd, msgrec);


	int recvReturn = -1;
	while(1)
	{
		memset(msgsend, 0, buffsize);
		memset(msgrec, 0, buffsize);
		printf("Message: ");
		getline(&msgsend, &buffsize, stdin);

		// implementing escape character
		// 27  is  ^[  in console
		if((int)msgsend[0] == 27)
		{
			printf("CLIENT TRIGGERED ESCAPE CHARACTER\n");
			free(msgsend);
			free(msgrec);
			close(servfd);
		}		
		
		printf("[SENDING] %s", msgsend);
		send(servfd, msgsend, buffsize, 0);

		recvReturn = recv(servfd, msgrec, 256, 0);
		if(recvReturn <= 0) 
		{
			printf("LOST CONNECTION WITH SERVER [%d]\n", servfd);
			break;
		}
		else printf("[SERVER %d] %s\n", servfd, msgrec);
	}

	free(msgsend);
	free(msgrec);
	close(servfd);

	return 0;
}
#include <stdio.h>
#include <stdlib.h>

//////////////////////////////////
// DAN TRAN
// CIS 3207 - OS
// APR/2019
// SIMPLE LINKED-LIST QUEUE
// SUPPORTS SEMI-GENERIC TYPING (using void* for data)
//


//struct for data nodes
struct node
{
	struct node *next;  //pointer to next data node
	void *data;	   //process id
};

//struct for the linked list queue
typedef struct
{
	struct node *head;  // the head data node
	struct node *tail;  // tail of data node to assist enqueue
	int count;	    // length of queue
}queue;


//initialize the queue. nullifies
// returns a dynamically allocated queue structure pointer
queue * createQueue()
{
	queue *que = (queue *)malloc(sizeof(queue));
	que->head = NULL;
	que->tail = NULL;
	que->count = 0;
	return que;
}

// allocate memory for struct node and return the pointer
struct node* createNode(void *data)
{
	struct node *newNode = (struct node*)calloc(1, sizeof(struct node));
	newNode->next = NULL;
	newNode->data = data;
	return newNode;
}


//delete a node. free() node.
void deleteNode(struct node *n)
{
	//printf("Freeing %d\n", n->id); 
	free(n);    //free the allocated memory
	n = NULL;   //nulls the pointer for safety
}

//nulify the queue and free all mallocs
void destroyQueue(queue *que)
{
	int i;
	struct node *cur = que->head;    
	struct node *next = NULL;
	
	// start with the head, store pointer to next node in *next;
	// delete the head (later referred as *cur)
	// set new cur to next
	// repeat until all nodes are destroyed.
	for(i = 0; i < que->count; i++)
	{
		if(cur != NULL)
		{
			next = cur->next;
			deleteNode(cur);
			cur = next;		
		}
		else
		{
			break;
		}
	}
	que->head = NULL;
	que->tail = NULL;
	que->count = 0;
	
	//finally free the malloc queue
	free(que);
}




// add new data node to queue
void enqueue(queue *que, void *data)
{
	struct node *n = createNode(data);
	// if the queue is empty,
	// set both head and tail to a new data node.
	if(que->head == NULL)
	{
		que->head = n;
			
		que->tail = que->head;
	
		que->count += 1;
		return;	 // return to avoid adding nodes again (code below)
	}
	
	// if queue isnt empty, add node to tail->next
	// set the tail to the new node
	que->tail->next = n;

	que->tail = que->tail->next;
	que->count += 1;
}

// return the head of the queue.
// set head of queue to oldhead.next
void* dequeue(queue *que)
{
	if(que->head != NULL)
	{
		struct node *ret = que->head;
		que->head = que->head->next;
		que->count -= 1;
		void *data = ret->data;
		free(ret);
		ret = NULL;
		return data;
	}
	return NULL;
}


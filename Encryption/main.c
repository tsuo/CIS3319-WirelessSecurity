//
//  main.c
//  Lab 2
//
//  Created by Bradley Juma on 9/21/17.
//  Copyright © 2017 Bradley Juma. All rights reserved.
//

#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>

#pragma warning(push)
#pragma warning(disable: warning-code) //4996 for _CRT_SECURE_NO_WARNINGS equivalent
// deprecated code here
#pragma warning(pop)

//#ifdef _WIN32
//#define _CRT_SECURE_NO_WARNINGS
//#endif

#define MAX_SIZE 256

void encrypt_data(FILE* input_file, FILE* output_file, char key[]);
void decrypt_data(FILE* input_file, FILE* output_file, char key[]);
int read_file(int len, char key_file[]);
void write_file(int len, char* cipher);
int getLength(char string[], int size);
void make_rand_key(int len, char* key_file);
void menu(void);

int main(int argc, const char * argv[]) {
    srand((unsigned int)time(NULL));       //seeds the pseudo-random generator
    menu();
    
    //c++11 says that I don't have to return 0. it's supposed to do it automatically. Get your shit together, gcc.
}

void menu()
{
    //int n;
    int choice = 0;
    //studentData details [MAX_STUDENTS]; //define a structure array of size MAX_STUDENTS
    char key[256] = {0};
    char file_name[32] = {0};
    char file_out[32] = {0};
    char key_file[32] = {0};
    
    printf("Press 1 to Encrypt File\n\n");
    printf("Press 2 to Decrypt File\n\n");
    printf("Press 3 to Exit\n\n");
    
    scanf("%d", &choice);
    while (getchar() != '\n');
    if (choice != 3)
    {
        do
        {
            //system("cls");
            printf("\nEnter the input filename: \n");
            fgets(file_name,32,stdin);
            file_name[strlen(file_name)-1] = '\0';
            
            printf("\nEnter the output filename: \n");
            fgets(file_out,32,stdin);
            file_out[strlen(file_out)-1] = '\0';
            
            switch (choice) {
                case 1:
                {
                    make_rand_key(256,key_file);
                    FILE* key_f = fopen(key_file,"rb");
                    fread(key,256,1,key_f);
                    fclose(key_f);
                    
                    FILE* in_f = fopen(file_name,"rb");
                    FILE* out_f = fopen(file_out, "wb");
                    encrypt_data(in_f, out_f, key);
                    fclose(in_f);
                    fclose(out_f);
                    
                    break;
                }
                    
                case 2:
                {
                    printf("Enter the correct key file name: ");
                    fgets(key_file,255,stdin);
                    key_file[strlen(key_file)-1] = '\0';
                    
                    FILE* key_f = fopen(key_file,"rb");
                    fread(key,256,1,key_f);
                    fclose(key_f);
                    
                    FILE* in_f = fopen(file_name,"rb");
                    FILE* out_f = fopen(file_out,"wb");
                    decrypt_data(in_f, out_f, key);
                    fclose(in_f);
                    fclose(out_f);
                    
                    break;
                }
                    
            }
            
            printf("\nPress 1 to Encrypt File\n\n");
            printf("Press 2 to Decrypt File\n\n");
            printf("Press 3 to Exit\n\n");
            
            scanf("%d", &choice);
            while (getchar() != '\n');
        } while (choice != 3);
    }
}

/*
 void menu(){
 
 // int for user's choice
 int choice = 0;
 // Loop until choice == 4
 while(choice != 3){
 printf("Menu\n");
 printf("Press 1 to Encrypt File\n\n");
 printf("Press 1 to Encrypt File\n\n");
 printf("Press 3 to exit\n");
 // Prompt user for choice
 printf("Enter a choice: ");
 // Get choice from user
 scanf("%d", &choice);
 // Switch-case is a good structure for processing menu choices
 switch (choice){
 case 1: // choice is 1
 printf("You chose 1\n");
 printf("Performing operation 1...\n\n");
 break;
 case 2: // choice is 2
 printf("You chose 2\n");
 printf("Performing operation 2...\n\n");
 break;
 default: // Invalid choice
 printf("Please enter a valid choice\n\n");
 }
 }
 return;
 }*/


int read_file(int len, char key_file[])
{
    
    FILE *fp = fopen(key_file, "r");
    if (fp == 0) {
        perror("fopen");
        exit(1);
    }
    
    if (len == 0) {
        fseek(fp,0L,SEEK_END);
        len = ftell(fp);
        
        printf("Length = %d\n", len);
        
        char *input = (char*)malloc(len + 1); //34 characters plus null
        rewind(fp);
        if (input == NULL) {
            printf("ERROR: Memory not allocated !\n");
            exit(2);
        }
        int j = 0;
        for (j = 0; j<len; ++j)
            input[j] = getc(fp);
        input[j] = '\0'; // j = null character
        
        printf("| %s |\n", input);
    }
    
    if (fp == NULL) {
        fprintf(stderr, "ERROR: File not opened. \n");
        exit(1);
    }
    fclose(fp);
    
    return len;
    
}

void write_file(int len, char* cipher )
{
    int n = 0;
    char string[1000];
    
    FILE *fp = fopen(cipher, "w");
    if (fp == 0) {
        perror("fopen");
        exit(1);
    }
    fprintf(fp, "Hello World!%s", string);
    char *output = (char*)malloc(len + 1);
    if (len == 0) {
        
        while (output[n] != '\0') {
            putc(output[n], fp);
        }
    }
    else {
        for (n = 0; n < len; n++) {
            putc(output[n], fp);
        }
        if (output != NULL) {
            printf("ErroR!");
        }
        free(output);
        output = NULL;
        fclose(fp);
        
    }
    
    
    
}

void make_rand_key(int len, char* key_file) {
    
    int i;
    FILE* fp;
    printf("Key filename: ");
    fgets(key_file,255,stdin);
    key_file[strlen(key_file)-1] = '\0';
    
    fp = fopen(key_file, "wb");
    if (fp == 0) {
        perror("fopen");
        exit(1);
    }
    
    if (len > 256)
        len = 256;
    else if (len <= 0)
        len = 16;
    
    for (i = 0; i < len; i++)
    {
        unsigned char c = (rand() % 255)+1;
        fwrite(&c,1,1,fp);
    }
    
    fclose(fp);
}
int getLength(char string[], int size) { // Function to get the length of a string.
    
    
    for (size = 0; string[size] != '\0'; size++) {
        
    }
    return size;
}
void encrypt_data(FILE* input_file, FILE* output_file, char key[])
{
    int key_count = 0; //Used to restart key if strlen(key) < strlen(encrypt)
    int encrypt_byte;
    
    while ((encrypt_byte = fgetc(input_file)) != EOF) //Loop through each byte of file until EOF
    {
        printf(".");
        //XOR the data and write it to a file
        fputc(encrypt_byte ^ key[(key_count++)%strlen(key)], output_file);
        
        //Increment key_count and start over if necessary
    }
    printf("Done");
}
void decrypt_data(FILE* input_file, FILE* output_file, char key[])
{
    //make_rand_key(<#int len#>, <#int key#>)
    int key_count = 0; //Used to restart key if strlen(key) < strlen(encrypt)
    int decrypt_byte;
    
    while ((decrypt_byte = fgetc(input_file)) != EOF) //Loop through each byte of file until EOF
    {
        printf(".");
        //XOR the data and write it to a file
        fputc(decrypt_byte ^ key[(key_count++)%strlen(key)], output_file);
        
        //Increment key_count and start over if necessary
    }
    printf("Done");
}

/*void make_rand_key(int len, char key[])
 {
 // Seed number for rand()
 srand((unsigned int)time(NULL));
 char length[100];
 int n = 0;
 FILE *fp1;
 fp1 = fopen("/Users/bradleyjuma/Desktop/Lab2p2/key.txt", "w");
 if(fp1 == 0) {
 perror("Error");
 exit(1);
 }
 
 // length = (char)key;
 key = (char*) malloc(len+1);
 if (len == 0) {
 
 while (key[n] != '\0') {
 putc(key[n], fp1);
 }
 }
 else {
 for (n = 0; n < len; n++) {
 putc(key[n], fp1);
 }
 if (key != NULL) {
 printf("ErroR!");
 }
 free(key);
 // ASCII characters 33 to 126
 while(key--) {
 key = (rand() % 26 + 65);
 srand(rand());
 fputs(&key, fp1);
 fclose(fp1);
 }
 }
 }*/




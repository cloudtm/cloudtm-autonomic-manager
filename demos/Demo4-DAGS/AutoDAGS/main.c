/*
* CINI, Consorzio Interuniversitario Nazionale per l'Informatica
* Copyright 2013 CINI and/or its affiliates and other
* contributors as indicated by the @author tags. All rights reserved.
* See the copyright.txt in the distribution for a full listing of
* individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 3.0 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/wait.h>

#include "hashtable.h"

#define	DEFAULT_NUM_TX	1000
#define DEFAULT_DATA_ITEMS	1000
#define DEFAULT_NUM_SERVERS	1
#define DEFAULT_NUM_CLIENTS	1

/*--------------------------------------------------*/

contest_list global_list;
contest_list client_list;
contest_list server_list;
contest_list network_list;

node_list *num_server, *num_clients, *object_replication_degree,  *data_items, *num_thread, *num_tx;

//------------------- flags for initialize() function ---------------------//

int file_presence = 0;
int min_ts_flag = 0, max_ts_flag = 0, files_flag = 0, trans_flag = 0;		//Used to check mandatory arguments.
int data_items_flag = 0;

//-------------------------------------------------------------------------//

char **file_list;
unsigned long long int MIN_TIME_STAMP;
unsigned long long int MAX_TIME_STAMP;
char * error = "Specify: \n\t- at least one file after '-f' (use comma to separate various file name);\n\t -minimum and maximum timestamp after respectively '-m' and '-M'\n\nExample: 'parser -f file1.log, ... ,fileN.log -m 1234 -M 12345'";

int min_servers, max_servers, min_clients, max_clients;
int num_of_transactions, num_of_data_items;

FILE *file_current, *file_conf;

//buffers used for strtok functions.
char buffer[1048576], *bufID, *bufVAL, *bufID_PRE_tmp, *buf_val_desc, *buf_val_value, *buf_tmp,*buf_tmp_value, *last_tok, *timestamp;


/*---------------------------------------------------*/

void free_filenames(){
	int f_l;
	for(f_l=0;f_l<file_presence;f_l++){
		free(file_list[f_l]);
	}
	free(file_list);
}

void print_lists(){
	FILE *file_out = fopen("simulation.conf","w+");
	if(file_out==NULL) printf("NULL\n");
	list_on_file(&global_list,file_out);
	list_on_file(&client_list,file_out);
	list_on_file(&server_list,file_out);
	list_on_file(&network_list,file_out);
	fclose(file_out);
}

void get_output(FILE *file_in, FILE *file_out, int class_id){
	char buff_line[60];
	char *duplicate, *class, *fields;
	int cmp;
	fprintf(file_out,"\nclasse%d\n",class_id);
	while(fgets(buff_line, sizeof(buff_line), file_in)!=NULL){
		if(buff_line[0]!='[') continue;
		duplicate = strdup(buff_line);
		duplicate++;
		duplicate[strlen(duplicate)-2] ='\0';
		class = strtok(duplicate,"|");
		fields = strtok(NULL,"|");
		cmp = atoi(class);
		if(cmp==class_id){
			fprintf(file_out,"%s\n",fields);
		}
		duplicate--;
		free(duplicate);
	}
}

void parse_output(){
	char buff_line[60];
	FILE *f_in, *f_out;
	f_in = fopen("output.txt","r");
	f_out = fopen("output_plot.txt","w");
	fgets(buff_line , sizeof(buff_line),f_in);
	int it_cl, num_cl = atoi(buff_line);
	for(it_cl = 0 ; it_cl < num_cl ; it_cl++){
		get_output(f_in, f_out, it_cl);
		fseek(f_in,0,SEEK_SET);
	}
	fclose(f_in);
	fclose(f_out);
}

void run_simulations(char* command, int min_servers, int max_servers, int min_clients, int max_clients) {
	int server_it, client_it, object_replication_degree_it, pid, status;
	node_list *tmp;
	FILE *file;

	if (data_items_flag) {
		tmp = get_node_list(&global_list, "cache_objects");
		set_node_value(tmp, num_of_data_items);
	}
	for (client_it = min_clients; client_it <= max_clients; client_it++) {
		set_node_value(num_clients, client_it);
		for (server_it = min_servers; server_it <= max_servers; server_it++) {
			set_node_value(num_server, server_it);
			// add comma to output file
			file = fopen("output.txt","a");
			fprintf(file,"%i,%i,",client_it,server_it);
			fclose(file);
			for (object_replication_degree_it = 1; object_replication_degree_it <= server_it; object_replication_degree_it++) {
				set_node_value(object_replication_degree, object_replication_degree_it);

				print_lists();

				pid = fork();
				if(pid==0){
					printf("Executing simulation with %i clients and %i servers, object replication degree = %i ...  \n", client_it,server_it, object_replication_degree_it);
					int ret = execl("/bin/bash","bash","-c",command,NULL);
					if (ret < 0) {
						printf("%s\n", strerror(errno));
						exit(1);
					}
				}
				else{
					wait(&status);
				}
				// add comma to output file
				file = fopen("output.txt","a");
				fprintf(file,",");
				fclose(file);

			}
			file = fopen("output.txt","a");
			fprintf(file,"\n");
			fclose(file);


		}
	}
	printf("Simulations terminated");
}

int create_configuration_file() {
	unsigned long long int time_st; //timestamp
	int i, it_file;
	inithashtab();

	for (it_file = 0; it_file < file_presence; it_file++) {
		file_current = fopen(file_list[it_file], "r");
		if (file_current == NULL) {
			printf("%s:\tFile not found\n", file_list[it_file]);
			free_filenames();
			exit(EXIT_FAILURE);
		} else
			printf("File: %s opened\n", file_list[it_file]);
		char * bufID_PRE = (char *) malloc(100);
		char * initial_id = (char *) malloc(8);
		while (fgets(buffer, sizeof(buffer), file_current) != NULL) { //scanning of (ISPN) files taken as arguments and hashtable filling.
			bufID = strtok(buffer, "[");
			bufVAL = strtok(NULL, "\0");
			bufVAL[strlen(bufVAL) - 3] = '\0';

			strtok(bufID, ":");
			for (i = 0; i < 5; i++) {
				strtok(NULL, ":");
			}
			timestamp = strtok(NULL, ":");
			bufID_PRE_tmp = strtok(NULL, ":");

			time_st = strtoull(timestamp, NULL, 10);

			if ((time_st >= MIN_TIME_STAMP || MIN_TIME_STAMP == 0)
					&& (time_st <= MAX_TIME_STAMP || MAX_TIME_STAMP == 0)) { //check if timestamp is in the specified range
				for (buf_tmp = strtok_r(bufVAL, ",", &last_tok);
						buf_tmp != NULL;
						buf_tmp = strtok_r(NULL, ",", &last_tok)) {
					strcpy(bufID_PRE, bufID_PRE_tmp);
					strtok(buf_tmp, ":");
					buf_val_desc = strtok(NULL, ":");
					buf_tmp_value = strtok(NULL, ":");
					strtok(buf_tmp_value, " ");
					buf_val_value = strtok(NULL, " ");
					buf_val_desc++;
					strcat(bufID_PRE, "_");
					strcat(bufID_PRE, buf_val_desc);
					install(bufID_PRE, buf_val_value);
				}

			}

		}

		free(bufID_PRE);
		free(initial_id);
		fclose(file_current);
	}

	file_conf = fopen("simile-simulation.conf", "r"); //filling of lists to create simulation.conf file.

	if (file_conf == NULL) {
		printf(
				"Error - a configuration fac-simile file had to be present in the working directory\n");
		cleanup();
		free_filenames();
		return -1;
	}

	print_on_list(file_conf, "Global", &global_list);

	fseek(file_conf, 0, SEEK_SET);
	print_on_list(file_conf, "Client", &client_list);

	fseek(file_conf, 0, SEEK_SET);
	print_on_list(file_conf, "Server", &server_list);

	fseek(file_conf, 0, SEEK_SET);
	print_on_list(file_conf, "Network", &network_list);

	fclose(file_conf);
}

int num_occurrences(char* input, char symb){
	int occ, count=0;
	for(occ=0;occ<strlen(input);occ++){
		if(input[occ]==symb) count++;
	}
	return count;
}

int initialize(int argc, char** argv){		//It takes arguments from command line (argv) and initializes global variables.
	int c;
	char * files;
	char * file_curr;
	int token_server = 2, token_client = 2, token_help = 0;

		
	while ((c = getopt(argc, argv, "f:m:M:s:S:c:C:e:d:h")) != -1){		//function to take arguments from command line.
		switch(c){
			case 'f':			//file list
				files = optarg;
				file_presence = num_occurrences(files,',')+1;
				file_list = (char **) malloc(file_presence * sizeof(char *));
				int f;
				for(file_curr=strtok(files,","),f=0;file_curr!=NULL;file_curr = strtok(NULL,","),f++)
					file_list[f] = strdup(file_curr);
				files_flag = 1;
				break;
			
			case 'm':			//minimum timestamp
				MIN_TIME_STAMP = strtoull(optarg,NULL,10);
				min_ts_flag = 1;
				break;
			
			case 'M':			//maximum timestamp
				MAX_TIME_STAMP = strtoull(optarg,NULL,10);
				max_ts_flag = 1;
				break;
			
			case 's':			//min number of servers
				min_servers = atoi(optarg);
				token_server--;
				break;
			
			case 'S':			//max number of servers
				max_servers = atoi(optarg);
				token_server--;
				break;

			case 'c':			//min number of servers
				min_clients = atoi(optarg);
				token_client--;
				break;

			case 'C':			//max number of servers
				max_clients = atoi(optarg);
				token_client--;
				break;

				
			case 'e':			//number of transactions for each client
				num_of_transactions = atoi(optarg);
				trans_flag++;
				break;
			case 'd':
				num_of_data_items = atoi(optarg);
				data_items_flag++;
				break;
			case 'h':
				token_help = 1;
				break;
		}
		
		global_list.head=NULL;
		client_list.head=NULL;
		server_list.head=NULL;
		network_list.head=NULL;
		
	}
	if(token_help==1){
		printf("Hai chiesto aiuto\n");
		free_filenames();
		exit(EXIT_SUCCESS);
	}
	if(token_server!=0 && token_server!=2){
		printf("Error - on server argouments\n");
		free_filenames();
		return -1;
	}
	if(token_server==2){
		min_servers = DEFAULT_NUM_SERVERS;
		max_servers = DEFAULT_NUM_SERVERS;
	}
	if(token_client!=0 && token_client!=2){
		printf("Error - on client argouments\n");
		free_filenames();
		return -1;
	}
	if(token_client==2){
		min_clients = DEFAULT_NUM_CLIENTS;
		max_clients = DEFAULT_NUM_CLIENTS;
	}
		
	if(MIN_TIME_STAMP>MAX_TIME_STAMP){			//check if timestamps are correctly specified
		printf("Error - minimumTS greater or equals to maximumTS\n");
		free_filenames();
		return -1;
	}
	if(min_servers<=0 || max_servers<=0){
		printf("Error - number of servers cannot be less or equal than zero\n");
		free_filenames();
		return -1;
	}
	if(min_servers > max_servers){
		printf("Error - minimum number of servers must be less or equal than maximum number of servers\n");
		free_filenames();
		return -1;
	}
	if(min_clients<=0 || max_clients<=0){
		printf("Error - number of clients cannot be less or equal than zero\n");
		free_filenames();
		return -1;
	}
	if(min_clients > max_clients){
		printf("Error - minimum number of clients must be less or equal than maximum number of clients\n");
		free_filenames();
		return -1;
	}
	return 1;
}

int main(int argc, char** argv){
	
	if(initialize(argc, argv)!=1){//get parameters from command line
		printf("INIT_ERROR\n");
		exit(EXIT_FAILURE);
	}

	create_configuration_file();

	num_server = get_node_list(&global_list,"num_servers"); //these list nodes will be modify more times inside loops.
	num_clients = get_node_list(&global_list,"num_clients");
	object_replication_degree = get_node_list(&global_list,"object_replication_degree");
	num_thread = get_node_list(&client_list,"number_of_threads");
	num_tx = get_node_list(&client_list,"number_of_transactions");
	data_items = get_node_list(&global_list,"cache_objects");
	
	if(trans_flag==0){
		num_of_transactions = DEFAULT_NUM_TX;
	}
	if(data_items_flag==0){
		num_of_data_items = DEFAULT_DATA_ITEMS;
	}
	set_node_value(num_tx, num_of_transactions);
	set_node_value(num_thread, 1);
	set_node_value(data_items, num_of_data_items);
	
	char *command = (char*) malloc(128);
	getcwd(command,128);
	strcat(command,"/DAGS");
	strcat(command," >> output.txt");
	
	remove("output.txt");

	run_simulations(command,min_servers,max_servers, min_clients,max_clients);

	//parse_output();
			
			// FREE OF RESOURCES //
	cleanup();
	clean_list(&global_list);
	clean_list(&client_list);
	clean_list(&server_list);
	clean_list(&network_list);
	
	free_filenames();
	
	free(command);
	return 1;
}

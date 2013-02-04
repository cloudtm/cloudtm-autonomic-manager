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

/* HASH TABLE */

typedef struct _val{
	double value;
	int count;
}val;

typedef struct _node{
  char *name;
  val v;
  struct _node *next;
}node;

/* LIST */

typedef struct _node_list{
	char name[64];
	char value[64];
	struct _node_list* next;
} node_list;

typedef struct _contest_list{
	char *contest;
	struct _node_list *head;
}contest_list;

#define HASHSIZE 200
static node* hashtab[HASHSIZE];


/* ------------------ FUNCTIONS ----------------------------*/

char* map(char *name, char *value);

static void insert_into_list(contest_list *list, char* entry_name, char* entry_value){
	node_list *nl = (node_list *) malloc(sizeof(node_list));
	strcpy(nl->name, entry_name);
	strcpy(nl->value, entry_value);
	nl->next = list->head;
	list->head = nl;
}

//used to remove a node from a list (it's unused until now).
static void remove_from_list(struct _contest_list *list, char* entry_name){
	node_list *tmp, *prev;
	
	if(strcmp(list->head->name,entry_name)==0){
		tmp = list->head;
		list->head = tmp->next;
		free(tmp->name);
		free(tmp->value);
		free(tmp);
	}else{
		prev = list->head;
		tmp = prev->next;
		while(tmp!=NULL){
			if(strcmp(tmp->name,entry_name)==0){
				prev->next = tmp->next;
				free(tmp->name);
				free(tmp->value);
				free(tmp);
			}
			prev = tmp;
			tmp = prev->next;
		}
	}
}

//it returns a list node
static node_list* get_node_list(contest_list *list, char *name){
	node_list *ret = list->head;
	while(ret!=NULL){
		if(strcmp(ret->name,name)==0) return ret;
		ret = ret->next;
	}
	return NULL;
}

static void set_list_contest(struct _contest_list *list, char* contest){ //set the ID of the list.
	list->contest = strdup(contest);
}

//used to initialize the hashtable.
void inithashtab(){
  int i;
  for(i=0;i<HASHSIZE;i++)
    hashtab[i]=NULL;
}

unsigned int hash(char *s){
  unsigned int h=0;
  for(;*s;s++)
    h=*s+h*31;
  return h%HASHSIZE;
}

//lookup function for the hashtable
node* lookup(char *n){
  unsigned int hi=hash(n);
  node* np=hashtab[hi];
  for(;np!=NULL;np=np->next){
    if(!strcmp(np->name,n))
      return np;
  }
  return NULL;
}

char* m_strdup(char *o){
  int l=strlen(o)+1;
  char *ns=(char*)malloc(l*sizeof(char));
  strcpy(ns,o);
  if(ns==NULL)
    return NULL;
  else
    return ns;
}

//it returns the (average) value that is present into the hashtable.
double get_node_hash(char* name){
  node* n=lookup(name);
  if(n==NULL)
    return -1;
  else
    return (n->v.value/n->v.count);
}

//this function installs a tuple into the hashtable.
int install(char* name, char* desc){
  unsigned int hi;
  node* np;
  if((np=lookup(name))==NULL){	//first insertion
    hi=hash(name);
    np=(node*)malloc(sizeof(node));
    if(np==NULL)
      return 0;
    np->name=m_strdup(name);
    if(np->name==NULL) return 0;
    np->next=hashtab[hi];
    hashtab[hi]=np;
    np->v.value = atof(desc);
	np->v.count = 1;
  }
  else{		//update
		np->v.value = np->v.value + atof(desc);
		np->v.count = np->v.count + 1;
  }
    
  if(np->name==NULL) return 0;

  return 1;
}

//it's used for debug
void displaytable(){
  int i;
  node *t;
  for(i=0;i<HASHSIZE;i++){
    if(hashtab[i]==NULL)
      printf("()");
    else{
      t=hashtab[i];
      printf("(");
      for(;t!=NULL;t=t->next)
	printf("(%s.%f, %d) ",t->name,t->v.value,t->v.count);
      printf(")");
    }
  }
}

//it frees memory of entire hashtable
void cleanup(){
  int i;
  node *np;
  for(i=0;i<HASHSIZE;i++){
    if(hashtab[i]!=NULL){
      np=hashtab[i];
      while(np!=NULL){
		hashtab[i] = np->next;
		free(np->name);
		free(np);
		np=hashtab[i];
      }
    }
  }
}

// This function fill the specific list (global or client or server...) with its own parameters.
void print_on_list(FILE *file_conf, char* contest, contest_list *list){
	char buffer[128];
	char *str, *str_dup, *tok_name, *tok_value, *ht_value;
	
	while(fgets(buffer,sizeof(buffer),file_conf)!=NULL){
		str = strndup(buffer,sizeof(buffer));
		if(str[0]=='#' || strcmp(str,"\n")==0);
		else if(str[0]=='['){
			str++;
			str[strlen(str)-2]='\0';
			if(strcmp(str,contest)==0){
				set_list_contest(list,str);
				
				while(fgets(buffer,sizeof(buffer),file_conf)!=NULL){
				
					//str_dup = crea_doppione(buffer);
					str_dup = strdup(buffer);
					if(str_dup[0]!='#' && (strcmp(str,"\n")!=0)){
						if(str_dup[0]=='[') {
							free(str_dup);
							break;
						}
						
						tok_name = strtok(str_dup," = ");
						tok_value = strtok(NULL," = ");
						if(tok_value!=NULL){
							//ht_value = map(tok_name,tok_value);
							if(ht_value!=NULL) {
								insert_into_list(list,tok_name,ht_value);
								free(ht_value);
							}
							else insert_into_list(list,tok_name,tok_value);
							
						}						
					}
					free(str_dup);
					//memset(&buffer,0,sizeof(buffer));
					bzero(&buffer,sizeof buffer);
				}
			}
			str--;			
		}free(str);
		//memset(buffer,0,sizeof(buffer));
		bzero(&buffer,sizeof buffer);
	}
}

//This function is used to map simulator parameters key name with infinispan ones.
char* map(char *name, char *value){
	int map_flag = 0;
	char *map_key;
	
	if(strcmp(name,"tx_abort_cpu_service_demand")==0){				//tx_abort_cpu_service_demand
		map_flag++;
		map_key = strdup("JMX_AvgRollbackRtt");
	}
	else if(strcmp(name,"tx_prepare_cpu_service_demand")==0){		//tx_prepare_cpu_service_demand
		map_flag++;
		map_key = strdup("JMX_AvgLocalPrepareTime");
	}
	else if(strcmp(name,"distributed_final_tx_commit_cpu_service_demand")==0){	//distributed_final_tx_commit_cpu_service_demand
		map_flag++;
		map_key = strdup("JMX_AvgLocalCommitTime");
	}
	else if(strcmp(name,"local_tx_final_commit_cpu_service_demand")==0){	//local_tx_final_commit_cpu_service_demand
		map_flag++;
		map_key = strdup("JMX_AvgRemoteCommitTime");
	}
	else if(strcmp(name,"tx_prepare_failed_cpu_service_demand")==0){	//tx_prepare_failed_cpu_service_demand
		map_flag++;
		map_key = strdup("JMX_AvgLocalRollbackTime");
	}
	else if(strcmp(name,"local_tx_get_cpu_service_demand")==0){	//local_tx_get_cpu_service_demand
		map_flag++;
		map_key = strdup("JMX_AverageReadTime");
	}
	else if(strcmp(name,"local_tx_put_cpu_service_demand")==0){	//local_tx_put_cpu_service_demand
		map_flag++;
		map_key = strdup("JMX_AverageWriteTime");
	}
	else if(strcmp(name,"local_tx_get_from_remote_cpu_service_demand")==0){	//local_tx_get_from_remote_cpu_service_demand
		map_flag++;
		map_key = strdup("JMX_AvgRemoteGetRtt");
	}
	else if(strcmp(name,"tx_send_remote_tx_get_cpu_service_demand")==0){	//tx_send_remote_tx_get_cpu_service_demand
		map_flag++;
		map_key = strdup("JMX_AvgRemoteGetRtt");
	}
	else if(strcmp(name,"local_prepare_successed_cpu_service_demand")==0){	//local_prepare_successed_cpu_service_demand
		map_flag++;
		map_key = strdup("JMX_AvgRemotePrepareTime");
	}
	else if(strcmp(name,"local_prepare_failed_cpu_service_demand")==0){	//local_prepare_failed_cpu_service_demand
		map_flag++;
		map_key = strdup("JMX_AvgRemotePrepareTime");
	}
	else if(strcmp(name,"update_cpu_service_demand")==0){	//update_cpu_service_demand
		map_flag++;
		map_key = strdup("JMX_AverageWriteTime");
	}
	
	
	if(map_flag==1){
		double avg_val = get_node_hash(map_key);
		char *value_ret = malloc(sizeof(char) * sizeof(double) + 100);
		sprintf(value_ret,"%f\n",avg_val);
		free(map_key);
		return value_ret;
	}
	return NULL;
	
}

//used to free a specific list.
static void clean_list(contest_list *list){
	node_list *nodo;	
	nodo = list->head;
	
	while(nodo!=NULL){
		list->head = nodo->next;
		free(nodo);
		nodo = list->head;
	}
	free(list->contest);
}

//it's used inside run_simulator's loops to change specific parameters' value.
static void set_node_value(node_list *nl, int value){
	char new_val[10];
	sprintf(new_val,"%d\n",value);
	strcpy(nl->value,new_val);
}

//this function prints a list on the file_output (simulation.conf)
static void list_on_file(contest_list *list, FILE *f_out){
	node_list * tmp;
	
	fprintf(f_out,"\n[%s]\n\n",list->contest);
	tmp = list->head;
	while(tmp!=NULL){
		fprintf(f_out,"%s = %s",tmp->name,tmp->value);
		tmp = tmp->next;
	}
}


#include "defns.h"
#include "global.h"
#include "hooks.h"

#if ! defined WIN32 && ! defined _CONSOLE
#include <sys/unistd.h>
#include <sys/time.h>
#include <sys/resource.h>
#ifndef _SC_NPROCESSORS_CONF
#define _SC_NPROCESSORS_CONF _SC_NPROC_CONF
#endif
#endif


void initiateCubist(){
  FILE *F;
  FileStem = "tasRtt";

  if ( ! (F = GetFile(".names", "r")) )
    Error(0, Fn, "");

  GetNames(F);   

  CubistModel = GetCommittee(".model");
}



double XactAverageExecutionTimes_getPrediction(char * attr){
  FILE *F;
  int IValsBase = 0;
  String *Label=Nil;
  DataRec ProcessingCase=Nil;
  float ProcessingErr=0.0;
  String ProcessingLabel=Nil;

  attributeString = attr;
  lineRead = 0;

  ProcessingCase = MyGetDataRec(false);
  
  ReplaceUnknowns(ProcessingCase, Nil);
  
  if ( LabelAtt ){
    if ( strlen(IgnoredVals + IValsBase) <= 14 ){
      strcpy(ProcessingLabel, IgnoredVals + IValsBase);
    } else {
      memcpy(ProcessingLabel, IgnoredVals + IValsBase, 14);
    }
    IValsOffset = IValsBase;
  }

  PredVal(ProcessingCase) =PredictValue(CubistModel, ProcessingCase, &ProcessingErr);

  return PredVal(ProcessingCase);
}

/*
void main () {
	initiateCubist();
	printf("\nInitialized");
	double pred =XactAverageExecutionTimes_getPrediction("0,31937,0,945020.9,1.6444097E7,27.538155,5,7,8,2");
	printf("\npred: %f",pred);
}
*/



#include "oracle_cubist_jni_JniCubistOracle.h"

#include "defns.h"
#include "global.c"
#include "hooks.c"

#if ! defined WIN32 && ! defined _CONSOLE
#include <sys/unistd.h>
#include <sys/time.h>
#include <sys/resource.h>
#ifndef _SC_NPROCESSORS_CONF
#define _SC_NPROCESSORS_CONF _SC_NPROC_CONF
#endif
#endif


JNIEXPORT void JNICALL Java_oracle_cubist_jni_JniCubistOracle_initiateCubist(JNIEnv *env, jobject thisobject, jstring js){
  FILE *F;
  FileStem = (*env)->GetStringUTFChars(env, js, 0); 

  if ( ! (F = GetFile(".names", "r")) )
    Error(0, Fn, "");

  GetNames(F);   

  CubistModel = GetCommittee(".model");
}


JNIEXPORT jdouble JNICALL Java_oracle_cubist_jni_JniCubistOracle_getPrediction(JNIEnv *env, jobject thisobject, jstring js){
  FILE *F;
  int IValsBase = 0;
  String *Label=Nil;
  DataRec ProcessingCase=Nil;
  float ProcessingErr=0.0;
  String ProcessingLabel=Nil;

  attributeString = (*env)->GetStringUTFChars(env, js, 0);
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


JNIEXPORT jdoubleArray JNICALL Java_oracle_cubist_jni_JniCubistOracle_getPredictionAndError(JNIEnv *env, jobject thisobject, jstring js){
  int IValsBase = 0;
  String *Label=Nil;
  DataRec ProcessingCase=Nil;
  float ProcessingErr=0.0;
  String ProcessingLabel=Nil;

  attributeString = (*env)->GetStringUTFChars(env, js, 0);
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

  jdouble resultTemp[2];
  resultTemp[0] = PredVal(ProcessingCase);
  resultTemp[1] = (double) ProcessingErr;

  jdoubleArray result;
  result = (*env)->NewDoubleArray(env, 2);
  (*env)->SetDoubleArrayRegion(env, result, 0, 2, resultTemp);
  
  return result;
}


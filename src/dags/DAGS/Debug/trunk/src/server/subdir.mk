################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../trunk/src/server/concurrency-control.c \
../trunk/src/server/cpu.c \
../trunk/src/server/data_distribution_manager.c \
../trunk/src/server/hashing.c \
../trunk/src/server/server.c 

OBJS += \
./trunk/src/server/concurrency-control.o \
./trunk/src/server/cpu.o \
./trunk/src/server/data_distribution_manager.o \
./trunk/src/server/hashing.o \
./trunk/src/server/server.o 

C_DEPS += \
./trunk/src/server/concurrency-control.d \
./trunk/src/server/cpu.d \
./trunk/src/server/data_distribution_manager.d \
./trunk/src/server/hashing.d \
./trunk/src/server/server.d 


# Each subdirectory must supply rules for building sources it contributes
trunk/src/server/%.o: ../trunk/src/server/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: Cross GCC Compiler'
	gcc -O0 -g3 -pg -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '



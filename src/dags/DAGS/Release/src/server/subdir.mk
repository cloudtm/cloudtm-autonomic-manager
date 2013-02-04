################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../src/server/concurrency-control.c \
../src/server/cpu.c \
../src/server/hashing.c \
../src/server/server.c 

OBJS += \
./src/server/concurrency-control.o \
./src/server/cpu.o \
./src/server/hashing.o \
./src/server/server.o 

C_DEPS += \
./src/server/concurrency-control.d \
./src/server/cpu.d \
./src/server/hashing.d \
./src/server/server.d 


# Each subdirectory must supply rules for building sources it contributes
src/server/%.o: ../src/server/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '



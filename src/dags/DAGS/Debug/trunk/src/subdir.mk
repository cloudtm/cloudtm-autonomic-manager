################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../trunk/src/application.c \
../trunk/src/conf-parser.c \
../trunk/src/serial_simulator.c 

OBJS += \
./trunk/src/application.o \
./trunk/src/conf-parser.o \
./trunk/src/serial_simulator.o 

C_DEPS += \
./trunk/src/application.d \
./trunk/src/conf-parser.d \
./trunk/src/serial_simulator.d 


# Each subdirectory must supply rules for building sources it contributes
trunk/src/%.o: ../trunk/src/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: Cross GCC Compiler'
	gcc -O0 -g3 -pg -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '



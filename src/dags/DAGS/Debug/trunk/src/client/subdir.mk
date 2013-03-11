################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../trunk/src/client/client.c \
../trunk/src/client/transaction_list_manager.c 

OBJS += \
./trunk/src/client/client.o \
./trunk/src/client/transaction_list_manager.o 

C_DEPS += \
./trunk/src/client/client.d \
./trunk/src/client/transaction_list_manager.d 


# Each subdirectory must supply rules for building sources it contributes
trunk/src/client/%.o: ../trunk/src/client/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: Cross GCC Compiler'
	gcc -O0 -g3 -pg -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '



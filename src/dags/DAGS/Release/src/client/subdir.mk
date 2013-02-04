################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../src/client/client.c \
../src/client/transaction_list_manager.c 

OBJS += \
./src/client/client.o \
./src/client/transaction_list_manager.o 

C_DEPS += \
./src/client/client.d \
./src/client/transaction_list_manager.d 


# Each subdirectory must supply rules for building sources it contributes
src/client/%.o: ../src/client/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '



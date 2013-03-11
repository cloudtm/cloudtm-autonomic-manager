################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../trunk/src/network/net.c 

OBJS += \
./trunk/src/network/net.o 

C_DEPS += \
./trunk/src/network/net.d 


# Each subdirectory must supply rules for building sources it contributes
trunk/src/network/%.o: ../trunk/src/network/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: Cross GCC Compiler'
	gcc -O0 -g3 -pg -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '



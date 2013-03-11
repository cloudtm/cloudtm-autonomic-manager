################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../trunk/src/lib/events_wait_queue.c \
../trunk/src/lib/numerical.c 

OBJS += \
./trunk/src/lib/events_wait_queue.o \
./trunk/src/lib/numerical.o 

C_DEPS += \
./trunk/src/lib/events_wait_queue.d \
./trunk/src/lib/numerical.d 


# Each subdirectory must supply rules for building sources it contributes
trunk/src/lib/%.o: ../trunk/src/lib/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: Cross GCC Compiler'
	gcc -O0 -g3 -pg -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '



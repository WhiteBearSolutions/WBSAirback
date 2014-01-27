################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
O_SRCS += \
../atmos/atmos_rest.o \
../atmos/atmos_util.o \
../atmos/crypto.o \
../atmos/transport.o 

C_SRCS += \
../atmos/atmos_rest.c \
../atmos/atmos_util.c \
../atmos/crypto.c \
../atmos/lister.c \
../atmos/terawriter.c \
../atmos/test.c \
../atmos/test_object_create.c \
../atmos/transport.c 

OBJS += \
./atmos/atmos_rest.o \
./atmos/atmos_util.o \
./atmos/crypto.o \
./atmos/lister.o \
./atmos/terawriter.o \
./atmos/test.o \
./atmos/test_object_create.o \
./atmos/transport.o 

C_DEPS += \
./atmos/atmos_rest.d \
./atmos/atmos_util.d \
./atmos/crypto.d \
./atmos/lister.d \
./atmos/terawriter.d \
./atmos/test.d \
./atmos/test_object_create.d \
./atmos/transport.d 


# Each subdirectory must supply rules for building sources it contributes
atmos/%.o: ../atmos/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '



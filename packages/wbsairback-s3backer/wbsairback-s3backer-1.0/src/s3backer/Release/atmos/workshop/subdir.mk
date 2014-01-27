################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../atmos/workshop/acl_data01.c \
../atmos/workshop/create01.c \
../atmos/workshop/delete01.c \
../atmos/workshop/getmeta_data01.c \
../atmos/workshop/meta_data01.c \
../atmos/workshop/meta_helpers_test.c \
../atmos/workshop/read01.c \
../atmos/workshop/sys_meta_data01.c \
../atmos/workshop/update01.c \
../atmos/workshop/updatemeta_data01.c 

OBJS += \
./atmos/workshop/acl_data01.o \
./atmos/workshop/create01.o \
./atmos/workshop/delete01.o \
./atmos/workshop/getmeta_data01.o \
./atmos/workshop/meta_data01.o \
./atmos/workshop/meta_helpers_test.o \
./atmos/workshop/read01.o \
./atmos/workshop/sys_meta_data01.o \
./atmos/workshop/update01.o \
./atmos/workshop/updatemeta_data01.o 

C_DEPS += \
./atmos/workshop/acl_data01.d \
./atmos/workshop/create01.d \
./atmos/workshop/delete01.d \
./atmos/workshop/getmeta_data01.d \
./atmos/workshop/meta_data01.d \
./atmos/workshop/meta_helpers_test.d \
./atmos/workshop/read01.d \
./atmos/workshop/sys_meta_data01.d \
./atmos/workshop/update01.d \
./atmos/workshop/updatemeta_data01.d 


# Each subdirectory must supply rules for building sources it contributes
atmos/workshop/%.o: ../atmos/workshop/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '



################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
O_SRCS += \
../block_cache.o \
../block_part.o \
../dcache.o \
../ec_protect.o \
../fuse_ops.o \
../hash.o \
../http_io.o \
../main.o \
../s3b_config.o \
../svnrev.o \
../test_io.o \
../tester.o 

C_SRCS += \
../block_cache.c \
../block_part.c \
../dcache.c \
../ec_protect.c \
../erase.c \
../fuse_ops.c \
../hash.c \
../http_atmos_io.c \
../http_io.c \
../main.c \
../s3b_config.c \
../svnrev.c \
../test_io.c \
../tester.c 

OBJS += \
./block_cache.o \
./block_part.o \
./dcache.o \
./ec_protect.o \
./erase.o \
./fuse_ops.o \
./hash.o \
./http_atmos_io.o \
./http_io.o \
./main.o \
./s3b_config.o \
./svnrev.o \
./test_io.o \
./tester.o 

C_DEPS += \
./block_cache.d \
./block_part.d \
./dcache.d \
./ec_protect.d \
./erase.d \
./fuse_ops.d \
./hash.d \
./http_atmos_io.d \
./http_io.d \
./main.d \
./s3b_config.d \
./svnrev.d \
./test_io.d \
./tester.d 


# Each subdirectory must supply rules for building sources it contributes
%.o: ../%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '



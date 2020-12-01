cmake_minimum_required(VERSION 3.4.1)

# 配置编译选项, 编译类型 动态库, C++ 源码为 native-lib.c
add_library(
        openssl
        SHARED
        native-lib.c)

find_library(
        log-lib
        log)

# 设置 openssl 函数库的静态库地址 方式一 报错
set(LIB_DIR ${CMAKE_SOURCE_DIR}/lib/${ANDROID_ABI})
add_library(crypto STATIC IMPORTED)

# 预编译 openssl 静态库
set_target_properties(
        crypto
        PROPERTIES
        IMPORTED_LOCATION
        ${LIB_DIR}/libcrypto.a)
# 指定头文件
include_directories(${CMAKE_SOURCE_DIR}/include)
# 方式一配置完毕


# 设置 openssl 函数库的静态库地址 方式二

# 指定 openssl 头文件查找目录
#           CMAKE_SOURCE_DIR 指的是当前的文件地址
#include_directories(${CMAKE_SOURCE_DIR}/include)

# 指定 openssl 静态库
# CMAKE_CXX_FLAGS 表示会将 C++ 的参数传给编译器
# CMAKE_C_FLAGS 表示会将 C 参数传给编译器

# 参数设置 : 传递 CMAKE_CXX_FLAGS C+= 参数给编译器时 , 在 该参数后面指定库的路径
#   CMAKE_SOURCE_DIR 指的是当前的文件地址
#   -L 参数指定动态库的查找路径
#set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -L${CMAKE_SOURCE_DIR}/lib/armeabi-v7a")

#message("CMake octopus ${CMAKE_SOURCE_DIR} , ${ANDROID_ABI}, {CMAKE_SOURCE_DIR}/lib/${ANDROID_ABI}")

# 链接动态库
target_link_libraries(
        openssl
        crypto
        android
        ${log-lib})
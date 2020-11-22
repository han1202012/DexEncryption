cmake_minimum_required(VERSION 3.4.1)

# 配置编译选项, 编译类型 动态库, C++ 源码为 native-lib.c
add_library(
    openssl
    SHARED
    native-lib.c )

# 设置 openssl 函数库的静态库地址
set(LIB_DIR ${CMAKE_SOURCE_DIR}/lib/${ANDROID_ABI})
add_library(crypto STATIC IMPORTED)

# 预编译 openssl 静态库
set_target_properties(crypto PROPERTIES IMPORTED_LOCATION ${LIB_DIR}/libcrypto.a)

# 指定头文件路径
include_directories(include)

# 链接动态库
target_link_libraries(
    openssl
    crypto
    log )
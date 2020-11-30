# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

add_library(
             dn_ssl
             SHARED
             src/main/cpp/native-lib.c )


set(LIB_DIR ${CMAKE_SOURCE_DIR}/src/main/cpp/lib/${ANDROID_ABI})
add_library(crypto STATIC IMPORTED)
set_target_properties(crypto PROPERTIES IMPORTED_LOCATION ${LIB_DIR}/libcrypto.a)

include_directories(src/main/cpp/include)

target_link_libraries(
                       dn_ssl
                        crypto

                      log )
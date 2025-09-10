cmake_minimum_required(VERSION 3.18.1)

project("native-lib")

add_library(
        native-lib
        SHARED
        native-lib.cpp
)

# Set linker option to max page size = 16KB
target_link_options(native-lib PRIVATE "-Wl,-z,max-page-size=16384")

find_library(
        log-lib
        log
)

target_link_libraries(
        native-lib
        ${log-lib}
)

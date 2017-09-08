PROJECT(muscleruby)

FILE(GLOB THIS_RUBY_FILES *.rb)

set(RUBY_DIR share/muscle/ruby)
install(FILES ${THIS_RUBY_FILES} DESTINATION ${RUBY_DIR})

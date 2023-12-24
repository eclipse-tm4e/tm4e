# Variable assignment
CC = gcc
CFLAGS = -Wall -O2

# Rule for building the main executable
my_program: main.o helper.o
   $(CC) $(CFLAGS) -o $@ $^

# Rule for compiling main source file
main.o: main.c
   $(CC) $(CFLAGS) -c $<

# Rule for compiling helper source file
helper.o: helper.c
   $(CC) $(CFLAGS) -c $<

# Phony target for cleaning up
clean:
   rm -f my_program *.o

#!/bin/bash
# Project version 1.1

# Compile all Java files
javac -cp test-lib/junit-platform-console-standalone-1.11.0.jar -d out $(find src test -name "*.java")

# Check if compilation was successful
if [ $? -ne 0 ]; then
  echo "Compilation failed."
  exit 1
fi

# Run all tests in the compiled classpath
RESULT=$(java -jar test-lib/junit-platform-console-standalone-1.11.0.jar -cp out --scan-class-path)

# Parse the test result to count total and successful tests
TOTAL_TESTS=$(echo "$RESULT" | grep -o '[0-9]\+ tests found' | grep -o '[0-9]\+')
PASSED_TESTS=$(echo "$RESULT" | grep -o '[0-9]\+ tests successful' | grep -o '[0-9]\+')

# Check if no tests were found or successful tests weren't found, default to 0
if [ -z "$TOTAL_TESTS" ]; then
  TOTAL_TESTS=0
fi

if [ -z "$PASSED_TESTS" ]; then
  PASSED_TESTS=0
fi

# Calculate percentage
if [ "$TOTAL_TESTS" -eq "0" ]; then
  PERCENTAGE=0
else
  PERCENTAGE=$((100 * PASSED_TESTS / TOTAL_TESTS))
fi

# Output the percentage score
echo "Percentage of tests passed: $PERCENTAGE%"

# Exit with success
exit 0

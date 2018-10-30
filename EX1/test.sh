#!/bin/bash 
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'
for file in $(ls FOLDER_4_INPUT); do
	if [ "$file" != "Input.txt" ]; then
		echo "Running test: $file"
		java -jar LEXER FOLDER_4_INPUT/"$file" FOLDER_5_OUTPUT/"$file" &> /dev/null
		if diff FOLDER_5_OUTPUT/"$file" FOLDER_6_EXPECTED_OUTPUT/"$file"; then
			echo -e "Result: ${GREEN}PASS${NC}"
		else
			echo -e "Result: ${RED}FAIL${NC}"
		fi
	fi
done

#!/bin/bash
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'
INPUT_FOLDER="FOLDER_4_INPUT"
OUTPUT_FOLDER="FOLDER_5_OUTPUT"
EXPECTED_OUTPUT_FOLDER="FOLDER_6_EXPECTED_OUTPUT"

[[ $* == *--all* ]] && HIDE=false || HIDE=true

mkdir -p ${EXPECTED_OUTPUT_FOLDER}
if [ "$AST" = true ] ; then
    mkdir -p ${OUTPUT_FOLDER}/AST/
fi

for file in $(ls "$INPUT_FOLDER/"); do
    if [ "$file" != "Input.txt" ] ; then
        if [ "$HIDE" = false ] ; then
            echo "Running test: $file"
        fi
        java -jar COMPILER "$INPUT_FOLDER/$file" "$OUTPUT_FOLDER/$file" &> /dev/null
        spim -f "$OUTPUT_FOLDER/$file" > "$OUTPUT_FOLDER/${file}_output.txt"
        if diff --strip-trailing-cr --ignore-all-space "$OUTPUT_FOLDER/${file}_output.txt" "$EXPECTED_OUTPUT_FOLDER/$file"; then
            if [ "$HIDE" = false ] ; then
                echo -e "Result: ${GREEN}PASS${NC}"
            fi
        else
            echo "Running test: $file"
            echo -e "Result: ${RED}FAIL${NC}"
            echo "Expected:"
            echo "$(cat ${EXPECTED_OUTPUT_FOLDER}/${file})"
            echo "Found: "
            echo "$(cat ${OUTPUT_FOLDER}/${file}_output.txt)"
            echo ""
            exit
        fi
    fi
done

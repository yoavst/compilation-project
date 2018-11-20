#!/bin/bash
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'
INPUT_FOLDER="FOLDER_4_INPUT"
OUTPUT_FOLDER="FOLDER_5_OUTPUT"
EXPECTED_OUTPUT_FOLDER="FOLDER_6_EXPECTED_OUTPUT"

[[ $* == *--all* ]] && HIDE=false || HIDE=true
[[ $* == *--ast* ]] && AST=true || AST=false

mkdir -p ${EXPECTED_OUTPUT_FOLDER}
if [ "$AST" = true ] ; then
    mkdir -p ${OUTPUT_FOLDER}/AST/
fi

for file in $(ls "$INPUT_FOLDER/"); do
    if [ "$file" != "Input.txt" ] ; then
        if [ "$HIDE" = false ] ; then
            echo "Running test: $file"
        fi
        java -jar PARSER "$INPUT_FOLDER/$file" "$OUTPUT_FOLDER/$file" &> /dev/null
        if cmp -s "$OUTPUT_FOLDER/$file" "$EXPECTED_OUTPUT_FOLDER/$file"; then
            if [ "$HIDE" = false ] ; then
                echo -e "Result: ${GREEN}PASS${NC}"
            fi
        else
            echo "Running test: $file"
            echo -e "Result: ${RED}FAIL${NC}"
            echo "Expected: $(cat ${EXPECTED_OUTPUT_FOLDER}/${file}), Found: $(cat ${OUTPUT_FOLDER}/${file})"
            echo "Input: $(cat ${INPUT_FOLDER}/${file})"
            if [ "$AST" = true ] ; then
                if [[ "$(cat ${OUTPUT_FOLDER}/${file})" == "OK" ]] ; then
                    dot -Tjpeg -o${OUTPUT_FOLDER}/AST/${file%%.*}.jpeg ${OUTPUT_FOLDER}/AST_IN_GRAPHVIZ_DOT_FORMAT.txt
                fi
            fi
            echo ""
        fi
    fi
done

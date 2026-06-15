#!/bin/zsh

export JAVA_HOME="/opt/homebrew/Cellar/openjdk@25/25.0.3/libexec/openjdk.jdk/Contents/Home"

BASE_DIR=${1:-"datasets/student-submissions/assg4/"}
ORACLE_DIR=${2:-"oracles/student-submissions/assg4/"}

if [ ! -d "$BASE_DIR" ]; then
  echo "Error: Directory $BASE_DIR does not exist."
  exit 1
fi

for GROUP_DIR in "$BASE_DIR"/*/; do
  # Remove trailing slash
  GROUP_DIR=${GROUP_DIR%/}
  FOLDER_NAME=$(basename "$GROUP_DIR")
  ORACLE_FILE="${ORACLE_DIR}/${FOLDER_NAME}-oracle.csv"
  
  echo "--------------------------------------------------"
  echo " Running Oracle Evaluation for:"
  echo " Target Dir:  ${GROUP_DIR}"
  echo " Oracle File: ${ORACLE_FILE}"
  echo "--------------------------------------------------"
  
  if [ ! -f "$ORACLE_FILE" ]; then
    echo "Warning: Oracle file $ORACLE_FILE not found. Skipping..."
    continue
  fi

  mvn exec:java -Dexec.mainClass="edu.northeastern.Main" \
                -Dexec.args="-f ${GROUP_DIR} -t ${ORACLE_FILE} -a -e dead-code,dup-code,spec-gen -r html" -q
done
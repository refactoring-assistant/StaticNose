#!/bin/zsh

export JAVA_HOME="/opt/homebrew/Cellar/openjdk@25/25.0.3/libexec/openjdk.jdk/Contents/Home"

BASE_DIR=${1:-"datasets/student-submissions/assg6"}

if [ ! -d "$BASE_DIR" ]; then
  echo "Error: Directory $BASE_DIR does not exist."
  exit 1
fi

for GROUP_DIR in "$BASE_DIR"/*/; do
  # Remove trailing slash for cleaner output
  GROUP_DIR=${GROUP_DIR%/}

  echo "--------------------------------------------------"
  echo " Running Maven project to generate oracle for: "
  echo " ${GROUP_DIR}"
  echo "--------------------------------------------------"

  mvn exec:java -Dexec.mainClass="edu.northeastern.Main" -Dexec.args="-f ${GROUP_DIR} -g" -q
done
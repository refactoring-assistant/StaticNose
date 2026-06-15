# StaticNose

StaticNose is a program that uses static analysis to detect code smells. It can currently detect 19 code smells defined by Martin Fowler.

## Installation

### Requirements
- Java 25 is required to build and run the program
- Maven is the build tool
- JAVA_HOME environment variable points to JDK 25

## Usage

Build the source using the command:

`$ mvn clean package`

This should generate *StaticNose-1.0.jar* file in *target/*. Run the jar file using `java -jar path/to/JAR` command.

### Arguments for common usages

1. Scanning a project for the Comments code smell

```terminaloutput
-f "<PATH TO DIRECTORY>" -s comments -r json
```

2. Scanning a project for multiple code smells

```terminaloutput
-f "<PATH TO DIRECTORY>" -s comments,data-class -r json
```

3. Generating an oracle file for a project

```terminaloutput
-f "<PATH TO DIRECTORY>" -g
```

4. Scanning a project for the Comments code smell and providing an oracle to evaluate the detector

```terminaloutput
-f "<PATH TO DIRECTORY>" -s comments -r json -t "<PATH TO ORACLE CSV>"
```

### Available Detectors

The following set of detectors are provided with StaticNose along with their short code for the CLI:

| Code Smell Detector | CLI short code | Code Smell Detected |
| :--- | :--- | :--- |
| AlternativeClassesDetector | alt-classes | Alternative Classes with Different Interfaces |
| CommentsDetector | comments | Comments |
| DataClassDetector | data-class | Data Class |
| DataClumpsDetector | data-clumps | Data Clumps |
| DivergentChangeDetector | divergent-change | Divergent Change |
| FeatureEnvyDetector | feature-envy | Feature Envy |
| InappropriateIntimacyDetector | intimacy | Inappropriate Intimacy |
| LargeClassDetector | large-class | Large Class |
| LazyClassDetector | lazy-class | Lazy Class |
| LongMethodDetector | long-method | Long Method |
| LongParameterListDetector | long-params | Long Parameter List |
| MessageChainsDetector | message-chains | Message Chains |
| MiddleManDetector | middle-man | Middle Man |
| ParallelInheritanceHierarchyDetector | parallel-hierarchy | Parallel Inheritance Hierarchies |
| PrimitiveObsessionDetector | prim-obsession | Primitive Obsession |
| RefusedBequestDetector | refused-bequest | Refused Bequest |
| ShotgunSurgeryDetector | shotgun | Shotgun Surgery |
| SwitchStatementDetector | switch-stmts | Switch Statements |
| TemporaryFieldDetector | temp-field | Temporary Field |

### Configuring Thresholds

Thresholds for the various code smell detectors can be customized by providing a JSON configuration file using the `-c` or `--config` flag. If a threshold is not provided in the configuration file, the default value is used.

An example of a threshold configuration file is given below and a sample config is provided at `config.json`.

The key of the code smell should exactly match the name of the code smell as seen in the "Code Smell" column in the table below.

```json
{
    "Comments": {
        "WORD_COUNT_TRESHOLD_BELOW_MIN": 3,
        "WORD_COUNT_ABOVE_MAX": 5,
        "COMMENT_TO_LLOC_RATIO": 0.3
    },
}
```

Here are the available thresholds and their default values:

| Code Smell | Threshold Name | Default Value | Definition |
| :--- | :--- |:--------------| :--- |
| Alternative Classes with Different Interfaces | SIMILARITY_THRESHOLD | 0.75          | Total similarity threshold for class volume, fields, and methods |
| Alternative Classes with Different Interfaces | WEIGHT_FIELDS | 0.20          | Weight assigned to the field similarity score |
| Alternative Classes with Different Interfaces | WEIGHT_VOLUME | 0.10          | Weight assigned to the volume similarity score |
| Alternative Classes with Different Interfaces | WEIGHT_METHODS | 0.70          | Weight assigned to the method similarity score |
| Comments | WORD_COUNT_TRESHOLD_BELOW_MIN | 3             | Minimum word count below which a comment is considered a ghost comment |
| Comments | WORD_COUNT_ABOVE_MAX | 5             | Maximum word count above which a comment is considered a novel comment |
| Comments | COMMENT_TO_LLOC_RATIO | 0.3           | Maximum allowed ratio of comment lines to logical lines of code |
| Data Class | ACCESSOR_OR_FIELD_FEW_LEVEL | 3             | Threshold for a small number of public fields or accessors |
| Data Class | ACCESSOR_OR_FIELD_MANY_LEVEL | 5             | Threshold for a large number of public fields or accessors |
| Data Class | WOC_LEVEL | 0.33          | Maximum weight of class (ratio of functional to total public methods) |
| Data Class | WMC_HIGH_LEVEL | 31            | Maximum weighted method count for a class with few fields |
| Data Class | WMC_VERY_HIGH_LEVEL | 47            | Maximum weighted method count for a class with many fields |
| Data Clumps | CLUMP_SIZE_THRESHOLD | 2             | Minimum number of identical parameters or arguments forming a clump |
| Divergent Change | UBIQUITY_THRESHOLD | 0.70          | Threshold above which a field is considered a ubiquitous utility/glue field |
| Duplicate Code | WINDOW_SIZE | 5             | Minimum number of consecutive statements to be considered duplicate code |
| Feature Envy | FOREIGN_DATA_THRESHOLD | 2             | Minimum number of accesses to a single foreign class's data |
| Large Class | WMC_THRESHOLD | 47            | Maximum weighted method count before a class is considered too large |
| Large Class | TCC_THRESHOLD | 0.33          | Minimum tight class cohesion below which the class is considered a large class |
| Lazy Class | LOW_WEIGHT_THRESHOLD | 5             | Maximum weight (fields and methods) below which a class is considered lazy |
| Long Method | MAX_LLOC | 30            | Maximum allowed logical lines of code for a single method |
| Long Method | MAX_COMPLEXITY | 15            | Maximum allowed cyclomatic complexity for a single method |
| Long Parameter List | MAX_METHOD_PARAMS | 3             | Maximum allowed parameters for a method |
| Long Parameter List | MAX_CONSTRUCTOR_PARAMS | 5             | Maximum allowed parameters for a constructor |
| Message Chains | CHAIN_THRESHOLD | 3             | Minimum depth of continuous method calls to be considered a message chain |
| Middle Man | DELEGATION_THRESHOLD | 0.5           | Minimum ratio of delegating methods to total methods in the class |
| Middle Man | FAN_OUT_THRESHOLD | 0             | Minimum number of unique external classes called by the delegating methods |
| Parallel Inheritance Hierarchies | MIN_SUBCLASSES | 2             | Minimum number of subclasses required in each hierarchy |
| Parallel Inheritance Hierarchies | PARALLEL_LINK_THRESHOLD | 2             | Minimum number of parallel links between the two hierarchies |
| Primitive Obsession | VALIDATION_DISTRIBUTION_THRESHOLD | 1             | Minimum number of methods sharing validation logic for a primitive field |
| Primitive Obsession | MAX_PRIMITIVE_FIELDS | 3             | Maximum allowed primitive fields in a class before considered an obsession |
| Primitive Obsession | COHESION_CANDIDATE_FIELDS | 3             | Minimum number of primitive fields required to analyze semantic cohesion |
| Primitive Obsession | MAX_DISJOINT_RATIO | 0.5           | Maximum allowed ratio of method pairs that do not share any primitive fields |
| Shotgun Surgery | THRESHOLD_CM | 2             | Minimum number of changing methods that call the target method |
| Shotgun Surgery | THRESHOLD_CC | 2             | Minimum number of changing classes that call the target method |
| Shotgun Surgery | THRESHOLD_FAN_OUT | 0             | Minimum fan-out (number of unique external methods called) |
| Switch Statements | MAX_SWITCH_CASES | 2             | Maximum allowed cases in a single switch statement |
| Switch Statements | MAX_IF_CHAIN_LENGTH | 2             | Maximum allowed branches in a single if-else chain |

### Report Generation

Three report formats are available, JSON, CSV and HTML. CSV format is primarily used for ease of reading the report directly and to generate and evaluate oracles.

JSON format is provided to be used with custom analysis viewers. HTML format is used to quickly view the outcome of the analysis in an interactive view.

Everytime the program is run, a `staticnose-report` folder is created at the root of the project directory examined. This folder contains all the reports in both formats ever generated for this project.

### Report Viewer

> [!WARNING]
> The report viewer is planned to be deprecated in the next major release. Please use the HTML report generator to generate .html files and view the reports through them.

A report viewer for the JSON format is provided at `./index.html`. To use this viewer, open the HTML file in a browser.

It will prompt you to upload a folder. You will be required to upload the entire project folder which contains the source code analyzed and the `staticnose-report` folder. 

> [!NOTE]
> The viewer runs entirely local and does not require the internet to run, your project files are never sent over the network.

### Configuring Oracles

An oracle is a CSV file that contains two columns, "File Name" and "Expected Code Smells". 
The File Name column must contain the complete file path, which is a current limitation of the project.

Once an oracle is configured for a project by labeling each file with the code smells it contains, the `-t` option can be used when running the program and supplying the oracle to it. 
Per file results and overall results will be provided in the terminal output.

### Script usage

> Please double-check the arguments passed to the program in each script before running.

There are 3 scripts corresponding to the 3 assignments:
- Assignment 4
- Assignment 5
- Assignment 6

#### Scripts in oracle-gen

These script help in generating individual oracles for each group for 1 specific assignment. It creates 1 oracle per folder found in the `BASE_DIR` directory.

#### Scripts in oracle-eval

These scripts help in evaluating the generated oracles. `BASE_DIR` is the directory in which all the group's folders are present. The script takes each folder and finds the corresponding oracle file from the directory given in `ORACLE_DIR`. The oracle file should match the group folder name. For example, if the group folder name is `group1`, the oracle file should be `group1-oracle.csv`.
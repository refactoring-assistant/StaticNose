# StaticNose

StaticNose is a program that uses static analysis to detect code smells. It can currently detect 19 code smells defined by Martin Fowler.

## Installation

Only the source code is currently available for StaticNose. To build, please clone this repository.

### Requirements
- Java 25 is required to build and run the program
- Maven is the build tool

## Usage

> [!WARNING]
> A current limitation of the project is that each Java file in the Java project to be examined must have a unique name across the entire project.

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

If a detector contains a threshold as a part of its algorithm, the threshold is provided as a constant field in the detector class.

### Report Generation

Two report formats are available, JSON and CSV. CSV format is primarily used for ease of reading the report directly and to generate and evaluate oracles.

JSON format is provided to be used with custom analysis viewers. One viewer is provided with the project, located at `./index.html`.

Everytime the program is run, a `staticnose-report` folder is created at the root of the project directory examined. This folder contains all the reports in both formats ever generated for this project.

### Report Viewer

A report viewer for the JSON format is provided at `./index.html`. To use this viewer, open the HTML file in a browser.

It will prompt you to upload a folder. You will be required to upload the entire project folder which contains the source code analyzed and the `staticnose-report` folder. 

> [!NOTE]
> The viewer runs entirely local and does not require the internet to run, your project files are never sent over the network.

### Configuring Oracles

An oracle is a CSV file that contains two columns, "File Name" and "Expected Code Smells". 
The File Name column must contain unique file names, which is a current limitation of the project.

Once an oracle is configured for a project by labeling each file with the code smells it contains, the `-t` option can be used when running the program and supplying the oracle to it. 
Per file results and overall results will be provided in the terminal output.

# ARTIC: Adaptive Random Testing Based on Combinatorial Interaction Coverage

**Overview**

For programs with combinatorial interaction inputs, failures are often caused by interactions among a small number of input parameter values. Traditional combinatorial testing can ensure systematic interaction coverage, but its generated test suites may lack sufficient randomness and diversity. In contrast, adaptive random testing (ART) improves test diversity, but existing ART-based methods still have limited ability to guide generation by combination coverage. 

To address these limitations, ARTIC incorporates combinatorial interaction coverage into adaptive random test case generation. It employs a multidimensional coverage matrix to record the t-way combinations covered by generated test cases. Based on this coverage information, candidate test cases are constructed through a two-step process that tentatively places uncovered t-way combinations and then assigns the remaining parameter values using randomized greedy selection strategy.

This repository provides the ARTIC implementation, comparison algorithms, subject combinatorial testing models, fault scenarios, and evaluation programs, including ARTIC<sub>GEOO</sub> for generate-and-execute-one-by-one testing pattern and ARTIC<sub>GAET</sub> for generate-all-and-execute-together testing pattern. The reported experimental results in the accompanying study were obtained using OpenJDK 11 on a PC with a 2.6GHz CPU, 32GB RAM, and a 64-bit Debian 10 operating system.

**Project Layout**
- `src/...`: source code
- `scenario`: scenario files
- `subject`: subject models
- `subject-bugs`: bug reports
- `IPM`: IPOG input models
- `lib`: bundled third-party jars
- `result`: matched experiment data

**Algorithms**

| Name in the paper | Java class |
| --- | --- |
| RT | `RT` |
| FSCS-HD | `FSCSHD` |
| FSCS-SD | `FSCSSD` |
| ARTsum | `ARTsum` |
| IPOG | `IPOG` |
| AETG | `AETG` |
| DPSO | `DPSO` |
| GSA | `GSA` |
| ARTIC<sub>GEOO</sub> | `ARTICGEOO` |
| ARTIC<sub>GAET</sub> | `ARTICGAET` |

**Evaluation Classes**
- `artic.evaluation.CATMeasureMain`
- `artic.evaluation.EMeasureMain`
- `artic.evaluation.PMeasureMain`
- `artic.evaluation.FDRMain`
- `artic.evaluation.FMeasureMain`
- `artic.evaluation.EfficiencyTimeMain`

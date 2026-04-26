# ARTIC
ARTIC: an Adaptive Random Testing algorithm based on combinatorial Interaction Coverage

**Project Layout**
- `src/...`: source code
- `scenario`: scenario files
- `subject`: subject models
- `subject-bugs`: bug reports
- `IPM`: IPOG input models
- `lib`: bundled third-party jars
- `result`: matched experiment data

**Algorithms**
- `RT`
- `FSCSHD`
- `FSCSSD`
- `ARTsum`
- `IPOG`
- `AETG`
- `DPSO`
- `GSA`
- `ARTICGEOO`
- `ARTICGAET`

**Evaluation Entrypoints**
- `artic.evaluation.CATMeasureMain`
- `artic.evaluation.EMeasureMain`
- `artic.evaluation.PMeasureMain`
- `artic.evaluation.FDRMain`
- `artic.evaluation.FMeasureMain`
- `artic.evaluation.EfficiencyTimeMain`

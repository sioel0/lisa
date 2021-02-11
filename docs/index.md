---
notoc: true
---

LiSA (Library for Static Analysis) aims to ease the creation and implementation of static analyzers based on the Abstract Interpretation theory.
LiSA provides an analysis engine that works on a generic and extensible control flow graph representation of the program to analyze. Abstract interpreters in LiSA are built 
for analyzing such representation, providing a unique analysis infrastructure for all the analyzers that will rely on it.

Building an analyzer upon LiSA boils down to writing a parser for the language that one aims to analyze, translating the source code or the compiled code towards 
the control flow graph representation of LiSA. Then, simple checks iterating over the results provided by the semantic analyses of LiSA can be easily defined to translate 
semantic information into warnings that can be of value for the final user. 

## How to contrubute

LiSA is developed and maintained by the [Software and System Verification (SSV)](https://ssv.dais.unive.it/) group @ Università Ca' Foscari in Venice, Italy. External contributions are always welcome! Check out our [contributing guidelines](https://github.com/UniVE-SSV/lisa/blob/master/CONTRIBUTING.md) for information on how to contribute to LiSA.

## Guides and Documentation

### LiSA's structure

* [CFG structure](structure/cfg.md)
* [Analysis Infrastructure](structure/analysis-infrastructure.md)
* [Syntactic Checks](structure/syntactic-checks.md)

### Building

* [Building LiSA](building-lisa.md)

### Testing LiSA

* [The IMP language](imp.md)
  
### Working with LiSA
    
* [Create a frontend](working/frontends.md)
* [Create new analysis components](working/analyses.md)

### Javadoc

Visit [this page](javadoc/index.md) for a the javadocs of each release.

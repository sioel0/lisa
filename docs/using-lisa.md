# Using LiSA 

LiSA operates on an intermediate representation based on control flow graphs, end exposes a set of interfaces that can be inherited for defining custom checks that generate warnings.
The basic workflow for a program using LiSA `v0.1a1` is the following:

```java
// create a new instance of LiSA
LiSA lisa = new LiSA();

// create a control flow graph to analyze
CFG cfg = new CFG(...);
// initialize the cfg
// add the cfg to the analysis
lisa.addCFG(cfg);

// add a syntactic check to execute during the analysis
lisa.addSyntacticCheck(new SyntacticCheck() {...} );

// start the analysis
lisa.run();	
```
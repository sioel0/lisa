# The CFG structure

LiSA adopts a flexible CFG structure for representing functions, methods and procedures coming from all languages. A [CFG][cfg] is a graph with [Statements][st] as nodes and [Edges][edge] as edges. This enables to encode control flow structures directly on the structure of the CFG, keeping only statements that have a direct effect on the program state.

## Translating statements and expressions

Since LiSA aims at analyzing programs written in different programming languages, the Statement and Expression structure is designed to be as flexible as possible without enforcing language-specific patterns. Specifically, LiSA adopts a model that can be described as **(almost) everything is a procedure**, meaning that all the native constructs of a language are modeled through procedure calls, with few exceptions. The class hierarchy of statements is the following (that is all contained into the [statements][stpkg] package):

![statements_hierarchy](statements.png)

As can be seen from the above schema, few constructs that are common to most programming languages have been specifically implemented. These are the ones that have a consistent semantic across different languages, and thus can be represented in an uniform way.

While most of the instances are self explainatory, the [Call][call] class - that is, where the magic happens - deserves some attention. LiSA defines three types of calls:
* [CFGCalls][cfgcall] are effective calls towards one of the CFGs defined in the program to analyze;
* [OpenCalls][opencall] are effective calls towards a CFG that has not been submitted to LiSA, that therefore has no knowledge on;
* [NativeCalls][nativecall] are usages of native constructs (e.g., `+`, `<`, `array[index]`, ...) that are simulated through a call;

Simulating native constructs without directly defining them enables different semantics for the same construct, depending on the language that they are written in (i.e., the parser that translated the code). An simple but meaningful example is the handling of array access statements. The statement `array[index]` has different meaning under different languages. For instance, in languages like Java and C, executing such a statement will lead to a runtime error if index is less than 0. However, in a program written in Python, `index = -1` will cause the array access statement to access the last element of the array. Having a unique semantics for array access will require instrumentation code to be prefixed to the statement to ensure that the correct semantics is obtained. For instance, if the semantics was the one of the Java language, a Python frontend would need to generate (and then translate) the following code to comply with the language semantics:
```python
if index < 0:
    index = array.length - index
array[index]
```
While this may be feasible for some statements' semantics, it is certainly not ideal, and might not be attainable for all kind of statements. Moreover, this enforces the definition of the various statements within LiSA, providing also a fixed semantics that authors of frontends need to adapt to. Instead, the approach taken by LiSA is to let frontends themselves define the constructs, together with their semantics. For instance, a Java frontend will translate the array access statement as a subclass of `NativeCall`:
```java
class JavaArrayAccess extends NativeCall { ... }
```
and its `semantics` method will provide the actual statement's semantics (here reported as partial pseudocode):
```
if index is definetly negative
    return bottom
else 
    return domain.valueof(heapAbstraction.get(array, index))
```
A Python frontend will then define its own instance of `NativeCall`:
```java
class PythonArrayAccess extends NativeCall { ... }
```
together with its `semantics` method (reported as partial pseudocode):
```
if index is definetly negative
    return domain.valueof(heapAbstraction.get(array, domain.lengthof(array) - index))
else 
    return domain.valueof(heapAbstraction.get(array, index))
```
While this avoids instrumentation code and the definition of a standard set of constructs inside LiSA, it also provides an elegant way to support multi-language analysis with precise language-based semantics.

## Encoding control flow 

The [Edge][edge] class has three concrete instances:
* [SequentialEdge][seq] modeling a sequential flow between two statements, where the second one is executed right after the first;
* [TrueEdge][true] modeling a conditional flow between two statments, where the second one is executed only if the result of the first is a _true_ boolean value;
* [FalseEdge][false] modeling a conditional flow between two statments, where the second one is executed only if the result of the first is a _false_ boolean value;

Below you can find examples on how to model the most common control flow structures.

### Sequences of statements

Encoding sequential flow between two statements is achieved as follows.

Source code:
```java
void foo() {
  x = 5;
  print(x);
}
```
LiSA code:
```java
CFG foo = new CFG(new CFGDescriptor("foo"));
Assignment a = new Assignment(foo, new Variable(foo, "x"), new Literal(foo, 5));
// depending on where 'print' is defined, a different instance of call can be used
Call print = new OpenCall(foo, "print", new Variable(foo, "x"));
foo.addNode(a, true);
foo.addNode(print);
foo.addEdge(new SequentialEdge(a, print));
```

### If statments

Encoding if statements is achieved as follows.

Source code:
```java
void foo(x) {
  if (x > 5)
	print("yes");
  else
    print("no");
}
```
LiSA code:
```java
CFG foo = new CFG(new CFGDescriptor("foo", new Variable("x")));
// assuming that GreaterThan is a subclass of NativeCall defined somwhere else in the frontend
Call gt = new GreaterThan(foo, new Variable(foo, "x"), new Literal(foo, 5));
// depending on where 'print' is defined, a different instance of call can be used
Call print1 = new OpenCall(foo, "print", new Literal(foo, "yes"));
Call print2 = new OpenCall(foo, "print", new Literal(foo, "no"));
foo.addNode(gt, true);
foo.addNode(print1);
foo.addNode(print2);
foo.addEdge(new TrueEdge(gt, print1));
foo.addEdge(new FalseEdge(gt, print2));
```

### While loops

Encoding while loops is achieved as follows.

Source code:
```java
void foo(x) {
  while (x > 5)
    x = x - 1;
  print(x);
}
```
LiSA code:
```java
CFG foo = new CFG(new CFGDescriptor("foo", new Variable("x")));
// assuming that GreaterThan and Sub are subclasses of NativeCall defined somwhere else in the frontend
Call gt = new GreaterThan(foo, new Variable(foo, "x"), new Literal(foo, 5));
Assignment a = new Assignment(foo, new Variable(foo, "x"), new Sub(foo, new Variable(foo, "x"), new Literal(foo, 1)));
// depending on where 'print' is defined, a different instance of call can be used
Call print = new OpenCall(foo, "print", new Variable(foo, "x"));
foo.addNode(gt, true);
foo.addNode(a);
foo.addNode(print);
foo.addEdge(new TrueEdge(gt, a));
foo.addEdge(new SequentialEdge(a, gt));
foo.addEdge(new FalseEdge(gt, print));
```

### For loops

Encoding for loops is achieved as follows.

Source code:
```java
void foo() {
  for (x = 10; x > 5; x = x - 1)
    print(x);
  print("done");
}
```
LiSA code:
```java
CFG foo = new CFG(new CFGDescriptor("foo"));
// assuming that GreaterThan and Sub are subclasses of NativeCall defined somwhere else in the frontend
Assignment a1 = new Assignment(foo, new Variable(foo, "x"), new Literal(foo, 10));
Call gt = new GreaterThan(foo, new Variable(foo, "x"), new Literal(foo, 5));
Assignment a2 = new Assignment(foo, new Variable(foo, "x"), new Sub(foo, new Variable(foo, "x"), new Literal(foo, 1)));
// depending on where 'print' is defined, a different instance of call can be used
Call print1 = new OpenCall(foo, "print", new Variable(foo, "x"));
Call print2 = new OpenCall(foo, "print", new Literal(foo, "done"));
foo.addNode(a1, true);
foo.addNode(gt);
foo.addNode(print1);
foo.addNode(a2);
foo.addNode(print2);
foo.addEdge(new SequentialEdge(a1, gt));
foo.addEdge(new TrueEdge(gt, print1));
foo.addEdge(new SequentialEdge(print1, a2));
foo.addEdge(new SequentialEdge(a2, gt));
foo.addEdge(new FalseEdge(gt, print2));
```

### Do-While loops

Encoding do-while loops is achieved as follows.

Source code:
```java
void foo(x) {
  do
    x = x - 1;
  while (x > 5);
  print(x);
}
```
LiSA code:
```java
CFG foo = new CFG(new CFGDescriptor("foo", new Variable("x")));
// assuming that GreaterThan and Sub are subclasses of NativeCall defined somwhere else in the frontend
Assignment a = new Assignment(foo, new Variable(foo, "x"), new Sub(foo, new Variable(foo, "x"), new Literal(foo, 1)));
Call gt = new GreaterThan(foo, new Variable(foo, "x"), new Literal(foo, 5));
// depending on where 'print' is defined, a different instance of call can be used
Call print = new OpenCall(foo, "print", new Variable(foo, "x"));
foo.addNode(a, true);
foo.addNode(gt);
foo.addNode(print);
foo.addEdge(new SequentialEdge(a, gt));
foo.addEdge(new TrueEdge(gt, a));
foo.addEdge(new FalseEdge(gt, print));
```

### About NoOps

Depending on how the language parser embedded in the frontend is written, there might be the need of immediately creating an exit point of a statement. This might be a problem when translating conditional statements, since there might be multiple exit points (e.g., the last instructions of both the true and false branches of an if statement). This can be achieved with a [NoOp][noop] statement as follows (this extends the example used to introduce the embedding of if statements):

```java
CFG foo = new CFG(new CFGDescriptor("foo", new Variable("x")));
Call gt = new GreaterThan(foo, new Variable(foo, "x"), new Literal(foo, 5));
Call print1 = new OpenCall(foo, "print", new Literal(foo, "yes"));
Call print2 = new OpenCall(foo, "print", new Literal(foo, "no"));
NoOp noop = new NoOp(foo);
foo.addNode(gt);
foo.addNode(print1);
foo.addNode(print2);
foo.addNode(noop);
foo.addEdge(new TrueEdge(gt, print1));
foo.addEdge(new FalseEdge(gt, print2));
foo.addEdge(new SequentialEdge(print1, noop));
foo.addEdge(new SequentialEdge(print2, noop));
```
When you, all NoOps that have been added can be automatically removed, causing a re-computation of the whole graph behind the CFG, with `foo.simplify()`. 

[cfg]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/cfg/CFG.java
[st]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/cfg/statement/Statement.java
[edge]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/cfg/edge/Edge.java
[seq]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/cfg/edge/SequentialEdge.java 
[true]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/cfg/edge/TrueEdge.java 
[false]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/cfg/edge/FalseEdge.java 
[noop]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/cfg/statement/NoOp.java
[stpkg]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/cfg/statement/
[call]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/cfg/statement/Call.java
[cfgcall]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/cfg/statement/CFGCall.java
[opencall]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/cfg/statement/OpenCall.java
[nativecall]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/cfg/statement/NativeCall.java
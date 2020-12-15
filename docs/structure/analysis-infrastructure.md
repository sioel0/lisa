# The Analysis Infrastructure

LiSA's analysis infrastructure is split into four segments:

1. [Statement][stmt] semantics, taking care of defining the effect of a statement on the analysis state in an analysis-independent fashion;
2. [Call graph][cg] abstraction, resolving cfg calls and evaluating their abstract result;
3. [Heap][heap] abstraction, modeling the heap structure of the program;
4. [Value][value] abstraction, abstracting the values of program variables.

Moreover, LiSA has an internal hierarchy of [SymbolicExpression][symbolic]s targeted by abstract domains, and statements need rewrite themselves as symbolic expressions.

## On Symbolic Expressions

Symbolic expressions are the internal language of LiSA: all abstract domains reason in terms of these expression. These expressions are defined, as can be seen in the image below, over an extemely small set of operations.

![symbolic_expressions](symbolic.png)

Statments defined in the program must rewrite themselves as symbolic expressions to model the side-effect free expression that is being built on the stack, that will eventually be fed to abstract domains to determine its effects. Possible expressions do not include CFG calls (as they are abstracted by the `CallGraph` instance used in the analysis). 

Symbolic expressions are split into two sub-hierarchies:
* Value expressions, that are expressions that work on constant values and identifiers;
* Heap expressions, that model heap operations.

As will be explained below, value abstractions can only reason about value expressions: all heap operations will be rewritten by heap abstractions before feeding the heap-free expression to the value abstraction. The rewriting happen in such a way that memory locations identified by combinations of heap expressions are mapped to a `HeapIdentifier`, agreeing to the logic of the heap abstraction used in the analysis.

## Analysis fundamentals

The overall analysis structure revolves around two basic structures: [Lattice][lattice] and [SemanticDomain][semdom]. These provide the base skeleton of all analysis objects.

### The Lattice interface

Classes implementing the `Lattice` interface represent elements of a lattice. For this reason, the interface defines methods representing lattice operations:

![lattice](lattice.png)

`Lattice` is a *generic* interface that is parametric to the concrete instance `L` of its implementing class, to have appropriate methods return types.

**Note 1:** `isBottom()` and `isTop()` are the methods used in LiSA to determine if a lattice instance represent the bottom or the top element, respectively. Default implementations of this method are provided, and these use reference equality against `bottom()` and `top()`, respectively:

```java
public default boolean isBottom() {
	return this == bottom();
}
	
public default boolean isTop() {
	return this == top();
}
```

For the above to work, the returned values of `top()` and `bottom()` *have* to be unique (that is, no `return new ...` should appear in the methods). If that is not the case, override these methods providing a coherent logic.

**Note 2:** Lattice operations follow common patterns that are not dependent on the specefic instance (like testing for top or bottom values). LiSA provides the `BaseLattice` class, that overrides lattice operations, handling these base cases. This class defines abstract auxiliary methods (like `lubAux()`) that subclasses can use to provide their implementation-specific logic, knowing that neither lattice instances are top (according to `isTop()`), bottom (according to `isBottom()`), or `null`, and that the two instances are not equal (according to both `==` and `equals()`). Unless explicitly needed, all concrete `Lattice` implementations should inherit from `BaseLattice`.

Three special instances of `Lattice` are provided within LiSA:
1. `FunctionalLattice`, that applies functional lifting to inner `Lattice` instances that are mapped to the same key (this type is parametric to `F`,`K` and `V`, that are the concrete type of `FunctionalLattice`, the type of the keys, and the type of the values - that must be a subtype of `Lattice` - respectively);
2. `SetLattice`, that models a lattice where elements are sets, and where the least upper bound is the set union (this type is parametric to `S` and `E`, that are the concrete type of `SetLattice` and the type of elements contained in the set, respectively);
3. `InverseSetLattice`, that models a lattice where elements are sets, and where the least upper bound is the set intersection (this type is parametric to `S` and `E`, that are the concrete type of `SetLattice` and the type of elements contained in the set, respectively);


### The SemanticDomain interface

Classes implementing the `SemanticDomain` interface represents entities that know how to reason about semantics of statements. `SemanticDomain` is parametric to the type `D` that is the concrete type of `SemanticDomain`, the type `E` of `SymbolicExpression`s that the domain can handle (e.g., a domain might be able to only reason about `ValueExpression`s), and the type `I` of `Identifier`s that the domain is able to reason on. The interface defines methods representing state transformations:

![semantic_domain](semdom.png)

* `D assign(I identifier, E expression)` yields a copy of a domain modified by the assignement of the abstract value corresponding to the evaluation of `expression` to `identifier`;
* `D assume(E expression)` yields a copy of a domain modified by assuming that `expression` holds;
* `D forgetIdentifier(I identifier)` forgets all information about a `identifier` (e.g. when it falls out of scope);
* `D forgetIdentifiers(Collection<I> identifiers)` forgets all information about all `identifiers`;
* `String representation()` yields a textual representation of the domain;
* `Satisfiability satisfies(E expression)` yields whether or not `expression` is satisfied in the program state represented by a domain (`Satisfiability` is similar to a Boolean lattice, having values of `bottom`, `unknown`, `satisfied` and `not_satisfied` and methods to combine different satisfiability levels);
* `D smallStepSemantics(E expression)` yields a copy of a domain modified by the evaluation of the semantics of `expression`.

## The Abstract State

The Abstract State of LiSA's analysis is structured as defined in *Pietro Ferrara, A generic framework for heap and value analyses of object-oriented programming languages, Theoretical Computer Science, Volume 631, 2016, Pages 43-72* ([DOI](https://doi.org/10.1016/j.tcs.2016.04.001.), [link](http://www.sciencedirect.com/science/article/pii/S0304397516300299)). In this framework, the abstract state is composed of a heap abstraction ([HeapDomain][heapdom]) and a value abstrction ([ValueDomain][valuedom]), where the semantics of expressions is first evaluated on the heap abstraction, that rewrites the expression by removing all the bits regarding heap operations with symbolic identifiers (`HeapIdentifier`s in LiSA). The rewrtitten expression is then processed by the value abstraction.

`HeapDomain` is an interface parametric on the concrete type `H` of the domain, and extends `Lattice<H>` and `SemanticDomain<H, SymbolicExpression, Identifier`, meaning that it can work on all `SymbolicExpression`s and can assign values to all types of `Identifier`s. It also extends the [HeapSemanticOperation][heapsemop] interface, meaning that il will provide rewritten expressions through method `getRewrittenExpressions()` (the returned collection usually contain a single expression, since expression rewriting is deterministc - more than one expression could be introduced through lattice operations, like least upper bound), and a substitution thorugh method `getSubstitution()`, yielding a list of materializations and summarizations of heap identifiers.

`ValueDomain` is an interface parametric on the concrete type `V` of the domain, and extends `Lattice<V>` and `SemanticDomain<V, ValueExpression, Identifier`, meaning that it can work on all `ValueExpression`s and can assign values to all types of `Identifier`s. A value abstraction must also react to the substitution provided by the heap abstraction: this is achieved through `applySubstitution()`, that comes with a default implementation.

LiSA's [AbstractState][absstate] is parametric on the type `H` of `HeapDomain` and the type `V` of `ValueDomain`, and implements `Lattice<AbstractState<H, V>>` and `SemanticDomain<AbstractState<H, V>, SymbolicExpression, Identifier>`. Semantic operations are implemented by invoking the corresponding operation on the heap domain, applying the yielded substitution to the value domain, and then invoking the operation on the value domain using the rewrtitten expressions produced by the heap domain.

### On non-relational analyses

Non-relational analyses compute independent values for different program variables, and are able to evaluate an expression to an abstract values by knowing the abstract values of program variables. The infrastructure for mapping variables to abstract values is abstracted away in LiSA through the concept of *environment* ([HeapEnvironment][heapenv] for heap analyses and [ValueEnvironment][valueenv] for value analyses), that is a `FunctionalLattice` mapping `Identifier`s to instances of [NonRelationalDomain][nonrel] (specifically, to [NonRelationalHeapDomain][nonrelheap] and [NonRelationalValueDomain][nonrelvalue] respectively). Environments are also instances of `SemanticDomain` (specifically, `HeapEnvironment` is a `HeapDomain` and `ValueEnvironment` is a `ValueDomain`).

`NonRelationalHeapDomain` (and `NonRelationalValueDomain`) is parametric to the type `T` of its concrete implementation, and is a special instance of `Lattice<T>` that, given a `HeapEnvironment<T>` (or `ValueEnvironment<T>` for its value counterpart), know how to evaluate a symbolic expression (with method `eval()`) and to check if an expression is satisfied (with method `satisfies()`).

## The Analysis State

## The CallGraph

## Statement semantics

## Executing an analysis

[stmt]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/cfg/statement/Statement.java
[cg]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/callgraph/CallGraph.java
[heap]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/analysis/HeapDomain.java
[value]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/analysis/ValueDomain.java
[symbolic]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/symbolic/SymbolicExpression.java 
[lattice]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/analysis/Lattice.java 
[semdom]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/analysis/SemanticDomain.java 
[heapdom]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/analysis/HeapDomain.java 
[valuedom]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/analysis/ValueDomain.java 
[heapsemop]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/analysis/HeapSemanticOperation.java
[absstate]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/analysis/AbstractState.java
[heapenv]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/analysis/nonrelational/HeapEnvironment.java
[valueenv]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/analysis/nonrelational/ValueEnvironment.java
[nonrel]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/analysis/nonrelational/NonRelationalDomain.java
[nonrelheap]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/analysis/nonrelational/NonRelationalHeapDomain.java
[nonrelvalue]:https://github.com/UniVE-SSV/lisa/blob/master/lisa/src/main/java/it/unive/lisa/analysis/nonrelational/NonRelationalValueDomain.java
package it.unive.lisa.symbolic.value.operator.binary;

import it.unive.lisa.caches.Caches;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.operator.Multiplication;
import it.unive.lisa.symbolic.value.operator.OverflowingOperator;
import it.unive.lisa.type.NumericType;
import it.unive.lisa.type.Type;
import it.unive.lisa.util.collections.externalSet.ExternalSet;
import java.util.function.Predicate;

/**
 * Given two expressions that both evaluate to numeric values, a
 * {@link BinaryExpression} using this operator computes the arithmetic
 * multiplication of those values. Both arguments and results are expected to be
 * 64-bits numbers, and this operation thus can overflow/underflow.<br>
 * <br>
 * First argument expression type: {@link NumericType} (64-bit)<br>
 * Second argument expression type: {@link NumericType} (64-bit)<br>
 * Computed expression type: {@link NumericType} (64-bit)
 * 
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public class Numeric64BitMul implements Multiplication, OverflowingOperator, BinaryOperator {

	/**
	 * The singleton instance of this class.
	 */
	public static final Numeric64BitMul INSTANCE = new Numeric64BitMul();

	private Numeric64BitMul() {
	}

	@Override
	public String toString() {
		return "*";
	}

	@Override
	public ExternalSet<Type> typeInference(ExternalSet<Type> left, ExternalSet<Type> right) {
		Predicate<Type> test = type -> type.isNumericType() && type.asNumericType().is64Bits();
		if (left.noneMatch(test) || right.noneMatch(test))
			return Caches.types().mkEmptySet();
		ExternalSet<Type> set = NumericType.commonNumericalType(left, right).filter(test);
		if (set.isEmpty())
			return Caches.types().mkEmptySet();
		return set;
	}
}
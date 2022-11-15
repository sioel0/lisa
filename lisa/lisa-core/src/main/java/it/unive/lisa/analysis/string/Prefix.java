package it.unive.lisa.analysis.string;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.operator.binary.*;
import java.util.Objects;

/**
 * The prefix string abstract domain.
 *
 * @author <a href="mailto:vincenzo.arceri@unipr.it">Vincenzo Arceri</a>
 * @author <a href="mailto:sergiosalvatore.evola@studenti.unipr.it">Sergio
 *             Salvatore Evola</a>
 * 
 * @see <a href=
 *          "https://link.springer.com/chapter/10.1007/978-3-642-24559-6_34">
 *          https://link.springer.com/chapter/10.1007/978-3-642-24559-6_34</a>
 */
public class Prefix extends BaseNonRelationalValueDomain<Prefix> {

	private final static Prefix TOP = new Prefix();
	private final static Prefix BOTTOM = new Prefix(null);
	private final String prefix;

	/**
	 * Builds the top prefix abstract element.
	 */
	public Prefix() {
		this("");
	}

	/**
	 * Builds a prefix abstract element.
	 * 
	 * @param prefix the prefix
	 */
	public Prefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public Prefix lubAux(Prefix other) throws SemanticException {
		String otherPrefixString = other.getPrefix();
		StringBuilder result = new StringBuilder();

		int i = 0;
		while (i <= prefix.length() - 1 &&
				i <= otherPrefixString.length() - 1 &&
				prefix.charAt(i) == otherPrefixString.charAt(i)) {
			result.append(prefix.charAt(i++));
		}

		if (result.length() != 0)
			return new Prefix(result.toString());

		else
			return TOP;
	}

	@Override
	public boolean lessOrEqualAux(Prefix other) throws SemanticException {
		if (other.getPrefix().length() <= this.getPrefix().length()) {
			Prefix lub = this.lubAux(other);
			String lubString = lub.getPrefix();

			return lubString.length() == other.getPrefix().length();
		}

		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Prefix prefix1 = (Prefix) o;
		return Objects.equals(prefix, prefix1.prefix);
	}

	@Override
	public int hashCode() {
		return Objects.hash(prefix);
	}

	@Override
	public Prefix top() {
		return TOP;
	}

	@Override
	public Prefix bottom() {
		return BOTTOM;
	}

	@Override
	public DomainRepresentation representation() {
		if (isBottom())
			return Lattice.bottomRepresentation();
		if (isTop())
			return Lattice.topRepresentation();

		return new StringRepresentation(prefix + '*');
	}

	@Override
	public Prefix evalNonNullConstant(Constant constant, ProgramPoint pp) {
		if (constant.getValue() instanceof String)
			return new Prefix((String) constant.getValue());

		return TOP;
	}

	@Override
	public Prefix evalBinaryExpression(BinaryOperator operator, Prefix left, Prefix right, ProgramPoint pp) {
		if (operator == StringConcat.INSTANCE) {
			return left;
		} else if (operator == StringContains.INSTANCE ||
				operator == StringEndsWith.INSTANCE ||
				operator == StringEquals.INSTANCE ||
				operator == StringIndexOf.INSTANCE ||
				operator == StringStartsWith.INSTANCE) {
			return TOP;
		}

		return TOP;
	}

	/**
	 * Yields the prefix of this abstract value.
	 * 
	 * @return the prefix of this abstract value.
	 */
	protected String getPrefix() {
		return this.prefix;
	}
}

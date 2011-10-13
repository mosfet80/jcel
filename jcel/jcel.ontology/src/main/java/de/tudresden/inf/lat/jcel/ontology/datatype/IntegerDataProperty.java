/*
 * Copyright 2009 Julian Mendez
 *
 *
 * This file is part of jcel.
 *
 * jcel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jcel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jcel.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package de.tudresden.inf.lat.jcel.ontology.datatype;

import java.util.Collections;
import java.util.Set;

/**
 * This class models an object property.
 * 
 * @author Julian Mendez
 * 
 */
public class IntegerDataProperty implements IntegerDataPropertyExpression,
		Comparable<IntegerDataProperty> {

	private final Integer id;

	/**
	 * Constructs an data property.
	 * 
	 * @param n
	 *            data property identifier
	 */
	public IntegerDataProperty(Integer n) {
		if (n == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.id = n;
	}

	@Override
	public <T> T accept(IntegerDataPropertyExpressionVisitor<T> visitor) {
		if (visitor == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		return visitor.visit(this);
	}

	@Override
	public int compareTo(IntegerDataProperty o) {
		if (o == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		return getId().compareTo(o.getId());
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = false;
		if (o instanceof IntegerDataProperty) {
			IntegerDataProperty other = (IntegerDataProperty) o;
			ret = getId().equals(other.getId());
		}
		return ret;
	}

	@Override
	public Set<Integer> getClassesInSignature() {
		return Collections.emptySet();
	}

	@Override
	public Set<Integer> getDataPropertiesInSignature() {
		return Collections.singleton(getId());
	}

	@Override
	public Set<Integer> getDatatypesInSignature() {
		return Collections.emptySet();
	}

	@Override
	public Integer getId() {
		return this.id;
	}

	@Override
	public Set<Integer> getIndividualsInSignature() {
		return Collections.emptySet();
	}

	@Override
	public Set<Integer> getObjectPropertiesInSignature() {
		return Collections.emptySet();
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	@Override
	public boolean hasOnlyLiterals() {
		return true;
	}

	@Override
	public boolean isLiteral() {
		return true;
	}

	@Override
	public String toString() {
		return getId().toString();
	}

}
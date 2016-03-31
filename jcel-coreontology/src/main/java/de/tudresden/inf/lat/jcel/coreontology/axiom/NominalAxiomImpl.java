/*
 *
 * Copyright (C) 2009-2015 Julian Mendez
 *
 *
 * This file is part of jcel.
 *
 *
 * The contents of this file are subject to the GNU Lesser General Public License
 * version 3
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Alternatively, the contents of this file may be used under the terms
 * of the Apache License, Version 2.0, in which case the
 * provisions of the Apache License, Version 2.0 are applicable instead of those
 * above.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package de.tudresden.inf.lat.jcel.coreontology.axiom;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * This is the default implementation of {@link NominalAxiom}.
 * 
 * @author Julian Mendez
 * 
 */
public class NominalAxiomImpl implements NominalAxiom {

	private final int classExpression;
	private final int individual;
	private final Set<Annotation> annotations;
	private final int hashCode;

	/**
	 * Constructs a new nominal axiom.
	 * 
	 * @param classId
	 *            class identifier in the axiom
	 * @param individualId
	 *            individual identifier in the axiom
	 * @param annotations
	 *            annotations
	 */
	NominalAxiomImpl(int classId, int individualId, Set<Annotation> annotations) {
		Objects.requireNonNull(annotations);
		this.classExpression = classId;
		this.individual = individualId;
		this.annotations = annotations;
		this.hashCode = this.classExpression + 0x1F * (this.individual + 0x1F * this.annotations.hashCode());
	}

	@Override
	public <T> T accept(NormalizedIntegerAxiomVisitor<T> visitor) {
		Objects.requireNonNull(visitor);
		return visitor.visit(this);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof NominalAxiom)) {
			return false;
		} else {
			NominalAxiom other = (NominalAxiom) obj;
			return (getClassExpression() == other.getClassExpression()) && (getIndividual() == other.getIndividual())
					&& getAnnotations().equals(other.getAnnotations());
		}
	}

	@Override
	public Set<Integer> getClassesInSignature() {
		return Collections.singleton(getClassExpression());
	}

	@Override
	public int getClassExpression() {
		return this.classExpression;
	}

	@Override
	public Set<Integer> getDataPropertiesInSignature() {
		return Collections.emptySet();
	}

	@Override
	public Set<Integer> getDatatypesInSignature() {
		return Collections.emptySet();
	}

	@Override
	public int getIndividual() {
		return this.individual;
	}

	@Override
	public Set<Integer> getIndividualsInSignature() {
		return Collections.singleton(getIndividual());
	}

	@Override
	public Set<Integer> getObjectPropertiesInSignature() {
		return Collections.emptySet();
	}

	@Override
	public Set<Annotation> getAnnotations() {
		return Collections.unmodifiableSet(this.annotations);
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(NormalizedIntegerAxiomConstant.NominalAxiom);
		sbuf.append(NormalizedIntegerAxiomConstant.LEFT_PAR);
		sbuf.append(getClassExpression());
		sbuf.append(NormalizedIntegerAxiomConstant.SP);
		sbuf.append(getIndividual());
		sbuf.append(NormalizedIntegerAxiomConstant.RIGHT_PAR);
		return sbuf.toString();
	}

}
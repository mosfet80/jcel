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

package de.tudresden.inf.lat.jcel.core.completion.basic;

import java.util.Collection;

import de.tudresden.inf.lat.jcel.core.completion.common.ClassifierStatus;
import de.tudresden.inf.lat.jcel.core.completion.common.SObserverRule;

/**
 * 
 * <ul>
 * <li>CR-2 : <b>if</b> A<sub>1</sub> \u2293 A<sub>2</sub> \u2291 B &isin;
 * <i>T</i>, <u>(x, A<sub>1</sub>) &isin; S</u>, <u>(x, A<sub>2</sub>) &isin;
 * S</u> <br>
 * <b>then</b> S := S &cup; {(x, B)}</li>
 * </ul>
 * <br>
 * 
 * Previous forms:
 * 
 * <ul>
 * <li>CR-2 : <b>if</b> A<sub>1</sub> \u2293 &hellip; \u2293 A<sub>i</sub>
 * \u2293 &hellip; \u2293 A<sub>n</sub> \u2291 B &isin; <i>T</i>, (x, A
 * <sub>1</sub>) &isin; S, &hellip; <u>(x, A<sub>i</sub>) &isin; S</u>, &hellip;
 * , (x, A<sub>n</sub>) &isin; S <br>
 * <b>then</b> S := S &cup; {(x, B)}</li>
 * </ul>
 * 
 * <ul>
 * <li>CR1 : <b>if</b> A<sub>1</sub>, &hellip; , A<sub>n</sub> &isin; S(X)
 * <b>and</b> A<sub>1</sub> \u2293 &hellip; \u2293 A<sub>n</sub> \u2291 B &isin;
 * O <b>and</b> B &notin; S(X) <br>
 * <b>then</b> S(X) := S(X) &cup; {B}</li>
 * </ul>
 * 
 * @author Julian Mendez
 */
public class CR2SRule implements SObserverRule {

	/**
	 * Constructs a new completion rule CR-2.
	 */
	public CR2SRule() {
	}

	@Override
	public boolean apply(ClassifierStatus status, int subClass, int superClass) {
		if (status == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		return applyRule(status, subClass, superClass);
	}

	private boolean applyRule(ClassifierStatus status, int x, int a) {
		Collection<Integer> subsumersOfX = status.getSubsumers(x);
		return status.getExtendedOntology().getGCI1Axioms(a).stream().map(axiom -> {

			boolean valid = true;

			if (a == axiom.getRightSubClass()) {
				valid = valid && subsumersOfX.contains(axiom.getLeftSubClass());
			} else {
				valid = valid && subsumersOfX.contains(axiom.getRightSubClass());
			}

			if (valid) {
				int b = axiom.getSuperClass();
				return status.addNewSEntry(x, b);
			} else {
				return false;
			}

		}).reduce(false, (accum, elem) -> (accum || elem));
	}

	@Override
	public boolean equals(Object o) {
		return (o != null) && getClass().equals(o.getClass());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}

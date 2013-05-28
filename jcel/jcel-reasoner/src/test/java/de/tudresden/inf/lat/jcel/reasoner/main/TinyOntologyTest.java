/*
 *
 * Copyright 2009-2013 Julian Mendez
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

package de.tudresden.inf.lat.jcel.reasoner.main;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;
import de.tudresden.inf.lat.jcel.coreontology.datatype.IntegerEntityType;
import de.tudresden.inf.lat.jcel.ontology.axiom.complex.ComplexIntegerAxiom;
import de.tudresden.inf.lat.jcel.ontology.axiom.extension.IntegerOntologyObjectFactory;
import de.tudresden.inf.lat.jcel.ontology.axiom.extension.IntegerOntologyObjectFactoryImpl;
import de.tudresden.inf.lat.jcel.ontology.datatype.IntegerClass;
import de.tudresden.inf.lat.jcel.ontology.datatype.IntegerClassExpression;
import de.tudresden.inf.lat.jcel.ontology.datatype.IntegerObjectProperty;

/**
 * Set of tests using tiny ontologies.
 * 
 * @see RuleBasedReasoner
 * 
 * @author Julian Mendez
 */
public class TinyOntologyTest extends TestCase {

	/**
	 * Constructs a new set of tests for the rule based reasoner.
	 */
	public TinyOntologyTest() {
	}

	private IntegerClass createNewClass(IntegerOntologyObjectFactory factory,
			String name) {
		return factory.getDataTypeFactory().createClass(
				factory.getEntityManager().createNamedEntity(
						IntegerEntityType.CLASS, name, false));
	}

	private IntegerObjectProperty createNewObjectProperty(
			IntegerOntologyObjectFactory factory, String name) {
		return factory.getDataTypeFactory().createObjectProperty(
				factory.getEntityManager().createNamedEntity(
						IntegerEntityType.OBJECT_PROPERTY, name, false));
	}

	private Set<IntegerClass> flatten(Set<Set<IntegerClass>> originalSet) {
		Set<IntegerClass> ret = new TreeSet<IntegerClass>();
		for (Set<IntegerClass> set : originalSet) {
			ret.addAll(set);
		}
		return ret;
	}

	/**
	 * <ol>
	 * <li>A &sqsube; B,</li>
	 * <li>B &sqsube; C</li>
	 * </ol>
	 * &vDash;
	 * <ul>
	 * <li>A &sqsube; C</li>
	 * </ul>
	 */
	public void testTinyOntology0() {
		IntegerOntologyObjectFactory factory = new IntegerOntologyObjectFactoryImpl();

		Set<ComplexIntegerAxiom> ontology = new HashSet<ComplexIntegerAxiom>();
		IntegerClass a = createNewClass(factory, "A");
		IntegerClass b = createNewClass(factory, "B");
		IntegerClass c = createNewClass(factory, "C");

		// 1
		ontology.add(factory.getComplexAxiomFactory().createSubClassOfAxiom(a,
				b));

		// 2
		ontology.add(factory.getComplexAxiomFactory().createSubClassOfAxiom(b,
				c));

		IntegerReasoner reasoner = new RuleBasedReasoner(ontology, factory);
		reasoner.classify();

		Set<IntegerClass> superClassesOfA = flatten(reasoner.getSuperClasses(a,
				false));
		assertTrue(superClassesOfA.contains(c));

		Set<IntegerClass> subClassesOfC = flatten(reasoner.getSubClasses(c,
				false));
		assertTrue(subClassesOfC.contains(a));

		verifyBottomAndTop(factory, reasoner);
	}

	/**
	 * <ol>
	 * <li>A &sqsube; &exist; r <i>.</i> A ,</li>
	 * <li>A &sqsube; B ,</li>
	 * <li>&exist; r <i>.</i> B &sqsube; C</li>
	 * </ol>
	 * &vDash;
	 * <ul>
	 * <li>A &sqsube; C</li>
	 * </ul>
	 */
	public void testTinyOntology1() {
		IntegerOntologyObjectFactory factory = new IntegerOntologyObjectFactoryImpl();

		Set<ComplexIntegerAxiom> ontology = new HashSet<ComplexIntegerAxiom>();
		IntegerClass a = createNewClass(factory, "A");
		IntegerClass b = createNewClass(factory, "B");
		IntegerClass c = createNewClass(factory, "C");
		IntegerObjectProperty r = createNewObjectProperty(factory, "r");

		// 1
		ontology.add(factory.getComplexAxiomFactory().createSubClassOfAxiom(a,
				factory.getDataTypeFactory().createObjectSomeValuesFrom(r, a)));

		// 2
		ontology.add(factory.getComplexAxiomFactory().createSubClassOfAxiom(a,
				b));

		// 3
		ontology.add(factory.getComplexAxiomFactory().createSubClassOfAxiom(
				factory.getDataTypeFactory().createObjectSomeValuesFrom(r, b),
				c));

		IntegerReasoner reasoner = new RuleBasedReasoner(ontology, factory);
		reasoner.classify();

		Set<IntegerClass> superClassesOfA = flatten(reasoner.getSuperClasses(a,
				false));
		assertTrue(superClassesOfA.contains(c));

		Set<IntegerClass> subClassesOfC = flatten(reasoner.getSubClasses(c,
				false));
		assertTrue(subClassesOfC.contains(a));

		verifyBottomAndTop(factory, reasoner);
	}

	/**
	 * <ol>
	 * <li>A &sqsube; B ,</li>
	 * <li>B &sqsube; A ,</li>
	 * </ol>
	 * &vDash;
	 * <ul>
	 * <li>A &equiv; B</li>
	 * </ul>
	 */
	public void testTinyOntology2() {
		IntegerOntologyObjectFactory factory = new IntegerOntologyObjectFactoryImpl();

		Set<ComplexIntegerAxiom> ontology = new HashSet<ComplexIntegerAxiom>();
		IntegerClass a = createNewClass(factory, "A");
		IntegerClass b = createNewClass(factory, "B");

		// 1
		ontology.add(factory.getComplexAxiomFactory().createSubClassOfAxiom(a,
				b));

		// 2
		ontology.add(factory.getComplexAxiomFactory().createSubClassOfAxiom(b,
				a));

		IntegerReasoner reasoner = new RuleBasedReasoner(ontology, factory);
		reasoner.classify();

		Set<IntegerClass> equivalentsOfA = reasoner.getEquivalentClasses(a);
		assertTrue(equivalentsOfA.contains(b));

		Set<IntegerClass> equivalentsOfB = reasoner.getEquivalentClasses(b);
		assertTrue(equivalentsOfB.contains(a));

		verifyBottomAndTop(factory, reasoner);
	}

	/**
	 * <ol>
	 * <li>&top; &sqsube; A ,</li>
	 * <li>A &sqsube; B</li>
	 * </ol>
	 * &vDash;
	 * <ul>
	 * <li>A &equiv; B</li>
	 * <li>B &equiv; &top;</li>
	 * </ul>
	 */
	public void testTinyOntology3() {
		IntegerOntologyObjectFactory factory = new IntegerOntologyObjectFactoryImpl();

		Set<ComplexIntegerAxiom> ontology = new HashSet<ComplexIntegerAxiom>();
		IntegerClass a = createNewClass(factory, "A");
		IntegerClass b = createNewClass(factory, "B");

		// 1
		ontology.add(factory.getComplexAxiomFactory().createSubClassOfAxiom(
				factory.getDataTypeFactory().getTopClass(), a));

		// 2
		ontology.add(factory.getComplexAxiomFactory().createSubClassOfAxiom(a,
				b));

		IntegerReasoner reasoner = new RuleBasedReasoner(ontology, factory);
		reasoner.classify();

		Set<IntegerClass> equivalentsOfA = reasoner.getEquivalentClasses(a);
		assertTrue(equivalentsOfA.contains(b));

		Set<IntegerClass> equivalentsOfB = reasoner.getEquivalentClasses(b);
		assertTrue(equivalentsOfB.contains(a));
		assertTrue(equivalentsOfB.contains(factory.getDataTypeFactory()
				.getTopClass()));

		Set<IntegerClass> equivalentsOfTop = reasoner
				.getEquivalentClasses(factory.getDataTypeFactory()
						.getTopClass());
		assertTrue(equivalentsOfTop.contains(b));

		verifyBottomAndTop(factory, reasoner);
	}

	/**
	 * <ol>
	 * <li>A &sqsube; &perp; ,</li>
	 * <li>B &sqsube; A</li>
	 * </ol>
	 * &vDash;
	 * <ul>
	 * <li>A &equiv; B</li>
	 * <li>B &equiv; &perp;</li>
	 * </ul>
	 */
	public void testTinyOntology4() {
		IntegerOntologyObjectFactory factory = new IntegerOntologyObjectFactoryImpl();

		Set<ComplexIntegerAxiom> ontology = new HashSet<ComplexIntegerAxiom>();
		IntegerClass a = createNewClass(factory, "A");
		IntegerClass b = createNewClass(factory, "B");

		// 1
		ontology.add(factory.getComplexAxiomFactory().createSubClassOfAxiom(a,
				factory.getDataTypeFactory().getBottomClass()));

		// 2
		ontology.add(factory.getComplexAxiomFactory().createSubClassOfAxiom(b,
				a));

		IntegerReasoner reasoner = new RuleBasedReasoner(ontology, factory);
		reasoner.classify();

		Set<IntegerClass> equivalentsOfA = reasoner.getEquivalentClasses(a);
		assertTrue(equivalentsOfA.contains(b));

		Set<IntegerClass> equivalentsOfB = reasoner.getEquivalentClasses(b);
		assertTrue(equivalentsOfB.contains(a));
		assertTrue(equivalentsOfB.contains(factory.getDataTypeFactory()
				.getBottomClass()));

		Set<IntegerClass> equivalentsOfBottom = reasoner
				.getEquivalentClasses(factory.getDataTypeFactory()
						.getBottomClass());
		assertTrue(equivalentsOfBottom.contains(b));

		verifyBottomAndTop(factory, reasoner);
	}

	/**
	 * <ol>
	 * <li>C &equiv; A<sub>1</sub> &sqcap; A<sub>2</sub> &sqcap; A<sub>3</sub>,</li>
	 * <li>D &equiv; A<sub>2</sub> &sqcap; A<sub>3</sub> &sqcap; A<sub>4</sub>,</li>
	 * <li>A<sub>1</sub> &equiv; &top;</li>
	 * <li>A<sub>4</sub> &equiv; &top;</li>
	 * </ol>
	 * &vDash;
	 * <ul>
	 * <li>C &equiv; D</li>
	 * </ul>
	 */
	public void testTinyOntology5() {
		IntegerOntologyObjectFactory factory = new IntegerOntologyObjectFactoryImpl();

		Set<ComplexIntegerAxiom> ontology = new HashSet<ComplexIntegerAxiom>();
		IntegerClass a1 = createNewClass(factory, "A1");
		IntegerClass a2 = createNewClass(factory, "A2");
		IntegerClass a3 = createNewClass(factory, "A3");
		IntegerClass a4 = createNewClass(factory, "A4");
		IntegerClass c = createNewClass(factory, "C");
		IntegerClass d = createNewClass(factory, "D");

		{
			Set<IntegerClassExpression> conjunction = new HashSet<IntegerClassExpression>();
			conjunction.add(a1);
			conjunction.add(a2);
			conjunction.add(a3);
			IntegerClassExpression defOfC = factory.getDataTypeFactory()
					.createObjectIntersectionOf(conjunction);
			Set<IntegerClassExpression> equivClasses = new HashSet<IntegerClassExpression>();
			equivClasses.add(c);
			equivClasses.add(defOfC);

			// 1
			ontology.add(factory.getComplexAxiomFactory()
					.createEquivalentClassesAxiom(equivClasses));
		}

		{
			Set<IntegerClassExpression> conjunction = new HashSet<IntegerClassExpression>();
			conjunction.add(a2);
			conjunction.add(a3);
			conjunction.add(a4);
			IntegerClassExpression defOfD = factory.getDataTypeFactory()
					.createObjectIntersectionOf(conjunction);
			Set<IntegerClassExpression> equivClasses = new HashSet<IntegerClassExpression>();
			equivClasses.add(d);
			equivClasses.add(defOfD);

			// 2
			ontology.add(factory.getComplexAxiomFactory()
					.createEquivalentClassesAxiom(equivClasses));
		}

		{

			Set<IntegerClassExpression> equivClasses = new HashSet<IntegerClassExpression>();
			equivClasses.add(a1);
			equivClasses.add(factory.getDataTypeFactory().getTopClass());

			// 3
			ontology.add(factory.getComplexAxiomFactory()
					.createEquivalentClassesAxiom(equivClasses));
		}

		{

			Set<IntegerClassExpression> equivClasses = new HashSet<IntegerClassExpression>();
			equivClasses.add(a4);
			equivClasses.add(factory.getDataTypeFactory().getTopClass());

			// 4
			ontology.add(factory.getComplexAxiomFactory()
					.createEquivalentClassesAxiom(equivClasses));
		}

		IntegerReasoner reasoner = new RuleBasedReasoner(ontology, factory);
		reasoner.classify();

		Set<IntegerClass> equivToC = reasoner.getEquivalentClasses(c);
		assertTrue(equivToC.contains(d));

		Set<IntegerClass> equivToD = reasoner.getEquivalentClasses(d);
		assertTrue(equivToD.contains(c));

		verifyBottomAndTop(factory, reasoner);
	}

	private void verifyBottomAndTop(IntegerOntologyObjectFactory factory,
			IntegerReasoner reasoner) {

		IntegerClass top = factory.getDataTypeFactory().getTopClass();
		IntegerClass bottom = factory.getDataTypeFactory().getBottomClass();

		assertTrue(reasoner.getSubClasses(bottom, true).isEmpty());
		assertTrue(reasoner.getSubClasses(bottom, false).isEmpty());
		assertTrue(reasoner.getSuperClasses(top, true).isEmpty());
		assertTrue(reasoner.getSuperClasses(top, false).isEmpty());
	}

}

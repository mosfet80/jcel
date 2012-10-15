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

package de.tudresden.inf.lat.jcel.ontology.normalization;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import de.tudresden.inf.lat.jcel.coreontology.datatype.IntegerAxiom;
import de.tudresden.inf.lat.jcel.coreontology.datatype.IntegerEntityType;
import de.tudresden.inf.lat.jcel.ontology.axiom.complex.IntegerSubClassOfAxiom;
import de.tudresden.inf.lat.jcel.ontology.axiom.extension.IntegerOntologyObjectFactory;
import de.tudresden.inf.lat.jcel.ontology.axiom.extension.IntegerOntologyObjectFactoryImpl;
import de.tudresden.inf.lat.jcel.ontology.datatype.IntegerClass;
import de.tudresden.inf.lat.jcel.ontology.datatype.IntegerClassExpression;
import de.tudresden.inf.lat.jcel.ontology.datatype.IntegerObjectProperty;

/**
 * @author Julian Mendez
 * 
 * @see NormalizerNR3_3
 */
public class NormalizerNR3_3Test extends TestCase {

	/**
	 * D<sub>1</sub> &#8851; &exist; r <i>.</i> (D<sub>2</sub> &#8851;
	 * D<sub>3</sub>) &#8849; D<sub>2</sub> &#8851; C<sub>1</sub> &#8851;
	 * &exist; s<sub>1</sub> <i>.</i> (D<sub>4</sub> &#8851; D<sub>5</sub>)
	 * &#8851; &exist; s<sub>2</sub> <i>.</i> D<sub>6</sub>
	 */
	public void testRule() {
		IntegerOntologyObjectFactory factory = new IntegerOntologyObjectFactoryImpl();
		NormalizerNR3_3 normalizer = new NormalizerNR3_3(factory);

		IntegerObjectProperty r = factory.getDataTypeFactory()
				.createObjectProperty(
						factory.getEntityManager().createNamedEntity(
								IntegerEntityType.OBJECT_PROPERTY, "r", false));
		IntegerObjectProperty s1 = factory
				.getDataTypeFactory()
				.createObjectProperty(
						factory.getEntityManager().createNamedEntity(
								IntegerEntityType.OBJECT_PROPERTY, "s1", false));
		IntegerObjectProperty s2 = factory
				.getDataTypeFactory()
				.createObjectProperty(
						factory.getEntityManager().createNamedEntity(
								IntegerEntityType.OBJECT_PROPERTY, "s2", false));
		IntegerClass c1 = factory.getDataTypeFactory().createClass(
				factory.getEntityManager().createNamedEntity(
						IntegerEntityType.CLASS, "C1", false));
		IntegerClass d1 = factory.getDataTypeFactory().createClass(
				factory.getEntityManager().createNamedEntity(
						IntegerEntityType.CLASS, "D1", false));
		IntegerClass d2 = factory.getDataTypeFactory().createClass(
				factory.getEntityManager().createNamedEntity(
						IntegerEntityType.CLASS, "D2", false));
		IntegerClass d3 = factory.getDataTypeFactory().createClass(
				factory.getEntityManager().createNamedEntity(
						IntegerEntityType.CLASS, "D3", false));
		IntegerClass d4 = factory.getDataTypeFactory().createClass(
				factory.getEntityManager().createNamedEntity(
						IntegerEntityType.CLASS, "D4", false));
		IntegerClass d5 = factory.getDataTypeFactory().createClass(
				factory.getEntityManager().createNamedEntity(
						IntegerEntityType.CLASS, "D5", false));
		IntegerClass d6 = factory.getDataTypeFactory().createClass(
				factory.getEntityManager().createNamedEntity(
						IntegerEntityType.CLASS, "D6", false));

		Set<IntegerClassExpression> set1 = new HashSet<IntegerClassExpression>();
		set1.add(d2);
		set1.add(d3);
		IntegerClassExpression dPrime0 = factory.getDataTypeFactory()
				.createObjectIntersectionOf(set1);
		IntegerClassExpression dPrime1 = factory.getDataTypeFactory()
				.createObjectSomeValuesFrom(r, dPrime0);

		Set<IntegerClassExpression> set2 = new HashSet<IntegerClassExpression>();
		set2.add(d1);
		set2.add(dPrime1);
		IntegerClassExpression d = factory.getDataTypeFactory()
				.createObjectIntersectionOf(set2);

		Set<IntegerClassExpression> set3 = new HashSet<IntegerClassExpression>();
		set3.add(d4);
		set3.add(d5);
		IntegerClassExpression dPrime2 = factory.getDataTypeFactory()
				.createObjectIntersectionOf(set3);
		IntegerClassExpression c2 = factory.getDataTypeFactory()
				.createObjectSomeValuesFrom(s1, dPrime2);

		IntegerClassExpression c3 = factory.getDataTypeFactory()
				.createObjectSomeValuesFrom(s2, d6);

		Set<IntegerClassExpression> set4 = new HashSet<IntegerClassExpression>();
		set4.add(d2);
		set4.add(c1);
		set4.add(c2);
		set4.add(c3);
		IntegerClassExpression c = factory.getDataTypeFactory()
				.createObjectIntersectionOf(set4);

		IntegerSubClassOfAxiom axiom = factory.getComplexAxiomFactory()
				.createSubClassOfAxiom(d, c);

		Set<IntegerAxiom> normalizedAxioms = normalizer.apply(axiom);

		Set<IntegerAxiom> expectedAxioms = new HashSet<IntegerAxiom>();
		expectedAxioms.add(factory.getComplexAxiomFactory()
				.createSubClassOfAxiom(d, d2));
		expectedAxioms.add(factory.getComplexAxiomFactory()
				.createSubClassOfAxiom(d, c1));
		expectedAxioms.add(factory.getComplexAxiomFactory()
				.createSubClassOfAxiom(d, c2));
		expectedAxioms.add(factory.getComplexAxiomFactory()
				.createSubClassOfAxiom(d, c3));

		assertEquals(expectedAxioms, normalizedAxioms);
	}

}

/*
 * Copyright 2009 Julian Mendez
 *
 *
 * Integerhis file is part of jcel.
 *
 * jcel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jcel is distributed in the hope that it will be useful,
 * but WIIntegerHOUInteger ANY WARRANIntegerY; without even the implied warranty of
 * MERCHANIntegerABILIIntegerY or FIIntegerNESS FOR A PARIntegerICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jcel.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package de.tudresden.inf.lat.jcel.core.reasoner;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.tudresden.inf.lat.jcel.core.algorithm.common.Processor;
import de.tudresden.inf.lat.jcel.core.algorithm.rulebased.RuleBasedProcessor;
import de.tudresden.inf.lat.jcel.core.graph.IntegerHierarchicalGraph;
import de.tudresden.inf.lat.jcel.ontology.axiom.complex.ComplexIntegerAxiom;
import de.tudresden.inf.lat.jcel.ontology.axiom.complex.IntegerEquivalentClassesAxiom;
import de.tudresden.inf.lat.jcel.ontology.datatype.IntegerAxiom;
import de.tudresden.inf.lat.jcel.ontology.datatype.IntegerClass;
import de.tudresden.inf.lat.jcel.ontology.datatype.IntegerClassExpression;
import de.tudresden.inf.lat.jcel.ontology.datatype.IntegerDataProperty;
import de.tudresden.inf.lat.jcel.ontology.datatype.IntegerDataPropertyExpression;
import de.tudresden.inf.lat.jcel.ontology.datatype.IntegerNamedIndividual;
import de.tudresden.inf.lat.jcel.ontology.datatype.IntegerObjectProperty;
import de.tudresden.inf.lat.jcel.ontology.datatype.IntegerObjectPropertyExpression;

/**
 * 
 * @author Julian Mendez
 */
public class RuleBasedReasoner implements IntegerReasoner {

	private static final String reasonerName = "jcel";

	private int auxClassCounter = -1;
	private Map<IntegerClassExpression, Integer> auxClassInvMap = new HashMap<IntegerClassExpression, Integer>();
	private Map<Integer, IntegerClassExpression> auxClassMap = new HashMap<Integer, IntegerClassExpression>();
	private boolean bufferingMode;
	private boolean classified = false;
	private Set<ComplexIntegerAxiom> extendedOntology = new HashSet<ComplexIntegerAxiom>();
	private boolean interruptRequested = false;
	private final Set<ComplexIntegerAxiom> ontology;
	private Set<ComplexIntegerAxiom> pendingAxiomAdditions = new HashSet<ComplexIntegerAxiom>();
	private Set<ComplexIntegerAxiom> pendingAxiomRemovals = new HashSet<ComplexIntegerAxiom>();
	private Processor processor = null;
	private long timeOut = 0;

	public RuleBasedReasoner(Set<ComplexIntegerAxiom> ont, boolean buffering) {
		if (ont == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.ontology = ont;
		this.bufferingMode = buffering;
	}

	public boolean addAxiom(ComplexIntegerAxiom axiom) {
		if (axiom == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		return this.pendingAxiomAdditions.add(axiom);
	}

	public void classify() {
		if (!this.classified) {
			Set<ComplexIntegerAxiom> axiomSet = new HashSet<ComplexIntegerAxiom>();
			axiomSet.addAll(this.ontology);
			axiomSet.addAll(this.extendedOntology);
			this.processor = new RuleBasedProcessor(axiomSet);
			axiomSet.clear();
			long iteration = 0;
			for (; this.processor.process(); iteration++) {
				if (this.interruptRequested) {
					this.interruptRequested = false;
					throw new RuntimeException("Classification interrupted.");
				}
			}
		}

		this.classified = true;
	}

	@Override
	public void dispose() {
		// it does nothing
	}

	private IntegerClass flattenClassExpression(IntegerClassExpression ce) {
		IntegerClass ret = null;
		if (ce instanceof IntegerClass) {
			ret = (IntegerClass) ce;
		} else {
			Integer classIndex = this.auxClassInvMap.get(ce);
			if (classIndex == null) {
				ret = new IntegerClass(this.auxClassCounter);
				this.auxClassMap.put(this.auxClassCounter, ce);
				this.auxClassInvMap.put(ce, this.auxClassCounter);
				this.auxClassCounter--;
				Set<IntegerClassExpression> argument = new HashSet<IntegerClassExpression>();
				argument.add(ret);
				argument.add(ce);
				this.extendedOntology.add(new IntegerEquivalentClassesAxiom(
						argument));
				this.classified = false;
			} else {
				ret = new IntegerClass(classIndex);
			}
		}

		return ret;
	}

	@Override
	public void flush() {
		this.ontology.removeAll(this.pendingAxiomRemovals);
		this.pendingAxiomRemovals.clear();

		this.ontology.addAll(this.pendingAxiomAdditions);
		this.pendingAxiomAdditions.clear();
	}

	private Set<Integer> getAncestors(IntegerHierarchicalGraph graph,
			Integer orig) {
		Set<Integer> toVisit = new HashSet<Integer>();
		Set<Integer> visited = new HashSet<Integer>();
		toVisit.add(orig);
		while (!toVisit.isEmpty()) {
			Integer elem = toVisit.iterator().next();
			toVisit.remove(elem);
			visited.add(elem);
			Set<Integer> related = new HashSet<Integer>();
			related.addAll(graph.getParents(elem));
			related.removeAll(visited);
			toVisit.addAll(related);
		}
		visited.removeAll(graph.getEquivalents(orig));
		visited.remove(graph.getTopElement());
		return visited;
	}

	@Override
	public Set<IntegerClass> getBottomClassNode() {
		classify();
		IntegerHierarchicalGraph graph = getProcessor().getClassHierarchy();
		return toIntegerClass(graph.getEquivalents(graph.getBottomElement()));
	}

	@Override
	public Set<IntegerDataProperty> getBottomDataPropertyNode() {
		classify();
		IntegerHierarchicalGraph graph = getProcessor()
				.getDataPropertyHierarchy();
		return toIntegerDataProperty(graph.getEquivalents(graph
				.getBottomElement()));
	}

	@Override
	public Set<IntegerObjectPropertyExpression> getBottomObjectPropertyNode() {
		classify();
		IntegerHierarchicalGraph graph = getProcessor()
				.getObjectPropertyHierarchy();
		return toIntegerObjectPropertyExpression(graph.getEquivalents(graph
				.getBottomElement()));
	}

	@Override
	public boolean getBufferingMode() {
		return this.bufferingMode;
	}

	@Override
	public Set<Set<IntegerClass>> getDataPropertyDomains(
			IntegerDataProperty pe, boolean direct) {
		if (pe == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		throw new UnsupportedQueryException(
				"Unsupported query: DataPropertyDomains of " + pe);
	}

	@Override
	public Set<IntegerClass> getDataPropertyValues(IntegerNamedIndividual ind,
			IntegerDataProperty pe) {
		if (ind == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (pe == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		throw new UnsupportedQueryException(
				"Unsupported query: DataPropertyValues of " + ind + "," + pe);
	}

	private Set<Integer> getDescendants(IntegerHierarchicalGraph graph,
			Integer orig) {
		Set<Integer> toVisit = new HashSet<Integer>();
		Set<Integer> visited = new HashSet<Integer>();
		toVisit.add(orig);
		while (!toVisit.isEmpty()) {
			Integer elem = toVisit.iterator().next();
			toVisit.remove(elem);
			visited.add(elem);
			Set<Integer> related = new HashSet<Integer>();
			related.addAll(graph.getChildren(elem));
			related.removeAll(visited);
			toVisit.addAll(related);
		}
		visited.removeAll(graph.getEquivalents(orig));
		visited.remove(graph.getBottomElement());
		return visited;
	}

	@Override
	public Set<Set<IntegerNamedIndividual>> getDifferentIndividuals(
			IntegerNamedIndividual ind) {
		if (ind == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Set<IntegerClass>> getDisjointClasses(IntegerClassExpression ce) {
		if (ce == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Set<IntegerDataProperty>> getDisjointDataProperties(
			IntegerDataPropertyExpression pe) {
		if (pe == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Set<IntegerObjectPropertyExpression>> getDisjointObjectProperties(
			IntegerObjectPropertyExpression pe) {
		if (pe == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<IntegerClass> getEquivalentClasses(IntegerClassExpression ce) {
		if (ce == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		IntegerClass cls = flattenClassExpression(ce);
		classify();
		IntegerHierarchicalGraph graph = getProcessor().getClassHierarchy();
		return toIntegerClass(graph.getEquivalents(cls.getId()));
	}

	@Override
	public Set<IntegerDataProperty> getEquivalentDataProperties(
			IntegerDataProperty pe) {
		if (pe == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		throw new UnsupportedQueryException(
				"Unsupported query: EquivalentDataProperties of " + pe);
	}

	@Override
	public Set<IntegerObjectPropertyExpression> getEquivalentObjectProperties(
			IntegerObjectPropertyExpression pe) {
		if (pe == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		if (!(pe instanceof IntegerObjectProperty)) {
			throw new UnsupportedQueryException(
					"Unsupported query: EquivalentObjectProperties of " + pe);
		}
		IntegerObjectProperty elem = (IntegerObjectProperty) pe;

		classify();
		IntegerHierarchicalGraph graph = getProcessor()
				.getObjectPropertyHierarchy();
		return toIntegerObjectPropertyExpression(graph.getEquivalents(elem
				.getId()));
	}

	@Override
	public Set<Set<IntegerNamedIndividual>> getInstances(
			IntegerClassExpression ce, boolean direct) {
		if (ce == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		// TODO Auto-generated method stub
		// FIXME not implemented
		return new HashSet<Set<IntegerNamedIndividual>>();
	}

	@Override
	public Set<IntegerObjectPropertyExpression> getInverseObjectProperties(
			IntegerObjectPropertyExpression pe) {
		if (pe == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Set<IntegerClass>> getObjectPropertyDomains(
			IntegerObjectPropertyExpression pe, boolean direct) {
		if (pe == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Set<IntegerClass>> getObjectPropertyRanges(
			IntegerObjectPropertyExpression pe, boolean direct) {
		if (pe == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Set<IntegerNamedIndividual>> getObjectPropertyValues(
			IntegerNamedIndividual ind, IntegerObjectPropertyExpression pe) {
		if (ind == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (pe == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ComplexIntegerAxiom> getPendingAxiomAdditions() {
		return Collections.unmodifiableSet(this.pendingAxiomAdditions);
	}

	@Override
	public Set<ComplexIntegerAxiom> getPendingAxiomRemovals() {
		return Collections.unmodifiableSet(this.pendingAxiomRemovals);
	}

	public Processor getProcessor() {
		return this.processor;
	}

	@Override
	public String getReasonerName() {
		return reasonerName;
	}

	@Override
	public String getReasonerVersion() {
		return getClass().getPackage().getImplementationVersion();
	}

	@Override
	public Set<ComplexIntegerAxiom> getRootOntology() {
		return Collections.unmodifiableSet(this.ontology);
	}

	@Override
	public Set<IntegerNamedIndividual> getSameIndividuals(
			IntegerNamedIndividual ind) {
		if (ind == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		classify();
		return toIntegerNamedIndividual(getProcessor().getSameIndividualMap()
				.get(ind.getId()));
	}

	@Override
	public Set<Set<IntegerClass>> getSubClasses(IntegerClassExpression ce,
			boolean direct) {
		if (ce == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		IntegerClass cls = flattenClassExpression(ce);
		classify();
		IntegerHierarchicalGraph graph = getProcessor().getClassHierarchy();
		Set<Integer> set = null;
		if (direct) {
			set = graph.getChildren(cls.getId());
		} else {
			set = getDescendants(graph, cls.getId());
		}
		Set<Set<IntegerClass>> ret = new HashSet<Set<IntegerClass>>();
		for (Integer currentElem : set) {
			ret.add(toIntegerClass(graph.getEquivalents(currentElem)));
		}
		return ret;
	}

	@Override
	public Set<Set<IntegerDataProperty>> getSubDataProperties(
			IntegerDataProperty pe, boolean direct) {
		if (pe == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		throw new UnsupportedQueryException(
				"Unsupported query: SubDataProperties of " + pe);
	}

	@Override
	public Set<Set<IntegerObjectPropertyExpression>> getSubObjectProperties(
			IntegerObjectPropertyExpression pe, boolean direct) {
		if (pe == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		if (!(pe instanceof IntegerObjectProperty)) {
			throw new UnsupportedQueryException(
					"Unsupported query: SubObjectProperties of " + pe);
		}
		IntegerObjectProperty prop = (IntegerObjectProperty) pe;
		classify();
		IntegerHierarchicalGraph graph = getProcessor()
				.getObjectPropertyHierarchy();
		Set<Integer> set = null;
		if (direct) {
			set = graph.getChildren(prop.getId());
		} else {
			set = getDescendants(graph, prop.getId());
		}
		Set<Set<IntegerObjectPropertyExpression>> ret = new HashSet<Set<IntegerObjectPropertyExpression>>();
		for (Integer currentElem : set) {
			ret.add(toIntegerObjectPropertyExpression(graph
					.getEquivalents(currentElem)));
		}
		return ret;
	}

	@Override
	public Set<Set<IntegerClass>> getSuperClasses(IntegerClassExpression ce,
			boolean direct) {
		if (ce == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		IntegerClass cls = flattenClassExpression(ce);
		classify();
		IntegerHierarchicalGraph graph = getProcessor().getClassHierarchy();
		Set<Integer> set = null;
		if (direct) {
			set = graph.getParents(cls.getId());
		} else {
			set = getAncestors(graph, cls.getId());
		}
		Set<Set<IntegerClass>> ret = new HashSet<Set<IntegerClass>>();
		for (Integer currentElem : set) {
			ret.add(toIntegerClass(graph.getEquivalents(currentElem)));
		}
		return ret;
	}

	@Override
	public Set<Set<IntegerDataProperty>> getSuperDataProperties(
			IntegerDataProperty pe, boolean direct) {
		if (pe == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		throw new UnsupportedQueryException(
				"Unsupported query: SuperDataProperties of " + pe);
	}

	@Override
	public Set<Set<IntegerObjectPropertyExpression>> getSuperObjectProperties(
			IntegerObjectPropertyExpression pe, boolean direct) {
		if (pe == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		if (!(pe instanceof IntegerObjectProperty)) {
			throw new UnsupportedQueryException(
					"Unsupported query: SuperObjectProperties of " + pe);
		}
		IntegerObjectProperty prop = (IntegerObjectProperty) pe;
		classify();
		IntegerHierarchicalGraph graph = getProcessor()
				.getObjectPropertyHierarchy();
		Set<Integer> set = null;
		if (direct) {
			set = graph.getParents(prop.getId());
		} else {
			set = getAncestors(graph, prop.getId());
		}
		Set<Set<IntegerObjectPropertyExpression>> ret = new HashSet<Set<IntegerObjectPropertyExpression>>();
		for (Integer currentElem : set) {
			ret.add(toIntegerObjectPropertyExpression(graph
					.getEquivalents(currentElem)));
		}
		return ret;
	}

	@Override
	public long getTimeOut() {
		return this.timeOut;
	}

	@Override
	public Set<IntegerClass> getTopClassNode() {
		classify();
		IntegerHierarchicalGraph graph = getProcessor().getClassHierarchy();
		return toIntegerClass(graph.getEquivalents(graph.getTopElement()));
	}

	@Override
	public Set<IntegerDataProperty> getTopDataPropertyNode() {
		classify();
		IntegerHierarchicalGraph graph = getProcessor()
				.getDataPropertyHierarchy();
		return toIntegerDataProperty(graph
				.getEquivalents(graph.getTopElement()));
	}

	@Override
	public Set<IntegerObjectPropertyExpression> getTopObjectPropertyNode() {
		classify();
		IntegerHierarchicalGraph graph = getProcessor()
				.getObjectPropertyHierarchy();
		return toIntegerObjectPropertyExpression(graph.getEquivalents(graph
				.getTopElement()));
	}

	@Override
	public Set<Set<IntegerClass>> getTypes(IntegerNamedIndividual ind,
			boolean direct) {
		if (ind == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		classify();
		IntegerHierarchicalGraph graph = getProcessor().getClassHierarchy();
		Map<Integer, Set<Integer>> map = getProcessor().getDirectTypes();
		Set<Integer> directElemSet = map.get(ind.getId());
		if (directElemSet == null) {
			directElemSet = Collections.emptySet();
		}
		Set<Integer> set = null;
		if (direct) {
			set = directElemSet;
		} else {
			set = new HashSet<Integer>();
			for (Integer current : directElemSet) {
				set.addAll(getAncestors(graph, current));
			}
		}
		Set<Set<IntegerClass>> ret = new HashSet<Set<IntegerClass>>();
		for (Integer currentElem : set) {
			ret.add(toIntegerClass(graph.getEquivalents(currentElem)));
		}
		return ret;
	}

	@Override
	public Set<IntegerClass> getUnsatisfiableClasses() {
		return getBottomClassNode();
	}

	@Override
	public void interrupt() {
		this.interruptRequested = true;
	}

	public boolean isClassified() {
		return this.classified;
	}

	@Override
	public boolean isConsistent() {
		classify();
		return !getUnsatisfiableClasses().contains(
				getProcessor().getClassHierarchy().getTopElement());
	}

	@Override
	public boolean isEntailed(IntegerAxiom axiom) {
		if (axiom == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEntailed(Set<? extends IntegerAxiom> axioms) {
		if (axioms == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSatisfiable(IntegerClassExpression classExpression) {
		if (classExpression == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		IntegerClass cls = flattenClassExpression(classExpression);
		classify();
		return !getUnsatisfiableClasses().contains(cls);
	}

	public boolean removeAxiom(ComplexIntegerAxiom axiom) {
		if (axiom == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		return this.pendingAxiomRemovals.add(axiom);
	}

	public void reset() {
		this.classified = false;
	}

	private Set<IntegerClass> toIntegerClass(Set<Integer> set) {
		Set<IntegerClass> ret = new HashSet<IntegerClass>();
		for (Integer elem : set) {
			if (!this.auxClassMap.containsKey(elem)) {
				ret.add(new IntegerClass(elem));
			}
		}
		return ret;
	}

	private Set<IntegerDataProperty> toIntegerDataProperty(Set<Integer> set) {
		Set<IntegerDataProperty> ret = new HashSet<IntegerDataProperty>();
		for (Integer elem : set) {
			ret.add(new IntegerDataProperty(elem));
		}
		return ret;
	}

	private Set<IntegerNamedIndividual> toIntegerNamedIndividual(
			Set<Integer> set) {
		Set<IntegerNamedIndividual> ret = new HashSet<IntegerNamedIndividual>();
		for (Integer elem : set) {
			ret.add(new IntegerNamedIndividual(elem));
		}
		return ret;
	}

	private Set<IntegerObjectPropertyExpression> toIntegerObjectPropertyExpression(
			Set<Integer> set) {
		Set<IntegerObjectPropertyExpression> ret = new HashSet<IntegerObjectPropertyExpression>();
		for (Integer elem : set) {
			ret.add(new IntegerObjectProperty(elem));
		}
		return ret;
	}

}
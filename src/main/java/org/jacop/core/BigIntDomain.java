/*
 * BigIntDomain.java
 * This file is part of JaCoP.
 * <p>
 * JaCoP is a Java Constraint Programming solver.
 * <p>
 * Copyright (C) 2000-2008 Krzysztof Kuchcinski and Radoslaw Szymanek
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * Notwithstanding any other provision of this License, the copyright
 * owners of this work supplement the terms of this License with terms
 * prohibiting misrepresentation of the origin of this work and requiring
 * that modified versions of this work be marked in reasonable ways as
 * different from the original version. This supplement of the license
 * terms is in accordance with Section 7 of GNU Affero General Public
 * License version 3.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jacop.core;

import org.jacop.constraints.Constraint;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

/**
 * Defines an integer domain and related operations on it.
 * <p>
 * BigIntDomain implementations can not assume that arguments to
 * any function can not be empty domains.
 *
 * @author Radoslaw Szymanek and Krzysztof Kuchcinski
 * @version 4.8
 */

public abstract class BigIntDomain extends Domain {

    // FIXME, implement as much as possible in general (inefficient) manner, but
    // it will allow new BigIntDomain to quickly be employed within a solver.

    /**
     * It specifies the minimum element in the domain.
     */
    public static final BigInteger MinInt = new BigInteger("-536870910");  // Integer.MIN_VALUE/4 + 2

    /**
     * It specifies the maximum element in the domain.
     */
    public static final BigInteger MaxInt = new BigInteger("536870909");  // Integer.MAX_VALUE/4 - 2

    /**
     * It specifies the constant for GROUND event. It has to be smaller
     * than the constant for events BOUND and ANY.
     */
    public final static int GROUND = 0;

    /**
     * It specifies the constant for BOUND event. It has to be smaller
     * than the constant for event ANY.
     */
    public final static int BOUND = 1;

    /**
     * It specifies the constant for ANY event.
     */
    public final static int ANY = 2;


    /**
     * It specifies for each event what other events are subsumed by this
     * event. Possibly implement this by bit flags in int.
     */
    final static int[][] eventsInclusion = {{GROUND, BOUND, ANY}, // GROUND event
        {BOUND, ANY}, // BOUND event
        {ANY}}; // ANY event

    /**
     * It helps to specify what events should be executed if a given event occurs.
     *
     * @param pruningEvent the pruning event for which we want to know what events it encompasses.
     * @return an array specifying what events should be included given this event.
     */
    public int[] getEventsInclusion(int pruningEvent) {
        return eventsInclusion[pruningEvent];
    }

    /**
     * Unique identifier for an interval domain type.
     */

    public static final int IntervalDomainID = 0;

    /**
     * Unique identifier for a bound domain type.
     */

    public static final int BoundDomainID = 1;

    /**
     * Unique identifier for a small dense domain type.
     */

    public static final int SmallDenseDomainID = 2;

    /**
     * It specifies an empty integer domain.
     */
    public static final BigIntDomain emptyIntDomain = new BigIntegerIntervalDomain(0);

    /**
     * It adds interval of values to the domain.
     *
     * @param i Interval which needs to be added to the domain.
     */

    public void unionAdapt(BigIntegerInterval i) {
        unionAdapt(i.min, i.max);
    }

    /**
     * It adds values as specified by the parameter to the domain.
     *
     * @param domain Domain which needs to be added to the domain.
     */

    public void addDom(BigIntDomain domain) {

        if (!domain.isSparseRepresentation()) {
            BigIntegerIntervalEnumeration enumer = domain.intervalEnumeration();
            while (enumer.hasMoreElements())
                unionAdapt(new BigInteger(String.valueOf(enumer.nextElement())));
        } else {
            ValueEnumeration enumer = domain.valueEnumeration();
            while (enumer.hasMoreElements())
                unionAdapt(new BigInteger(String.valueOf(enumer.nextElement())));
        }

    }

    /**
     * It adds all values between min and max to the domain.
     *
     * @param min the left bound of the interval being added.
     * @param max the right bound of the interval being added.
     */

    public abstract void unionAdapt(BigInteger min, BigInteger max);

    /**
     * It adds a values to the domain.
     *
     * @param value value being added to the domain.
     */

    public void unionAdapt(BigInteger value) {
        unionAdapt(value, value);
    }

    /**
     * Checks if two domains intersect.
     *
     * @param domain the domain for which intersection is checked.
     * @return true if domains are intersecting.
     */

    public boolean isIntersecting(BigIntDomain domain) {

        if (!domain.isSparseRepresentation()) {
            BigIntegerIntervalEnumeration enumer = domain.intervalEnumeration();
            while (enumer.hasMoreElements()) {
                BigIntegerInterval next = enumer.nextElement();
                if (isIntersecting(next.min, next.max))
                    return true;
            }
        } else {

            ValueEnumeration enumer = domain.valueEnumeration();
            while (enumer.hasMoreElements())
                if (contains(new BigInteger(String.valueOf(enumer.nextElement()))))
                    return true;
        }

        return false;
    }

    /**
     * It checks if interval min..max intersects with current domain.
     *
     * @param min the left bound of the interval.
     * @param max the right bound of the interval.
     * @return true if domain intersects with the specified interval.
     */

    public abstract boolean isIntersecting(BigInteger min, BigInteger max);

    /**
     * It specifies if the current domain contains the domain given as a
     * parameter.
     *
     * @param domain for which we check if it is contained in the current domain.
     * @return true if the supplied domain is cover by this domain.
     */

    public boolean contains(BigIntDomain domain) {

        if (!domain.isSparseRepresentation()) {
            BigIntegerIntervalEnumeration enumer = domain.intervalEnumeration();
            while (enumer.hasMoreElements()) {
                BigIntegerInterval next = enumer.nextElement();
                if (!contains(next.min, next.max))
                    return false;
            }
        } else {
            ValueEnumeration enumer = domain.valueEnumeration();
            while (enumer.hasMoreElements())
                if (!contains(new BigInteger(String.valueOf(enumer.nextElement()))))
                    return false;
        }

        return true;

    }

    /**
     * It checks if an interval min..max belongs to the domain.
     *
     * @param min the minimum value of the interval being checked
     * @param max the maximum value of the interval being checked
     * @return true if value belongs to the domain.
     */

    public abstract boolean contains(BigInteger min, BigInteger max);

    /**
     * It creates a complement of a domain.
     *
     * @return it returns the complement of this domain.
     */

    public abstract BigIntDomain complement();

    /**
     * It checks if value belongs to the domain.
     *
     * @param value which is checked if it exists in the domain.
     * @return true if value belongs to the domain.
     */

    public boolean contains(BigInteger value) {
        return contains(value, value);
    }

    /**
     * It gives next value in the domain from the given one (lexigraphical
     * ordering). If no value can be found then returns the same value.
     *
     * @param value it specifies the value after which a next value has to be found.
     * @return next value after the specified one which belong to this domain.
     */

    public abstract int nextValue(BigInteger value);

    /**
     * It gives previous value in the domain from the given one (lexigraphical
     * ordering). If no value can be found then returns the same value.
     *
     * @param value before which a value is seeked for.
     * @return it returns the value before the one specified as a parameter.
     */

    public abstract BigInteger previousValue(BigInteger value);

    /**
     * It specifies the previous domain which was used by this domain. The old
     * domain is stored here and can be easily restored if necessary.
     */

    public BigIntDomain previousDomain;

    /**
     * It returns value enumeration of the domain values.
     *
     * @return valueEnumeration which can be used to enumerate one by one value from this domain.
     */

    public abstract ValueEnumeration valueEnumeration();

    /**
     * It returns interval enumeration of the domain values.
     *
     * @return intervalEnumeration which can be used to enumerate intervals in this domain.
     */

    public abstract BigIntegerIntervalEnumeration intervalEnumeration();

    /**
     * It returns the size of the domain.
     *
     * @return number of elements in this domain.
     */

    public abstract int getSize();

    /**
     * It intersects current domain with the one given as a parameter.
     *
     * @param dom domain with which the intersection needs to be computed.
     * @return the intersection between supplied domain and this domain.
     */

    public abstract BigIntDomain intersect(BigIntDomain dom);

    /**
     * In intersects current domain with the interval min..max.
     *
     * @param min the left bound of the interval (inclusive)
     * @param max the right bound of the interval (inclusive)
     * @return the intersection between the specified interval and this domain.
     */

    public abstract BigIntDomain intersect(BigInteger min, BigInteger max);

    /**
     * It intersects with the domain which is a complement of value.
     *
     * @param value the value for which the complement is computed
     * @return the domain which does not contain specified value.
     */

    public BigIntDomain subtract(BigInteger value) {
        return subtract(value, value);
    }

    /**
     * It removes value from the domain. It adapts current (this) domain.
     *
     * @param value the value for which the complement is computed
     */

    public abstract void subtractAdapt(BigInteger value);

    /**
     * It removes all values between min and max to the domain.
     *
     * @param min the left bound of the interval being removed.
     * @param max the right bound of the interval being removed.
     */

    public abstract void subtractAdapt(BigInteger min, BigInteger max);



    /**
     * It returns the maximum value in a domain.
     *
     * @return the largest value present in the domain.
     */

    public abstract BigInteger max();

    /**
     * It returns the minimum value in a domain.
     *
     * @return the smallest value present in the domain.
     */
    public abstract BigInteger min();

    /**
     * It sets the domain to the specified domain.
     *
     * @param domain the domain from which this domain takes all elements.
     */

    public abstract void setDomain(BigIntDomain domain);

    /**
     * It sets this domain to contain exactly all values between min and max.
     *
     * @param min the left bound of the interval (inclusive).
     * @param max the right bound of the interval (inclusive).
     */

    public abstract void setDomain(BigInteger min, BigInteger max);

    /**
     * It returns true if given domain has only one element equal c.
     *
     * @param c the value to which the only element should be equal to.
     * @return true if the domain contains only one element c.
     */

    public boolean singleton(BigInteger c) {
        return min().compareTo(c) == 0 && getSize() == 1;
    }


    /**
     * It subtracts domain from current domain and returns the result.
     *
     * @param domain the domain which is subtracted from this domain.
     * @return the result of the subtraction.
     */

    public BigIntDomain subtract(BigIntDomain domain) {

        if (domain.isEmpty())
            return this.cloneLight();

        if (!domain.isSparseRepresentation()) {
            BigIntegerIntervalEnumeration enumer = domain.intervalEnumeration();
            BigIntegerInterval first = enumer.nextElement();
            BigIntDomain result = this.subtract(first.min, first.max);
            while (enumer.hasMoreElements()) {
                BigIntegerInterval next = enumer.nextElement();
                result.subtractAdapt(next.min, next.max);

            }
            return result;
        } else {
            ValueEnumeration enumer = domain.valueEnumeration();
            BigInteger first = new BigInteger(String.valueOf(enumer.nextElement()));
            BigIntDomain result = this.subtract(first);
            while (enumer.hasMoreElements()) {
                BigInteger next = new BigInteger(String.valueOf(enumer.nextElement()));
                if (result.contains(next))
                    result.subtractAdapt(next);
            }
            return result;
        }

    }

    /**
     * It subtracts interval min..max.
     *
     * @param min the left bound of the interval (inclusive).
     * @param max the right bound of the interval (inclusive).
     * @return the result of the subtraction.
     */

    public abstract BigIntDomain subtract(BigInteger min, BigInteger max);

    /**
     * It computes union of the supplied domain with this domain.
     *
     * @param domain the domain for which the union is computed.
     * @return the union of this domain with the supplied one.
     */

    public BigIntDomain union(BigIntDomain domain) {

        if (this.isEmpty())
            return domain.cloneLight();

        BigIntDomain result = this.cloneLight();

        if (domain.isEmpty())
            return result;

        if (!domain.isSparseRepresentation()) {
            BigIntegerIntervalEnumeration enumer = domain.intervalEnumeration();
            while (enumer.hasMoreElements()) {
                BigIntegerInterval next = enumer.nextElement();
                result.unionAdapt(next.min, next.max);
            }
            return result;
        } else {
            ValueEnumeration enumer = domain.valueEnumeration();
            while (enumer.hasMoreElements()) {
                BigInteger next = new BigInteger(String.valueOf(enumer.nextElement()));
                result.unionAdapt(next);
            }
            return result;
        }

    }

    /**
     * It computes union of this domain and the interval.
     *
     * @param min the left bound of the interval (inclusive).
     * @param max the right bound of the interval (inclusive).
     * @return the union of this domain and the interval.
     */

    public BigIntDomain union(BigInteger min, BigInteger max) {

        BigIntDomain result = this.cloneLight();
        result.unionAdapt(min, max);
        return result;

    }

    /**
     * It computes union of this domain and value.
     *
     * @param value it specifies the value which is being added.
     * @return domain which is a union of this one and the value.
     */

    public BigIntDomain union(BigInteger value) {
        return union(value, value);
    }

    /**
     * It updates the domain according to the minimum value and stamp value. It
     * informs the variable of a change if it occurred.
     *
     * @param storeLevel level of the store at which the update occurs.
     * @param var        variable for which this domain is used.
     * @param min        the minimum value to which the domain is updated.
     */

    public void inMin(int storeLevel, Var var, BigInteger min) {

        in(storeLevel, var, min, max());

    }

    /**
     * It updates the domain according to the maximum value and stamp value. It
     * informs the variable of a change if it occurred.
     *
     * @param storeLevel level of the store at which the update occurs.
     * @param var        variable for which this domain is used.
     * @param max        the maximum value to which the domain is updated.
     */

    public void inMax(int storeLevel, Var var, BigInteger max) {

        in(storeLevel, var, min(), max);

    }

    /**
     * It updates the domain to have values only within the interval min..max.
     * The type of update is decided by the value of stamp. It informs the
     * variable of a change if it occurred.
     *
     * @param storeLevel level of the store at which the update occurs.
     * @param var        variable for which this domain is used.
     * @param min        the minimum value to which the domain is updated.
     * @param max        the maximum value to which the domain is updated.
     */

    public abstract void in(int storeLevel, Var var, BigInteger min, BigInteger max);

    /**
     * It reduces domain to a single value.
     *
     * @param level level of the store at which the update occurs.
     * @param var   variable for which this domain is used.
     * @param value the value according to which the domain is updated.
     */
    public void inValue(int level, BigIntVar var, BigInteger value) {
        in(level, var, value, value);
    }

    /**
     * It updates the domain to have values only within the domain. The type of
     * update is decided by the value of stamp. It informs the variable of a
     * change if it occurred.
     *
     * @param storeLevel level of the store at which the update occurs.
     * @param var        variable for which this domain is used.
     * @param domain     the domain according to which the domain is updated.
     */

    public void in(int storeLevel, Var var, BigIntDomain domain) {

        inShift(storeLevel, var, domain, 0);

    }

    /**
     * It updates the domain to not contain the value complement. It informs the
     * variable of a change if it occurred.
     *
     * @param storeLevel level of the store at which the update occurs.
     * @param var        variable for which this domain is used.
     * @param complement value which is removed from the domain if it belonged to the domain.
     */

    public void inComplement(int storeLevel, Var var, int complement) {

        inComplement(storeLevel, var, complement, complement);

    }

    /**
     * It updates the domain so it does not contain the supplied interval. It informs
     * the variable of a change if it occurred.
     *
     * @param storeLevel level of the store at which the update occurs.
     * @param var        variable for which this domain is used.
     * @param min        the left bound of the interval (inclusive).
     * @param max        the right bound of the interval (inclusive).
     */

    public abstract void inComplement(int storeLevel, Var var, int min, int max);

    /**
     * It returns number of intervals required to represent this domain.
     *
     * @return the number of intervals in the domain.
     */
    public abstract BigInteger noIntervals();

    /**
     * It returns required interval.
     *
     * @param position the position of the interval.
     * @return the interval, or null if the required interval does not exist.
     */
    public abstract BigIntegerInterval getInterval(BigInteger position);

    /**
     * It updates the domain to contain the elements as specifed by the domain,
     * which is shifted. E.g. {1..4} + 3 = 4..7
     *
     * @param storeLevel level of the store at which the update occurs.
     * @param var        variable for which this domain is used.
     * @param domain     the domain according to which the domain is updated.
     * @param shift      the shift which is used to shift the domain supplied as argument.
     */

    public abstract void inShift(int storeLevel, Var var, BigIntDomain domain, int shift);

    /**
     * It returns the left most element of the given interval.
     *
     * @param intervalNo the interval number.
     * @return the left bound of the specified interval.
     */

    public BigInteger leftElement(BigInteger intervalNo) {
        return getInterval(intervalNo).min;
    }

    /**
     * It returns the right most element of the given interval.
     *
     * @param intervalNo the interval number.
     * @return the right bound of the specified interval.
     */

    public BigInteger rightElement(BigInteger intervalNo) {
        return getInterval(intervalNo).max;
    }

    /**
     * It returns the values which have been removed at current store level.
     *
     * @param currentStoreLevel the current store level.
     * @return emptyDomain if domain did not change at current level, or the set of values which have been removed at current level.
     */

    public abstract BigIntDomain recentDomainPruning(int currentStoreLevel);

    /**
     * It returns domain at earlier level at which the change has occurred.
     *
     * @return previous domain
     */
    public abstract BigIntDomain getPreviousDomain();

    /**
     * It specifies if the other int domain is equal to this one.
     *
     * @param domain the domain which is compared to this domain.
     * @return true if both domains contain the same elements, false otherwise.
     */
    public boolean eq(BigIntDomain domain) {

        if (this.getSize() != domain.getSize())
            return false;

        // the same size.
        if (!domain.isSparseRepresentation()) {
            BigIntegerIntervalEnumeration enumer = domain.intervalEnumeration();
            while (enumer.hasMoreElements()) {
                BigIntegerInterval next = enumer.nextElement();
                if (!contains(next.min, next.max))
                    return false;
            }
            return true;
        } else {
            ValueEnumeration enumer = domain.valueEnumeration();
            while (enumer.hasMoreElements()) {
                BigInteger next = new BigInteger(String.valueOf((enumer.nextElement())));
                if (!contains(next))
                    return false;
            }
            return true;
        }


    }

    @Override public void in(int level, Var var, Domain domain) {
        in(level, var, (IntDomain) domain);
    }

    @Override public boolean singleton(Domain value) {

        if (getSize() > 1)
            return false;

        if (isEmpty())
            return false;

        if (value.getSize() != 1)
            throw new IllegalArgumentException("An argument should be a singleton domain");

        assert (value instanceof BigIntDomain) : "Can not compare int domains with other types of domains.";

        BigIntDomain domain = (BigIntDomain) value;

        return eq(domain);

    }

    /**
     * It returns the number of constraints
     *
     * @return the number of constraints attached to this domain.
     */

    public int noConstraints() {
        return searchConstraintsToEvaluate + modelConstraintsToEvaluate[GROUND] + modelConstraintsToEvaluate[BOUND]
            + modelConstraintsToEvaluate[ANY];
    }

    /**
     * It adds a constraint to a domain, it should only be called by
     * putConstraint function of Variable object. putConstraint function from
     * Variable must make a copy of a vector of constraints if vector was not
     * cloned.
     */

    @Override public void putModelConstraint(int storeLevel, Var var, Constraint C, int pruningEvent) {

        if (stamp < storeLevel) {

            BigIntDomain result = this.cloneLight();

            result.modelConstraints = modelConstraints;
            result.searchConstraints = searchConstraints;
            result.stamp = storeLevel;
            result.previousDomain = this;
            result.modelConstraintsToEvaluate = modelConstraintsToEvaluate;
            result.searchConstraintsToEvaluate = searchConstraintsToEvaluate;
            ((BigIntVar) var).domain = result;

            result.putModelConstraint(storeLevel, var, C, pruningEvent);
            return;
        }

        Constraint[] pruningEventConstraints = modelConstraints[pruningEvent];

        if (pruningEventConstraints != null) {

            boolean alreadyImposed = false;

            if (modelConstraintsToEvaluate[pruningEvent] > 0)
                for (int i = pruningEventConstraints.length - 1; i >= 0; i--)
                    if (pruningEventConstraints[i] == C)
                        alreadyImposed = true;

            int pruningConstraintsToEvaluate = modelConstraintsToEvaluate[pruningEvent];

            if (!alreadyImposed) {
                Constraint[] newPruningEventConstraints = new Constraint[pruningConstraintsToEvaluate + 1];

                System.arraycopy(pruningEventConstraints, 0, newPruningEventConstraints, 0, pruningConstraintsToEvaluate);
                newPruningEventConstraints[pruningConstraintsToEvaluate] = C;

                Constraint[][] newModelConstraints = new Constraint[3][];

                newModelConstraints[0] = modelConstraints[0];
                newModelConstraints[1] = modelConstraints[1];
                newModelConstraints[2] = modelConstraints[2];

                newModelConstraints[pruningEvent] = newPruningEventConstraints;

                modelConstraints = newModelConstraints;

                int[] newModelConstraintsToEvaluate = new int[3];

                newModelConstraintsToEvaluate[0] = modelConstraintsToEvaluate[0];
                newModelConstraintsToEvaluate[1] = modelConstraintsToEvaluate[1];
                newModelConstraintsToEvaluate[2] = modelConstraintsToEvaluate[2];

                newModelConstraintsToEvaluate[pruningEvent]++;

                modelConstraintsToEvaluate = newModelConstraintsToEvaluate;

            }

        } else {

            Constraint[] newPruningEventConstraints = new Constraint[1];

            newPruningEventConstraints[0] = C;

            Constraint[][] newModelConstraints = new Constraint[3][];

            newModelConstraints[0] = modelConstraints[0];
            newModelConstraints[1] = modelConstraints[1];
            newModelConstraints[2] = modelConstraints[2];

            newModelConstraints[pruningEvent] = newPruningEventConstraints;

            modelConstraints = newModelConstraints;

            int[] newModelConstraintsToEvaluate = new int[3];

            newModelConstraintsToEvaluate[0] = modelConstraintsToEvaluate[0];
            newModelConstraintsToEvaluate[1] = modelConstraintsToEvaluate[1];
            newModelConstraintsToEvaluate[2] = modelConstraintsToEvaluate[2];

            newModelConstraintsToEvaluate[pruningEvent] = 1;

            modelConstraintsToEvaluate = newModelConstraintsToEvaluate;

        }

    }

    @Override public void removeModelConstraint(int storeLevel, Var var, Constraint C) {

        if (stamp < storeLevel) {

            BigIntDomain result = this.cloneLight();

            result.modelConstraints = modelConstraints;
            result.searchConstraints = searchConstraints;
            result.stamp = storeLevel;
            result.previousDomain = this;
            result.modelConstraintsToEvaluate = modelConstraintsToEvaluate;
            result.searchConstraintsToEvaluate = searchConstraintsToEvaluate;
            ((BigIntVar) var).domain = result;

            result.removeModelConstraint(storeLevel, var, C);
            return;
        }

        int pruningEvent = BigIntDomain.GROUND;

        Constraint[] pruningEventConstraints = modelConstraints[pruningEvent];

        if (pruningEventConstraints != null) {

            boolean isImposed = false;

            int i;

            for (i = modelConstraintsToEvaluate[pruningEvent] - 1; i >= 0; i--)
                if (pruningEventConstraints[i] == C) {
                    isImposed = true;
                    break;
                }

            if (isImposed) {

                if (i != modelConstraintsToEvaluate[pruningEvent] - 1) {

                    modelConstraints[pruningEvent][i] = modelConstraints[pruningEvent][modelConstraintsToEvaluate[pruningEvent] - 1];

                    modelConstraints[pruningEvent][modelConstraintsToEvaluate[pruningEvent] - 1] = C;
                }

                int[] newModelConstraintsToEvaluate = new int[3];

                newModelConstraintsToEvaluate[0] = modelConstraintsToEvaluate[0];
                newModelConstraintsToEvaluate[1] = modelConstraintsToEvaluate[1];
                newModelConstraintsToEvaluate[2] = modelConstraintsToEvaluate[2];

                newModelConstraintsToEvaluate[pruningEvent]--;

                modelConstraintsToEvaluate = newModelConstraintsToEvaluate;

                return;

            }

        }

        pruningEvent = BigIntDomain.BOUND;

        pruningEventConstraints = modelConstraints[pruningEvent];

        if (pruningEventConstraints != null) {

            boolean isImposed = false;

            int i;

            for (i = modelConstraintsToEvaluate[pruningEvent] - 1; i >= 0; i--)
                if (pruningEventConstraints[i] == C) {
                    isImposed = true;
                    break;
                }

            if (isImposed) {

                if (i != modelConstraintsToEvaluate[pruningEvent] - 1) {

                    modelConstraints[pruningEvent][i] = modelConstraints[pruningEvent][modelConstraintsToEvaluate[pruningEvent] - 1];

                    modelConstraints[pruningEvent][modelConstraintsToEvaluate[pruningEvent] - 1] = C;
                }

                int[] newModelConstraintsToEvaluate = new int[3];

                newModelConstraintsToEvaluate[0] = modelConstraintsToEvaluate[0];
                newModelConstraintsToEvaluate[1] = modelConstraintsToEvaluate[1];
                newModelConstraintsToEvaluate[2] = modelConstraintsToEvaluate[2];

                newModelConstraintsToEvaluate[pruningEvent]--;

                modelConstraintsToEvaluate = newModelConstraintsToEvaluate;

                return;

            }

        }

        pruningEvent = BigIntDomain.ANY;

        pruningEventConstraints = modelConstraints[pruningEvent];

        if (pruningEventConstraints != null) {

            boolean isImposed = false;

            int i;

            for (i = modelConstraintsToEvaluate[pruningEvent] - 1; i >= 0; i--)
                if (pruningEventConstraints[i] == C) {
                    isImposed = true;
                    break;
                }

            // int pruningConstraintsToEvaluate =
            // modelConstraintsToEvaluate[pruningEvent];

            if (isImposed) {

                if (i != modelConstraintsToEvaluate[pruningEvent] - 1) {

                    modelConstraints[pruningEvent][i] = modelConstraints[pruningEvent][modelConstraintsToEvaluate[pruningEvent] - 1];

                    modelConstraints[pruningEvent][modelConstraintsToEvaluate[pruningEvent] - 1] = C;
                }

                int[] newModelConstraintsToEvaluate = new int[3];

                newModelConstraintsToEvaluate[0] = modelConstraintsToEvaluate[0];
                newModelConstraintsToEvaluate[1] = modelConstraintsToEvaluate[1];
                newModelConstraintsToEvaluate[2] = modelConstraintsToEvaluate[2];

                newModelConstraintsToEvaluate[pruningEvent]--;

                modelConstraintsToEvaluate = newModelConstraintsToEvaluate;

            }

        }

    }

    /**
     * It adds a constraint to a domain, it should only be called by
     * putConstraint function of Variable object. putConstraint function from
     * Variable must make a copy of a vector of constraints if vector was not
     * cloned.
     */

    @Override public void putSearchConstraint(int storeLevel, Var var, Constraint C) {

        if (!searchConstraints.contains(C)) {

            if (stamp < storeLevel) {

                BigIntDomain result = this.cloneLight();

                result.modelConstraints = modelConstraints;

                result.searchConstraints = new ArrayList<>(searchConstraints.subList(0, searchConstraintsToEvaluate));
                result.searchConstraintsCloned = true;
                result.stamp = storeLevel;
                result.previousDomain = this;
                result.modelConstraintsToEvaluate = modelConstraintsToEvaluate;
                result.searchConstraintsToEvaluate = searchConstraintsToEvaluate;
                ((BigIntVar) var).domain = result;

                result.putSearchConstraint(storeLevel, var, C);
                return;
            }

            if (searchConstraints.size() == searchConstraintsToEvaluate) {
                searchConstraints.add(C);
                searchConstraintsToEvaluate++;
            } else {
                // Exchange the first satisfied constraint with just added
                // constraint
                // Order of satisfied constraints is not preserved

                if (searchConstraintsCloned) {
                    Constraint firstSatisfied = searchConstraints.get(searchConstraintsToEvaluate);
                    searchConstraints.set(searchConstraintsToEvaluate, C);
                    searchConstraints.add(firstSatisfied);
                    searchConstraintsToEvaluate++;
                } else {
                    searchConstraints = new ArrayList<>(searchConstraints.subList(0, searchConstraintsToEvaluate));
                    searchConstraintsCloned = true;
                    searchConstraints.add(C);
                    searchConstraintsToEvaluate++;
                }
            }
        }
    }

    /**
     * It removes a constraint from a domain, it should only be called by
     * removeConstraint function of Variable object.
     *
     * @param storeLevel the current level of the store.
     * @param var        the variable for which the constraint is being removed.
     * @param C          the constraint being removed.
     */

    public void removeSearchConstraint(int storeLevel, Var var, Constraint C) {

        if (stamp < storeLevel) {

            BigIntDomain result = this.cloneLight();

            result.modelConstraints = modelConstraints;
            result.searchConstraints = searchConstraints;
            result.stamp = storeLevel;
            result.previousDomain = this;
            result.modelConstraintsToEvaluate = modelConstraintsToEvaluate;
            result.searchConstraintsToEvaluate = searchConstraintsToEvaluate;
            ((BigIntVar) var).domain = result;

            result.removeSearchConstraint(storeLevel, var, C);
            return;
        }

        assert (stamp == storeLevel);

        int i = 0;

        // TODO , improve by using interval find function.

        while (i < searchConstraintsToEvaluate) {
            if (searchConstraints.get(i) == C) {

                searchConstraints.set(i, searchConstraints.get(searchConstraintsToEvaluate - 1));
                searchConstraints.set(searchConstraintsToEvaluate - 1, C);
                searchConstraintsToEvaluate--;

                break;
            }
            i++;
        }
    }

    /**
     * It removes a constraint from a domain, it should only be called by
     * removeConstraint function of Variable object.
     */

    @Override public void removeSearchConstraint(int storeLevel, Var var, int position, Constraint C) {

        if (stamp < storeLevel) {

            BigIntDomain result = this.cloneLight();

            result.modelConstraints = modelConstraints;
            result.searchConstraints = searchConstraints;
            result.stamp = storeLevel;
            result.previousDomain = this;
            result.modelConstraintsToEvaluate = modelConstraintsToEvaluate;
            result.searchConstraintsToEvaluate = searchConstraintsToEvaluate;
            ((BigIntVar) var).domain = result;

            result.removeSearchConstraint(storeLevel, var, position, C);
            return;
        }

        assert (stamp == storeLevel);

        assert (searchConstraints.get(position) == C) : "Position of the removed constraint not specified properly";

        if (position < searchConstraintsToEvaluate) {

            searchConstraints.set(position, searchConstraints.get(searchConstraintsToEvaluate - 1));
            searchConstraints.set(searchConstraintsToEvaluate - 1, C);
            searchConstraintsToEvaluate--;

        }

    }

    public abstract BigIntDomain cloneLight();

    /**
     * Returns the lexical ordering between the sets
     *
     * @param domain the set that should be lexically compared to this set
     * @return -1 if s is greater than this set, 0 if s is equal to this set and else it returns 1.
     */
    public int lex(IntDomain domain) {

        ValueEnumeration thisEnumer = this.valueEnumeration();
        ValueEnumeration paramEnumer = domain.valueEnumeration();

        int i, j;

        while (thisEnumer.hasMoreElements()) {

            i = thisEnumer.nextElement();

            if (paramEnumer.hasMoreElements()) {

                j = paramEnumer.nextElement();

                if (i < j)
                    return -1;
                else if (j < i)
                    return 1;
            } else
                return 1;
        }

        if (paramEnumer.hasMoreElements())
            return -1;

        return 0;

    }

    /**
     * It returns the number of elements smaller than el.
     *
     * @param el the element from which counted elements must be smaller than.
     * @return the number of elements which are smaller than the provided element el.
     */
    public int elementsSmallerThan(BigInteger el) {

        int counter = -1;

        BigInteger value = el.subtract(new BigInteger("1"));

        while (value.compareTo(el) != 0) {
            value = el;
            el = previousValue(el);
            counter++;
        }

        return counter;
    }


    /**
     * It computes an intersection with a given domain and stores it in this domain.
     *
     * @param intersect domain with which the intersection is being computed.
     * @return type of event which has occurred due to the operation.
     */
    public abstract int intersectAdapt(IntDomain intersect);

    /**
     * It computes a union between this domain and the domain provided as a parameter. This
     * domain is changed to reflect the result.
     *
     * @param union the domain with is used for the union operation with this domain.
     * @return it returns information about the pruning event which has occurred due to this operation.
     */
    public int unionAdapt(BigIntDomain union) {

        BigIntDomain result = union(union);

        if (result.getSize() == getSize())
            return Domain.NONE;
        else {
            setDomain(result);
            // FIXME, how to setup events for domain extending events?
            return BigIntDomain.ANY;
        }
    }

    /**
     * It computes an intersection of this domain with an interval [min..max].
     * It adapts this domain to the result of the intersection.
     *
     * @param min the minimum value of the interval used in the intersection computation.
     * @param max the maximum value of the interval used in the intersection computation.
     * @return it returns information about the pruning event which has occurred due to this operation.
     */
    public abstract int intersectAdapt(int min, int max);


    /**
     * It computes the size of the intersection between this domain and the domain
     * supplied as a parameter.
     *
     * @param domain the domain with which the intersection is computed.
     * @return the size of the intersection.
     */
    public int sizeOfIntersection(BigIntDomain domain) {
        return intersect(domain).getSize();
    }

    /**
     * It access the element at the specified position.
     *
     * @param index the position of the element, indexing starts from 0.
     * @return the value at a given position in the domain.
     */
    public abstract BigInteger getElementAt(int index);

    /**
     * It constructs and int array containing all elements in the domain.
     * The array will have size equal to the number of elements in the domain.
     *
     * @return the int array containing all elements in a domain.
     */
    public int[] toIntArray() {

        int[] result = new int[getSize()];

        ValueEnumeration enumer = this.valueEnumeration();
        int i = 0;

        while (enumer.hasMoreElements())
            result[i++] = enumer.nextElement();

        return result;
    }

    /**
     * It returns the value to which this domain is grounded. It assumes
     * that a domain is a singleton domain.
     *
     * @return the only value remaining in the domain.
     */
    public BigInteger value() {

        assert (singleton()) : "function value() called when domain is not a singleton domain.";

        return min();

    }

    private final static Random generator = new Random();

    /**
     * It returns a random value from the domain.
     *
     * @return random value.
     */
    //public int getRandomValue() {
    ///    return getElementAt(generator.nextInt(getSize()));
    //}

    /*
     * Finds result interval for multiplication of {a..b} * {c..d}
     */
    public final static BigIntegerInterval mulBounds(BigInteger a, BigInteger b, BigInteger c, BigInteger d) {

	    BigInteger ac = multiplyBigInt(a, c), ad = multiplyBigInt(a, d),
	    bc = multiplyBigInt(b, c), bd = multiplyBigInt(b, d);
        BigInteger min = ac.min(ad).min(bc.min(bd));
        BigInteger max = ac.max(ad).max(bc.max(bd));

        return new BigIntegerInterval(min, max);
    }

    /*
     * Finds result interval for {a..b}^2
     */
    public final static BigIntegerInterval squareBounds(BigInteger a, BigInteger b) {

        BigInteger aa = multiplyBigInt(a, a),
	    bb = multiplyBigInt(b, b);
        BigInteger min = (aa.compareTo(bb) < 0) ? aa : bb; //Math.min(aa, bb);
    	BigInteger max = (aa.compareTo(bb) > 1) ? aa : bb; //Math.max(aa, bb);

        if (a.compareTo(new BigInteger("0")) < 0 && b.compareTo(new BigInteger("0")) > 0)
	    min = new BigInteger("0");

        return new BigIntegerInterval(min, max);
    }

    /*
     * Finds result interval for division of {a..b} / {c..d} for div and mod constraints
     */
    public final static BigIntegerInterval divBounds(BigInteger a, BigInteger b, BigInteger c, BigInteger d) {

        BigInteger min, max;

        BigIntegerInterval result;

        if (a.compareTo(new BigInteger("0")) <= 0  &&
                b.compareTo(new BigInteger("0")) >= 0 &&
                c.compareTo(new BigInteger("0")) <= 0 &&
                d.compareTo(new BigInteger("0")) >= 0) { // case 1
            min = BigIntDomain.MinInt;
            max = BigIntDomain.MaxInt;
            result = new BigIntegerInterval(min, max);
        } else if (c.compareTo(new BigInteger("0")) == 0 &&
                d.compareTo(new BigInteger("0")) == 0 &&
                (a.compareTo(new BigInteger("0")) > 0 || b.compareTo(new BigInteger("0")) < 0)) // case 2
            throw Store.failException;

        else if (c.compareTo(new BigInteger("0")) < 0 &&
                d.compareTo(new BigInteger("0")) > 0 &&
                (a.compareTo(new BigInteger("0")) > 0 || b.compareTo(new BigInteger("0")) < 0)) { // case 3
            max = a.abs().max(b.abs());
            min = max.negate();
            result = new BigIntegerInterval(min, max);
        } else if (c.compareTo(new BigInteger("0")) == 0 &&
                d.compareTo(new BigInteger("0")) != 0 &&
                (a.compareTo(new BigInteger("0")) > 0 || b.compareTo(new BigInteger("0")) < 0)) // case 4 a
            result = divBounds(a, b, new BigInteger("1"), d);
        else if (c.compareTo(new BigInteger("0")) != 0 &&
                d.compareTo(new BigInteger("0")) == 0 &&
                (a.compareTo(new BigInteger("0")) > 0 || b.compareTo(new BigInteger("0")) < 0)) // case 4 b
            result = divBounds(a, b, c, new BigInteger("-1"));

        else if ((c.compareTo(new BigInteger("0")) > 0 ||
                d.compareTo(new BigInteger("0")) < 0) &&
                c.compareTo(d) <= 0) { // case 5
            BigInteger ac = a.divide(c), ad = a.divide(d), bc = b.divide(c), bd = b.divide(d);
            min = ac.min(d).min(bc.min(bd));
            max = ac.max(ad).max(bc.max(bd));
            result = new BigIntegerInterval(min, max);
        } else
            throw Store.failException; // can happen if a..b or c..d are not proper intervals

        return result;
    }

    /*
     * Finds result interval for division of {a..b} / {c..d} for mul constraints
     */
    public final static BigIntegerInterval divIntBounds(BigInteger a, BigInteger b, BigInteger c, BigInteger d) {
        BigInteger min, max;

        BigIntegerInterval result;

        if (a.compareTo(new BigInteger("0")) <= 0 &&
                b.compareTo(new BigInteger("0")) >= 0 &&
                c.compareTo(new BigInteger("0")) <= 0 && d.compareTo(new BigInteger("0")) >= 0) { // case 1
            min = BigIntDomain.MinInt;
            max = BigIntDomain.MaxInt;
            result = new BigIntegerInterval(min, max);
        } else if (c.compareTo(new BigInteger("0")) == 0 &&
                d.compareTo(new BigInteger("0")) == 0 &&
                (a.compareTo(new BigInteger("0")) > 0 || b.compareTo(new BigInteger("0")) < 0)) // case 2
            throw Store.failException;

        else if (c.compareTo(new BigInteger("0")) < 0 &&
                d.compareTo(new BigInteger("0")) > 0 &&
                (a.compareTo(new BigInteger("0")) > 0 || b.compareTo(new BigInteger("0")) < 0)) { // case 3
            max = a.abs().max(b.abs());
            min = max.negate();
            result = new BigIntegerInterval(min, max);
        } else if (c.compareTo(new BigInteger("0")) == 0 &&
                d.compareTo(new BigInteger("0")) != 0
                && (a.compareTo(new BigInteger("0")) > 0 || b.compareTo(new BigInteger("0")) < 0)) // case 4 a
            result = divIntBounds(a, b, new BigInteger("1"), d);
        else if (c.compareTo(new BigInteger("0")) != 0 &&
                d.compareTo(new BigInteger("0")) == 0 &&
                (a.compareTo(new BigInteger("0")) > 0 || b.compareTo(new BigInteger("0")) < 0)) // case 4 b
            result = divIntBounds(a, b, c, new BigInteger("-1"));

        else if ((c.compareTo(new BigInteger("0")) > 0
                || d.compareTo(new BigInteger("0")) < 0)
                && c.compareTo(new BigInteger("0")) <= d.compareTo(new BigInteger("0"))) { // case 5
            BigInteger ac =  a.divide(c), ad =  a.divide(d), bc = b.divide(c), bd = b.divide(d);
            BigInteger low = ac.min(ad).min(bc.min(bd));
            BigInteger high = ac.max(ad).max(bc.max(bd));
            // TODO: If something breaks check here
            if (low.compareTo(high) > 0)
                throw Store.failException;
            result = new BigIntegerInterval(low, high);
        } else
            throw Store.failException; // can happen if a..b or c..d are not proper intervals

        return result;
    }

    /**
     * Returns the product of the arguments,
     * if the result overflows MaxInt or MinInt is returned.
     *
     * @param x the first value
     * @param y the second value
     * @return the result or MaxInt/MinInt if result causes overflow
     */
    public static BigInteger multiplyBigInt(BigInteger x, BigInteger y) {
        BigInteger r = x.multiply(y);
        return r;
    }

    /**
     * Returns the sum of its arguments,
     * if the result overflows MaxInt or MinInt is returned.
     *
     * @param x the first value
     * @param y the second value
     * @return the result or MaxInt/MinInt if result causes overflow
     */
    public static BigInteger BigInteger(BigInteger x, BigInteger y) {
        BigInteger r = x.add(y);
        return r;
    }

    /**
     * Returns the difference of the arguments,
     * if the result overflows MaxInt or MinInt is returned.
     *
     * @param x the first value
     * @param y the second value to subtract from the first
     * @return the result or MaxInt/MinInt if result causes overflow
     */
    public static BigInteger subtractBigInt(BigInteger x, BigInteger y) {
        BigInteger r = x.subtract(y);
        return r;
    }
}

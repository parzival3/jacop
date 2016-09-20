/**
 *  TaskNormalView.java 
 *  This file is part of JaCoP.
 *
 *  JaCoP is a Java Constraint Programming solver. 
 *	
 *	Copyright (C) 2000-2008 Krzysztof Kuchcinski and Radoslaw Szymanek
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *  
 *  Notwithstanding any other provision of this License, the copyright
 *  owners of this work supplement the terms of this License with terms
 *  prohibiting misrepresentation of the origin of this work and requiring
 *  that modified versions of this work be marked in reasonable ways as
 *  different from the original version. This supplement of the license
 *  terms is in accordance with Section 7 of GNU Affero General Public
 *  License version 3.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.jacop.constraints.cumulative;

import org.jacop.core.IntVar;

/**
 * Represents tasks for cumulative constraint
 * 
 * @author Krzysztof Kuchcinski
 * @version 4.5
 */

class TaskNormalView extends TaskView {

  TaskNormalView(Task t) {
    super(t);
  }

  // last complition time
  int lct() {
    return start.max() + dur.max();
  }
  
  // erliest complition time
  int ect() {
    return start.min() + dur.min();
  }
  
  // erliest start time
  int est() {
    return start.min();
  }

  // last start time
  int lst() {
    return start.max();
    // return lct() - dur.min();  // ???
  }

  int env(int C) {
    return C * est() + e();
  }

  void updateEdgeFind(int est) {
    if (est > start.min())
      start.domain.inMin(store.level, start, est);
  }
  
  void updateNotFirstNotLast(int lct) {
    int max = lct - dur.max();
    if (max < start.max())
      start.domain.inMax(store.level, start, max);
  }

  void updateDetectable(int est) {
    if (est > start.min())
      start.domain.inMin(store.level, start, est);    
  }
  
  // @Override
  // public String toString() {
  //   return "[" + super.toString() + ", normal view: est = " + est() + ", lct =  " + lct() + ", ect = " + ect() + ", lst = " + lst() + ", treeIndex = " + treeIndex + "]";
  // }
}

/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of Javalanche.
 * 
 * Javalanche is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Javalanche is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with Javalanche.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.javalanche.mutation.bytecodeMutations.replaceVariables;

import java.lang.reflect.Method;

import org.junit.Test;

import de.unisb.cs.st.javalanche.mutation.bytecodeMutations.BaseBytecodeTest;
import de.unisb.cs.st.javalanche.mutation.bytecodeMutations.replaceVariables.classes.ReplaceVariables5TEMPLATE;
import de.unisb.cs.st.javalanche.mutation.results.Mutation.MutationType;

public class ReplaceLocalVariablesTypesBytecodeTest extends BaseBytecodeTest {

	private Class<?> clazz;

	public ReplaceLocalVariablesTypesBytecodeTest() throws Exception {
		super(ReplaceVariables5TEMPLATE.class);
		verbose = true;
		clazz = prepareTest();
	}

	@Test
	public void testDoubles() throws Exception {
		Method m1 = clazz.getMethod("m1");
		checkUnmutated(1., m1, clazz);
		checkMutation(9, MutationType.REPLACE_VARIABLE, 0, new Object[0], 2.2,
				m1, clazz);
		checkMutation(9, MutationType.REPLACE_VARIABLE, 1, new Object[0], 3.2,
				m1, clazz);
	}

	@Test
	public void testFloats() throws Exception {
		Method m = clazz.getMethod("m2");
		checkUnmutated(1.f, m, clazz);
		checkMutation(15, MutationType.REPLACE_VARIABLE, 0, new Object[0],
				1.2f, m, clazz);
	}

	@Test
	public void testLongs() throws Exception {
		Method m = clazz.getMethod("m3");
		checkUnmutated(1l, m, clazz);
		checkMutation(21, MutationType.REPLACE_VARIABLE, 0, new Object[0],
				1234567890l, m, clazz);
	}

	@Test
	public void testObject() throws Exception {
		Method m = clazz.getMethod("m4");
		checkUnmutated("A", m, clazz);
		checkMutation(27, MutationType.REPLACE_VARIABLE, 0, new Object[0], "B",
				m, clazz);
	}


}

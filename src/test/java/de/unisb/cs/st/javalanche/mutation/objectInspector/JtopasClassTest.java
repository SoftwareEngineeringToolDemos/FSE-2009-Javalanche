/*
* Copyright (C) 2011 Saarland University
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
package de.unisb.cs.st.javalanche.mutation.objectInspector;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

public class JtopasClassTest {

	private static final String TEST_CLASS_FILENAME = "TestTokenProperties.clazz";

	private static final String TEST_CLASS_CLASSNAME = "de.susebox.java.util.TestTokenProperties";

	private static class TestingClassLoader extends ClassLoader {

		public TestingClassLoader() throws IOException {
		}

		@SuppressWarnings("unchecked")
		public Class<?> loadClass(final String name)
				throws ClassNotFoundException {
			if (name.equals(TEST_CLASS_CLASSNAME)) {
				try {
					ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES
							| ClassWriter.COMPUTE_MAXS);
					CheckClassAdapter cc = new CheckClassAdapter(cw);
					TraceClassVisitor tcv = new TraceClassVisitor(cc,
							new PrintWriter(System.out));
					ClassReader cr = new ClassReader(JtopasClassTest.class
							.getClassLoader().getResourceAsStream(
									TEST_CLASS_FILENAME));
//					cr.accept(new ObjectInspectorClassAdapter(tcv),
//							ClassReader.EXPAND_FRAMES);
					byte[] bytecode = cw.toByteArray();
					return super
							.defineClass(name, bytecode, 0, bytecode.length);
				} catch (IOException ex) {
					throw new ClassNotFoundException("Load error: "
							+ ex.toString(), ex);
				}
			}
			return super.loadClass(name);
		}

	}

	@Ignore
	// To run this test JTopas has to be on the classpath
	public void xtestJTopasClass() {
		TestingClassLoader tcl;
		try {
			tcl = new TestingClassLoader();
			Class<?> cc = tcl.loadClass(TEST_CLASS_CLASSNAME);
			Assert.assertNotNull(cc);
			Method m1 = cc.getMethod("testTokenPos");
			try {
				m1.invoke(null, new Object[0]);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void preventJunitError() {
	}
}

/**
 *  Copyright 2012-2015 Gunnar Morling (http://www.gunnarmorling.de/)
 *  and/or other contributors as indicated by the @authors tag. See the
 *  copyright.txt file in the distribution for a full listing of all
 *  contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.mapstruct.ap.testutil.runner;

import java.net.URL;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.mapstruct.ap.testutil.WithClasses;
import org.mapstruct.ap.testutil.compilation.annotation.ExpectedCompilationOutcome;
import org.mapstruct.ap.testutil.compilation.annotation.ProcessorOption;

/**
 * A JUnit4 runner for Annotation Processor tests.
 * <p>
 * Test classes and test methods are safe to be executed in parallel.
 * <p>
 * The classes to be compiled for a given test method must be specified via {@link WithClasses}. In addition the
 * following things can be configured optionally :
 * <ul>
 * <li>Processor options to be considered during compilation via {@link ProcessorOption}.</li>
 * <li>The expected compilation outcome and expected diagnostics can be specified via {@link ExpectedCompilationOutcome}
 * . If no outcome is specified, a successful compilation is assumed.</li>
 * </ul>
 *
 * @author Gunnar Morling
 * @author Andreas Gudian
 */
public class AnnotationProcessorTestRunner extends BlockJUnit4ClassRunner {
    static final ModifiableURLClassLoader TEST_CLASS_LOADER = new ModifiableURLClassLoader();
    private final Class<?> klass;
    private Class<?> klassToUse;
    private ReplacableTestClass replacableTestClass;

    /**
     * @param klass the test class
     *
     * @throws Exception see {@link BlockJUnit4ClassRunner#BlockJUnit4ClassRunner(Class)}
     */
    public AnnotationProcessorTestRunner(Class<?> klass) throws Exception {
        super( klass );
        this.klass = klass;
    }

    /**
     * newly loads the class with the test class loader and sets that loader as context class loader of the thread
     *
     * @param klass the class to replace
     *
     * @return the class loaded with the test class loader
     */
    private static Class<?> replaceClassLoaderAndClass(Class<?> klass) {
        replaceContextClassLoader( klass );

        try {
            return Thread.currentThread().getContextClassLoader().loadClass( klass.getName() );
        }
        catch ( ClassNotFoundException e ) {
            throw new RuntimeException( e );
        }

    }

    private static void replaceContextClassLoader(Class<?> klass) {
        String classFileName = klass.getName().replace( ".", "/" ) + ".class";
        URL classResource = klass.getClassLoader().getResource( classFileName );
        String fullyQualifiedUrl = classResource.toExternalForm();
        String basePath = fullyQualifiedUrl.substring( 0, fullyQualifiedUrl.length() - classFileName.length() );

        ModifiableURLClassLoader testClassLoader = new ModifiableURLClassLoader();
        testClassLoader.addURL( basePath );

        Thread.currentThread().setContextClassLoader( testClassLoader );
    }

    @Override
    protected TestClass createTestClass(final Class<?> testClass) {
        replacableTestClass = new ReplacableTestClass( testClass );
        return replacableTestClass;
    }

    private FrameworkMethod replaceFrameworkMethod(FrameworkMethod m) {
        try {
            return new FrameworkMethod(
                klassToUse.getDeclaredMethod( m.getName(), m.getMethod().getParameterTypes() ) );
        }
        catch ( NoSuchMethodException e ) {
            throw new RuntimeException( e );
        }
    }

    @Override
    protected Statement methodBlock(FrameworkMethod method) {
        CompilingStatement statement = new CompilingStatement( method );
        if ( statement.needsRecompilation() ) {
            klassToUse = replaceClassLoaderAndClass( klass );

            replacableTestClass.replaceClass( klassToUse );
        }

        method = replaceFrameworkMethod( method );

        Statement next = super.methodBlock( method );

        statement.setNextStatement( next );

        return statement;
    }

}

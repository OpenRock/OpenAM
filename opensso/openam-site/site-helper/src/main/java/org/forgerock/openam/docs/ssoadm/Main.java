/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 ForgeRock AS. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 */

package org.forgerock.openam.docs.ssoadm;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;

import javax.annotation.processing.AbstractProcessor;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public final class Main
{

  /**
   * Display ssoadm subcommand annotations transformed to DocBook 5 XML
   * fragments.
   *
   * To use this, change to the opensso directory, compile the content of this
   * package, and also the com.sun.identity.cli.annotation classes, and then
   * run this class:
   * $ javac openam-site/src/main/java/org/forgerock/openam/docs/ssoadm/*.java products/amserver/source/com/sun/identity/cli/annotation/*.java
   * $ java -classpath openam-site/src/main/java/:products/amserver/source/ org.forgerock.openam.docs.ssoadm.Main
   *
   * @param args Not currently used. Instead the files are hard coded.
   */
  public static void main(String[] args)
  {
    // Files with assertions holding ssoadm subcommand content.
    // So far, there are only two of these that I know about.
    File[] files = new File[2];
    files[0] = new File("products/amserver/source/com/sun/identity/cli/definition/AccessManager.java");
    files[1] = new File("products/federation/openfm/source/com/sun/identity/federation/cli/definition/FederationManager.java");

    // Provide the files to the compiler as per the JavaCompiler Javadoc.
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fileManager =
        compiler.getStandardFileManager(null, null, null);
    Iterable<? extends JavaFileObject> compilationUnits =
        fileManager.getJavaFileObjectsFromFiles(Arrays.asList(files));

    // Call the assertion processor as per
    // http://today.java.net/pub/a/today/2008/04/10/source-code-analysis-using-java-6-compiler-apis.html
    SsoadmAP ssoadm = new SsoadmAP();
    CompilationTask task = compiler.getTask(null, fileManager, null,
        null, null, compilationUnits);
    LinkedList<AbstractProcessor> processors = new LinkedList<AbstractProcessor>();
    processors.add(ssoadm);
    task.setProcessors(processors);
    task.call();

    // Now that processing is complete, display the sorted XML content
    for (String c: ssoadm.getSubcommands()) System.out.println(c);
  }
}

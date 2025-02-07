/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.gradle;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl2smt.ocldiff.OCLDiffGenerator;
import de.monticore.ocl2smt.util.OCL_Loader;
import de.monticore.od4report.OD4ReportMill;
import de.monticore.odbasis._ast.ASTODArtifact;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.*;
// import org.gradle.work.NormalizeLineEndings;

@CacheableTask
public abstract class OCLSemDiffTask extends DefaultTask {

  @InputFile
  @PathSensitive(PathSensitivity.NONE)
  // @NormalizeLineEndings
  public abstract RegularFileProperty getCd();

  @InputFiles
  @Optional
  @PathSensitive(PathSensitivity.RELATIVE)
  // @NormalizeLineEndings
  public abstract ConfigurableFileCollection getPositiveOCL();

  @InputFiles
  @Optional
  @PathSensitive(PathSensitivity.RELATIVE)
  // @NormalizeLineEndings
  public abstract ConfigurableFileCollection getNegativeOCL();

  @OutputDirectory
  public abstract DirectoryProperty getOutputDir();

  @OutputFile
  @Optional
  public abstract RegularFileProperty getTraceOD();

  protected Set<ASTOCLCompilationUnit> loadOCL(File cdFile, Set<File> oclFiles) throws IOException {
    Set<ASTOCLCompilationUnit> result = new HashSet<>();
    for (File f : oclFiles) {
      result.add(OCL_Loader.loadAndCheckOCL(f, cdFile));
    }
    return result;
  }

  @TaskAction
  public void run() throws IOException {
    OCLMill.init();
    CD4CodeMill.init();

    FileUtils.deleteDirectory(getOutputDir().get().getAsFile()); // TODO: Is this required?

    // Load Input
    ASTCDCompilationUnit cd = OCL_Loader.loadAndCheckCD(getCd().get().getAsFile());

    Set<ASTOCLCompilationUnit> positiveOCL =
        loadOCL(getCd().get().getAsFile(), getPositiveOCL().getFiles());
    Set<ASTOCLCompilationUnit> negativeOCL =
        loadOCL(getCd().get().getAsFile(), getNegativeOCL().getFiles());

    Pair<ASTODArtifact, Set<ASTODArtifact>> diff;
    Set<ASTODArtifact> witnesses;
    ASTODArtifact trace;
    // Compute Diff
    if (negativeOCL.isEmpty()) {
      witnesses = new HashSet<>();
      witnesses.add(OCLDiffGenerator.oclWitness(cd, positiveOCL, false));
    } else {
      diff = OCLDiffGenerator.oclDiff(cd, positiveOCL, negativeOCL);
      witnesses = diff.getRight();
      if (getTraceOD().isPresent()) {
        trace = diff.getLeft();
        FileUtils.writeStringToFile(
            getTraceOD().getAsFile().get(),
            OD4ReportMill.prettyPrint(trace, false),
            Charset.defaultCharset());
      }
    }

    // Write Results
    for (ASTODArtifact wit : witnesses) {
      String fileName = wit.getObjectDiagram().getName() + ".od";
      FileUtils.writeStringToFile(
          getOutputDir().file(fileName).get().getAsFile(),
          OD4ReportMill.prettyPrint(wit, false),
          Charset.defaultCharset());
    }
  }
}

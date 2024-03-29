package cd.test;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import cd.Config;
import cd.Main;
import cd.util.FileUtil;

@RunWith(Parameterized.class)
public class TestSamplePrograms extends AbstractTestSamplePrograms {

	/**
	 * If you want to run the test on just one file, then initialize this
	 * variable like:
	 * {@code justFile = new File("javali_tests/A2/Inheritance.javali")}.
	 */

//	public static final File justFile = new File("./javali_tests/exec/A4/manyregs_11.javali");
	public static final File justFile = null;

	/**
	 * Directory in which to search for test files. If null, then the default is
	 * the current directory (to include all files). To run only tests in a
	 * particular directory, use sth. like:
	 * {@code testDir = new File("javali_tests/A2/")}.
	 */
//	public static final File testDir = new File("javali_tests/exec");
	public static final File testDir = new File("javali_tests");

	@Parameters
	public static Collection<Object[]> testFiles() {
		List<Object[]> result = new ArrayList<Object[]>();
		if (justFile != null)
			result.add(new Object[] { justFile });
		else if (testDir != null) {
			for (File file : FileUtil.findFiles(testDir)) {
				result.add(new Object[] { file });
			}
		} else {
			for (File file : FileUtil.findFiles(new File("."))) {
				result.add(new Object[] { file });
			}
		}
		return result;
	}

	/**
	 * @param file
	 *            The javali file to test.
	 */
	public TestSamplePrograms(File file) {
		this.file = file;
		this.sfile = new File(file.getPath() + Config.ASMEXT);
		this.binfile = new File(file.getPath() + Config.BINARYEXT);
		this.infile = new File(file.getPath() + ".in");
		this.parserreffile = new File(file.getPath() + ".parser.ref");
		this.semanticreffile = new File(file.getPath() + ".semantic.ref");
		this.execreffile = new File(file.getPath() + ".exec.ref");
		this.cfgreffile = new File(file.getPath() + ".cfg.dot.ref");
		this.optreffile = new File(file.getPath() + ".opt.ref");
		this.errfile = new File(String.format("%s.err", file.getPath()));
		this.main = new Main();
		this.main.debug = new StringWriter();

		this.main.cfgdumpbase = file;

	}

}

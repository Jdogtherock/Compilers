package edu.cornell.cs.sam.ui;

import edu.cornell.cs.sam.core.AssemblerException;
import edu.cornell.cs.sam.core.Memory;
import static edu.cornell.cs.sam.core.Memory.Type;
import edu.cornell.cs.sam.core.SamAssembler;
import edu.cornell.cs.sam.core.Sys;
import edu.cornell.cs.sam.core.SystemException;
import edu.cornell.cs.sam.core.Processor;
import edu.cornell.cs.sam.core.Program;
import edu.cornell.cs.sam.core.Video;
import edu.cornell.cs.sam.utils.ProgramState;
import edu.cornell.cs.sam.utils.SamThread;
import edu.cornell.cs.sam.utils.XMLUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This class holds an execution script to test multiple sam files and report
 * the results.
 */
public class TestScript {
	protected Sys sys;

	protected Processor proc;

	protected Memory mem;

	protected List<Test> tests = new ArrayList<Test>();

	protected File sourceFile = null;

	/**
	 * Creates a new TestScript
	 */
	public TestScript() {
		sys = new Sys();
		proc = sys.cpu();
		mem = sys.mem();
	}

	/**
	 * Returns the file this TestScript is using
	 * 
	 * @return the source file
	 */
	public File getSourceFile() {
		return sourceFile;
	}

	/**
	 * Sets the file this testscript will load
	 * 
	 * @param file
	 *            the file to load
	 */
	public void setSourceFile(File file) {
		sourceFile = file;
	}

	/**
	 * Loads the provided input stream
	 * 
	 * @param toParse
	 *            The stream on which the XML file can be read
	 * @throws TestScriptException
	 *             if there is a problem parsing the XML file
	 */
	public void load(InputStream toParse) throws TestScriptException {
		Document doc;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(toParse);
		}
		catch (ParserConfigurationException e) {
			throw new TestScriptException("File Parse Error");
		}
		catch (IOException e) {
			throw new TestScriptException("File Parse Error");
		}
		catch (org.xml.sax.SAXException e) {
			throw new TestScriptException("File Parse Error");
		}

		NodeList l = doc.getElementsByTagName("testscript");
		if (l.getLength() != 1) throw new TestScriptException("Invalid File");
		Element root = (Element) l.item(0);
		if (root.getAttribute("version") == null || !root.getAttribute("version").equals("1.0")) 
			throw new TestScriptException("Incorrect Version in Test Script");

		l = root.getElementsByTagName("test");
		for (int i = 0; i < l.getLength(); i++) {
			tests.add(processTest((Element) l.item(i)));
		}
	}

	/**
	 * Processes an individual test element
	 */
	protected Test processTest(Element e) throws TestScriptException {
		String fileName = e.getAttribute("filename");
		if (fileName == null) throw new TestScriptException("Invalid filename for test");
		Test t = new Test(fileName);
		t.setScriptFile(this);

		//process the io
		NodeList l = e.getElementsByTagName("io");
		if (l.getLength() == 0) throw new TestScriptException("Each test must have a return value");
		for (int i = 0; i < l.getLength(); i++) {
			Element io = (Element) l.item(i);
			String classParam = io.getAttribute("class");
			String typeParam = io.getAttribute("type");
			Object data;

			if (classParam == null || typeParam == null) 
				throw new TestScriptException("Each io object must have a class and type");

			NodeList list = io.getChildNodes();
			if (list.getLength() > 1 || list.item(0) == null || list.item(0).getNodeType() != Node.TEXT_NODE) 
				throw new TestScriptException("Each IO object must have a value");

			String dataString = ((Text) list.item(0)).getData();
			if (typeParam.equals("int")) {
				try {
					data = new Test.INT(Integer.parseInt(dataString));
				}
				catch (NumberFormatException e1) {
					throw new TestScriptException("Error parsing integer data");
				}
			}
			else if (typeParam.equals("ma")) {
				try {
					data = new Test.MA(Integer.parseInt(dataString));
				}
				catch (NumberFormatException e1) {
					throw new TestScriptException("Error parsing integer data");
				}
			}
			else if (typeParam.equals("pa")) {
				try {
					data = new Test.PA(Integer.parseInt(dataString));
				}
				catch (NumberFormatException e1) {
					throw new TestScriptException("Error parsing integer data");
				}
			}
			else if (typeParam.equals("float")) {
				try {
					data = new Test.FLOAT(Float.parseFloat(dataString));
				}
				catch (NumberFormatException e1) {
					throw new TestScriptException("Error parsing float data");
				}
			}
			else if (typeParam.equals("char")) {
				if (dataString == null || (dataString.length() != 1 && dataString.length() != 2)) 
					throw new TestScriptException("One-letter String required for Characters");
				if (dataString.length() == 2 && dataString.charAt(0) != '\\') 
					throw new TestScriptException("Two character Character strings must be " + 
						"escape sequences");
				if (dataString.length() == 2) {
					switch (dataString.charAt(1)) {
						case 'n':
							data = new Test.CH('\n');
							break;
						case 't':
							data = new Test.CH('\t');
							break;
						case 'r':
							data = new Test.CH('\r');
							break;
						case '\\':
							data = new Test.CH('\\');
							break;
						default:
							throw new TestScriptException("Invalid Escape Expression");
					}
				}
				else
					data = new Test.CH(dataString.charAt(0));
			}
			else if (typeParam.equals("string"))
				data = dataString;
			else
				throw new TestScriptException("IO objects must be of type int, char, or float");

			if (classParam.equals("return") && t.getReturnValue() != null)
				throw new TestScriptException("Only one return value per test allowed");
			else if (classParam.equals("return") && typeParam.equals("string"))
				throw new TestScriptException("String return values are not allowed");
			else if (classParam.equals("return"))
				t.setReturnValue(data);
			else if (classParam.equals("read"))
				t.addToRead(data);
			else if (classParam.equals("write"))
				t.addToWrite(data);
			else
				throw new TestScriptException("IO objects must be of class return, read, or write");
		}

		if (t.getReturnValue() == null) 	
			throw new TestScriptException("All test scripts must have a return value specified");

		return t;
	}

	/**
	 * Saves the test script to a file
	 * 
	 * @param toSave
	 *            the file to save the XML test script to
	 * @throws TestScriptException
	 *             if there is an error writing the file
	 */
	public void save(File toSave) throws TestScriptException {
		Document xmlDoc;
		try {
			xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		}
		catch (javax.xml.parsers.ParserConfigurationException e) {
			throw new TestScriptException("Error with Java XML");
		}
		Element rootElem = xmlDoc.createElement("testscript");
		rootElem.setAttribute("version", "1.0");
		xmlDoc.appendChild(rootElem);
		sourceFile = toSave;
		for (int i = 0; i < tests.size(); i++) {
			Test t = tests.get(i);
			Element testElem = xmlDoc.createElement("test");
			testElem.setAttribute("filename", t.getFileName());
			addIOType(xmlDoc, testElem, "read", t.getRead());
			addIOType(xmlDoc, testElem, "write", t.getWrite());
			addIO(xmlDoc, testElem, "return", t.getReturnValue());
			rootElem.appendChild(testElem);
		}

		try {
			XMLUtils.writeXML(xmlDoc, new PrintWriter(new BufferedWriter(new FileWriter(toSave))));
		}
		catch (IOException e) {
			throw new TestScriptException("Error writing XML file");
		}
	}	

	protected static void addIOType(Document xmlDoc, Element testElem, String classParam, Collection<?> coll) {
		for (Object o: coll) 
			addIO(xmlDoc, testElem, classParam, o);
	}

	protected static void addIO(Document xmlDoc, Element testElem, String classParam, Object o) {
		Element ioElem = xmlDoc.createElement("io");
		ioElem.setAttribute("class", classParam);
		if (o instanceof Test.INT){
			ioElem.setAttribute("type", "int");
			ioElem.appendChild(xmlDoc.createTextNode(Integer.toString(((Test.INT)o).intValue())));
		}
		else if (o instanceof Test.FLOAT){
			ioElem.setAttribute("type", "float");
			ioElem.appendChild(xmlDoc.createTextNode(Float.toString(((Test.FLOAT)o).floatValue())));
		}
		else if (o instanceof Test.CH){
			ioElem.setAttribute("type", "char");
			ioElem.appendChild(xmlDoc.createTextNode(Character.toString(((Test.CH)o).charValue())));
		}
		else if (o instanceof Test.MA){
			ioElem.setAttribute("type", "ma");
			ioElem.appendChild(xmlDoc.createTextNode(Integer.toString(((Test.MA)o).intValue())));
		}
		else if (o instanceof Test.PA){
			ioElem.setAttribute("type", "pa");
			ioElem.appendChild(xmlDoc.createTextNode(Integer.toString(((Test.PA)o).intValue())));
		}
		else if (o instanceof String){
			ioElem.setAttribute("type", "string");
			ioElem.appendChild(xmlDoc.createTextNode(o.toString()));
		}
		else
			return;
		testElem.appendChild(ioElem);
	}

	/**
	 * Return the vector of the tests
	 * 
	 * @return the tests
	 */
	public List<Test> getTests() {
		return tests;
	}

	/**
	 * Remove all tests from this script
	 */
	public void clearTests() {
		for (int i = 0; i < tests.size(); i++)
			tests.get(i).clear();
	}

	/**
	 * Deletes all of the tests that have been marked
	 * 
	 * @see TestScript.Test#delete()
	 */
	public void deleteTests() {
		for (int i = 0; i < tests.size(); i++) {
			if (tests.get(i).delete == true) {
				tests.remove(i);
				i--;
			}
		}
	}

	/**
	 * Represents one single test
	 */
	public static class Test implements Video {
		protected String fileName;

		protected Queue<Object> rqueue = new LinkedList<Object>(); 
		protected Queue<Object> wqueue = new LinkedList<Object>(); 

		protected Object returnValue;

		protected boolean completed = false;

		protected Object actualReturnValue;

		protected boolean ioSuccessful = true;
		
		protected boolean stackCleared = true;

		protected boolean delete;

		protected List<ProgramState> stateSteps = 
			new ArrayList<ProgramState>();

		protected TestScript scriptFile;

		protected Program code = null;

		public Test(String fileName) {
			this.fileName = fileName;
		}

		public void clear() {
			completed = false;
			ioSuccessful = true;
			actualReturnValue = null;
		}

		public void addToRead(Object o) {
			rqueue.offer(o);
		}

		public void addToWrite(Object o) {
			wqueue.offer(o);
		}

		public Queue<Object> getRead() {
			return rqueue;
		}

		public Queue<Object> getWrite() {
			return wqueue;
		}

		public String getFileName() {
			if (scriptFile != null && scriptFile.getSourceFile() != null) {
				File f = new File(fileName);
				f = new File(scriptFile.getSourceFile().getParent(), f.getName());
				if (f.exists()) return f.getName();
			}
			return fileName;
		}

		public File getFile() {
			File f = new File(fileName);
			if (f.exists()) return f;
			if (scriptFile != null && scriptFile.getSourceFile() != null) {
				f = new File(scriptFile.getSourceFile().getParent(), f.getName());
			}
			if (f.exists()) return f;
			return new File(fileName);
		}

		public Object getReturnValue() {
			return returnValue;
		}

		public void setFileName(String string) {
			fileName = string;
		}

		public void setReturnValue(Object o) {
			returnValue = o;
		}

		public Object getActualReturnValue() {
			return actualReturnValue;
		}

		public boolean isCompleted() {
			return completed;
		}

		public boolean isIoSuccessful() {
			return ioSuccessful;
		}

		public void delete() {
			delete = true;
		}

		public boolean error() {
			if (!returnValue.equals(actualReturnValue)) return true;
			if (!ioSuccessful) return true;
			if (!stackCleared) return true;
			return false;
		}

		public boolean isStackCleared(){
			return stackCleared;
		}

		public List<ProgramState> getStateSteps() {
			return stateSteps;
		}

		public void addStep(ProgramState step) {
			stateSteps.add(step);
		}

		public void resetState() {
			stateSteps = new ArrayList<ProgramState>();
		}

		public TestScript getScriptFile() {
			return scriptFile;
		}

		public void setScriptFile(TestScript file) {
			scriptFile = file;
		}

		public void setRead(Collection<?> collection) {
			this.rqueue = new LinkedList<Object>(collection);
		}

		public void setWrite(Collection<?> collection) {
			this.wqueue = new LinkedList<Object>(collection);
		}

		public void assemble() throws TestScriptException {
			try {
				code = SamAssembler.assemble(new BufferedReader(new FileReader(getFile())));
			}
			catch (FileNotFoundException e) {
				throw new TestScriptException("Could not find test (" + getFileName() + ")");
			}
			catch (AssemblerException e) {
				throw new TestScriptException("Assembler reported error with test " + getFileName());
			}
			catch (IOException e) {
				throw new TestScriptException("I/O Error while reading test");
			}
		}

		public Program getCode() throws TestScriptException {
			if (code == null) assemble();
			return code;
		}

		public int run(Sys sys, SamThread thread) throws TestScriptException {
			Processor proc = sys.cpu();
			Memory mem = sys.mem();
			proc.init();
			mem.init();
			sys.setVideo(this);

			try {
				proc.load(getCode());

				while (proc.get(Processor.HALT) != 1) {
					if (thread != null && thread.interruptRequested()) 
						return SamThread.THREAD_INTERRUPTED;

					int executing = proc.get(Processor.PC);
					proc.step();
					addStep(new ProgramState(executing, mem.getStack(), proc.getRegisters()));
				}

				switch (mem.getType(0)) {
					case CH:
						actualReturnValue = new CH((char) mem.getValue(0));
						break;
					case FLOAT:
						actualReturnValue = new FLOAT(Float.intBitsToFloat(mem.getValue(0)));
						break;
					case INT:
						actualReturnValue = new INT(mem.getValue(0));
						break;
					case PA:
						actualReturnValue = new PA(mem.getValue(0));
						break;
					case MA:
						actualReturnValue = new MA(mem.getValue(0));
						break;
					default:
						actualReturnValue = new String("Error");
				}
				if (proc.get(Processor.SP) != 1) stackCleared = false;
			}

			catch (SystemException e) {
				actualReturnValue = new String("Error");
			}
			completed = true;
			return TestThread.THREAD_TEST_COMPLETED;
		}

		public int readInt() {
			if (rqueue.isEmpty() || !(rqueue.peek() instanceof INT)) {
				ioSuccessful = false;
				return 0;
			}
			else
				return ((INT) rqueue.remove()).intValue();
		}

		public String readString() {
			if (rqueue.isEmpty() || !(rqueue.peek() instanceof String)) {
				ioSuccessful = false;
				return "";
			}
			else
				return ((String) rqueue.remove());
		}

		public char readChar() {
			if (rqueue.isEmpty() || !(rqueue.peek() instanceof CH)) {
				ioSuccessful = false;
				return 0;
			}
			else
				return ((CH) rqueue.remove()).charValue();
		}

		public float readFloat() {
			if (rqueue.isEmpty() || !(rqueue.peek() instanceof FLOAT)) {
				ioSuccessful = false;
				return 0;
			}
			else
				return ((FLOAT) rqueue.remove()).floatValue();
		}

		public void writeInt(int a) {
			if (wqueue.isEmpty() || !(wqueue.peek() instanceof INT) || 
				((INT) wqueue.remove()).intValue() != a) ioSuccessful = false;
		}

		public void writeFloat(float a) {
			if (wqueue.isEmpty() || !(wqueue.peek() instanceof FLOAT) || 
				((FLOAT) wqueue.remove()).floatValue() != a) ioSuccessful = false;
		}

		public void writeChar(char a) {
			if (wqueue.isEmpty() || !(wqueue.peek() instanceof CH) || 
				((CH) wqueue.remove()).charValue() != a) ioSuccessful = false;
		}

		public void writeString(String a) {
			if (wqueue.isEmpty() || !(wqueue.peek() instanceof String) || 
				!a.equals((String) wqueue.remove())) ioSuccessful = false;
		}

		public static class MA{
			int value;
			public MA(int v){
				value = v;
			}
			
			public boolean equals(Object o){
				if(o instanceof MA)
					return value == ((MA)o).value;
				return false;
			}
			
			public String toString(){
				return "MA(" + value + ")";
			}

			public int intValue(){
				return value;
			}
		}

		public static class CH{
			char value;
			public CH(char v){
				value = v;
			}
			
			public boolean equals(Object o){
				if(o instanceof CH)
					return value == ((CH)o).value;
				return false;
			}
			
			public String toString(){
				return "CH(" + value + ")";
			}

			public char charValue(){
				return value;
			}
		}

		public static class FLOAT{
			float value;
			public FLOAT(float v){
				value = v;
			}
			
			public boolean equals(Object o){
				if(o instanceof FLOAT)
					return value == ((FLOAT)o).value;
				return false;
			}
			
			public String toString(){
				return "FLOAT(" + value + ")";
			}

			public float floatValue(){
				return value;
			}
		}

		public static class INT{
			int value;
			public INT(int v){
				value = v;
			}
			
			public boolean equals(Object o){
				if(o instanceof INT)
					return value == ((INT)o).value;
				return false;
			}
			
			public String toString(){
				return "INT(" + value + ")";
			}

			public int intValue(){
				return value;
			}
		}

		public static class PA{
			int value;
			public PA(int v){
				value = v;
			}
			
			public boolean equals(Object o){
				if(o instanceof PA)
					return value == ((PA)o).value;
				return false;
			}
			
			public String toString(){
				return "PA(" + value + ")";
			}

			public int intValue(){
				return value;
			}
		}
	}

	public static class TestScriptException extends Exception {
		private String message;
		private Throwable t;

		public TestScriptException(String message) {
			this.message = message;
		}
		
		public TestScriptException(String message, Throwable t){
			this.message = message;
			this.t = t;
		}
		
		public String getMessage() {
			return message;
		}
		
		public Throwable getCause(){
			return t;
		}
		
	}

	public static class TestThread extends SamThread {
		protected Processor proc;

		protected Sys sys;

		protected List<? extends TestScript.Test> tests;

		public static final int THREAD_TEST_COMPLETED = 4;

		public TestThread(SamThread.ThreadParent parent, Sys sys, 
			List<? extends TestScript.Test> tests) {
			setParent(parent);
			this.sys = sys;
			this.tests = tests;
			proc = sys.cpu();
		}

		public void setTests(List<? extends TestScript.Test> tests) {
			this.tests = tests;
		}

		public List<? extends TestScript.Test> getTests() {
			return tests;
		}

		public Sys getSys() {
			return sys;
		}

		public void execute() throws TestScriptException{
			SamThread.ThreadParent parent = getParent();

			for (int i = 0; i < tests.size(); i++) {

				if (interruptRequested()) {
					parent.threadEvent(THREAD_INTERRUPTED, null);
					return;
				}

				TestScript.Test test = tests.get(i);
				int status = test.run(sys, this);
				parent.threadEvent(status, null);
			}
			parent.threadEvent(THREAD_EXIT_OK, null);
		}
	}
}


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class main {
	static boolean BENCHMARK = false;
	static boolean verbose = false;
	public static void main(String[] args) {
		HashMap<String, String> arguments = new HashMap<>();
		for(int i = 0;i<args.length-1;i++) {
			arguments.put(args[i], args[i+1]);
		}
		if(arguments.containsKey("--benchmark")) {
			if(arguments.get("--benchmark").equals("1")) BENCHMARK = true;
		}
		if(arguments.containsKey("--verbose")) {
			if(arguments.get("--verbose").equals("1")) verbose = true;
		}

		String filename = arguments.get("-i");
		String is = arguments.get("-d");

		long F = 0;
		if(BENCHMARK) {
			F = System.nanoTime();
		}
		execute(filename, is);
		if(BENCHMARK) {
			System.out.println("Execution took : "+(System.nanoTime()-F) / 1000000.0+" milli-seconds");
		}
	}

	public static String execute(String filename, String is) {
		 HashMap<String, String> libs = new HashMap<>(); // name, path (example. ./lib/and.gate)
		 HashMap<String, Boolean> VAR_TABLE = new HashMap<>(); // name, value
		 ArrayList<Boolean> INPUTS = new ArrayList<>(); // $0, $1, ..
		boolean EXECUTE_NEXT = true;
		boolean COMING_IF = false;
		String[] inputs = {"0"};
		if(is.contains(",")) inputs = is.split(",");
		else inputs[0] = is;
		for (String input : inputs)
			INPUTS.add(input.equals("1")? Boolean.TRUE : Boolean.FALSE);
		if(verbose) System.out.println("Executing "+filename+" with input "+is);
		String out = "";
		String ERR_MSG = "";
		if(! new File(filename).exists()) {
			System.out.println("ERROR: '"+filename+"' doesn't exist");
			System.exit(1);
		}
		if(verbose) System.out.println("Executing "+filename+" with input "+is);
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

			String line;
			while ((line = br.readLine()) != null) {
				if(EXECUTE_NEXT) {
					if(verbose) System.out.println("Executing line : "+line);
				line = line.toLowerCase();
				if(COMING_IF) EXECUTE_NEXT = false;
				if(line.startsWith("comment") || line.startsWith("nul")) {
					/**
					 * Comment command
					 */
					continue;
				}else {
					/*
					 * Any other command
					 */
					String cmd = line.contains(" ")? line.split("\\s+")[0] : line;
					if(cmd.equals("prints")) {
						/**
						 * prints command : prints string
						 */
						System.out.print(line.replaceFirst(cmd+" ", "").replace("\\s+", ""));
					}
					else if(cmd.equals("printl")) {
						/**
						 * printl command : prints a new line
						 */
						System.out.print(System.lineSeparator());
					}
					else if(cmd.equals("print")) {
						/**
						 * print command : print a variable
						 * example : print $0 | print x | print not(x)
						 */
						System.out.print(value_fetcher(line.split("\\s+")[1], INPUTS, VAR_TABLE, libs));
					}
					else if(cmd.equals("return")) {
						/**
						 * Return a value
						 */
						return value_fetcher(line.split("\\s+")[1], INPUTS, VAR_TABLE, libs);
					}
					else if(cmd.equals("if")) {
						String[] conditions = line.split("\\s+")[1].split("="); // $1=$0 -> $1, $0
						boolean first = false;
						boolean finall = false;
						for(int i = 0;i<conditions.length;i++) {
							if(i == 0) {
								first = value_fetcher(conditions[i], INPUTS, VAR_TABLE, libs).equals("1");
								finall = first;
							}else {
								boolean temp = false;
								temp = value_fetcher(conditions[i], INPUTS, VAR_TABLE, libs).equals("1");
								finall = temp && finall;
							}
						}
						if(verbose) System.out.println("if statement final : "+finall);
						if(!finall) EXECUTE_NEXT = false;
						else COMING_IF = true;
					}
					else if(cmd.equals("put")) {
						String value = line.split("\\s+")[1];
						String var_name = line.split("\\s+")[3];
						VAR_TABLE.put(var_name, value_fetcher(value, INPUTS, VAR_TABLE, libs).equals("1"));
					}
					else if(cmd.equals("import")) {
						String path = line.split("\\s+")[1]; // ./lib/and.gate
						String lib_name = line.split("\\s+")[3]; // and
						if(filename.contains("/")) {
							path = filename.substring(0,filename.lastIndexOf("/"))+"/"+path;
						}
						if(new File(path).exists()) {
							libs.put(lib_name, path);
						}else System.out.println("ERROR: library path '"+path+"' doesn't exist");
					}
				}
				}
				else {
					COMING_IF = false;
					EXECUTE_NEXT = true;
				}
			}

		}catch(Exception e) {
			System.out.println("ERROR: "+ERR_MSG);
			e.printStackTrace();
		}
		return out;
	}
	/**
	 * Fetches value
	 */
	public static String value_fetcher(String in, ArrayList<Boolean> INPUTS, HashMap<String, Boolean> VAR_TABLE, HashMap<String, String> libs) {
		String out = "0";
		if(in.startsWith("$")) out = INPUTS.get(Integer.parseInt(in.replaceFirst("\\$", ""))) ? "1" : "0";
		else if(in.contains("(") && in.contains(")")) {
			String func_name = in.substring(0,in.indexOf("("));
			String input = in.substring(in.indexOf("(")+1, in.lastIndexOf(")"));
			if(func_name.equals("not")) out = value_fetcher(input, INPUTS, VAR_TABLE, libs).equals("1")? "0" : "1";
			else if(libs.containsKey(func_name)) {
				if(input.contains(",")) {
					String[] ins = input.split(",");
					StringBuilder outt = new StringBuilder();
					for(int i = 0;i<ins.length;i++) {
						outt.append(value_fetcher(ins[i], INPUTS, VAR_TABLE, libs));
						if(! (i+1 == ins.length)) outt.append(",");
					}
					input = outt.toString();
				}else input = value_fetcher(input, INPUTS, VAR_TABLE, libs);
			out = execute(libs.get(func_name), input);
			}else {
				System.out.println("ERROR : Requested library '"+func_name+"' isn't yet defined");
			}
		} else {
			if(VAR_TABLE.containsKey(in)) {
				out = VAR_TABLE.get(in)? "1" : "0";
			}else {
				out = in;
			}
		}
		return out;
	}
}

// Joseph Collins, Tyler Coverstone, Stephan Rotolante
// COP3404 Introduction to System Software
// SIC/XE Assembler

package sicxeassm;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Scanner;



public class SicXeAssm {

    //OPTABE and SYMTAB
    private static Hashtable<String, OPT> OPTAB;
    private static Hashtable<String, SYM> SYMTAB;
    //Location Counter and Starting Address    
    private static Integer LOCCTR;
    private static Integer STARTADDRESS;
    //Writing to Intermediate files
    private static FileWriter FILEWRITER;
    private static PrintWriter PRINTWRITER;
    //String to for supporting the looping based off OPCODE
    private static String OPCODE;
    
    public static void main(String[] args) {
        //Init OPTAB AND SYMTAB
        OPTAB = createOPTAB();
        SYMTAB = createSYMTAB();
        //Init FileWriting
        FILEWRITER = createFileWriter();
        PRINTWRITER = createPrintWriter(FILEWRITER);
        
        
        
        
        //PassOne
        passOne(args[0]);
        
        
        
        
        
        
        //System.out.println(Integer.toHexString(OPTAB.get("CLEAR").getOpcode()));
        
        
    }
    
    //Init Functions
    public static Hashtable<String, OPT> createOPTAB() {
        
        //Default load factor is .75
        Hashtable hashTable = new Hashtable(59);
        
        //Constant Directives
        hashTable.put("START",new OPT("START",0,0));
        hashTable.put("END",new OPT("END",0,0));
        hashTable.put("BYTE",new OPT("BYTE",0,0));
        hashTable.put("WORD",new OPT("WORD",0,0));
        hashTable.put("RESB",new OPT("RESB",0,0));
        hashTable.put("RESW",new OPT("RESW",0,0));
        
        //Mnemonics with format size and HEX converted to decimal
        //0x## tells the compiler to conver the number to its decimal form
        //First page of commands in APPENDIX A
        hashTable.put("ADD", new OPT("ADD",3,4,0x18));
        hashTable.put("ADDF", new OPT("ADDF",3,4,0x58));
        hashTable.put("ADDR", new OPT("ADDR",2,0x90));
        hashTable.put("AND", new OPT("AND",3,4,0x40));
        hashTable.put("CLEAR", new OPT("CLEAR",2,0xB4));
        hashTable.put("COMP", new OPT("COMP",3,4,0x28));
        hashTable.put("COMPF", new OPT("COMPF",3,4,0x88));
        hashTable.put("COMPR",new OPT("COMPR",2,0xA0));
        hashTable.put("DIV", new OPT("DIV",3,4,0x24));
        hashTable.put("DIVF", new OPT("DIVF",3,4,0x64));
        hashTable.put("DIVR", new OPT("DIVR",3,4,0x9C));
        hashTable.put("FIX", new OPT("FIX",1,0xC4));
        hashTable.put("FLOAT", new OPT("FLOAT",1,0xC0));
        hashTable.put("J", new OPT("J",3,4,0x3C));
        hashTable.put("JEQ", new OPT("JEQ",3,4,0x30));
        hashTable.put("JGT", new OPT("JGT",3,4,0x34));
        hashTable.put("JLT", new OPT("JLT",3,4,0x38));
        hashTable.put("JSUB", new OPT("JSUB",3,4,0x48));
        hashTable.put("LDA", new OPT("LDA",3,4,0x00));
        hashTable.put("LDB", new OPT("LDB",3,4,0x68));
        hashTable.put("LDCH", new OPT("LDCH",3,4,0x50));
        hashTable.put("LDF", new OPT("LDF",3,4,0x70));
        hashTable.put("LDL", new OPT("LDL",3,4,0x08));
        hashTable.put("LDS", new OPT("LDS",3,4,0x6C));
        hashTable.put("LDT", new OPT("LDT",3,4,0x74));
        hashTable.put("LDX", new OPT("LDX",3,4,0x04));
        hashTable.put("MUL", new OPT("MUL",3,4,0x20));
        
        //Second page begins ("This is purely so we can make sure its correct later")
        hashTable.put("MULF", new OPT("MULF",3,4,0x60));
        hashTable.put("MULR", new OPT("MULR",2,0x98));
        hashTable.put("NORM", new OPT("NORM",1,0xC8));
        hashTable.put("OR", new OPT("OR",3,4,0x44));
        hashTable.put("RD", new OPT("RD",3,4,0xD8));
        hashTable.put("RMO", new OPT("RMO",2,0xAC));
        hashTable.put("RSUB", new OPT("RSUB",3,4,0x4C));
        hashTable.put("SHIFTL", new OPT("SHIFTL",2,0xA4));
        hashTable.put("SHIFTR", new OPT("SHIFTR",2,0xA8));
        hashTable.put("STA", new OPT("STA",3,4,0x0C));
        hashTable.put("STB", new OPT("STB",3,4,0x78));
        hashTable.put("STCH", new OPT("STCH",3,4,0x54));
        hashTable.put("STF", new OPT("STF",3,4,0x80));
        hashTable.put("STS", new OPT("STS",3,4,0x7C));
        hashTable.put("STT", new OPT("STT",3,4,0x84));
        hashTable.put("STX", new OPT("STX",3,4,0x10));
        hashTable.put("SUB", new OPT("SUB",3,4,0x1C));
        hashTable.put("SUBF", new OPT("SUBF",3,4,0x5C));
        
        //Third and last page of APPENDIX A instructions
        hashTable.put("SUBR", new OPT("SUBR",2,0x94));
        hashTable.put("TD", new OPT("TD",3,4,0xE0));
        hashTable.put("TIX", new OPT("TIX",3,4,0x2C));
        hashTable.put("TIXR", new OPT("TIXR",2,0xB8));
        hashTable.put("WD", new OPT("WD",3,4,0xDC));
        
        //List of UNSUPPORTED COMMANDS FOR ERROR HANDLING
        //Set format to -1 for UNSUPPORTED COMMAND
        hashTable.put("LPS", new OPT("RD",-1,0));
        hashTable.put("SIO", new OPT("SIO",-1,0));
        hashTable.put("SSK", new OPT("SSK",-1,0));
        hashTable.put("STI", new OPT("STI",-1,0));
        hashTable.put("STSW", new OPT("STSW",-1,0));
        hashTable.put("SVC", new OPT("SVC",-1,0));
        hashTable.put("TIO", new OPT("TIO",-1,0));
        hashTable.put("HIO", new OPT("HIO",-1,0));
        hashTable.put("EQU", new OPT("EQU",-1,0));
        hashTable.put("USE", new OPT("USE",-1,0));
        hashTable.put("CSECT", new OPT("CSECT",-1,0));
          
        return hashTable;
    }
    public static Hashtable<String, SYM> createSYMTAB(){
        return new Hashtable();
    }
    public static FileWriter createFileWriter(){
        try {
        return new FileWriter("intermediate.txt");
        }
        catch(IOException e){
            e.printStackTrace();
            System.out.println("Error writing to intermediate.txt!");
            return null;
        }
    }
    public static PrintWriter createPrintWriter(FileWriter filewriter){
        return new PrintWriter(filewriter);
    }
    //Add to SYMTAB
    public static void addToSYMTAB(String label, SYM sym){
        if(SYMTAB.contains(label)){
            SYMTAB.get(label).setFlags("Duplicate Label");
        }
        else{      
        SYMTAB.put(label,sym); 
        }
    }
    
    public static void passOne(String filename){
        try {
            File file = new File(filename);
            Scanner scan = new Scanner(file);
            
            //Begins the parsing of the first line
            String firstLine = scan.nextLine();
            
            //Checks to make sure the line is not whitespace
            if(firstLine.isEmpty() != true){
                
                String[] line = firstLine.split("\\s+");
                
               //If START is the OPCODE for the second string
                if(OPTAB.get(line[1]).getOP() == "START"){
                    
                writeToFile(line[0]); //This Will be Name of Program
                writeToFile(line[1]); //This will be OPCODE Start
                writeToFile(line[2]); //This will be LOCCTR INIT
                
                //INIT Starting Address and Location Counter
                LOCCTR = Integer.parseInt(line[2]);
                STARTADDRESS = LOCCTR;
                OPCODE = line[1].trim();
                //Write to file
                writeToFile(Integer.toHexString(LOCCTR));
                writeToFile("");
                }
                else {
                    writeToFile("E:START NOT FOUND IN PROGRAM.");
                    System.out.println("Could not find START of programs.");
                }
                
            }
            
            //Begins the loop for reading the rest of the program
            //First makes sure the line is not whitespace
            //If the line is whitespace then it moves to the next
            firstLine = scan.nextLine();
            
            while(!OPCODE.equals("END")){
               
                if(scan.hasNextLine()){
                    firstLine = scan.nextLine();
                }
                //Loop until non-empty line
                while(firstLine.isEmpty() == true && scan.hasNextLine() == true){
                    firstLine = scan.nextLine();
                }
                //Split based on whitespace
                
                String[] line = firstLine.trim().split("\\s+");
                
                //Length of three means LABEL OPCODE OPERAND
                if(line.length == 3){
                    OPCODE = line[1];
                    
                    writeToFile(line[0]);
                    writeToFile(line[1]);
                    
                    
                    if(SYMTAB.get(line[0]) == null){
                        SYMTAB.put(line[0],new SYM(line[0],LOCCTR));
                    }
                    else {
                        SYMTAB.get(line[0]).setFlags("DUPLICATE DECLARATION");
                    }
                    
                    if(OPTAB.get(line[1]).getOP().equals("WORD")){
                        writeToFile(line[2]);
                        writeToFile(Integer.toHexString(LOCCTR));
                        LOCCTR += 3;
                         writeToFile(Integer.toHexString(LOCCTR));
                        
                    }
                    else if(OPTAB.get(line[1]).getOP().equals("RESW")){
                        writeToFile(Integer.toHexString(LOCCTR));
                        LOCCTR += 3 * Integer.parseInt(line[2]);
                        writeToFile(Integer.toHexString(LOCCTR));
                    }
                    else if(OPTAB.get(line[1]).getOP().equals("RESB")){
                        writeToFile(Integer.toHexString(LOCCTR));
                        LOCCTR += Integer.parseInt(line[2]);
                        writeToFile(Integer.toHexString(LOCCTR));
                    }
                    else if(OPTAB.get(line[1]).getOP().equals("BYTE")){
                        writeToFile(line[2]);
                        writeToFile(Integer.toHexString(LOCCTR));
                        writeToFile(Integer.toHexString(LOCCTR));
                   
                    }
                    else {
                        writeToFile(line[2]);
                        writeToFile(Integer.toHexString(LOCCTR));
                       LOCCTR += OPTAB.get(line[1]).getFormat1();
                        writeToFile(Integer.toHexString(LOCCTR));
                    }

                    writeToFile("");
                    
                }
                //Length of two means OPCODE OPERAND
                else if(line.length == 2){
                    OPCODE = line[0];
                    writeToFile(line[0]);
                    writeToFile(line[1]);
                    writeToFile(Integer.toHexString(LOCCTR));
                   
                    if(OPTAB.get(line[0]) != null){
                        LOCCTR += OPTAB.get(line[0]).getFormat1();
                    }
                    if(line[0].charAt(0) == '+'){
                        //Checks to see if FORMAT 4 is supported
                        if(OPTAB.get(line[0].substring(1)).getFormat2() == null){
                            System.out.println("Error FORMAT 4 NOT SUPPORTED");
                            writeToFile("E:FORMAT 4 NOT SUPPORTED WITH OPCODE");
                            writeToFile("");
                        }
                        else {
                            LOCCTR += 4;
                            writeToFile(Integer.toHexString(LOCCTR));
                            writeToFile("");
                        }
                    }
                    else{
                        writeToFile(Integer.toHexString(LOCCTR));
                        writeToFile("");
                    }
                    
                }
                
            }
            
                
                
                
            
            
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
            System.out.println("The File Was Not Found!");
        }
        
        closeFile();
    }
    
    //File Writing Functions
    public static void writeToFile(String line){
        PRINTWRITER.print(line+"\n");
    }
    public static void closeFile(){
        PRINTWRITER.close();
    }
}

class OPT {
    
    private String Mnemonic;
    private Integer FORMAT1;
    private Integer FORMAT2;
    private Integer OPCODE;
    
    //Constructors to support Format 1 and Format 2 codes
    OPT(String name, Integer form1, Integer form2, Integer opcode){
        this.Mnemonic = name;
        this.FORMAT1 = form1;
        this.FORMAT2 = form2;
        this.OPCODE = opcode;
    }
    OPT(String name, Integer form1, Integer opcode){
        this.Mnemonic = name;
        this.FORMAT1 = form1;
        this.FORMAT2 = null;
        this.OPCODE = opcode;
    }
    
    //Setter Functions
    public void setOP(String name){
        this.Mnemonic = name;
    }
    public void setFormat1(Integer format){
        this.FORMAT1 = format;
    }
    public void setFormat2(Integer format){
        this.FORMAT2 = format;
    }
    public void setOpcode(Integer op){
        this.OPCODE = op;
    }
    
    //Getter Functions
    public String getOP(){
        return this.Mnemonic;
    }
    public Integer getFormat1(){
        return this.FORMAT1;
    }
    public Integer getFormat2(){
        return this.FORMAT2;
    }
    public Integer getOpcode(){
        return this.OPCODE;
    }
    
}

class SYM {
    private String LABEL;
    private Integer ADDRESS;
    private String FLAGS;
    
    SYM(String label, Integer address){
        this.LABEL = label;
        this.ADDRESS = address;
        this.FLAGS = null;
    }
    SYM(String label, Integer address, String flag){
        this.LABEL = label;
        this.ADDRESS = address;
        this.FLAGS = flag;
    }
    
    //Setters
    public void setLabel(String label){
        this.LABEL = label;
    }
    public void setAddress(Integer address){
        this.ADDRESS = address;
    }
    public void setFlags(String flag){
        this.FLAGS = flag;
    }
    
    //Getters
    public String getLabel(){
        return this.LABEL;
    }
    public Integer getAddress(){
        return this.ADDRESS;
    }
    public String getFlags(){
        return this.FLAGS;
    }
}

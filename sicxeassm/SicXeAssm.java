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
import java.util.ArrayList;
import java.util.Stack;



public class SicXeAssm {

    //OPTABE and SYMTAB
    private static Hashtable<String, OPT> OPTAB;
    private static Hashtable<String, SYM> SYMTAB;
    //Location Counter and Starting Address    
    public static Integer LOCCTR;
    public static Integer STARTADDRESS;
    public static Integer PROGRAMLENGTH;
    
    //Writing to Intermediate files and OUTPUT FILES
    private static FileWriter FILEWRITER;
    private static FileWriter FILEWRITEROBJ;
    private static FileWriter FILEWRITERLST;
    
    private static PrintWriter PRINTWRITER;
    //String to for supporting the looping based off OPCODE
    private static String OPCODE;
    private static String ERROR;
    
    public static void main(String[] args) {
        //Init OPTAB AND SYMTAB
        OPTAB = createOPTAB();
        SYMTAB = createSYMTAB();
        //Init FileWriting
        FILEWRITER = createFileWriter("intermediate.txt");
        
        FILEWRITEROBJ = createFileWriter(args[0]+".obj");
        FILEWRITERLST = createFileWriter(args[0]+".lst");
        
        PRINTWRITER = createPrintWriter(FILEWRITER);
        
        //PassOne
        passOne(args[0]);
        
        passTwo("intermediate.txt");
        
        
        
        
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
        hashTable.put("BASE", new OPT("BASE",0,0));
        
        
        //Mnemonics with format size and HEX converted to decimal
        //0x## tells the compiler to convert the number to its decimal form
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
        Hashtable hash = new Hashtable();
        hash.put("A",new SYM("A",0));
        hash.put("X",new SYM("X",1));
        hash.put("L",new SYM("L",2));
        hash.put("B",new SYM("B",3));
        hash.put("S",new SYM("S",4));
        hash.put("T",new SYM("T",5));
        hash.put("F",new SYM("F",6));
        hash.put("PC",new SYM("PC",8));
        hash.put("SW",new SYM("SW",9));
        return hash;
    }
    
    public static FileWriter createFileWriter(String fileName){
        try {
        return new FileWriter(fileName);
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
            //INIT File
            File file = new File(filename);
            Scanner scan = new Scanner(file);
            
            //INIT Variables
            String SYMBOL = null;
            String OPCODE = null;
            String OPERAND = null;
            String COMMENT = null;
            Integer LOCCTR = null;
            
            
            //Begins the parsing of the first line
            String firstLine = scan.nextLine();
            
            //Loops until it finds the first line and it is not empty
            while(firstLine.isEmpty() == true && scan.hasNextLine() == true){
                    firstLine = scan.nextLine();
                }
            
            //Spilts the based on characters trailed by whitespace
            String[] line = firstLine.trim().split("\\s+");
            
            //Checks to see this is a comment line
            if(line[0].charAt(0) == '.'){
                writeToFile(firstLine.trim());
                firstLine = scan.nextLine();
            }
            //Checks if OPCODE == START
            else {
                if(line[1].equals("START")){

                    OPCODE = line[1].trim();
                    LOCCTR = Integer.parseInt(line[2]);
                    STARTADDRESS = Integer.parseInt(line[2]);
                    writeToFile(line[0]);
                    writeToFile(OPCODE);
                    writeToFile(LOCCTR.toString());
                    writeToFile(LOCCTR.toString());
                    writeToFile("");
                }
            }
            
            while(!OPCODE.equals("END")){
                
                SYMBOL = null;
                OPERAND = null;
                COMMENT = null;
                setErrors(null);
                
                
                if(scan.hasNextLine()){
                    firstLine = scan.nextLine();
                }
                //Loop until line is not empty
                while(firstLine.isEmpty() == true && scan.hasNextLine() == true){
                    firstLine = scan.nextLine();
                }
                
                ArrayList<String> correctLine = new ArrayList<String>();
                
                //Split based off whitespace
                line = firstLine.trim().split("\\s+");
                
                int index = -1;
                for(int i = 0; i < line.length; i++){
                    if(line[i].charAt(0) == '.'){
                        index = i;
                    }
                }
                if(index != -1){
                        COMMENT = "";
                    for(int i = index; i < line.length; i++){
                        COMMENT = COMMENT.concat(line[i]+" ");
                    }
                    for(int i =0; i < index; i++){
                        correctLine.add(line[i]);
                    }
                    correctLine.add(COMMENT);
                }
                else {
                    for(int i =0; i < line.length; i++){
                        correctLine.add(line[i]);
                    }
                }
                
                
                
                
                //Check if line is a comment
                if(COMMENT != null && correctLine.size() == 1){
                    
                    writeToFile(COMMENT);
                    writeToFile("");
                }
                else {
                    String wordOne = correctLine.get(0);
                    String wordTwo = correctLine.get(0).substring(1);
                    
                    
                    if(OPTAB.containsKey(wordOne) || OPTAB.containsKey(wordTwo)){
                        
                        OPCODE = correctLine.get(0);
                        //OPCODE
                        if(correctLine.size() == 1){
                            writeToFile(OPCODE);
                            writeToFile(Integer.toHexString(LOCCTR));
                            LOCCTR  = incLOCCTR(OPCODE,OPERAND,LOCCTR);
                            writeToFile(Integer.toHexString(LOCCTR));
                            if(ERROR != null){
                                writeToFile(ERROR);
                            }
                            writeToFile("");
                        }
                        else if(correctLine.size() == 2){
                            //OPCODE COMMENT
                            if(COMMENT != null){
                                writeToFile(OPCODE);
                                writeToFile(Integer.toHexString(LOCCTR));
                                LOCCTR  = incLOCCTR(OPCODE,OPERAND,LOCCTR);
                                writeToFile(Integer.toHexString(LOCCTR));
                                writeToFile(COMMENT);
                                if(ERROR != null){
                                writeToFile(ERROR);
                                }
                                writeToFile("");
                                
                            }
                            //OPCODE OPERAND
                            else {
                                OPERAND = correctLine.get(1);
                                writeToFile(OPCODE);
                                writeToFile(OPERAND);
                                writeToFile(Integer.toHexString(LOCCTR));
                                LOCCTR  = incLOCCTR(OPCODE,OPERAND, LOCCTR);
                                writeToFile(Integer.toHexString(LOCCTR));
                                if(ERROR != null){
                                writeToFile(ERROR);
                                }
                                writeToFile("");
                            }
                        }
                        //OPCODE OPERAND COMMENT
                        else if(correctLine.size() == 3){
                                OPERAND = correctLine.get(1);
                                
                                writeToFile(OPCODE);
                                writeToFile(OPERAND);
                                writeToFile(Integer.toHexString(LOCCTR));
                                LOCCTR  = incLOCCTR(OPCODE,OPERAND, LOCCTR);
                                writeToFile(Integer.toHexString(LOCCTR));
                                writeToFile(COMMENT);
                                if(ERROR != null){
                                writeToFile(ERROR);
                                }
                                writeToFile("");
                        }
                        
                    }
                    else {
                        SYMBOL = correctLine.get(0);
                        OPCODE = correctLine.get(1);
                        OPERAND = correctLine.get(2);
                        
                        
                        if(SYMTAB.contains(SYMBOL)){
                            ERROR = "THIS IS A DUPLICATE SYMBOL";
                        }
                        else {
                            SYMTAB.put(SYMBOL, new SYM(SYMBOL,LOCCTR));
                        }
                        //SYMBOL DIRECTIVE
                        if(correctLine.size() == 3){
                            writeToFile(SYMBOL);
                            writeToFile(OPCODE);
                            writeToFile(OPERAND);
                            writeToFile(Integer.toHexString(LOCCTR));
                            LOCCTR  = incLOCCTR(OPCODE, OPERAND, LOCCTR);
                            writeToFile(Integer.toHexString(LOCCTR));
                            if(ERROR != null){
                                writeToFile(ERROR);
                               } 
                            writeToFile("");
                            
                        }
                        else {
                            writeToFile(SYMBOL);
                            writeToFile(OPCODE);
                            writeToFile(OPERAND);
                            writeToFile(Integer.toHexString(LOCCTR));
                            LOCCTR = incLOCCTR(OPCODE, OPERAND, LOCCTR);
                            writeToFile(Integer.toHexString(LOCCTR));
                            writeToFile(COMMENT);
                            if(ERROR != null){
                                writeToFile(ERROR);
                               } 
                            writeToFile("");
                        }
                    
                    
                    }
                 
                }
       
            }
            
            
            
            
            
           
            
             
               
                
                
            
        PROGRAMLENGTH = LOCCTR - STARTADDRESS;    
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
            System.out.println("The File Was Not Found!");
        }
        
        closeFile();
    }
    
    public static void passTwo(String filename){
        PRINTWRITER = createPrintWriter(FILEWRITEROBJ); 
        try{
            //INIT File
            File file = new File(filename);
            Scanner scan = new Scanner(file);
            String nameOfProgram = scan.nextLine().trim();
            String OPCODE = scan.nextLine().trim();
            ArrayList<String> INPUT = new ArrayList();
            
            
            while(nameOfProgram == null){
                nameOfProgram = scan.nextLine();
            }
            
            if(OPCODE.equals("START")){
                PRINTWRITER.printf('H'+"%-6.6s"+"%06X"+"%06X"+"\n",nameOfProgram,STARTADDRESS,PROGRAMLENGTH);
            }
            
            //Begins TextRecords and Assembling of Object code.
            //This looping moves the scanner to the right position in the intermediate file
            String key = scan.nextLine().trim();
                while(key.length() != 0){
                    key = scan.nextLine();
                }
                key = scan.nextLine();
                
                
            while(!OPCODE.equals("END")){
                
                
                String STARTADDRESS = "";
                String ENDADDRESS = "";
                String VALUE = "";
                String SYMBOL = "";
                String COMMENT = "";
                        
                
                
                while(key.length() != 0){
                   INPUT.add(key);
                   key = scan.nextLine();
                }
                
                int length = INPUT.size();
                
                for(int i = 0; i < length; i++){
                    //System.out.println(INPUT.get(i));
                }
                
                if(isComment(INPUT.get(0)) && INPUT.size() == 1){
                    COMMENT = INPUT.get(0);
                }
                else {
                    OPCODE = INPUT.get(0);
                }
                
                if(INPUT.size() > 1 ){
                    String checkOP = "";
                    if(INPUT.get(0).contains("+") && OPTAB.containsKey(INPUT.get(0).substring(1))){
                        checkOP = INPUT.get(0).substring(1);
                    }
                    else {
                        checkOP = INPUT.get(0);
                    }
                    
                    if(OPTAB.containsKey(checkOP)){
                        OPCODE = checkOP;
                        String subSYM = "";
                        if(INPUT.get(1).contains(",")){
                            int endSub = INPUT.get(1).indexOf(",");
                            subSYM = INPUT.get(1).substring(0,endSub);
                        }
                        else {
                            subSYM = INPUT.get(1);
                        }
                        if(SYMTAB.containsKey(INPUT.get(1))|| SYMTAB.containsKey(subSYM)){
                        SYMBOL = subSYM;
                        VALUE = INPUT.get(1);
                        STARTADDRESS = INPUT.get(2);
                        ENDADDRESS = INPUT.get(3);
                        }
                        else if(!OPCODE.equals("END")){

                        STARTADDRESS = INPUT.get(1);
                        ENDADDRESS = INPUT.get(2);
                        }
                        else {
                            STARTADDRESS = "0";
                            ENDADDRESS = "0";
                        }
                    }
                    else {
                        SYMBOL = INPUT.get(0);
                        OPCODE = INPUT.get(1);
                        VALUE = INPUT.get(2);
                        STARTADDRESS = INPUT.get(3);
                        ENDADDRESS = INPUT.get(4);
                        
                    }
                }
                
                if(!isComment(INPUT.get(0))){
                INSTRUCTION INS = new INSTRUCTION(OPCODE,SYMBOL,ENDADDRESS,STARTADDRESS,VALUE);
                    int address = -1;
                if(SYMTAB.containsKey(SYMBOL)){
                    address = SYMTAB.get(SYMBOL).getAddress();
                }
                INS.createObjCode(OPTAB.get(OPCODE).getOpcode(),address);
                System.out.println(INS.toString());
                }
                
                
                INPUT.clear();
                
                if(scan.hasNextLine()){
                key = scan.nextLine();
                OPCODE = "";
                }
            }
            
            
            
         }
         catch(FileNotFoundException e){
            e.printStackTrace();
            System.out.println("The File Was Not Found!");
        }
        closeFile(); 
    }
    
    //LOCCTR
    public static Integer incLOCCTR(String OPCODE, String OPERAND, Integer LOCCTR){
        if(OPCODE.charAt(0) == '+'){
            String substring = OPCODE.substring(1);
            if(OPTAB.get(substring).getFormat2() != null){
                return LOCCTR += OPTAB.get(substring).getFormat2();
                
            }
            else {
                return LOCCTR;
            }
        }
        else {
            if(OPCODE.equals("WORD")){
                return LOCCTR += 3;
            }
            else if(OPCODE.equals("RESW")){
                return LOCCTR += 3 * Integer.parseInt(OPERAND);
                
            }
            else if(OPCODE.equals("RESB")){
                return LOCCTR += Integer.parseInt(OPERAND);
                
            }
            else if(OPCODE.equals("BYTE")){
                char[] charArray = OPERAND.toCharArray();
                Integer amountToAdd = 0;
                
                if(charArray[0] == 'X'){
                    int i = 2;
                    while(charArray[i] != '\''){
                        amountToAdd++;
                        i++;
                    }
                    amountToAdd = amountToAdd/2;
                }
                else if(charArray[0] == 'C'){
                    int i = 2;
                    while(charArray[i] != '\''){
                        amountToAdd++;
                        i++;
                    }
                }
                else {
                    setErrors("UNRECOGNIZED CHARACTERS");
                }
                return LOCCTR += amountToAdd;
            }
            else {
                if(OPTAB.get(OPCODE).getFormat1() != -1){
                   return LOCCTR += OPTAB.get(OPCODE).getFormat1();
                }
                else {
                    setErrors("FORMAT/COMMAND NOT SUPPORTED");
                    return LOCCTR;
                }
                
            }
            
        }
    }
    
    
    //File Writing Functions
    public static void writeToFile(String line){
        PRINTWRITER.print(line+"\n");
    }
    public static void writeFormat(String line){
        
    }
    public static void closeFile(){
        PRINTWRITER.close();
    }
    public static void setErrors(String error){
        ERROR = error;
    }
    
    
    public static boolean isOpcode(String OP){
        if(OPTAB.containsKey(OP)){
            return true;
        }
        else {
            return false;
        }
    }
    public static boolean isComment(String COM){
        if(COM.charAt(0) == '.'){
            return true;
        }
        else {
            return false;
        }
    }
    public static boolean isSymbol(String SYM){
        if(SYMTAB.containsKey(SYM)){
            return true;
        }
        else {
            return false;
        }
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

class INSTRUCTION {
    public static String OPCODE;
    private static String SYMBOL;
    private static Integer FORMAT;
    public static String VALUES;
   
    
    public INSTRUCTION(String OPCODE,String SYMBOL, String ENDADDRESS, String STARTADDRESS, String VALUES){
        this.OPCODE = OPCODE;
        this.SYMBOL = SYMBOL;
        this.FORMAT = setFormat(ENDADDRESS,STARTADDRESS);
        this.VALUES = VALUES;
    }
    
    public static void createObjCode(Integer z, Integer SYMLOC){
        String OBJCODE = "";
        
        if(OPCODE != null){
            switch(FORMAT){
                case 1:
                    OBJCODE = String.format("%-2.02X",z);
                    break;
                case 2:
                    OBJCODE = String.format("%04X",z);
                    break;
                case 3:
                   
                   int n = 1 << 5;
                   int i = 1 << 4;
                   int x = 1 << 3;
                   int b = 1 << 2;
                   int p = 1 << 1;
                   int e = 1 << 0;
                   
                   int binaryCode = z << 4;
                   String operand = VALUES;
                   
                   
                   if(operand == ""){
                       binaryCode = (binaryCode| n |i) << 12;
                   }
                   else {
                       switch(operand.charAt(0)){
                           case '#':
                               binaryCode |= i;
                               operand = operand.substring(1);
                               break;
                           case '@':
                               binaryCode |= n;
                               operand = operand.substring(1);
                           default:
                               binaryCode |= n | i;
                               
                               if(operand.contains(",")){
                                    binaryCode |= x;   
                               }
                               break;
                           
                       }
                       
                   
                   }
                   int displacement;
                   if(SYMLOC == -1){
                       displacement = Integer.parseInt(operand);
                   } else {
                       int targetAddress = SYMLOC;
                       displacement = targetAddress;
                       
                   }
                   
                   
                   System.out.println(Integer.toHexString(binaryCode));
                   break;
                case 4:
                   OBJCODE = OBJCODE = String.format("%08X",z);
                   break;
                default:
                    OBJCODE = "DEFAULT";
                    break;
            }
        }
        else {
            
        }
        System.out.println(OBJCODE+" ");
        
    }
    
    
    public static Integer setFormat(String END, String START){
        int x = Integer.parseInt(END,16);
        int y = Integer.parseInt(START,16);
        
        
        int value = x-y;
        return value;
    }
    
    @Override
    public String toString(){
        String text;
        if(SYMBOL != null){
            text = OPCODE + " "+SYMBOL+" "+FORMAT+" "+VALUES;
        }
        else if (!OPCODE.equals("END")){
            text = OPCODE +" "+FORMAT;
        }
        else {
            text = OPCODE;
        }
        return text;
    }
}
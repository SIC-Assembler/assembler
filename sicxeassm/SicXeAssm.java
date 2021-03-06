// Joseph Collins, Tyler Coverstone, Stephan Rotolante
// COP3404 Introduction to System Software
// SIC/XE Assembler
//30 November 2017

package sicxeassm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Scanner;



public class SicXeAssm {
    private static int LOCCTR;
    private static int STARTADDRESS;
    private static int PROGRAMLENGTH;
    private static int BASEADDRESS;
    public int LASTENTEREDADDRESS;
    public static boolean startedText;
    private static String PROGRAMNAME;
    private static Hashtable<String, OPT> OPTAB;
    private static Hashtable<String, SYM> SYMTAB;
    public static ArrayList<INSTRUCTION> INSTRUCTIONS;
    public static ArrayList<ModRecord> MODRECORDS;
    public static ListIterator<INSTRUCTION> LISTINSTRUCTIONS;
    public static ListIterator<ModRecord> LISTMODS;
    
    public static void main(String[] args) {
        OPTAB = INITIALIZERS.getOPTAB();
        SYMTAB = INITIALIZERS.getSYMTAB();
        
        try {
            passOne(args[0]);
            passTwo(args[0]);
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
            System.out.println("FILE NOT FOUND!");
        }
        
    }
    
    
    public static void passOne(String filename) throws FileNotFoundException{
        File file = new File(filename);
        Scanner fileScanner = new Scanner(file);
        
        FileWriter Intermediate = createFileWriter("Intermediate.txt");
        PrintWriter intWriter = createPrintWriter(Intermediate);
        
        INSTRUCTIONS = new ArrayList();
        String SYMBOL = null;
        String OPCODE = null;
        String OPERAND = null;
        String COMMENT = null;
        LOCCTR = 0;
        
        String inputOfLine = fileScanner.nextLine().trim();
        while(isLineEmpty(inputOfLine) && fileScanner.hasNextLine() == true){
            inputOfLine = fileScanner.nextLine().trim();  
        }
        
        String[] operations = inputOfLine.trim().split("\\s+");
        
        if(operations[1].equals("START")){
            PROGRAMNAME = operations[0];
            OPCODE = operations[1];
            STARTADDRESS = Integer.parseInt(operations[2]);
            LOCCTR = Integer.parseInt(operations[2]);
        }
        
        inputOfLine = fileScanner.nextLine().trim();
        
        
        while(!OPCODE.equals("END")){
            OPCODE = "";
            SYMBOL = "";
            OPERAND = "";
            COMMENT = "";
            String[] operate;
            
            while(isLineEmpty(inputOfLine) && fileScanner.hasNextLine()){
                inputOfLine = fileScanner.nextLine();
            }
            if(inputOfLine.startsWith(".")){
                COMMENT = inputOfLine.trim();   
            }
            else {
                if(inputOfLine.contains(".") & inputOfLine.charAt(0) != '.'){
                operations = inputOfLine.trim().split("\\.");
                COMMENT = "."+operations[1];
                operate = operations[0].trim().split("\\s+");
                }
                 else {
                operate = inputOfLine.trim().split("\\s+");
                }
                if(operate.length == 3){
                    SYMBOL = operate[0];
                    OPCODE = operate[1];
                    OPERAND = operate[2];
                }else if(operate.length == 2){
                    OPCODE = operate[0];
                    OPERAND = operate[1];
                }else if(operate.length == 1){
                    OPCODE = operate[0];
                }else if(operate == null){
                
                }
            }
            
            if(!SYMBOL.equals("")){
                if(SYMTAB.contains(SYMBOL)){
                    System.out.println("DUPLICATE SYMBOL");
                }
                else {
                    SYMTAB.put(SYMBOL,new SYM(SYMBOL,LOCCTR));
                }
            }
            
            if(!OPCODE.equals("")){
                
                INSTRUCTION ins = new INSTRUCTION(SYMBOL,OPCODE,OPERAND,LOCCTR);
                int length = LOCCTR;
                
                if(COMMENT != ""){
                    ins.setComment(COMMENT);
                }
                if(OPTAB.get(ins.OPCODE).getFormat1() == -1){
                    ins.setErrors(ins.OPCODE+" is not supported");
                }
                
                addToList(ins);
                
                LOCCTR = incLOCCTR(LOCCTR,OPCODE,OPERAND);
                length = LOCCTR - length;
                ins.setLength(length);
                //System.out.println(ins);
                if(COMMENT != ""){
                    intWriter.print(ins+" "+COMMENT+"\n");
                } else {
                    intWriter.print(ins+"\n");
                }
                      
                        
            }
            
            if(fileScanner.hasNextLine()){
                inputOfLine = fileScanner.nextLine();
            }
            
        }
        intWriter.close();
        
        PROGRAMLENGTH = LOCCTR - STARTADDRESS;
    }
    
    public static void passTwo(String filename){
        FileWriter OBJECTCODE = createFileWriter(filename+".obj");
        FileWriter LSTFILE = createFileWriter(filename+".lst");
        
        MODRECORDS = new ArrayList();
        
        PrintWriter toOBJ = createPrintWriter(OBJECTCODE);
        PrintWriter toLST = createPrintWriter(LSTFILE);
        
        
        toOBJ.print(new HeaderRecord(PROGRAMNAME,STARTADDRESS,PROGRAMLENGTH));
        
        LISTINSTRUCTIONS = INSTRUCTIONS.listIterator();
        while(LISTINSTRUCTIONS.hasNext()){
            INSTRUCTION ins = LISTINSTRUCTIONS.next();
            
            if(ins.OPCODE.equals("BASE")){
                BASEADDRESS = SYMTAB.get(ins.OPERAND).getAddress();
            }else {    
            String objCode = createObjectCode(ins);
            ins.setObjectCode(objCode);
            }          
        } 
        LISTINSTRUCTIONS = INSTRUCTIONS.listIterator();
        startedText = false;
        int lastRecord = STARTADDRESS;
        int lastLength = 0;
        TextRecord text = new TextRecord(STARTADDRESS);
        
        while(LISTINSTRUCTIONS.hasNext()){
            INSTRUCTION ins = LISTINSTRUCTIONS.next();
            
            if(startedText == false){
                
                text = new TextRecord(ins.ADDRESS);
                text.add(ins.OBJECTCODE);
                lastRecord = ins.ADDRESS;
                lastLength = ins.LENGTH;
                
                startedText = true;
            } else if((text.LENGTH + ins.LENGTH) >= 30 ) {
                toOBJ.print(text+"\n");
                TextRecord nextText = new TextRecord(ins.ADDRESS);
                nextText.add(ins.OBJECTCODE);
                lastRecord = ins.ADDRESS;
                lastLength = ins.LENGTH;
                text = nextText;
            } else if(ins.ADDRESS != (lastRecord + lastLength)){ 
                toOBJ.print(text+"\n");
                TextRecord nextText = new TextRecord(ins.ADDRESS);
                nextText.add(ins.OBJECTCODE);
                lastRecord = ins.ADDRESS;
                lastLength = ins.LENGTH;
                text = nextText;
            } else if(ins.OPCODE.equals("END")){
                toOBJ.print(text+"\n");
            }
            else {
                text.add(ins.OBJECTCODE);
                lastRecord = ins.ADDRESS;
                lastLength = ins.LENGTH;
            }
            if(ins.isFormat4 & !ins.isImmediate){
                MODRECORDS.add(new ModRecord(ins.ADDRESS));
            }
            
        }
        LISTMODS = MODRECORDS.listIterator();
        
        int x = 0;
        
       while(LISTMODS.hasNext()){
               LISTMODS.next();
               ModRecord mod = MODRECORDS.get(x);
               toOBJ.print(mod+"\n");
               x++;
        } 
       
       
  
       EndRecord endRecord = new EndRecord(STARTADDRESS);
       toOBJ.print(endRecord);
       
       LISTINSTRUCTIONS = INSTRUCTIONS.listIterator();
       
       String lstFormat = String.format("%-15s%-15s%-16s%-60s\n","Line Number","Location","Object Code", "Source Code");
       toLST.print(lstFormat);
       
       int lineNumber = 1;
       int y = 0;
       while(LISTINSTRUCTIONS.hasNext()){
           LISTINSTRUCTIONS.next();
           INSTRUCTION ins = INSTRUCTIONS.get(y);
           String insFormat = String.format("%-15d%-15s%-16s%-15s%-15s%-15s%-30s",lineNumber,Integer.toHexString(ins.ADDRESS).toUpperCase(),ins.OBJECTCODE,ins.SYMBOL,ins.OPCODE,ins.OPERAND,ins.COMMENT);
           if(ins.ERRORS != null){
               toLST.print("ERROR: "+ins.ERRORS+"\n");
           }
           lineNumber++;
           y++;
           toLST.print(insFormat+"\n");
       }
       
       
       
       
       toOBJ.close();
       toLST.close();
    }
    
    
    
    public static boolean isLineEmpty(String line){
        if(line.isEmpty()){
            return true;
        }
        else {
            return false;
        }
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
    public static PrintWriter createPrintWriter(FileWriter file){
        return new PrintWriter(file);
    }
    public static void addToList(INSTRUCTION x){
        INSTRUCTIONS.add(x);
    }
    public static int incLOCCTR(int LOCCTR, String OPCODE, String OPERAND){
        String substring = "";
        if(OPCODE.charAt(0) == '+'){
            substring = OPCODE.substring(1);
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
            else if (OPCODE.equals("BASE")){
                return LOCCTR;
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
                    
                }
                return LOCCTR += amountToAdd;
            }
            else {
                if(OPTAB.get(OPCODE).getFormat1() != -1){
                   return LOCCTR += OPTAB.get(OPCODE).getFormat1();
                }
                else {
                    
                    return LOCCTR;
                }
                
            }
            
        }
        
        
        
        
        
        
    }
    
    public static String createObjectCode(INSTRUCTION ins){
        
        String OBJECTCODE = "";
        int BINCODE = 0;
        
        

        if(ins.isDirective){
            if(ins.OPCODE.equals("WORD")){
                BINCODE = Integer.parseInt(ins.OPERAND);
                return OBJECTCODE = String.format("%06X",BINCODE);     
            } else if(ins.OPCODE.equals("BASE")){
                System.out.println(ins.OPERAND);
                BASEADDRESS = SYMTAB.get(ins.OPERAND).getAddress();
            } else if(ins.OPCODE.equals("NOBASE")){
                BASEADDRESS = 0;
            } else if(ins.OPCODE.equals("BYTE")){
                char testChar = ins.OPERAND.charAt(0);
                
                int start = ins.OPERAND.indexOf('\'');
                int end = ins.OPERAND.length() - 1;
                
                String charToParse = ins.OPERAND.substring(start+1, end);
                
                if(testChar == 'C'){
                    for(int i = 0; i < charToParse.length(); i++){
                        
                         OBJECTCODE += Integer.toHexString(charToParse.charAt(i)).toUpperCase();
                        
                    }
                    return OBJECTCODE;
                    
                    
                } else if(testChar == 'X'){
                        ins.setLength(charToParse.length()/2);
                        return OBJECTCODE = charToParse;
                        
                }
                
                
                
            }
        } else if(OPTAB.containsKey(ins.OPCODE)){
            
            switch (ins.LENGTH){
                case 1:
                    OBJECTCODE = Integer.toHexString(OPTAB.get(ins.OPCODE).getOpcode());
                    break;
                case 2:
                    OBJECTCODE = Integer.toHexString(OPTAB.get(ins.OPCODE).getOpcode()).toUpperCase();
                    String format = String.format("%01X",SYMTAB.get(ins.OPERANDS[0]).getAddress());
                    OBJECTCODE += format;
                    if(ins.OPERANDS[1] != null ){
                        format = Integer.toHexString(SYMTAB.get(ins.OPERANDS[1]).getAddress());
                    } else {
                        format = Integer.toHexString(0);
                    }
                    OBJECTCODE += format;
                    break;
                case 3:
                case 4:
                    int n = 1 << 5;
                    int i = 1 << 4;
                    int x = 1 << 3;
                    int b = 1 << 2;
                    int p = 1 << 1;
                    int e = 1;
                    
                    BINCODE = OPTAB.get(ins.OPCODE).getOpcode() << 4;
                    
                    String operand = ins.OPERAND;
                    int outsideDisplacement = 0;
                    boolean hasEntered = false;
                    if(ins.OPCODE.equals("RSUB")){
                        BINCODE |= n | i;
                        BINCODE = (BINCODE) << 12;
                    } else {
                        if(ins.isImmediate){
                            BINCODE |= i;
                            operand = operand.substring(1);
                        } else if(ins.isIndirect) {
                            BINCODE |= n;
                            operand = operand.substring(1);
                        } else {
                            BINCODE |= n | i;
                            if(ins.isIndexed){
                                BINCODE |= x;
                            }
                        }
                    }
                    
                    int displacement;
                    String substring = "";
                    
                    if(ins.isImmediate){
                        substring = ins.OPERANDS[0].substring(1);
                    } else if(ins.isIndirect){
                        substring = ins.OPERANDS[0].substring(1);
                    } else {
                        substring = ins.OPERANDS[0];
                    }
                        
                    if(SYMTAB.get(substring) == null & substring != ""){
                        String charLess = "";
                        if(ins.isImmediate){
                            charLess = ins.OPERANDS[0].substring(1);
                        } else if(ins.isIndirect){
                            charLess = ins.OPERANDS[0].substring(1);
                        } else {
                            charLess = substring;
                        }
                        displacement = Integer.parseInt(charLess);
                        
                         if(ins.isFormat4){
                            BINCODE |= e;
                            
                            BINCODE = (BINCODE << 20) | (displacement & 0xFFFFF); 
                        } else {
                            BINCODE = (BINCODE << 12) | (displacement & 0xFFF);
                           
                        }
                    } else if(substring != "") {
                            int targetAddress = SYMTAB.get(substring).getAddress();
                            displacement = targetAddress;
                            
                        if(!ins.isFormat4){
                            displacement -= ins.ADDRESS + 3;
                            
                            if(displacement >= -2048 && displacement <= 2047){
                                BINCODE |= p;
                            } else {
                                BINCODE |= b;
                                displacement = targetAddress - BASEADDRESS;
                               
                            }
                        } 
                        
                        if(ins.isFormat4){
                            BINCODE |= e;
                            
                            BINCODE = (BINCODE << 20) | (displacement & 0xFFFFF); 
                        } else {
                            BINCODE = (BINCODE << 12) | (displacement & 0xFFF);
                           
                        }
                        
                    }

                        OBJECTCODE = String.format(ins.isFormat4 ? "%08X" : "%06X", BINCODE);
                    break;
     
            }
            
            
        }
      
        
        return OBJECTCODE;
        
    }
    
}













class INSTRUCTION {
    
    public String SYMBOL;
    public String OPCODE;
    public String OPERAND;
    public String[] OPERANDS;
    public String COMMENT;
    public String ERRORS;  
    public boolean isFormat4;
    public boolean isImmediate;
    public boolean isIndexed;
    public boolean isIndirect;
    public boolean hasOperand;
    public boolean isDirective;
    public int LENGTH;
    public int ADDRESS;
    public int OPERANDADDRESS;
    public String OBJECTCODE;
    
    public INSTRUCTION(String SYMBOL,String OPCODE, String OPERAND,String COMMENT,String ERRORS,int address){
        this.SYMBOL = SYMBOL;
        this.OPCODE = OPCODE;
        this.OPERAND = OPERAND;
        this.OPERANDS = setOperands(OPERAND);
        this.COMMENT = "";
        this.ERRORS = ERRORS;
        this.ADDRESS = address;
        this.OBJECTCODE = "";
        this.isFormat4 = isFormat4();
        this.isDirective = isDirective();
        
        if(OPERAND != ""){
            this.isImmediate = isImmediate();
            this.isIndexed = isIndexed();
            this.isIndirect = isIndirect();
            this.hasOperand = true;
        } else {
            this.hasOperand = false;
        }
        
        
    }
    
    public INSTRUCTION(String SYMBOL, String OPCODE, String OPERAND,String ERRORS, int address){
        this(SYMBOL,OPCODE,OPERAND,null,ERRORS,address);
    }
    public INSTRUCTION(String SYMBOL, String OPCODE, String OPERAND,int address){
        this(SYMBOL,OPCODE,OPERAND,null,null,address);
    }
   
    public INSTRUCTION(String COMMENT){
        this(null,null,null,COMMENT,0);
    }
    
    public boolean isFormat4(){
        if(OPCODE.charAt(0) == '+'){
            OPCODE = OPCODE.substring(1);
            return true;
        }
        else {
            return false;
        }
    }
    public boolean isImmediate(){
        if(OPERAND.charAt(0) == '#'){
            return true;
        } else {
            return false;
        }
            
    }
    public boolean isIndexed(){
        if(OPERANDS[1] != null && OPERANDS[1].charAt(0) =='X'){
            return true;
        }
        else {
            return false;
        }
    }
    public boolean isIndirect(){ 
        if(OPERAND.charAt(0) == '@'){
            return true;
        } else {
            return false;
        }
        
    }
    public boolean isDirective(){
        if(OPCODE.equals("WORD") || OPCODE.equals("RESB") || OPCODE.equals("BYTE") || OPCODE.equals("RESW") || OPCODE.equals("BASE") || OPCODE.equals("NOBASE")){
            return true;
        } else {
            return false;
        }
    }
    
    public void setComment(String x){
        this.COMMENT = x;
    }
    public void setErrors(String x){
        this.ERRORS = x;
    }
    public void setLength(int x){
        LENGTH = x;
    }
    public String[] setOperands(String Operand){
        OPERANDS = new String[2];
        if(Operand == null){
            OPERANDS[0] = null;
            OPERANDS[1] = null;
            return OPERANDS;
        }else {
            if(Operand.contains(",")){
                OPERANDS = Operand.split("\\,");
                return OPERANDS;
            }
            else {
                OPERANDS[0] = Operand;
                OPERANDS[1] = null; 
                return OPERANDS;
            }
        }
    }
    public void setOperandAddress(int address){
        OPERANDADDRESS = address;
    }
   
   
    public void setObjectCode(String x){
        this.OBJECTCODE = x;
    }
    @Override
    public String toString(){
        String test = "";
            //test += SYMBOL+" "+OPCODE+" "+OPERANDS[0]+ " FORMAT4: "+isFormat4 +" "+"IsImmedaite?: "+isImmediate+" "+"IsIndex: "+isIndexed+" "+Integer.toHexString(ADDRESS);
            test += SYMBOL+" "+OPCODE+" "+OPERAND+" "+Integer.toHexString(ADDRESS).toUpperCase();
        return test;
    }
}





class HeaderRecord {
    public static String PROGRAMNAME;
    public static Integer STARTADDRESS;
    public static Integer PROGRAMLENGTH;
    
    public HeaderRecord(String ProgramName, int StartAddress,int ProgramLength){
        PROGRAMNAME = ProgramName;
        STARTADDRESS = StartAddress;
        PROGRAMLENGTH = ProgramLength;
    }
    
    @Override
    public String toString(){
        String header = "";
        
        header = String.format('H'+"%-6.6s"+"%06X"+"%06X"+"\n",PROGRAMNAME,STARTADDRESS,PROGRAMLENGTH);
        return header;
    }
}

class TextRecord {
    public int STARTADDRESS;
    public int LENGTH;
    public boolean wasAdded;
    public ArrayList<String> OBJECTCODES;
    
    public int MAX = 30;
    
    TextRecord(int start){
        this.STARTADDRESS = start;
        LENGTH = 0;
        OBJECTCODES = new ArrayList<String>();
    }
    
    public void add(String objectcode){
        if(objectcode.length() == 0){
            
        } else if(LENGTH + objectcode.length()/2 <= MAX){
            OBJECTCODES.add(objectcode);
            LENGTH += objectcode.length()/2;
        } else {

        }
       
    }
    
    @Override
    public String toString(){
        String textRecord = String.format("T%06X%02X",STARTADDRESS, LENGTH);
       ListIterator<String> ins = OBJECTCODES.listIterator();
        while(ins.hasNext()){
            String objectCodes = ins.next();
            textRecord += objectCodes;
        }
        return textRecord;
    }
}

class ModRecord {
    public int STARTADDRESS;
    public int LENGTH = 5;
    
    ModRecord(int start){
        this.STARTADDRESS = start+1;
        int LENGTH = 5;
    }
    
    @Override
    public String toString(){
        String modRecord = String.format("M%06X%02X",STARTADDRESS,LENGTH);
        return modRecord;   
    }
}

class EndRecord {
    public int STARTADDRESS;
    
    EndRecord(int start){
        this.STARTADDRESS = start;
    }
    
    @Override
    public String toString(){
        String endRecord = String.format("E%06X",STARTADDRESS);
        
        return endRecord;
    }
}

class INITIALIZERS {
    private static Hashtable<String, OPT> OPTAB;
    private static Hashtable<String, SYM> SYMTAB;
    
    static {
        OPTAB = new Hashtable(59);
        
        //Constant Directives
        OPTAB.put("START",new OPT("START",0,0));
        OPTAB.put("END",new OPT("END",0,0));
        OPTAB.put("BYTE",new OPT("BYTE",0,0));
        OPTAB.put("WORD",new OPT("WORD",0,0));
        OPTAB.put("RESB",new OPT("RESB",0,0));
        OPTAB.put("RESW",new OPT("RESW",0,0));
        OPTAB.put("BASE", new OPT("BASE",0,0));
        OPTAB.put("NOBASE",new OPT("NOBASE",0,0));
        
        
        //Mnemonics with format size and HEX converted to decimal
        //0x## tells the compiler to convert the number to its decimal form
        //First page of commands in APPENDIX A
        OPTAB.put("ADD", new OPT("ADD",3,4,0x18));
        OPTAB.put("ADDF", new OPT("ADDF",3,4,0x58));
        OPTAB.put("ADDR", new OPT("ADDR",2,0x90));
        OPTAB.put("AND", new OPT("AND",3,4,0x40));
        OPTAB.put("CLEAR", new OPT("CLEAR",2,0xB4));
        OPTAB.put("COMP", new OPT("COMP",3,4,0x28));
        OPTAB.put("COMPF", new OPT("COMPF",3,4,0x88));
        OPTAB.put("COMPR",new OPT("COMPR",2,0xA0));
        OPTAB.put("DIV", new OPT("DIV",3,4,0x24));
        OPTAB.put("DIVF", new OPT("DIVF",3,4,0x64));
        OPTAB.put("DIVR", new OPT("DIVR",3,4,0x9C));
        OPTAB.put("FIX", new OPT("FIX",1,0xC4));
        OPTAB.put("FLOAT", new OPT("FLOAT",1,0xC0));
        OPTAB.put("J", new OPT("J",3,4,0x3C));
        OPTAB.put("JEQ", new OPT("JEQ",3,4,0x30));
        OPTAB.put("JGT", new OPT("JGT",3,4,0x34));
        OPTAB.put("JLT", new OPT("JLT",3,4,0x38));
        OPTAB.put("JSUB", new OPT("JSUB",3,4,0x48));
        OPTAB.put("LDA", new OPT("LDA",3,4,0x00));
        OPTAB.put("LDB", new OPT("LDB",3,4,0x68));
        OPTAB.put("LDCH", new OPT("LDCH",3,4,0x50));
        OPTAB.put("LDF", new OPT("LDF",3,4,0x70));
        OPTAB.put("LDL", new OPT("LDL",3,4,0x08));
        OPTAB.put("LDS", new OPT("LDS",3,4,0x6C));
        OPTAB.put("LDT", new OPT("LDT",3,4,0x74));
        OPTAB.put("LDX", new OPT("LDX",3,4,0x04));
        OPTAB.put("MUL", new OPT("MUL",3,4,0x20));
        
        //Second page begins ("This is purely so we can make sure its correct later")
        OPTAB.put("MULF", new OPT("MULF",3,4,0x60));
        OPTAB.put("MULR", new OPT("MULR",2,0x98));
        OPTAB.put("NORM", new OPT("NORM",1,0xC8));
        OPTAB.put("OR", new OPT("OR",3,4,0x44));
        OPTAB.put("RD", new OPT("RD",3,4,0xD8));
        OPTAB.put("RMO", new OPT("RMO",2,0xAC));
        OPTAB.put("RSUB", new OPT("RSUB",3,4,0x4C));
        OPTAB.put("SHIFTL", new OPT("SHIFTL",2,0xA4));
        OPTAB.put("SHIFTR", new OPT("SHIFTR",2,0xA8));
        OPTAB.put("STA", new OPT("STA",3,4,0x0C));
        OPTAB.put("STB", new OPT("STB",3,4,0x78));
        OPTAB.put("STCH", new OPT("STCH",3,4,0x54));
        OPTAB.put("STF", new OPT("STF",3,4,0x80));
        OPTAB.put("STS", new OPT("STS",3,4,0x7C));
        OPTAB.put("STL", new OPT("STL",3,3,0x14));
        OPTAB.put("STT", new OPT("STT",3,4,0x84));
        OPTAB.put("STX", new OPT("STX",3,4,0x10));
        OPTAB.put("SUB", new OPT("SUB",3,4,0x1C));
        OPTAB.put("SUBF", new OPT("SUBF",3,4,0x5C));
        
        //Third and last page of APPENDIX A instructions
        OPTAB.put("SUBR", new OPT("SUBR",2,0x94));
        OPTAB.put("TD", new OPT("TD",3,4,0xE0));
        OPTAB.put("TIX", new OPT("TIX",3,4,0x2C));
        OPTAB.put("TIXR", new OPT("TIXR",2,0xB8));
        OPTAB.put("WD", new OPT("WD",3,4,0xDC));
        
        //List of UNSUPPORTED COMMANDS FOR ERROR HANDLING
        //Set format to -1 for UNSUPPORTED COMMAND
        OPTAB.put("LPS", new OPT("RD",-1,0));
        OPTAB.put("SIO", new OPT("SIO",-1,0));
        OPTAB.put("SSK", new OPT("SSK",-1,0));
        OPTAB.put("STI", new OPT("STI",-1,0));
        OPTAB.put("STSW", new OPT("STSW",-1,0));
        OPTAB.put("SVC", new OPT("SVC",-1,0));
        OPTAB.put("TIO", new OPT("TIO",-1,0));
        OPTAB.put("HIO", new OPT("HIO",-1,0));
        OPTAB.put("EQU", new OPT("EQU",-1,0));
        OPTAB.put("USE", new OPT("USE",-1,0));
        OPTAB.put("CSECT", new OPT("CSECT",-1,0));
        
        SYMTAB = new Hashtable();
        SYMTAB.put("A",new SYM("A",0));
        SYMTAB.put("X",new SYM("X",1));
        SYMTAB.put("L",new SYM("L",2));
        SYMTAB.put("B",new SYM("B",3));
        SYMTAB.put("S",new SYM("S",4));
        SYMTAB.put("T",new SYM("T",5));
        SYMTAB.put("F",new SYM("F",6));
        SYMTAB.put("PC",new SYM("PC",8));
        SYMTAB.put("SW",new SYM("SW",9));
    }
    
    public static Hashtable<String, OPT> getOPTAB(){
        return OPTAB;
    }
    public static Hashtable<String, SYM> getSYMTAB(){
        return SYMTAB;
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
